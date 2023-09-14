(ns why-does-that-sound-good.components.live-suggestion-panel
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [reagent.core :as r]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.components.piano-panel :refer [piano-preview]]
   [why-does-that-sound-good.components.icons :refer [delete-icon plus-icon]]
   [why-does-that-sound-good.components.button :refer [button]]
   [why-does-that-sound-good.components.scales-pane :refer [scales-view top-scale-suggestion]]
   [why-does-that-sound-good.subs :as subs]
   [why-does-that-sound-good.components.similarity :refer [similarity-badge]]
   [why-does-that-sound-good.utils :as utils]))

(defn live-block-chord-suggestion [block s]
  (let [hovered? (r/atom false)]
    (fn [block s]
      [:div
       {:class "flex justify-between items-center cursor-pointer px-8"
        :on-mouse-enter #(do
                           (re-frame/dispatch [::events/on-chord-suggestion-hover s])
                           (reset! hovered? true))
        :on-mouse-leave #(do (re-frame/dispatch [::events/on-chord-suggestion-hover nil])
                             (reset! hovered? false))}
       [:div
        {:class (str/join " " [(if @hovered? "visible" "invisible") "relative right-4"])}
        [button
         {:on-click #(re-frame/dispatch [::events/on-live-block-save block :chord s])
          :title "Add chord to current section"}
         [plus-icon]]]
       [:div
        {:class "flex flex-col gap-y items-start"}
        [:span
         {:class "font-semibold text-xl"}
         (utils/music-structure->str s)]
        [similarity-badge (:similarity s)]]
       [:div
        {:on-click #(re-frame/dispatch [::events/on-notes-play (:chord-notes s)])
         :title "Click to play"}
        [piano-preview (:notes block) :chord-notes (:chord-notes s)]]])))

(defn live-block-chord-suggestions [block suggestions & {:keys [current?]
                                                         :or {current? false}}]
  [:div
   {:class "flex flex-col overflow-auto gap-y-4 py-2"}
   (for [s suggestions]
     ^{:key (utils/music-structure->str s)}
     [live-block-chord-suggestion block s])])

(defn live-block [block & {:keys [current?]
                           :or {current? false}}]
  (let [hovered? (r/atom false)]
    (fn [block current?]
      (let [block-id (:id block)
            chord-suggestions @(re-frame/subscribe [::subs/block-chord-suggestions block-id])]
        [:div
         {:class (str/join " " [(when current? "border-2 dark:border-neutral-100")
                                "flex flex-col dark:bg-neutral-700 items-end rounded-xl"])
          :on-mouse-enter #(re-frame/dispatch [::events/on-block-hover-toggle block-id])
          :on-mouse-leave #(re-frame/dispatch [::events/on-block-hover-toggle nil])}
         [:div
          {:class " flex items-center justify-between w-full cursor-pointer py-3 px-8 dark:bg-neutral-600 rounded-t-xl"
           :on-mouse-enter #(reset! hovered? true)
           :on-mouse-leave #(reset! hovered? false)}
          [:div
           {:class (str/join " " [(if @hovered? "visible" "invisible") "relative right-4 flex flex-col gap-y-2"])}
           [button
            {:on-click #(re-frame/dispatch [::events/on-live-block-save block])
             :title "Add to current section"}
            [plus-icon]]
           [button
            {:on-click #(when (js/confirm "Are you sure you want to delete this block?")
                          (.stopPropagation %)
                          (re-frame/dispatch [::events/on-block-delete {:block-id (:id block) :section-id :live}]))
             :title "Delete"}
            [delete-icon]]]
          [:span
           {:class "dark:text-neutral-100"}
           "Input"]
          [:div
           {:on-click #(re-frame/dispatch [::events/on-notes-play (:notes block)])
            :title "Click to play"}
           [piano-preview (:notes block)]]]
         [live-block-chord-suggestions block chord-suggestions :current? current?]]))))

(defn live-section-panel []
  (let [live-section-blocks @(re-frame/subscribe [::subs/live-section-blocks])
        live-section-scales @(re-frame/subscribe [::subs/section-scale-suggestions :live])
        max-live-blocks @(re-frame/subscribe [::subs/max-live-blocks])]
    [:<>
     (when (seq live-section-blocks)
       [button
        {:class "flex gap-x-2 items-center self-start"
         :on-click #(when (js/confirm "Are you sure you want to delete all live blocks?")
                      (re-frame/dispatch [::events/on-section-clear :live]))}
        [delete-icon]
        "Clear Live Blocks"])
     [:div
      {:class (str/join " " [(when (= 1 max-live-blocks) "justify-center") "flex grow gap-x-4 overflow-y-auto"])}
      (when (< 1 max-live-blocks)
        [:div
         {:class "flex gap-x-4 overflow-y-auto flex-row-reverse basis-1/3"}
         (for [[block-id block] (reverse (butlast live-section-blocks))]
           ^{:key block-id}
           [live-block block])])
      (if (seq live-section-blocks)
        [:<>
         [live-block (last (vals live-section-blocks)) :current? true]
         (when (< 1 max-live-blocks)
           [:div
            {:class "py-3 px-8 dark:bg-neutral-600 rounded-xl flex flex-col"}
            [scales-view {:section-id :live :suggestions live-section-scales}]])]
        [:div
         {:class "flex basis-1/3 rounded-xl  border-2 border-dashed dark:border-neutral-600 justify-center items-center"}
         [:p.text-center.dark:text-neutral-400 "Start playing to see suggestions here"]])]]))
