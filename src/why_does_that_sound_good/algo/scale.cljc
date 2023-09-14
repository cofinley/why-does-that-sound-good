(ns why-does-that-sound-good.algo.scale
  (:require
   [clojure.math.combinatorics :as combo]
   [clojure.set :as set]
   [why-does-that-sound-good.pitch :as pitch]
   [why-does-that-sound-good.algo.chord :as chord]
   [why-does-that-sound-good.utils :as utils]))

(defn steps->intervals
  " '(2 2 1 2) => '(0 2 4 5 7) "
  [steps]
  (if (empty? steps)
    (list 0)
    (cons 0 (reductions + steps))))

(defn scale->pitches
  "[:C :major] => (2 2 1 2 2 2 1) => (0 2 4 5 7 9 12) => '(:C :D :E :F :G :A :B)"
  [root-pitch scale-type]
  (->>
   scale-type
   pitch/SCALE
   steps->intervals
   butlast  ; redundant root at end of scale
   (map #(pitch/interval->pitch root-pitch %))))

(def ALL-SCALES
  (into {} (for [root-pitch (vals pitch/REVERSE-NOTES)
                 scale-type (keys pitch/SCALE)
                 :let [scale-desc {:root root-pitch :scale-type scale-type}
                       pitches (scale->pitches root-pitch scale-type)]
                 :when (contains? pitch/ENABLED-SCALES scale-type)]
             [scale-desc pitches])))

(defn pitches->scales [pitches & {:keys [min-scale-similarity find-closest?]
                                  :or {min-scale-similarity 0.95 find-closest? false}}]
  (when (> (count pitches) 1)
    (let [scales (map (fn [[scale-desc scale-pitches]]
                        (assoc scale-desc
                               :scale-pitches scale-pitches
                               :similarity (utils/pitch-similarity pitches (set scale-pitches) (:root scale-desc))))
                      ALL-SCALES)
          max-similarity-found (:similarity (apply max-key :similarity scales))]
      (->> scales
           (filter #(if find-closest?
                      (= (:similarity %) max-similarity-found)
                      (>= (:similarity %) min-scale-similarity)))
           (sort-by (comp - :similarity))))))

(def max-chord-interval 21)  ;; a 13th; highest interval in pitch/CHORD; anything higher can likely be folded down an octave

(defn scale-pitches->intervals
  "Start with scale (e.g. Cmaj) #{:C :D :E :F :G :A :B}
  -> repeat up to max-chord-interval intervals from root pitch (e.g. :B) #{:B :C :D :E :F :G :A :B :C :D :E :F :G :A :B :C :D :E :F :G :A}
  -> to semitone intervals '#{0 1 3 5 6 ...}"
  [scale-pitches root-pitch]
  (->> scale-pitches
       (sort (fn [a b] (- (pitch/NOTES a) (pitch/NOTES b))))
       cycle
       (drop-while #(not= root-pitch %))
       (take max-chord-interval)
       (partition 2 1)
       (map #(apply utils/get-pitch-distance %))
       steps->intervals
       (take-while #(<= % max-chord-interval))
       (apply sorted-set)))

(defn scale-pitch->diatonic-chords
  "Given a set of pitches, find available chord types (based on valid intervals in the scale) and construct diatonic chords"
  [scale-pitches root-pitch & {:keys [octave]
                               :or {octave 4}}]
  (let [root-note (pitch/note (str (name root-pitch) octave))
        all-scale-intervals (scale-pitches->intervals scale-pitches root-pitch)
        diatonic-chord-types (filter (fn [[_ chord-intervals]]
                                       (set/subset? chord-intervals all-scale-intervals))
                                     pitch/CHORD)]
    (map (fn [[chord-type chord-intervals]]
           {:root root-pitch
            :chord-type chord-type
            :chord-intervals chord-intervals
            :chord-pitches->readable-intervals (chord/chord->readable-intervals {:root root-pitch :chord-type chord-type})
            :chord-notes (map #(+ root-note %) (sort chord-intervals))})
         diatonic-chord-types)))

(defn scale->diatonic-chords
  "For each pitch in scale, construct their diatonic chords"
  [scale-desc & {:keys [octave]
                 :or {octave 4}}]
  (let [scale-pitches (get ALL-SCALES scale-desc)]
    (zipmap scale-pitches
            (map-indexed (fn [idx scale-pitch]
                           (let [last-pitch (if (zero? idx) nil (nth scale-pitches (dec idx)))
                                 last-pitch-cyclic-index (if (zero? idx) -1 (pitch/NOTES last-pitch))
                                 current-pitch-cyclic-index (pitch/NOTES scale-pitch)
                                 bump-octave? (< current-pitch-cyclic-index last-pitch-cyclic-index)
                                 used-octave (if bump-octave? ;; Bump octave once past C (backwards on pitch/NOTES indices)
                                               (inc octave)
                                               octave)]
                             (scale-pitch->diatonic-chords scale-pitches scale-pitch :octave used-octave)))
                         scale-pitches))))

(defn merge-chord-suggestions-with-scale-diatonic-chords
  "Take scale's diatonic chords (grouped by pitch) and insert chord suggestions at the top of the list for the respective scale pitch"
  [chords diatonic-chords]
  (reduce (fn [all-chords chord]
            (let [root-pitch (:root chord)
                  all-chords-for-root (get all-chords root-pitch)]
              (assoc all-chords root-pitch
                     ;; TODO: after similarity, sort by 'popularity'/complexity
                     (sort-by (comp - #(or (:similarity %) 0))
                              (map (fn [all-chord]
                                     (if (= (:chord-type chord) (:chord-type all-chord))
                                       chord  ; Replace scale chord with suggested chord [has :similarity, block Id]
                                       all-chord))
                                   all-chords-for-root)))))
          diatonic-chords
          chords))

(defn block-variation-combo->scales
  "1. Union pitches of all variations in a combo (i.e. one variation from each block)
   2. Find scale(s) by overlapping scale-pitches
   3. Generate diatonic chords for scale and merge in chord suggestions"
  [block-variation-combo current-scales original-pitches min-scale-similarity find-closest?]
  (let [combined-variation-combo-notes (apply set/union (map #(or (:chord-notes %) (:notes %)) block-variation-combo))
        combined-variation-combo-pitches (set (map pitch/find-pitch-class-name combined-variation-combo-notes))
        scales (pitches->scales combined-variation-combo-pitches :min-scale-similarity min-scale-similarity :find-closest? find-closest?)
        lowest-octave (min (pitch/note->octave (apply min combined-variation-combo-notes)))
        chord-blocks (filter #(some? (:chord-notes %)) block-variation-combo)]
    (reduce (fn [final-scales scale]
              (let [scale-desc (select-keys scale [:root :scale-type])
                    diatonic-chords (scale->diatonic-chords scale-desc :octave lowest-octave)]
                (if (contains? final-scales scale-desc)
           ;; Might not be necessary to track chord combos for a scale
                  (update-in final-scales [scale-desc :chord-combos] conj block-variation-combo)
                  (assoc final-scales scale-desc
                         {:scale-pitches (:scale-pitches scale)
                          :combined-pitches combined-variation-combo-pitches
                          :original-pitches original-pitches
                          :variation-combo-pitch-similarity (:similarity scale)  ;; Similarity based on the variation combo (could include chord suggestions)
                          :original-pitch-similarity (utils/pitch-similarity original-pitches (set (:scale-pitches scale)) (:root scale-desc))  ;; Similarity based on exactly what was played/selected
                          :chord-combos [block-variation-combo]
                          :all-chords (merge-chord-suggestions-with-scale-diatonic-chords chord-blocks diatonic-chords)}))))
            current-scales
            scales)))

(defn blocks->block-variations
  "For each block, return a list of variations.
   If the block has 2+ notes, return the chord suggestions.
   If the block only has one note, return the note in a list.
   Leverage pre-existing chord suggestions {block-id suggestions} if provided (true if within UI context)"
  [blocks & {:keys [pregenerated-block-chord-suggestions]
             :or {pregenerated-block-chord-suggestions {}}}]
  (let [{chord-blocks true
         note-blocks false} (group-by #(>= (count (:notes %)) 2) blocks)
        {blocks-with-chord-selection true
         blocks-without-chord-selection false} (group-by #(some? (:selected-suggestion %)) chord-blocks)
        selected-chords (->> blocks-with-chord-selection
                             (map :selected-suggestion)
                             (remove nil?)
                             (map list))
        new-chords (->> blocks-without-chord-selection
                        ;; UI will pass in existing block chord suggestions (:chord-suggestions db) to avoid recalculation
                        ;; Running outside of UI context will generate chord suggestions for any blocks which haven't selected any (selection stored in block)
                        (map #(or (get pregenerated-block-chord-suggestions (:id %))
                                  (chord/mem-block->chords % :find-closest? true))))]
    (concat selected-chords new-chords (map list note-blocks))))  ;; seq of variations for every block

(defn blocks->scales
  "Find scales by combining block variations (i.e. chord suggestion if 2+ notes in block or individual note)
   Returns similar scales with diatonic chords + any suggested chords"
  [blocks & {:keys [min-scale-similarity find-closest? pregenerated-block-chord-suggestions]
             :or {min-scale-similarity 0.95 find-closest? false pregenerated-block-chord-suggestions {}}}]
  (let [original-pitches (->> blocks
                              (map utils/block->pitches)
                              (apply set/union))
        block-variations (blocks->block-variations blocks :pregenerated-block-chord-suggestions pregenerated-block-chord-suggestions)  ;; seq of variations for every block
        block-variation-combos (apply combo/cartesian-product block-variations)]
    (loop [combos block-variation-combos
           scales {}
           seen-pitch-sets #{}]
      (if (empty? combos)
        (into (sorted-map-by (fn [scale-desc-1 scale-desc-2]
                               ;; Compare similarity _and_ scale name since going into map where similarity might be same across multiple scales
                               ;; Should maybe compare :original-pitch-similarity, though might be close enough
                               (compare [(get-in scales [scale-desc-2 :variation-combo-pitch-similarity]) (utils/music-structure->str scale-desc-2)]
                                        [(get-in scales [scale-desc-1 :variation-combo-pitch-similarity]) (utils/music-structure->str scale-desc-1)])))
              scales)
        (let [variation-combo (first combos)
              combined-variation-combo-notes (apply set/union (map #(or (:chord-notes %) (:notes %)) variation-combo))
              combined-variation-combo-pitches (set (map pitch/find-pitch-class-name combined-variation-combo-notes))
              new-pitch-set? (contains? seen-pitch-sets combined-variation-combo-pitches)]
          (if new-pitch-set?
            (recur (rest combos)
                   scales
                   seen-pitch-sets)
            (recur (rest combos)
                   (block-variation-combo->scales variation-combo scales original-pitches min-scale-similarity find-closest?)
                   (conj seen-pitch-sets combined-variation-combo-pitches))))))))

(def mem-blocks->scales (memoize blocks->scales))
