(ns why-does-that-sound-good.components.block
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.subs :as subs]
   [why-does-that-sound-good.pitch :as pitch]
   [why-does-that-sound-good.components.similarity :refer [similarity-badge]]
   [why-does-that-sound-good.components.button :refer [button]]
   [why-does-that-sound-good.components.icons :as icons]
   [why-does-that-sound-good.utils :as utils]))

(defn recording-section [block]
  [:div
   [:div.flex.items-center
    [:div.rounded-full.w-4.h-4.bg-red-500]
    [:span "Recording. Enter notes..."]]
   [button
    {:on-click #(re-frame/dispatch [::events/on-recording?-toggle block])
     :title "Stop recording"}
    "Stop recording"]])

(defn block-icons [block overridden? on-view-chord-suggestions-click]
  [:div.flex.justify-between.gap-x-2
   [button
    {:on-click #(re-frame/dispatch [::events/on-block-play block])
     :title "Play (selected) notes"}
    [icons/play-icon]]
   [button
    {:on-click #(re-frame/dispatch [::events/on-recording?-toggle block])
     :title "Re-record"}
    [icons/re-record-icon]]
   (when (>= (count (:notes block)) 2)
     [button
     {:on-click on-view-chord-suggestions-click
      :title "View chord suggestions"}
     [icons/view-chord-suggestions-icon]])
   (when overridden?
     [button
      {:on-click #(re-frame/dispatch [::events/on-chord-suggestion-select (:id block) nil])
       :title "Remove chord selection"}
      [icons/eraser-icon]])
   [button
    {:on-click #(when (js/confirm "Are you sure you want to delete this block?")
                  (re-frame/dispatch [::events/on-block-delete {:block-id (:id block)}]))
     :title "Delete"}
    [icons/delete-icon]]])

(defn top-chord-suggestion [suggestion]
  [:div
   [:span "Top chord suggestion: " (utils/music-structure->str suggestion)]])

(defn chord-suggestions-view [{:keys [suggestions selected-suggestion min-similarity on-min-similarity-change on-suggestion-hover on-suggestion-select on-close]}]
  [:div.self-start.w-full
   [:button.float-right.text-2xl.w-max
    {:on-click on-close}
    "×"]
   [:label.flex.items-center.gap-2.mt-2
    {:title "Minimum similarity with original input notes that chords must have to be shown here"}
    "Min. Similarity: " (or min-similarity 100) "%"]
   [:input
    {:type "range"
     :value min-similarity
     :min 1
     :max 100
     :on-change #(on-min-similarity-change (-> % .-target .-value js/parseInt (/ 100)))}]
   [:div.max-h-40.overflow-y-auto.px-1
    [:table.w-full
     [:thead
      [:tr.border-b
       [:th ""] ; Selected indicator
       [:th.text-center "Key"]
       [:th.text-center "Chord"]
       [:th.text-center "Similarity"]
       [:th ""]]] ; Actions column
     [:tbody
      (for [[i suggestion] (map-indexed vector suggestions)]
        ^{:key (str (name (:root suggestion)) (name (:chord-type suggestion)))}
        [:tr.hover:dark:bg-neutral-500.group
         {:on-mouse-enter #(on-suggestion-hover suggestion)
          :on-mouse-leave #(on-suggestion-hover nil)
          :on-mouse-down #(re-frame/dispatch [::events/on-notes-play (:chord-notes suggestion)])
          :on-key-up #(when (= "Tab" (.-key %))
                        (on-suggestion-hover (:chord-notes suggestion))
                        (re-frame/dispatch [::events/on-notes-play (:chord-notes suggestion)]))
          :tab-index i}
         [:td {:class (if (and
                           (= (:root suggestion) (:root selected-suggestion))
                           (= (:chord-type suggestion) (:chord-type selected-suggestion)))
                        "visible"
                        "invisible")}
          "•"]
         [:td.text-center (:root suggestion)]
         [:td.text-center (:chord-type suggestion)]
         [:td.text-center [similarity-badge (:similarity suggestion)]]
         [:td.mx-auto
          [:button.invisible.group-hover:visible.pl-3
           {:on-click #(on-suggestion-select suggestion)
            :title "Use chord suggestion"}
           [icons/check-mark-icon]]]])]]]])

(defn block-view [block selected? recording?]
  (let [hovered? (r/atom false)
        show-chord-suggestions? (r/atom false)]
    (fn [block selected? recording?]
      (let [chord-suggestions @(re-frame/subscribe [::subs/block-chord-suggestions (:id block)])
            min-similarity @(re-frame/subscribe [::subs/block-min-chord-similarity (:id block)])
            selected-suggestion @(re-frame/subscribe [::subs/block-selected-suggestion (:id block)])]
        [:div
         {:class (str (if selected? "dark:bg-neutral-600" "dark:bg-transparent hover:dark:bg-neutral-700") " flex w-1/6 p-4 justify-center items-center cursor-pointer border-l last:border-r dark:border-neutral-700")
          :on-click #(if block
                       (when-not @show-chord-suggestions?
                         (re-frame/dispatch [::events/on-block-select (:id block)]))
                       (re-frame/dispatch [::events/on-block-add]))
          :on-mouse-enter #(when-not @show-chord-suggestions?
                             (reset! hovered? true)
                             (re-frame/dispatch [::events/on-block-hover-toggle (:id block)]))
          :on-mouse-leave #(when-not @show-chord-suggestions?
                             (reset! hovered? false)
                             (re-frame/dispatch [::events/on-block-hover-toggle nil]))}
         (if block
           (if (and @show-chord-suggestions? chord-suggestions)
             [chord-suggestions-view
              {:suggestions chord-suggestions
               :selected-suggestion selected-suggestion
               :min-similarity min-similarity
               :on-min-similarity-change #(re-frame/dispatch [::events/on-block-min-chord-similarity-change block %])
               :on-suggestion-hover #(re-frame/dispatch [::events/on-chord-suggestion-hover %])
               :on-suggestion-select #(do
                                        (re-frame/dispatch [::events/on-chord-suggestion-select (:id block) %])
                                        (reset! show-chord-suggestions? false))
               :on-close #(reset! show-chord-suggestions? false)}]
             [:div
              (if selected-suggestion
                [:span.font-semibold.text-lg
                 (utils/music-structure->str selected-suggestion)]
                [:div
                 [:div "Notes: " (->> block
                                      :notes
                                      sort
                                      (map pitch/find-note-name)
                                      (map name)
                                      (str/join " "))]
                 (when (seq chord-suggestions)
                   [top-chord-suggestion (first chord-suggestions)])])
              (when (and selected? recording?)
                [recording-section block])
              (when (and @hovered? (not recording?))
                [block-icons block (some? (:selected-suggestion block)) #(reset! show-chord-suggestions? true)])])
           [:button "+ Add Block"])]))))
