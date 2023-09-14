(ns why-does-that-sound-good.test-utils
  (:require
   [why-does-that-sound-good.algo.chord :as chord]
   [why-does-that-sound-good.pitch :as pitch]))

(defn chord->example-notes [root chord-type]
  (->> {:root root :chord-type chord-type}
       chord/ALL-CHORDS
       (map #(pitch/construct-note % 4))))

(def c-major-blocks
  (map-indexed (fn [i [root chord-type]]
                 {:id i :notes (chord->example-notes root chord-type)})
               [[:C :maj]
                [:D :min]
                [:E :min]
                [:F :maj]
                [:G :maj]
                [:A :min]
                [:B :dim]]))
