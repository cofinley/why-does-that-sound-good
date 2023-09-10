(ns why-does-that-sound-good.components.settings-panel
  (:require
   [re-frame.core :as re-frame]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.subs :as subs]))

(defn settings-panel []
  (let [access @(re-frame/subscribe [::subs/midi-access])
        inputs (some->> access .-inputs .values)
        outputs (some->> access .-outputs .values)
        current-input @(re-frame/subscribe [::subs/midi-input])
        current-output @(re-frame/subscribe [::subs/midi-output])
        max-live-blocks @(re-frame/subscribe [::subs/max-live-blocks])
        play-chords-broken? @(re-frame/subscribe [::subs/play-chords-broken?])
        live-block-debounce @(re-frame/subscribe [::subs/live-block-debounce])]
    [:div.flex.flex-col.gap-y-4
     [:label.font-bold "MIDI Input"
      [:select.flex.p-2.rounded.dark:bg-neutral-700.dark:text-neutral-100.border.dark:border-neutral-500
       {:on-change #(re-frame/dispatch [::events/on-midi-select-input (-> % .-target .-value)])
        :value (if (nil? current-input) "" current-input)}
       (for [input inputs]
         ^{:key (.-id input)}
         [:option (.-name input)])]]
     [:label.font-bold "MIDI Output"
      [:select.flex.p-2.rounded.dark:bg-neutral-700.dark:text-neutral-100.border.dark:border-neutral-500
       {:on-change #(re-frame/dispatch [::events/on-midi-select-output (-> % .-target .-value)])
        :value (if (nil? current-output) "" current-output)}
       (for [output outputs]
         ^{:key (.-id output)}
         [:option (.-name output)])]]
     [:label.font-bold.flex.gap-x-2 "Play chords broken?"
      [:input
       {:class "w-5 h-5 p-1 rounded"
        :type "checkbox" :checked play-chords-broken? :on-change #(re-frame/dispatch [::events/on-play-chords-broken-change (-> % .-target .-checked)])}]]
     [:label.font-bold "Max Live Blocks"
      [:input
       {:class "dark:text-neutral-100 flex p-1.5 rounded dark:bg-neutral-700 border dark:border-neutral-500"
        :type "number" :min 1 :max 12 :value max-live-blocks :on-change #(re-frame/dispatch [::events/on-max-live-blocks-change (-> % .-target .-value int)])}]]
     [:label.font-bold "Live Block Sensitivity"
      [:div.flex.items-center.gap-x-2
       [:input
        {:class "dark:text-neutral-100 flex p-1.5 rounded dark:bg-neutral-700 border dark:border-neutral-500"
         :type "number" :min 1 :max 1000 :value live-block-debounce :on-change #(re-frame/dispatch [::events/on-live-block-debounce-change (-> % .-target .-value int)])}]
       [:span "milliseconds"]]]]))
