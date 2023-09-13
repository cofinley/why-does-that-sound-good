(ns why-does-that-sound-good.algo.scale-test
  (:require [clojure.test :refer [deftest testing is are]]
            [why-does-that-sound-good.algo.scale :as scale]
            [why-does-that-sound-good.pitch :as pitch]
            [why-does-that-sound-good.algo.chord :as chord]))

(deftest steps->intervals-test
  (are [expected-intervals input-steps] (= expected-intervals (scale/steps->intervals input-steps))
    '(0) ()
    '(0 1 3 6) '(1 2 3)
    '(0 2 4 5 7 9 11 12) '(2 2 1 2 2 2 1)))

(deftest scale->pitches-test
  (are [expected-pitches root-pitch scale-type] (= expected-pitches (scale/scale->pitches root-pitch scale-type))
    '(:C :D :E :F :G :A :B)      :C  :major
    '(:C :D :Eb :F :G :Ab :Bb)   :C  :minor
    '(:C# :Eb :F :F# :Ab :Bb :C) :C# :major
    ;; TODO: convert accidentals accordingly
    '(:C# :Eb :F :F# :Ab :Bb :C) :Db :major))

(deftest scale-pitches->intervals-test
  ;; Intervals all the way to up a 13th
  (is (= #{0 2 4 5 7 9 11 12 14 16 17 19 21} (scale/scale-pitches->intervals #{:C :D :E :F :G :A :B} :C))))

(deftest pitches->scales-test
  (testing "find-closest? true"
    (is (= '({:root :C,
              :scale-type :major,
              :scale-pitches (:C :D :E :F :G :A :B),
              :similarity 1.0}
             {:root :A,
              :scale-type :minor,
              :scale-pitches (:A :B :C :D :E :F :G),
              :similarity 1.0})
           (scale/pitches->scales #{:C :D :E :F :G :A :B} :find-closest? true))))
  (testing "min-similarity"
    (is (= '({:root :C,
              :scale-type :major,
              :scale-pitches (:C :D :E :F :G :A :B),
              :similarity 0.9375}
             {:root :A,
              :scale-type :melodic-major,
              :scale-pitches (:A :B :C# :D :E :F :G),
              :similarity 0.9375}
             {:root :D,
              :scale-type :melodic-minor,
              :scale-pitches (:D :E :F :G :A :B :C#),
              :similarity 0.9375}
             {:root :A,
              :scale-type :minor,
              :scale-pitches (:A :B :C :D :E :F :G),
              :similarity 0.9375})
           (scale/pitches->scales #{:C :C# :D :E :F :G :A :B} :min-scale-similarity 0.90)))))

(deftest scale-pitch->diatonic-chords-test
  (is (= '({:root :C,
            :chord-type :maj,
            :chord-intervals #{0 7 4},
            :chord-pitches->readable-intervals {:C :1, :G :5, :E :M3},
            :chord-notes (60 64 67)}
           {:root :C,
            :chord-type :6*9,
            :chord-intervals #{0 7 4 9 14},
            :chord-pitches->readable-intervals {:C :1, :G :5, :E :M3, :A :6, :D :9},
            :chord-notes (60 64 67 69 74)}
           {:root :C,
            :chord-type :maj7,
            :chord-intervals #{0 7 4 11},
            :chord-pitches->readable-intervals {:C :1, :G :5, :E :M3, :B :M7},
            :chord-notes (60 64 67 71)}
           {:root :C,
            :chord-type :maj9,
            :chord-intervals #{0 7 4 11 14},
            :chord-pitches->readable-intervals {:C :1, :G :5, :E :M3, :B :M7, :D :9},
            :chord-notes (60 64 67 71 74)}
           {:root :C,
            :chord-type :maj11,
            :chord-intervals #{0 7 4 17 11 14},
            :chord-pitches->readable-intervals
            {:C :1, :G :5, :E :M3, :F :11, :B :M7, :D :9},
            :chord-notes (60 64 67 71 74 77)}
           {:root :C,
            :chord-type :sus2,
            :chord-intervals #{0 7 2},
            :chord-pitches->readable-intervals {:C :1, :G :5, :D :M2},
            :chord-notes (60 62 67)}
           {:root :C,
            :chord-type :6,
            :chord-intervals #{0 7 4 9},
            :chord-pitches->readable-intervals {:C :1, :G :5, :E :M3, :A :6},
            :chord-notes (60 64 67 69)}
           {:root :C,
            :chord-type :sus4,
            :chord-intervals #{0 7 5},
            :chord-pitches->readable-intervals {:C :1, :G :5, :F :4},
            :chord-notes (60 65 67)})
         (scale/scale-pitch->diatonic-chords (get scale/ALL-SCALES {:root :C :scale-type :major})
                                             :C))))

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
