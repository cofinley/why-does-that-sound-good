(ns why-does-that-sound-good.utils-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [why-does-that-sound-good.utils :as utils]))

(deftest block->used-notes-test
  (testing "with just notes, no block wrapper/metadata"
    (is (= #{60 64 67} (utils/block->used-notes #{60 64 67}))))
  (testing "with block, no chord selection"
    (is (= #{60 64 67} (utils/block->used-notes {:id 1 :notes #{60 64 67}}))))
  (testing "with block and chord selection"
    (is (= '(60 64 67) (utils/block->used-notes {:id 1 :notes #{60 64 67} :selected-suggestion {:root :C :chord-type :major :chord-notes '(60 64 67)}})))))

(deftest block->pitches-test
  (testing "with just notes, no block wrapper/metadata"
    (is (= #{:C :E :G} (utils/block->pitches #{60 64 67}))))
  (testing "with block, no chord selection"
    (is (= #{:C :E :G} (utils/block->pitches {:id 1 :notes #{60 64 67}}))))
  (testing "with block and chord selection"
    (is (= #{:C :E :G} (utils/block->pitches {:id 1 :notes #{60 64 67} :selected-suggestion {:root :C :chord-type :major :chord-notes '(60 64 67)}})))))

(deftest in?-test
  (are [coll el expected] (= expected (utils/in? coll el))
    []      1 nil
    [1 2 3] 1 true
    [1 2 3] 4 nil))

(deftest get-cyclic-distance-test
  (are [start end total-length expected-distance] (= expected-distance (utils/get-cyclic-distance start end total-length))
    1  1 10 0
    2  1 10 1 ;; Going backwards in the cycle is shorter
    3  1 10 2
    4  1 10 3
    5  1 10 4
    6  1 10 5 ;; Furthest distance
    7  1 10 4 ;; Going forwards and wrapping around the cycle is shorter now
    8  1 10 3
    9  1 10 2
    10 1 10 1))

(deftest get-pitch-distance
  (are [start-pitch end-pitch expected-distance] (= expected-distance (utils/get-pitch-distance start-pitch end-pitch))
    :C  :C 0
    :C# :C 1 ;; Going backwards in the cycle is shorter
    :Db :C 1 ;; Either accidental representation works
    :D  :C 2
    :D# :C 3
    :E  :C 4
    :F  :C 5
    :F# :C 6 ;; Furthest distance
    :G  :C 5
    :G# :C 4 ;; Going forwards and wrapping around to next C is shorter now
    :A  :C 3
    :A# :C 2
    :B  :C 1))

(deftest music-structure->str-test
  (testing "chord"
    (is (= "Cmaj" (utils/music-structure->str {:root :C :chord-type :maj})))
    (is (= "C#maj" (utils/music-structure->str {:root :C# :chord-type :maj}))))
  (testing "scale"
    (is (= "Cmajor" (utils/music-structure->str {:root :C :scale-type :major}))))
  (testing "supports spaces"
    (is (= "C major" (utils/music-structure->str {:root :C :scale-type :major} :space? true)))))

(deftest jaccard-index-test
  (are [set-1 set-2 expected-index] (= expected-index (utils/jaccard-index set-1 set-2))
    #{1 2 3}   #{1 2 3}   1.0
    #{1 2 3 4} #{1 2}     0.5
    #{1 2}     #{1 2 3 4} 0.5  ;; order-independent
    #{}        #{}        0
    #{1 2}     #{3 4}     0.0))

(deftest pitch-similarity-test
  (are [input-pitches dest-pitches dest-pitches-root expected-index] (= expected-index (utils/pitch-similarity input-pitches dest-pitches dest-pitches-root))
    #{:C :D :E}    #{:C :D :E} :C 1.0
    #{:C :D :E :F} #{:C :D}    :C 0.75  ;; If chord/scale root pitch is in input-pitches, give more weight (base Jaccard Index would normally be 0.5)
    #{}            #{}         :C 0
    #{:C :D}       #{:E :F}    :C 0.5))

(deftest upsert-in-test
  (are [m ks v expected] (= expected (utils/upsert-in m ks v))
    {}         [:foo] 1 {:foo [1]}
    {:foo [1]} [:foo] 2 {:foo [1 2]}))
