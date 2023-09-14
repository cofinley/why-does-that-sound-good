(ns why-does-that-sound-good.events
  (:require
   [clojure.set :as set]
   [re-frame.core :refer [reg-event-db reg-event-fx reg-fx inject-cofx path after dispatch]]
   [why-does-that-sound-good.db :as db]
   [why-does-that-sound-good.midi :as midi]
   [why-does-that-sound-good.algo.chord :as chord]
   [why-does-that-sound-good.algo.scale :as scale]
   [why-does-that-sound-good.utils :refer [block->used-notes]]))

;; Interceptors

(def data->local-store (after db/data->local-store))  ;; Store state in localStorage after each event

(def interceptors [(path :data)
                   data->local-store])  ;; Define all interceptors

;; Effectful handlers handle whole :db but sometimes we still need to save the nested :data to localStorage
(def db->local-store [(after (fn [db _] (db/data->local-store (:data db))))])

;; -- Helpers -----------------------------------------------------------------

(defn allocate-next-id
  "Returns the next node id.
  Assumes node are sorted.
  Returns one more than the current largest id."
  [nodes]
  ((fnil inc 0) (last (keys nodes))))

(reg-event-fx  ;; Use -fx over -db to access cofx
 ::initialize-db
 [(inject-cofx :local-store-data)]  ;; Custom interceptor using cofx (defined in db.cljs), read from localStorage on init
 (fn [{:keys [db local-store-data]} _]
   {:db (assoc db/default-db
               :data (merge (:data db/default-db)
                            (if (keys local-store-data) local-store-data {})))}))  ;; :db is meta here and represents the default/db; it is not a key used

; Source: https://github.com/Day8/re-frame/issues/233#issuecomment-252738662
(defonce timeouts
  (atom {}))

(reg-fx
 :dispatch-debounce
 (fn [[id event-vec n]]
   (js/clearTimeout (@timeouts id))
   (swap! timeouts assoc id
          (js/setTimeout (fn []
                           (dispatch event-vec)
                           (swap! timeouts dissoc id))
                         n))))

(reg-fx
 :stop-debounce
 (fn [id]
   (js/clearTimeout (@timeouts id))
   (swap! timeouts dissoc id)))

(defn add-note [db note]
  (let [current-block-id (:current-block-id db)
        recording? (:recording? db)]
    (if (and recording? current-block-id)
      (update-in db [:data :blocks current-block-id :notes] conj note)
      db)))

;; Live Blocks

(reg-event-fx
 ::on-live-block-add
 (fn [{:keys [db]} [_ live-notes]]
   (let [last-live-block-notes (set (:notes (get-in db [:data :blocks (last (get-in db [:data :sections :live :block-ids]))])))
         new-notes (set/union (:active live-notes) (:finished live-notes))
         same-as-previous-notes? (= last-live-block-notes new-notes)]
     (if (or (< (count new-notes) 1) same-as-previous-notes?)
       {:db db}
       (let [next-block-id (get-in db [:data :next-block-id])
             new-block (-> next-block-id
                           db/create-block
                           (assoc :notes new-notes))
             live-block-ids (get-in db [:data :sections :live :block-ids])
             max-live-blocks (get-in db [:data :max-live-blocks])]
         {:db (-> db
                  (assoc-in [:data :blocks next-block-id] new-block)
                  (assoc-in [:data :blocks next-block-id :notes] new-notes)
                  ;; Push live block onto FIFO queue
                  (cond->
                   (= max-live-blocks (count live-block-ids)) (->
                                                                  (update-in [:data :blocks] dissoc (first live-block-ids))
                                                                  (update :chord-suggestions dissoc (first live-block-ids))
                                                                  (assoc-in [:data :sections :live :block-ids] (subvec live-block-ids 1)))
                   :always (update-in [:data :sections :live :block-ids] conj next-block-id))
                  (assoc-in [:data :next-block-id] (inc next-block-id)))
          :fx [[:dispatch [::on-chord-suggestions-generate new-block]]
               [:dispatch [::on-scale-suggestions-generate :live]]]})))))

;; TODO dispatch scale generation and/or merge this into a bigger 'add block to section' event handler
(reg-event-db
 ::on-live-block-save
 db->local-store
 (fn [db [_ block & {:keys [chord]
                     :or {chord nil}}]]
   (when-let [current-section-id (:current-section-id db)]
     (let [next-block-id (get-in db [:data :next-block-id])
           new-block (-> (db/create-block next-block-id)
                         (assoc :notes (:notes block))
                         (assoc :min-chord-similarity (:min-chord-similarity block))
                         (cond->
                             ;; Select chord suggestion if saving from live block's suggestions
                          (some? chord) (assoc :selected-suggestion chord)))
           chord-suggestions (get-in db [:chord-suggestions (:id block)])]
       (-> db
           (assoc-in [:data :blocks next-block-id] new-block)
           (update-in [:data :sections current-section-id :block-ids] conj next-block-id)
           (assoc-in [:chord-suggestions next-block-id] chord-suggestions)
           (assoc-in [:data :next-block-id] (inc next-block-id)))))))

(reg-event-db
 ::on-note-click
 db->local-store
 (fn [db [_ note]]
   ; TODO play note regardless of recording? (convert to -fx)
   (add-note db note)))

;; MIDI

(reg-event-fx
 ::on-midi-message
 db->local-store
 (fn [{:keys [db]} [_ msg]]
   (let [event (midi/parse-midi-data (.-data msg))
         note-on? (= :note-on (:command event))
         note-off? (= :note-off (:command event))
         control-change? (= :control-change (:command event))
         recording? (:recording? db)
         note (:note event)
         currently-sustained? (:sustain? db)
         live-block-debounce (get-in db [:data :live-block-debounce])
         ;; TODO process db changes in separate function, maybe all of this, maybe just live-note part
         new-db (cond
                  note-on? (if recording?
                             (add-note db note)
                             (update-in db [:live-notes :active] conj note))
                  note-off? (when-not recording?
                              (-> db
                                  (update-in [:live-notes :active] disj note)
                                  (cond->
                                   currently-sustained? (update-in [:live-notes :finished] conj note))))
                  control-change? (let [sustain? (and (= 64 (:cc event)) (= 127 (:value event)))]
                                    (-> db
                                        (assoc :sustain? sustain?)
                                        (cond->
                                         sustain? (assoc-in [:live-notes :finished] (get-in db [:live-notes :active]))
                                         :else (assoc-in [:live-notes :finished] #{}))))
                  :else db)]
     {:db new-db
      :dispatch-debounce [::live-block [::on-live-block-add (:live-notes new-db)] live-block-debounce]})))

(reg-event-db
 ::on-midi-access
 (fn [db [_ access]]
   (assoc db :midi-access access)))

(reg-fx
 :watch-midi-input  ;; Custom effect for event handler below
 (fn [input]
   (js/console.log "Listening to MIDI " (.-name input))
   (set! (.-onmidimessage input) #(dispatch [::on-midi-message %]))))

;; Play

(reg-fx
 ::play-notes
 (fn [{:keys [output notes broken? individual-notes?]
       :or {broken? false}}]
   (let [sorted-notes (sort notes)]
     (if individual-notes?
       (midi/play-scale output sorted-notes)
       (midi/play-chord output sorted-notes :broken? broken?)))))

(reg-event-fx
 ::on-notes-play
 (fn
   ([{:keys [db]} [_ notes & {:keys [broken? individual-notes?]
                              :or {broken? (get-in db [:data :play-chords-broken?])
                                   individual-notes? false}}]]
    (let [output-name (get-in db [:data :midi-output])
          outputs (some->> db :midi-access .-outputs .values)
          output (some->> outputs (filter #(= (.-name %) output-name)) first)]
      {::play-notes {:output output :notes notes :broken? broken? :individual-notes? individual-notes?}}))))

;; Settings

(reg-event-fx
 ::on-midi-select-input
 db->local-store
 (fn [{:keys [db]} [_ input-name]]
   (let [input-name (or input-name (get-in db [:data :midi-input]))
         inputs (some->> db :midi-access .-inputs .values)
         input (some->> inputs (filter #(= (.-name %) input-name)) first)]
     {:db (assoc-in db [:data :midi-input] input-name)
      :watch-midi-input input})))

(reg-event-db
 ::on-midi-select-output
 interceptors
 (fn [data [_ output-name]]
   (assoc data :midi-output output-name)))

(reg-event-db
 ::on-max-live-blocks-change
 db->local-store
 (fn [db [_ new-max]]
   (let [current-max (get-in db [:data :max-live-blocks])
         live-block-ids (get-in db [:data :sections :live :block-ids])
         diff (- current-max new-max)
         truncate? (pos? diff)]
     (if (and (< new-max (count live-block-ids)) truncate?)
       (let [kept-block-ids (subvec live-block-ids diff)]
         (-> db
             (assoc-in [:data :max-live-blocks] new-max)
             (assoc-in [:data :sections :live :block-ids] kept-block-ids)
             (update-in [:data :blocks] select-keys kept-block-ids)
             (update :chord-suggestions select-keys kept-block-ids)))
       (assoc-in db [:data :max-live-blocks] new-max)))))

(reg-event-db
  ::on-play-chords-broken-change
  interceptors
  (fn [data [_ broken?]]
    (assoc data :play-chords-broken? broken?)))

(reg-event-db
 ::on-live-block-debounce-change
 interceptors
 (fn [data [_ debounce-ms]]
   (assoc data :live-block-debounce debounce-ms)))

(reg-event-db
 ::on-settings-reset
 interceptors
 (fn [data _]
   (assoc data
          :play-chords-broken? false
          :live-block-debounce db/default-live-block-creation-debounce
          :max-live-blocks db/default-max-live-blocks)))

;; Blocks

(reg-event-db
 ::on-block-select
 (fn [db [_ block-id]]
   (let [current-block-id (:current-block-id db)
         new-block-id (if (= block-id current-block-id) nil block-id)]
     (assoc db :current-block-id new-block-id))))

(reg-event-fx
 ::on-block-add
 db->local-store
 (fn [{:keys [db]} _]
   (let [current-section-id (:current-section-id db)
         next-block-id (get-in db [:data :next-block-id])]
     {:db (-> db
              (assoc-in [:data :blocks next-block-id] (db/create-block next-block-id))
              (update-in [:data :sections current-section-id :block-ids] conj next-block-id)
              (assoc-in [:data :next-block-id] (inc next-block-id))
              (assoc :recording? true))
      :dispatch [::on-block-select next-block-id]})))

(reg-event-fx
 ::on-block-delete
 db->local-store
 (fn [{:keys [db]} [_ {:keys [block-id section-id]
                       :or {section-id (:current-section-id db)}}]]
   (let [section-block-ids (get-in db [:data :sections section-id :block-ids])]
     {:db (-> db
              ;; Remove block's chord suggestions
              (update :chord-suggestions dissoc block-id)
              ;; Remove from section (maybe store current section in backlink in block?)
              (assoc-in [:data :sections section-id :block-ids] (filterv #(not= % block-id) section-block-ids))
              ;; Remove block
              (update-in [:data :blocks] dissoc block-id))
      :dispatch [::on-scale-suggestions-generate section-id]})))

(reg-event-db
 ::on-block-hover-toggle
 (fn [db [_ block-id]]
   (assoc db :temp-block-id block-id)))

(reg-event-db
 ::on-block-clear
 db->local-store
 (fn [db [_ block]]
   (if-let [current-block-id (:id block)]
     (assoc-in db [:data :blocks current-block-id :notes] #{})
     db)))

(reg-event-fx
 ::on-recording?-toggle
 (fn [{:keys [db]} [_ block]]
   (let [recording? (not (:recording? db))]
     (-> {:db (assoc db :recording? recording?)}
         (cond->
          recording? (assoc :dispatch [::on-block-clear block])
          (not recording?) (assoc :dispatch [::on-chord-suggestions-generate block]))))))

(reg-event-fx
 ::on-block-play
 (fn [_ [_ block]]
   {:dispatch [::on-notes-play (block->used-notes block)]}))

;; Section

(reg-event-db
 ::on-section-select
 (fn [db [_ section-id]]
   (assoc db :current-section-id section-id)))

(reg-event-fx
 ::on-section-add
 db->local-store
 (fn [{:keys [db]} _]
   (let [next-section-id (get-in db [:data :next-section-id])]
     {:db (-> db
              (assoc-in [:data :sections next-section-id] (db/create-section next-section-id))
              (assoc-in [:data :next-section-id] (inc next-section-id)))
      :dispatch [::on-section-select next-section-id]})))

(reg-event-db
 ::on-section-clear
 db->local-store
 (fn [db [_ section-id]]
   (let [section-block-ids (get-in db [:data :sections section-id :block-ids])]
     (-> db
         (assoc-in [:data :sections section-id :block-ids] [])
         (update-in [:data :blocks] dissoc section-block-ids)
         (update :chord-suggestions dissoc section-block-ids)
         (update :scale-suggestions dissoc section-id)
         (assoc-in [:data :sections section-id :min-scale-similarity] db/default-min-similarity)))))

(reg-event-db
 ::on-section-delete
 db->local-store
 (fn [db [_ section]]
   (let [section-block-ids (:block-ids section)]
     (-> db
         (update-in [:data :sections] dissoc (:id section))
         (update-in [:data :blocks] dissoc section-block-ids)
         (assoc :current-section-id nil)
         (assoc :current-block-id nil)))))

(reg-event-db
 ::on-section-name-edit-toggle
 (fn [db [_ section]]
   (assoc db :editing-section-name-id (:id section))))

(reg-event-db
 ::on-section-name-change
 db->local-store
 (fn [db [_ section-id new-name]]
   (assoc-in db [:data :sections section-id :name] new-name)))

;; Chord Suggestions

(reg-event-db
 ::generate-block-chord-suggestions
 (fn [db _]
   (assoc db :chord-suggestions
          (reduce (fn [m [block-id block]]
                    (assoc m block-id (chord/mem-block->chords block :find-closest? true)))
                  {}
                  (get-in db [:data :blocks])))))

(reg-event-db
 ::on-chord-suggestions-generate
 db->local-store
 (fn [db [_ block & {:keys [min-similarity]
                     :or {min-similarity nil}}]]
   (let [chords (if (nil? min-similarity)
                  (chord/mem-block->chords block :find-closest? true)
                  (chord/mem-block->chords block :min-chord-similarity min-similarity))]
     (-> db
         (assoc-in [:chord-suggestions (:id block)] chords)
         (cond->
          (nil? min-similarity) (assoc-in [:data :blocks (:id block) :min-chord-similarity] (:similarity (first chords))))))))

(reg-event-fx
 ::on-block-min-chord-similarity-change
 db->local-store
 (fn [{:keys [db]} [_ block min-similarity]]
   {:db (assoc-in db [:data :blocks (:id block) :min-chord-similarity] min-similarity)
    :dispatch-debounce [::block-min-chord-similarity-change [::on-chord-suggestions-generate block :min-similarity min-similarity] 100]}))

(reg-event-db
 ::on-chord-suggestion-hover
 (fn [db [_ chord]]
   (assoc db :temp-chord-suggestion chord)))

(reg-event-db
 ::on-chord-suggestion-select
 db->local-store
 (fn [db [_ block-id suggestion]]
   (assoc-in db [:data :blocks block-id :selected-suggestion] suggestion)))

;; Scale Suggestions

(reg-event-db
 ::on-scale-suggestions-generate
 db->local-store
 (fn [db [_ section-id & {:keys [min-similarity]
                          :or {min-similarity nil}}]]
   (let [section-block-ids (get-in db [:data :sections section-id :block-ids])
         blocks (vals (select-keys (get-in db [:data :blocks]) section-block-ids))
         pregenerated-block-chord-suggestions (select-keys (:chord-suggestions db) section-block-ids)
         scales (when (<= 2 (count blocks))
                  (if (nil? min-similarity)
                    (scale/mem-blocks->scales blocks :pregenerated-block-chord-suggestions pregenerated-block-chord-suggestions :find-closest? true)
                    (scale/mem-blocks->scales blocks :pregenerated-block-chord-suggestions pregenerated-block-chord-suggestions :min-scale-similarity min-similarity)))]
     (-> db
         (assoc-in [:scale-suggestions section-id] scales)
         (cond->
          (and (seq scales) (nil? min-similarity)) (assoc-in [:data :sections section-id :min-scale-similarity] (:variation-combo-pitch-similarity (val (first scales)))))))))

(reg-event-db
 ::generate-section-scale-suggestions
 (fn [db _]
   (assoc db :scale-suggestions
          (reduce (fn [m [section-id section]]
                    (let [section-block-ids (:block-ids section)
                          blocks (vals (select-keys (get-in db [:data :blocks]) section-block-ids))
                          pregenerated-block-chord-suggestions (select-keys (:chord-suggestions db) section-block-ids)]
                      (when (<= 2 (count blocks))
                        (assoc m section-id (scale/mem-blocks->scales blocks :pregenerated-block-chord-suggestions pregenerated-block-chord-suggestions :find-closest? true)))))
                  {}
                  (get-in db [:data :sections])))))

(reg-event-fx
 ::on-section-min-scale-similarity-change
 db->local-store
 (fn [{:keys [db]} [_ section-id min-similarity]]
   {:db (assoc-in db [:data :sections section-id :min-scale-similarity] min-similarity)
    :dispatch-debounce [::section-min-scale-similarity-change [::on-scale-suggestions-generate section-id :min-similarity min-similarity] 100]}))

;; TODO dispatch scale generation and/or merge this into a bigger 'add block to section' event handler
(reg-event-db
 ::on-scale-chord-save
 db->local-store
 (fn [db [_ chord]]
   (let [next-block-id (get-in db [:data :next-block-id])
         new-block (-> (db/create-block next-block-id)
                       (assoc :notes (:chord-notes chord))
                       (assoc :min-chord-similarity 1)
                       (assoc :selected-suggestion chord))
         chord-suggestions [(assoc chord :similarity 1)]]
     (-> db
         (assoc-in [:data :blocks next-block-id] new-block)
         (update-in [:data :sections (:current-section-id db) :block-ids] conj next-block-id)
         (assoc-in [:chord-suggestions next-block-id] chord-suggestions)
         (assoc-in [:data :next-block-id] (inc next-block-id))))))
