(ns why-does-that-sound-good.algo.chord-test
  (:require
   [clojure.test :refer [deftest are]]
   [why-does-that-sound-good.pitch :as pitch]
   [why-does-that-sound-good.algo.chord :as chord]))

(deftest find-closest-octave-test
  (are [pitch closest-note expected-closest-octave] (= expected-closest-octave (chord/find-closest-octave pitch closest-note))
    :C (pitch/note :C4) 4
    :B (pitch/note :C4) 3))

(deftest get-relative-chord-notes-test
  (are [original-notes chord-pitches expected-notes] (= expected-notes (chord/get-relative-chord-notes original-notes chord-pitches))
    [60 64 67]    #{:C :E :G}    '(60 64 67)
    [60 64 67]    #{:C :E :G :B} '(60 64 67 71)
    [60 64 67 71] #{:C :E :G}    '(60 64 67)
    [60 64 67 72] #{:C :E :G}    '(60 64 67 72)))

(deftest block->chords-test
  (are [block expected-chords] (= expected-chords (chord/block->chords block :find-closest? true))
    {:id 1 :notes #{60 64 67}} '({:root :C
                                  :chord-type :maj
                                  :chord-pitches #{:C :G :E}
                                  :similarity 1
                                  :original-block-id 1
                                  :lowest-note-root? 1
                                  :chord-pitches->readable-intervals {:C :1 :G :5 :E :M3}
                                  :chord-notes (60 64 67)}
                                 {:root :E
                                  :chord-type :m+5
                                  :chord-pitches #{:E :G :C}
                                  :similarity 1
                                  :original-block-id 1
                                  :lowest-note-root? 0
                                  :chord-pitches->readable-intervals {:E :1 :G :m3 :C :+5}
                                  :chord-notes (60 64 67)})

    {:id 1 :notes #{}} nil))
