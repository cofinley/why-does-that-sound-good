(ns why-does-that-sound-good.utils
  (:require
   [clojure.set :as set]
   [clojure.string :as str]
   [why-does-that-sound-good.pitch :as pitch]))

(defn block->used-notes [block]
  (or (get-in block [:selected-suggestion :chord-notes])
      (:notes block)
      block))

(defn block->pitches [block]
  (->> block
       block->used-notes
       (map pitch/find-pitch-class-name)
       set))

(defn in?
  "true if coll contains element, else nil"
  [coll el]
  (some #(= el %) coll))

(defn get-cyclic-distance [a b len]
  (let [distance (mod (- b a) len)
        reverse-distance (mod (- a b) len)]
    (min distance reverse-distance)))

(defn get-pitch-distance [a b]
  (get-cyclic-distance (pitch/NOTES a) (pitch/NOTES b) (count pitch/REVERSE-NOTES)))

(defn music-structure->str
  [{:keys [root chord-type scale-type]} & {:keys [space?]
                                           :or {space? false}}]
  (str/join (if space? " " "") [(name root) (name (or chord-type scale-type))]))

(defn median [coll]
  (if (= (count coll) 1)
    (first coll)
    (nth (sort coll) (dec (quot (count coll) 2)))))

(defn jaccard-index [set1 set2]
  (let [intersection (count (set/intersection set1 set2))
        union (count (set/union set1 set2))]
    (if (zero? union)
      0
      (float (/ intersection union)))))

(defn pitch-similarity
  "Get Jaccard Index of input pitches vs. chord/scale pitches (weighted more if root pitch in input)"
  [input-pitches dest-pitches dest-pitches-root]
  (let [jaccard-index (jaccard-index input-pitches dest-pitches)
        dest-root-in-pitches (contains? input-pitches dest-pitches-root)
        dest-root-in-input-pitches-weight 1
        dest-root-in-input-pitches-coefficient (if dest-root-in-pitches dest-root-in-input-pitches-weight 0)]
    (/ (+ jaccard-index dest-root-in-input-pitches-coefficient)
       (+ 1             dest-root-in-input-pitches-weight))))

(defn pitches->example-notes
  "For showing scale pitches on piano preview at middle C
   Add root pitch to end as well to round it out"
  [pitches]
  (loop [notes []
         note-range (range 60 85)
         ps (conj (vec pitches) (first pitches))]
    (if (empty? ps)
      notes
      (let [note (first note-range)
            note-pitch (pitch/find-pitch-class-name note)
            pitch-match? (= (first ps) note-pitch)]
        (if pitch-match?
          (recur (conj notes note) (range (inc note) 85) (subvec ps 1))
          (recur notes (range (inc note) 85) ps))))))

(defn upsert-in [m ks v]
  (if (get-in m ks)
    (update-in m ks conj v)
    (assoc-in m ks [v])))
