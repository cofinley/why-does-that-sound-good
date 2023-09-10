(ns why-does-that-sound-good.algo.chord
  (:require
   [why-does-that-sound-good.pitch :as pitch]
   [why-does-that-sound-good.utils :as utils]))

(def CHORD-INTERVAL-NAMES
  {0 :1
   1 :m2
   2 :M2
   3 :m3
   4 :M3
   5 :4
   6 :-5
   7 :5
   8 :+5
   9 :6
   10 :m7
   11 :M7
   12 :o
   13 :-9
   14 :9
   15 :-10
   17 :11
   18 :+11
   21 :13})

(def ALL-CHORDS
  (into {} (for [root-pitch (vals pitch/REVERSE-NOTES)
                 [chord-type intervals] pitch/CHORD
                 :let [combined-key {:root root-pitch :chord-type chord-type}
                       pitches (set (map #(pitch/interval->pitch root-pitch %) intervals))]]
             [combined-key pitches])))

(defn find-closest-note-name-to-pitch [pitch note-names]
  (loop [note-names note-names
         closest-distance 13
         closest-note-name nil]
    (if (empty? note-names)
      closest-note-name
      (let [note-name (first note-names)
            destination-pitch (keyword (first (name note-name)))
            distance (utils/get-pitch-distance pitch destination-pitch)]
        (if (or (nil? closest-note-name) (< distance closest-distance))
          (recur (rest note-names) distance note-name)
          (recur (rest note-names) closest-distance closest-note-name))))))

(defn match-pitches-to-note-names
  "Try to match chord pitches (e.g. :D) to original note names (e.g. :D4)
   1. Find exact matches between pitches and the pitches of the notes ({:D :D4})
   2. Separate out anything that wasn't matched (i.e. chord pitches which had no note match, and notes which had no chord pitch match)"
  [pitches notes]
  (let [note-pitch->note-name (reduce (fn [m note] (assoc m (pitch/find-pitch-class-name note) (pitch/find-note-name note))) {} notes)
        chord-pitch->note-name (reduce (fn [m chord-pitch] (assoc m chord-pitch (get note-pitch->note-name chord-pitch))) {} pitches)]
    {:matched-note-names (vals (into {} (filter (fn [[_ note-name]] (some? note-name))
                                                chord-pitch->note-name)))
     :unmatched-pitches (keys (into {} (filter (fn [[_ note-name]] (nil? note-name))
                                               chord-pitch->note-name)))
     :unmatched-note-names (filter (fn [note-name]
                                     (not (some #(= % note-name)
                                                (vals chord-pitch->note-name))))
                                   (vals note-pitch->note-name))}))

;; TODO: keep same-pitch notes in chord, even if user plays same pitch in multiple octaves
(defn get-relative-chord-notes
  "Voice chord closest to original notes
   AKA match chord pitch to closest original note octaves
   Because we are fuzzy-finding based on pitch and not intervals, we have to reconstruct octave information"
  [original-notes chord-pitches]
  (let [original-octave-center (utils/median (map pitch/note->octave original-notes))
        matches (match-pitches-to-note-names chord-pitches original-notes)]
    (loop [chord-pitches-remaining (:unmatched-pitches matches)
           original-note-names-remaining (:unmatched-note-names matches)
           chord-notes []]
      (if (empty? chord-pitches-remaining)
        (concat (map pitch/note (:matched-note-names matches)) chord-notes)
        (let [chord-pitch (first chord-pitches-remaining)
              closest-note-name (when-not (empty? original-note-names-remaining)
                                  (find-closest-note-name-to-pitch chord-pitch original-note-names-remaining))
              closest-octave (if (empty? original-note-names-remaining)
                               original-octave-center
                               (:octave (pitch/note-info closest-note-name)))
              chord-note (pitch/construct-note chord-pitch closest-octave)]
          (recur (rest chord-pitches-remaining)
                 (remove #(= % closest-note-name) original-note-names-remaining)
                 (conj chord-notes chord-note)))))))

(defn chord->readable-intervals [root chord-type]
  (let [intervals (pitch/CHORD chord-type)]
    (reduce (fn [m interval]
              (let [pitch (pitch/interval->pitch root interval)
                    readable-interval (CHORD-INTERVAL-NAMES interval)]
                (assoc m pitch readable-interval))) {} intervals)))

(defn block->chords [block & {:keys [min-chord-similarity find-closest?]
                              :or {min-chord-similarity 0.90 find-closest? false}}]
  (let [notes (or (:notes block) block)]
    (when (> (count notes) 1)
      (let [block-pitches (set (map pitch/find-pitch-class-name notes))
            lowest-pitch (pitch/find-pitch-class-name (first (sort notes)))
            chords (map (fn [[chord-key chord-pitches]]
                          (assoc chord-key
                                 :chord-pitches chord-pitches
                                 :similarity (utils/pitch-similarity block-pitches chord-pitches (:root chord-key))))
                        ALL-CHORDS)
            max-similarity-found (:similarity (apply max-key :similarity chords))]
        (->> chords
             (filter #(if find-closest?
                        (= (:similarity %) max-similarity-found)
                        (>= (:similarity %) min-chord-similarity)))
             (map #(assoc %
                          :original-block-id (:id block)
                          :lowest-note-root? (if (= lowest-pitch (:root %)) 1 0)
                          :chord-pitches->intervals (chord->readable-intervals (:root %) (:chord-type %))
                          :chord-notes (get-relative-chord-notes notes (:chord-pitches %))))
             (sort-by (juxt (comp - :similarity) (comp - :lowest-note-root?))))))))

(def mem-block->chords (memoize block->chords))
