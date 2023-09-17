(ns why-does-that-sound-good.algo.chord
  (:require
   [why-does-that-sound-good.pitch :as pitch]
   [why-does-that-sound-good.utils :as utils]))

(def ALL-CHORDS
  (into {} (for [root-pitch (vals pitch/REVERSE-NOTES)
                 [chord-type {:keys [intervals]}] pitch/CHORD
                 :let [chord-desc {:root root-pitch :chord-type chord-type}
                       pitches (set (map #(pitch/interval->pitch root-pitch %) intervals))]]
             [chord-desc pitches])))

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

(defn find-closest-octave
  "Find closest octave to note given a pitch (not necessarily same octave as note)"
  [pitch closest-note]
  (loop [octaves (map #(+ % (pitch/note->octave closest-note)) (range -1 2))
         min-distance 100
         closest-octave nil]
    (if (empty? octaves)
      closest-octave
      (let [octave (first octaves)
            note (pitch/construct-note pitch octave)
            distance (abs (- closest-note note))]
        (if (< distance min-distance)
          (recur (rest octaves) distance octave)
          (recur (rest octaves) min-distance closest-octave))))))

(defn match-pitches-to-note-names
  "Try to match chord pitches (e.g. :D) to original note names (e.g. :D4)
   1. Find exact matches between pitches and the pitches of the notes ({:D :D4})
   2. Separate out anything that wasn't matched (i.e. chord pitches which had no note match, and notes which had no chord pitch match)"
  [chord-pitches original-notes]
  (let [note-pitch->note-names (reduce (fn [m note]
                                         ;; There might be multiple instances of the same pitch played; upsert them
                                         (utils/upsert-in m [(pitch/find-pitch-class-name note)] (pitch/find-note-name note)))
                                       {}
                                       original-notes)
        chord-pitch->note-names (reduce (fn [m chord-pitch]
                                          (assoc m chord-pitch (get note-pitch->note-names chord-pitch)))
                                        {}
                                        chord-pitches)
        {matched-chord-pitches true
         unmatched-chord-pitches false} (group-by #(some? (val %)) chord-pitch->note-names)]
    {:matched-note-names (flatten (vals matched-chord-pitches))
     :unmatched-chord-pitches (or (keys unmatched-chord-pitches) (list))
     :unmatched-note-names (flatten (filter (fn [note-names]
                                              (not (some #(= % note-names) (vals chord-pitch->note-names))))
                                            (vals note-pitch->note-names)))}))

(defn get-relative-chord-notes
  "Voice chord closest to original notes
   AKA match chord pitch to closest original note octaves
   Because we are fuzzy-finding based on pitch and not intervals, we have to reconstruct octave information"
  [original-notes chord-pitches]
  (let [original-octave-center (utils/median (map pitch/note->octave original-notes))
        matches (match-pitches-to-note-names chord-pitches original-notes)]
    (loop [chord-pitches-remaining (:unmatched-chord-pitches matches)
           original-note-names-remaining (:unmatched-note-names matches)
           chord-notes []]
      (if (empty? chord-pitches-remaining)
        (sort (concat (map pitch/note (:matched-note-names matches)) chord-notes))
        (let [chord-pitch (first chord-pitches-remaining)
              closest-note-name (when-not (empty? original-note-names-remaining)
                                  (find-closest-note-name-to-pitch chord-pitch original-note-names-remaining))
              closest-octave (if (empty? original-note-names-remaining)
                               original-octave-center
                               (find-closest-octave chord-pitch (pitch/note closest-note-name)))
              chord-note (pitch/construct-note chord-pitch closest-octave)]
          (recur (rest chord-pitches-remaining)
                 (remove #(= % closest-note-name) original-note-names-remaining)
                 (conj chord-notes chord-note)))))))

(defn chord->readable-intervals [{:keys [root chord-type]}]
  (let [chord-details (get pitch/CHORD chord-type)
        intervals (:intervals chord-details)
        interval-names (:interval-names chord-details)]
    (reduce (fn [m [i interval]]
              (let [pitch (pitch/interval->pitch root interval)
                    readable-interval (nth interval-names i)]
                (assoc m pitch readable-interval))) {}
            (map-indexed vector intervals))))

(defn block->chords [block & {:keys [min-chord-similarity find-closest?]
                              :or {min-chord-similarity 0.90 find-closest? false}}]
  (let [notes (or (:notes block) block)]
    (when (> (count notes) 1)
      (let [block-pitches (set (map pitch/find-pitch-class-name notes))
            lowest-pitch (pitch/find-pitch-class-name (first (sort notes)))
            chords (map (fn [[chord-desc chord-pitches]]
                          (assoc chord-desc
                                 :chord-pitches chord-pitches
                                 :similarity (utils/pitch-similarity block-pitches chord-pitches (:root chord-desc))))
                        ALL-CHORDS)
            max-similarity-found (:similarity (apply max-key :similarity chords))]
        (->> chords
             (filter #(if find-closest?
                        (= (:similarity %) max-similarity-found)
                        (>= (:similarity %) min-chord-similarity)))
             (map #(assoc %
                          :original-block-id (:id block)
                          :lowest-note-root? (if (= lowest-pitch (:root %)) 1 0)
                          :chord-pitches->readable-intervals (chord->readable-intervals %)
                          :chord-notes (get-relative-chord-notes notes (:chord-pitches %))))
             (sort-by (juxt (comp - :similarity) (comp - :lowest-note-root?))))))))

(def mem-block->chords (memoize block->chords))
