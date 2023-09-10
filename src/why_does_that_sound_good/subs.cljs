(ns why-does-that-sound-good.subs
  (:require
   [re-frame.core :as re-frame]
   [why-does-that-sound-good.algo.scale :as scale]
   [clojure.set :as set]))

(re-frame/reg-sub
 ::data
 :-> :data)

;; MIDI

(re-frame/reg-sub
 ::midi-access
 :-> :midi-access)

(re-frame/reg-sub
 ::midi-input
 :<- [::data]
 :-> :midi-input)

(re-frame/reg-sub
 ::midi-output
 :<- [::data]
 :-> :midi-output)

(re-frame/reg-sub
 ::live-notes
 (fn [db _]
   (set/union (get-in db [:live-notes :active]) (get-in db [:live-notes :finished]))))

;; Sections

(re-frame/reg-sub
 ::sections
 :<- [::data]
 :-> :sections)

(re-frame/reg-sub
  ::section
  :<- [::sections]
  (fn [sections [_ section-id]]
    (get sections section-id)))

(re-frame/reg-sub
 ::tabbed-sections
 :<- [::sections]
 (fn [sections _]
   (dissoc sections :live)))

(re-frame/reg-sub
 ::current-section-id
 :-> :current-section-id)

(re-frame/reg-sub
 ::current-section
 :<- [::sections]
 :<- [::current-section-id]
 (fn [[sections current-section-id] _]
   (get sections current-section-id)))

(re-frame/reg-sub
 ::editing-section-name-id
 :-> :editing-section-name-id)

;; Blocks

(re-frame/reg-sub
 ::all-blocks
 :<- [::data]
 (fn [data _]
   (:blocks data)))

(re-frame/reg-sub
 ::section-blocks
 :<- [::current-section]
 :<- [::all-blocks]
 (fn [[current-section all-blocks] _]
   (let [section-block-ids (:block-ids current-section)]
     (select-keys all-blocks section-block-ids))))

(re-frame/reg-sub
 ::current-block-id
 :-> :current-block-id)

(re-frame/reg-sub
 ::temp-block-id
 :-> :temp-block-id)

(re-frame/reg-sub
 ; Identify notes to show on keyboard, based on hover and overridden (chord selected) states
 ::notes-to-display
 :<- [::all-blocks]
 :<- [::current-block-id]
 :<- [::temp-block-id]
 (fn [[blocks current-block-id temp-block-id]]
   (let [block-id (or temp-block-id current-block-id)
         block (get blocks block-id)
         original-notes (:notes block)
         overridden-chord-notes (get-in block [:selected-suggestion :chord-notes])]
     {:overridden? (not (nil? overridden-chord-notes))
      :notes (or overridden-chord-notes original-notes)})))

(re-frame/reg-sub
 ::recording?
 :-> :recording?)

(re-frame/reg-sub
 ::live-section-blocks
 :<- [::data]
 (fn [data _]
   (let [live-section-block-ids (get-in data [:sections :live :block-ids])]
     (into (sorted-map) (select-keys (:blocks data) live-section-block-ids)))))

;; Chord Suggestions

(re-frame/reg-sub
 ::chord-suggestions
 :-> :chord-suggestions)

(re-frame/reg-sub
 ::block-chord-suggestions
 :<- [::chord-suggestions]
 (fn [chord-suggestions [_ block-id]]
   (get chord-suggestions block-id)))

(re-frame/reg-sub
 ::block-min-chord-similarity
 :<- [::section-blocks]
 (fn [blocks [_ block-id]]
   (let [block (get blocks block-id)]
     (Math/round (* 100 (:min-chord-similarity block))))))

(re-frame/reg-sub
 ::temp-chord-suggestion
 :-> :temp-chord-suggestion)

(re-frame/reg-sub
 ::block-selected-suggestion
 :<- [::section-blocks]
 (fn [blocks [_ block-id]]
   (let [block (get blocks block-id)]
     (:selected-suggestion block))))

;; Scale Suggestions

(re-frame/reg-sub
 ::scale-suggestions
 :-> :scale-suggestions)

(re-frame/reg-sub
 ::section-scale-suggestions
 :<- [::scale-suggestions]
 (fn [scale-suggestions [_ section-id]]
   (get scale-suggestions section-id)))

(re-frame/reg-sub
 ::section-min-scale-similarity
 :<- [::sections]
 (fn [sections [_ section-id]]
   (Math/round (* 100 (:min-scale-similarity (get sections section-id))))))

(re-frame/reg-sub
 ::max-live-blocks
 :<- [::data]
 :-> :max-live-blocks)

(re-frame/reg-sub
 ::play-chords-broken?
 :<- [::data]
 :-> :play-chords-broken?)

(re-frame/reg-sub
 ::live-block-debounce
 :<- [::data]
 :-> :live-block-debounce)
