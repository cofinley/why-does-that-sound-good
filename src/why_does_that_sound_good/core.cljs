(ns why-does-that-sound-good.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   ;; [re-pressed.core :as rp]
   [breaking-point.core :as bp]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.views :as views]
   [why-does-that-sound-good.config :as config]
   [promesa.core :as p]))

(defn get-midi-access []
  (p/let [access (.requestMIDIAccess js/navigator)]
    (re-frame/dispatch-sync [::events/on-midi-access access])
    (re-frame/dispatch-sync [::events/on-midi-select-input nil])))

(defn dev-setup []
  (when config/debug?
    (println "dev mode")))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (.getElementById js/document "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [views/root-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::events/initialize-db])
  ;; (re-frame/dispatch-sync [::rp/add-keyboard-event-listener "keydown"])
  (re-frame/dispatch-sync [::bp/set-breakpoints
                           {:breakpoints [:mobile
                                          768
                                          :tablet
                                          992
                                          :small-monitor
                                          1200
                                          :large-monitor]
                            :debounce-ms 166}])
  (re-frame/dispatch [::events/generate-block-chord-suggestions])
  (re-frame/dispatch [::events/generate-section-scale-suggestions])
  (dev-setup)
  (mount-root)
  (get-midi-access))
