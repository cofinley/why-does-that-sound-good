(ns why-does-that-sound-good.algo.scale-test
  (:require
   [clojure.test :refer [deftest testing is are]]
   [why-does-that-sound-good.algo.scale :as scale]))

(deftest steps->intervals-test
  (are [input-steps expected-intervals] (= expected-intervals (scale/steps->intervals input-steps))
    () '(0)
    '(1 2 3) '(0 1 3 6)
    '(2 2 1 2 2 2 1) '(0 2 4 5 7 9 11 12)))

(deftest scale->pitches-test
  (are [root-pitch scale-type expected-pitches] (= expected-pitches (scale/scale->pitches root-pitch scale-type))
    :C  :major '(:C :D :E :F :G :A :B)
    :C  :minor '(:C :D :Eb :F :G :Ab :Bb)
    :C# :major '(:C# :Eb :F :F# :Ab :Bb :C)
    ;; TODO: convert accidentals accordingly
    :Db :major '(:C# :Eb :F :F# :Ab :Bb :C)))

(deftest scale-pitches->intervals-test
  ;; Intervals all the way to up a 13th
  (is (= #{0 2 4 5 7 9 11 12 14 16 17 19 21} (scale/scale-pitches->intervals #{:C :D :E :F :G :A :B} :C))))

(deftest pitches->scales-test
  (testing "find-closest? true"
    (is (= '({:root :C
              :scale-type :major
              :scale-pitches (:C :D :E :F :G :A :B)
              :similarity 1.0}
             {:root :A
              :scale-type :minor
              :scale-pitches (:A :B :C :D :E :F :G)
              :similarity 1.0})
           (scale/pitches->scales #{:C :D :E :F :G :A :B} :find-closest? true))))
  (testing "min-similarity"
    (is (= '({:root :C
              :scale-type :major
              :scale-pitches (:C :D :E :F :G :A :B)
              :similarity 0.9375}
             {:root :A
              :scale-type :melodic-major
              :scale-pitches (:A :B :C# :D :E :F :G)
              :similarity 0.9375}
             {:root :D
              :scale-type :melodic-minor
              :scale-pitches (:D :E :F :G :A :B :C#)
              :similarity 0.9375}
             {:root :A
              :scale-type :minor
              :scale-pitches (:A :B :C :D :E :F :G)
              :similarity 0.9375})
           (scale/pitches->scales #{:C :C# :D :E :F :G :A :B} :min-scale-similarity 0.90)))))

(deftest scale-pitch->diatonic-chords-test
  (is (= '({:root :C
            :chord-type "M"
            :chord-pitches->readable-intervals {:C :1 :E :M3 :G :5}
            :chord-notes (60 64 67)}
           {:root :C
            :chord-type "maj7"
            :chord-pitches->readable-intervals {:C :1 :E :M3 :G :5 :B :M7}
            :chord-notes (60 64 67 71)}
           {:root :C
            :chord-type "maj9"
            :chord-pitches->readable-intervals
            {:C :1 :E :M3 :G :5 :B :M7 :D :M9}
            :chord-notes (60 64 67 71 74)}
           {:root :C
            :chord-type "maj13"
            :chord-pitches->readable-intervals
            {:C :1 :E :M3 :G :5 :B :M7 :D :M9 :A :M13}
            :chord-notes (60 64 67 71 74 81)}
           {:root :C
            :chord-type "6"
            :chord-pitches->readable-intervals {:C :1 :E :M3 :G :5 :A :M6}
            :chord-notes (60 64 67 69)}
           {:root :C
            :chord-type "6add9"
            :chord-pitches->readable-intervals
            {:C :1 :E :M3 :G :5 :A :M6 :D :M9}
            :chord-notes (60 64 67 69 74)}
           {:root :C
            :chord-type "sus4"
            :chord-pitches->readable-intervals {:C :1 :F :4 :G :5}
            :chord-notes (60 65 67)}
           {:root :C
            :chord-type "sus2"
            :chord-pitches->readable-intervals {:C :1 :D :M2 :G :5}
            :chord-notes (60 62 67)}
           {:root :C
            :chord-type "5"
            :chord-pitches->readable-intervals {:C :1 :G :5}
            :chord-notes (60 67)}
           {:root :C
            :chord-type "sus24"
            :chord-pitches->readable-intervals {:C :1 :D :M2 :F :4 :G :5}
            :chord-notes (60 62 65 67)}
           {:root :C
            :chord-type "M7add13"
            :chord-pitches->readable-intervals
            {:C :1 :E :M3 :G :5 :A :M6 :B :M7 :D :M9}
            :chord-notes (60 64 67 69 71 74)}
           {:root :C
            :chord-type "Madd9"
            :chord-pitches->readable-intervals {:C :1 :E :M3 :G :5 :D :M9}
            :chord-notes (60 64 67 74)}
           {:root :C
            :chord-type "M7sus4"
            :chord-pitches->readable-intervals {:C :1 :F :4 :G :5 :B :M7}
            :chord-notes (60 65 67 71)}
           {:root :C
            :chord-type "M9sus4"
            :chord-pitches->readable-intervals
            {:C :1 :F :4 :G :5 :B :M7 :D :M9}
            :chord-notes (60 65 67 71 74)})
         (scale/scale-pitch->diatonic-chords (get scale/ALL-SCALES {:root :C :scale-type :major}) :C))))
