(ns why-does-that-sound-good.components.scales-pane
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.subs :as subs]
   [why-does-that-sound-good.utils :as utils]
   [why-does-that-sound-good.components.drawer :refer [drawer]]
   [why-does-that-sound-good.components.button :refer [button]]
   [why-does-that-sound-good.components.piano-panel :refer [piano-preview]]
   [why-does-that-sound-good.components.similarity :refer [similarity-badge]]
   [why-does-that-sound-good.components.icons :refer [chevron-up-icon chevron-down-icon plus-icon]]))

(defn scale-pitch-chord-ticker [pitch-chords]
  (let [index (r/atom 0)
        hovered? (r/atom false)]
    (fn [pitch-chords]
      (let [chord (if (<= 0 @index (dec (count pitch-chords)))
                    (nth pitch-chords @index)
                    nil)
            chord-str (if chord (utils/music-structure->str chord) "")]
        [:div.flex.gap-x.items-center
         {:on-mouse-enter #(do
                             (reset! hovered? true)
                             (re-frame/dispatch [::events/on-block-hover-toggle (:original-block-id chord)])
                             (re-frame/dispatch [::events/on-chord-suggestion-hover chord]))
          :on-mouse-leave #(do
                             (reset! hovered? false)
                             (re-frame/dispatch [::events/on-block-hover-toggle nil])
                             (re-frame/dispatch [::events/on-chord-suggestion-hover nil]))}
         [:div
          {:class (str/join " " [(if @hovered? "visible" "invisible") "relative right-2"])}
          [button
           {:on-click #(re-frame/dispatch [::events/on-scale-chord-save chord])
            :title "Add chord to current section"}
           [plus-icon
            {:class "!w-4 !h-4"}]]]
         [:div.flex.flex-col.gap-y-1.items-center
          [:button
           {:on-click #(let [new-index (dec @index)]
                         (reset! index new-index)
                         (re-frame/dispatch [::events/on-chord-suggestion-hover (nth pitch-chords new-index)]))
            :class (when (= 0 @index) "invisible")
            :title "Go to previous diatonic chord"}
           [chevron-up-icon]]
          [:div.flex.flex-col.items-center.cursor-pointer
           {:on-click #(re-frame/dispatch [::events/on-notes-play (:chord-notes chord)])
            :title "Click to play"}
           [:span.font-semibold chord-str]
           (when-let [s (:similarity chord)]
             [similarity-badge s])]
          [:button
           {:on-click #(let [new-index (inc @index)]
                         (reset! index new-index)
                         (re-frame/dispatch [::events/on-chord-suggestion-hover (nth pitch-chords new-index)]))
            :class (when (= @index (dec (count pitch-chords))) "invisible")
            :title "Go to next diatonic chord"}
           [chevron-down-icon]]]]))))

(defn scale-piano-preview [scale-pitches]
  (let [example-notes (utils/pitches->example-notes scale-pitches)]
    [:div
     {:on-click #(re-frame/dispatch [::events/on-notes-play example-notes :individual-notes? true])}
     [piano-preview example-notes]]))

(defn scales-view [{:keys [section-id suggestions drawer?]
                    :or {drawer? false}}]
  (let [section-block-count (count (:block-ids @(re-frame/subscribe [::subs/section section-id])))
        min-similarity @(re-frame/subscribe [::subs/section-min-scale-similarity section-id])]
    [:<>
     (when-not drawer? [:h2.text-xl "Scales"])
     (if (< 1 section-block-count)
       [:<>
        [:label.flex.items-center.gap-2.mt-2
         {:title "Minimum similarity with original input notes that scales must have to be shown here (based on combination of blocks' notes)"}
         "Min. Similarity: " (or min-similarity 100) "%"]
        [:input
         {:type "range"
          :value min-similarity
          :min 1
          :max 100
          :on-change #(re-frame/dispatch [::events/on-section-min-scale-similarity-change section-id (-> % .-target .-value js/parseInt (/ 100))])}]
        [:div.max-h-full.overflow-y-auto.flex.flex-col.divide-y.dark:divide-neutral-400
         (for [[scale-key scale-details] suggestions]
           ^{:key scale-key}
           [:div.p-4.pl-0
            [:div.flex.justify-between.items-center
             [:div.flex.flex-col.items-start.gap-y
              [:h3.text-xl.font-bold (:root scale-key) " " (:scale-type scale-key)]
              [similarity-badge (:variation-combo-pitch-similarity scale-details)]]  ;; Should maybe be :original-pitch-similarity, though might close enough
             [scale-piano-preview (:scale-pitches scale-details)]]
            [:div.ml-4
             [:div
              [:div.flex.gap-x-6.my-2.justify-between
               (for [pitch (:scale-pitches scale-details)]
                 ^{:key pitch}
                 [scale-pitch-chord-ticker (get (:all-chords scale-details) pitch)])]]]])]]
       [:p.dark:text-neutral-300 "Enter more than one block to show scales"])]))

(defn top-scale-suggestion [section]
  (let [show-drawer? (r/atom false)]
    (fn []
      (let [suggestions @(re-frame/subscribe [::subs/section-scale-suggestions (:id section)])
            top-suggestion (first suggestions)
            [scale-key _] (or top-suggestion [])
            top-name (if scale-key (str/join " " [(name (:root scale-key)) (name (:scale-type scale-key))]) "None")
            display-text "Top scale suggestion: "]
        (if (< 1 (count (:block-ids section)))
          [:<>
           [drawer
            {:show? @show-drawer?
             :title "Scales"
             :side :left
             :on-close #(reset! show-drawer? false)}
            [scales-view {:section-id (:id section) :suggestions suggestions :drawer? true}]]
           [:div.flex.gap-x-2.items-center
            [:span display-text top-name]
            [button
             {:on-click #(reset! show-drawer? true)}
             (str "View All (" (count suggestions) ")")]]]
          [:p.dark:text-neutral-500 "Enter more than one block to show scales"])))))
