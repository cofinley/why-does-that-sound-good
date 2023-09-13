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
  (are [expected coll el] (= expected (utils/in? coll el))
    nil [] 1
    true [1 2 3] 1
    nil [1 2 3] 4))

(deftest get-cyclic-distance-test
  (are [expected-distance start end total-length] (= expected-distance (utils/get-cyclic-distance start end total-length))
    0  1 1 10
    1  2 1 10  ;; Going backwards in the cycle is shorter
    2  3 1 10
    3  4 1 10
    4  5 1 10
    5  6 1 10  ;; Furthest distance
    4  7 1 10  ;; Going forwards and wrapping around the cycle is shorter now
    3  8 1 10
    2  9 1 10
    1 10 1 10))

(deftest get-pitch-distance
  (are [expected-distance start-pitch end-pitch] (= expected-distance (utils/get-pitch-distance start-pitch end-pitch))
    0 :C  :C
    1 :C# :C  ;; Going backwards in the cycle is shorter
    1 :Db :C  ;; Either accidental representation works
    2 :D  :C
    3 :D# :C
    4 :E  :C
    5 :F  :C
    6 :F# :C  ;; Furthest distance
    5 :G  :C
    4 :G# :C  ;; Going forwards and wrapping around to next C is shorter now
    3 :A  :C
    2 :A# :C
    1 :B  :C))

(deftest music-structure->str-test
  (testing "chord"
    (is (= "Cmaj" (utils/music-structure->str {:root :C :chord-type :maj})))
    (is (= "C#maj" (utils/music-structure->str {:root :C# :chord-type :maj}))))
  (testing "scale"
    (is (= "Cmajor" (utils/music-structure->str {:root :C :scale-type :major}))))
  (testing "supports spaces"
    (is (= "C major" (utils/music-structure->str {:root :C :scale-type :major} :space? true)))))

(deftest jaccard-index-test
  (are [expected-index set-1 set-2] (= expected-index (utils/jaccard-index set-1 set-2))
    1.0 #{1 2 3}   #{1 2 3}
    0.5 #{1 2 3 4} #{1 2}
    0.5 #{1 2}     #{1 2 3 4}  ;; order-independent
    0   #{}        #{}
    0.0 #{1 2}     #{3 4}))

(deftest pitch-similarity-test
  (are [expected-index input-pitches dest-pitches dest-pitches-root] (= expected-index (utils/pitch-similarity input-pitches dest-pitches dest-pitches-root))
    1.0  #{:C :D :E}    #{:C :D :E}    :C
    0.75 #{:C :D :E :F} #{:C :D}       :C  ;; If chord/scale root pitch is in input-pitches, give more weight (base Jaccard Index would normally be 0.5)
    0    #{}            #{}            :C
    0.5  #{:C :D}       #{:E :F}       :C))

(deftest upsert-in-test
  (are [expected m ks v] (= expected (utils/upsert-in m ks v))
    {:foo [1]} {} [:foo] 1
    {:foo [1 2]} {:foo [1]} [:foo] 2))
