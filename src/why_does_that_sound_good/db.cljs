(ns why-does-that-sound-good.db
  (:require [cljs.reader]
            [re-frame.core :as re-frame]))

(def default-min-similarity 0.95)
(def default-max-live-blocks 10)
(def default-live-block-creation-debounce 400)

(defn create-section [id]
  {:id id
   :name (str "Section " id)
   :block-ids []
   :min-scale-similarity default-min-similarity})

(defn create-block [id]
  {:id id
   :enabled? true
   :locked? false
   :min-chord-similarity default-min-similarity
   :selected-suggestion nil
   :notes #{}})

(defn add-section-to-db [db section]
  (assoc-in db [:data :sections (:id section)] section))

(defn add-block-to-db [db block]
  (assoc-in db [:data :blocks (:id block)] block))

(def default-db
  {:data {:sections {:live (create-section :live)
                     1 (create-section 1)}
          :blocks {}
          :max-live-blocks default-max-live-blocks
          :play-chords-broken? false
          :live-block-debounce default-live-block-creation-debounce
          :next-section-id 2
          :next-block-id 1
          :midi-input nil
          :midi-output nil}
   ; Selected Ids
   :current-section-id 1
   :current-block-id nil
   ; Temp block Id on hover/compare
   :temp-block-id nil
   :recording? false
   :midi-access nil
   :editing-section-name-id nil
   ; Chord suggestions
   :chord-suggestions {}  ;; by block Id
   :show-block-chord-suggestions-id nil
   :temp-chord-suggestion nil
   ; Scale suggestions
   :scale-suggestions {}  ;; by section Id
   :live-notes {:active #{} :finished #{}}
   :sustain? false})

(def localstorage-key "wdtsg")

(defn data->local-store
  "Puts state into localStorage"
  [data]
  (.setItem js/localStorage localstorage-key (str data)))     ;; sorted-map written as an EDN map

(re-frame/reg-cofx
 :local-store-data
 (fn [cofx _]
   (assoc cofx :local-store-data
          (into (sorted-map)
                (some->> (.getItem js/localStorage localstorage-key)
                         (cljs.reader/read-string))))))

