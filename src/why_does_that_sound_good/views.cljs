(ns why-does-that-sound-good.views
  (:require
   [reagent.core :as r]
   [re-frame.core :as re-frame]
   [why-does-that-sound-good.subs :as subs]
   [why-does-that-sound-good.components.button :refer [button]]
   [why-does-that-sound-good.components.drawer :refer [drawer]]
   [why-does-that-sound-good.components.settings-panel :refer [settings-panel]]
   [why-does-that-sound-good.components.piano-panel :refer [piano-panel]]
   [why-does-that-sound-good.components.scales-pane :refer [top-scale-suggestion]]
   [why-does-that-sound-good.components.live-suggestion-panel :refer [live-section-panel]]
   [why-does-that-sound-good.components.block :refer [block-view]]
   [why-does-that-sound-good.components.icons :refer [github-icon settings-icon]]
   [why-does-that-sound-good.components.section-tab :refer [section-tab]]))

(defn section-tabs []
  (let [sections @(re-frame/subscribe [::subs/tabbed-sections])
        current-section-id @(re-frame/subscribe [::subs/current-section-id])
        editing-section-name-id @(re-frame/subscribe [::subs/editing-section-name-id])]
    [:div.flex
     (for [[section-id section] sections]
       ^{:key (:id section)}
       [section-tab section :selected? (= current-section-id section-id) :editing-name? (= editing-section-name-id section-id)])
     [section-tab]]))

(defn blocks-panel []
  (let [blocks @(re-frame/subscribe [::subs/section-blocks])
        current-block-id @(re-frame/subscribe [::subs/current-block-id])
        recording? @(re-frame/subscribe [::subs/recording?])]
    [:div.flex.border-y.dark:border-neutral-700
     (for [[block-id block] blocks]
       ^{:key (:id block)}
       [block-view block (= current-block-id block-id) recording?])
     [block-view]]))

(defn recorded-section-panel []
  (let [current-section @(re-frame/subscribe [::subs/current-section])]
    [:div.flex.flex-col.gap-y-2
     [section-tabs]
     (when current-section
       [:<>
        [top-scale-suggestion current-section]
        [blocks-panel]])]))

(defn title []
  [:div
   [:div.flex.items-center.gap-x-2
    [:h1.text-3xl.font-bold.tracking-tight
     "Why Does That Sound Good?"]
    [:a
     {:class "ml-2" :href "https://github.com/cofinley/why-does-that-sound-good" :target "_blank" :title "Source code"}
     [github-icon
      {:class "w-7 h-7"}]]]
   [:span.text-sm.dark:text-neutral-400
    "Real-time MIDI fuzzy chord and scale identification"]])

(defn settings-button []
  (let [show-drawer? (r/atom false)]
    (fn []
      [:<>
       [drawer
        {:title "Settings"
         :side :right
         :show? @show-drawer?
         :on-close #(reset! show-drawer? false)}
        [settings-panel]]
       [button
        {:class "flex items-center gap-x-2"
         :on-click #(reset! show-drawer? true)}
        [settings-icon
         {:class "w-4 h-4"}]
        "Settings"]])))

(defn root-panel []
  [:div.min-h-screen.max-h-screen.flex.flex-col.dark:bg-neutral-900.dark:text-neutral-100.p-2.gap-y-2
   [:div.flex.justify-between.items-center
    [title]
    [settings-button]]
   [recorded-section-panel]
   [live-section-panel]
   [piano-panel]])
