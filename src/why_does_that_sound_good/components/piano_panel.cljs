(ns why-does-that-sound-good.components.piano-panel
  (:require
   [clojure.string :as str]
   [re-frame.core :as re-frame]
   [breaking-point.core :as bp]
   [why-does-that-sound-good.utils :as utils]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.subs :as subs]
   [clojure.set :as set]
   [why-does-that-sound-good.pitch :as pitch]))

(def white-key-color "#CBCBCB")
(def white-key-border-color "#AAA")
(def black-key-color "#222")
(def black-key-border-color "#000")
(def white-key-color-played "#6366f1") ;; indigo-500
(def black-key-color-played "#4338CA") ;; indigo-700
(def white-key-color-overridden "#A855F7")  ;; purple-500
(def black-key-color-overridden "#9333EA")  ;; purple-600
(def key-kept-in-chord "gray")
(def key-added-in-chord "#16A34A")  ;; green-600
(def key-removed-in-chord "#EF4444")  ;; red-500

(def piano-keys
  (take 88 (map
            (fn [index pitch color]
              {:index index
               :pitch pitch
               :midi (+ 21 index)
               :color color})
            (range)
            (cycle [:A :Bb :B :C :C# :D :Eb :E :F :F# :G :Ab])
            (cycle [:w :b  :w :w :b  :w :b  :w :w :b  :w :b]))))

(def margin-keys #{:A :B :D :E :G})

(defn piano-panel []
  (let [{notes :notes notes-overridden? :overridden?} @(re-frame/subscribe [::subs/notes-to-display])
        live-notes @(re-frame/subscribe [::subs/live-notes])
        temp-chord-suggestion @(re-frame/subscribe [::subs/temp-chord-suggestion])
        screen-width @(re-frame/subscribe [::bp/screen-width])
        piano-width (* 0.9 screen-width)
        piano-height (* 0.08 piano-width)
        border-width (* 0.0015 piano-width)
        num-white-keys (count (filter #(= :w (:color %)) piano-keys))
        white-key-width (/ piano-width num-white-keys)
        black-key-width (/ white-key-width 2)
        black-key-height (/ piano-height 1.6)
        black-key-offset (- (- (/ black-key-width 2)) border-width)
        margin (str "0 0 0 " black-key-offset "px")]
    [:div.flex.self-center.rounded.pl-2
     (for [key piano-keys
           :let [white? (= :w (:color key))
                 highlighted? (or (utils/in? live-notes (:midi key)) (utils/in? notes (:midi key)))
                 chord-note? (utils/in? (:chord-notes temp-chord-suggestion) (:midi key))
                 overridden? (and notes-overridden? highlighted?)
                 interval (if chord-note? (get (:chord-pitches->readable-intervals temp-chord-suggestion) (:pitch key)) nil)
                 key-color (if white?
                             (cond
                               overridden? white-key-color-overridden
                               highlighted? white-key-color-played
                               :else white-key-color)
                             (cond
                               overridden? black-key-color-overridden
                               highlighted? black-key-color-played
                               :else black-key-color))
                 border (str border-width "px solid " (if white?
                                                        (cond
                                                          overridden? black-key-color-overridden
                                                          highlighted? black-key-color-played
                                                          :else white-key-border-color)
                                                        black-key-border-color))
                 on-click #(re-frame/dispatch [::events/on-note-click (:midi key)])]]
       ^{:key (:index key)}
       [:button.flex.flex-col.justify-end
        {:style (if white?
                  {:height piano-height
                   :width white-key-width
                   :border-top border
                   :border-bottom border
                   :border-left border
                   :background-color key-color
                   :margin (if (contains? margin-keys (:pitch key)) margin 0)}
                  {:height black-key-height
                   :width black-key-width
                   :z-index 2
                   :border border
                   :background-color key-color
                   :margin margin})
         :class "cursor-pointer hover:dark:!bg-neutral-400 hover:dark:!border-neutral-500"
         :on-click on-click
         :title (:pitch key)}
        (when chord-note?
          [:div
           {:class (str/join " "
                             [(if white? "mr-3.5" "-ml-1.5")
                              (if (= :1 interval) "dark:text-orange-300 dark:border-orange-300" "dark:border-neutral-100")
                              "text-sm font-bold drop-shadow-md rounded-full bg-blue-500 h-7 w-7 z-10 mb-3 border-2 pt-0.5"])}
           (or interval "")])])]))

(defn piano-preview [notes & {:keys [chord-notes]
                              :or {chord-notes []}}]
  (let [white-key-width 25
        min-note (apply min notes)
        prev-c (max 21 (- min-note (mod min-note 12)))
        max-note (apply max notes)
        next-c (min 108 (+ max-note (- 12 (mod max-note 12))))
        piano-key-span (filter #(<= prev-c (:midi %) next-c) piano-keys)
        num-white-keys (count (filter #(= :w (:color %)) piano-key-span))
        chord-specific-notes (set/difference (set chord-notes) (set notes))
        block-specific-notes (set/difference (set notes) (set chord-notes))
        shared-notes (set/intersection (set notes) (set chord-notes))
        piano-width (* white-key-width num-white-keys)
        piano-height (* 2.3 white-key-width)
        border-width (* 0.0015 piano-width)
        black-key-width (/ white-key-width 2)
        black-key-height (/ piano-height 1.6)
        black-key-offset (- (- (/ black-key-width 2)) border-width)
        margin (str "0 0 0 " black-key-offset "px")]
    [:div.flex.rounded.overflow-hidden.pl-2
     {:class "cursor-pointer"}
     (for [key piano-key-span
           :let [white? (= :w (:color key))
                 octave? (= 0 (mod (:midi key) 12))
                 highlighted? (utils/in? (if (seq chord-notes) chord-notes notes) (:midi key))
                 chord-specific-note? (utils/in? chord-specific-notes (:midi key))
                 block-specific-note? (if (seq chord-notes) (utils/in? block-specific-notes (:midi key)) false)
                 shared-note? (utils/in? shared-notes (:midi key))
                 key-color (cond
                             chord-specific-note? key-added-in-chord
                             block-specific-note? key-removed-in-chord
                             shared-note? key-kept-in-chord
                             highlighted? (if white? white-key-color-played black-key-color-played)
                             :else (if white? white-key-color black-key-color))
                 border (str border-width "px solid rgba(0,0,0,0.5)")]]

       ^{:key (:index key)}
       [:button.flex.flex-col.justify-end
        {:style (if white?
                  {:height piano-height
                   :width white-key-width
                   :border-top border
                   :border-bottom border
                   :border-left border
                   :background-color key-color
                   :margin (if (contains? margin-keys (:pitch key)) margin 0)}
                  {:height black-key-height
                   :width black-key-width
                   :z-index 2
                   :border border
                   :background-color key-color
                   :margin margin})}
        (when (and octave? (not (seq chord-notes)))
          [:span
           {:class (str/join " " [(if highlighted? "dark:text-neutral-100" "dark:text-neutral-900") "relative text-xs opacity-80"])}
           (str "C" (pitch/note->octave (:midi key)))])])]))
