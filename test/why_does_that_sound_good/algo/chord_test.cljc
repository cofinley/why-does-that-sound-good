(ns why-does-that-sound-good.algo.chord-test
  (:require [clojure.test :refer [deftest is are]]
            [why-does-that-sound-good.algo.chord :as chord]))

(deftest find-closest-octave-test
  (is (= 4 (chord/find-closest-octave :C 60)))
  (is (= 3 (chord/find-closest-octave :B 60))))

(deftest get-relative-chord-notes-test
  (are [expected-notes original-notes chord-pitches] (= expected-notes (chord/get-relative-chord-notes original-notes chord-pitches))
    '(60 64 67)    [60 64 67]    #{:C :E :G}
    '(60 64 67 71) [60 64 67]    #{:C :E :G :B}
    '(60 64 67)    [60 64 67 71] #{:C :E :G}
    '(60 64 67 72) [60 64 67 72] #{:C :E :G}))
