(ns why-does-that-sound-good.pitch)

(def interval-name->semitone
  {:1 0
   :M2 2
   :m3 3
   :M3 4
   :4 5
   :d5 6
   :5 7
   :A5 8
   :m6 8
   :M6 9
   :d7 9
   :m7 10
   :M7 11
   :m9 13
   :M9 14
   :A9 15
   :m10 15
   :11 17
   :A11 18
   :m13 20
   :M13 21})

;; From tonal.js
;; https://github.com/tonaljs/tonal/blob/febd2404924bd8bfe9712aa407ee88943125b9d5/packages/chord-type/data.ts

(def BASE-CHORD
  (array-map
   ;; Major
   "M"           {:interval-names [:1 :M3 :5]                    :aliases ["^","","maj"]                                    :name "major"}
   "maj7"        {:interval-names [:1 :M3 :5 :M7]                :aliases ["Δ","ma7","M7","Maj7","^7"]                      :name "major seventh"}
   "maj9"        {:interval-names [:1 :M3 :5 :M7 :M9]            :aliases ["Δ9","^9"]                                       :name "major ninth"}
   "maj13"       {:interval-names [:1 :M3 :5 :M7 :M9 :M13]       :aliases ["Maj13","^13"]                                   :name "major thirteenth"}
   "6"           {:interval-names [:1 :M3 :5 :M6]                :aliases ["add6","add13","M6"]                             :name "sixth"}
   "6add9"       {:interval-names [:1 :M3 :5 :M6 :M9]            :aliases ["6/9","69","M69"]                                :name "sixth added ninth"}
   "M7b6"        {:interval-names [:1 :M3 :m6 :M7]               :aliases ["^7b6"]                                          :name "major seventh flat sixth"}
   "maj#4"       {:interval-names [:1 :M3 :5 :M7 :A11]           :aliases ["Δ#4","Δ#11","M7#11","^7#11","maj7#11"]          :name "major seventh sharp eleventh"}
   ;; Minor
   ;;; Normal
   "m"           {:interval-names [:1 :m3 :5]                    :aliases ["min","-"]                                       :name "minor"}
   "m7"          {:interval-names [:1 :m3 :5 :m7]                :aliases ["min7","mi7","-7"]                               :name "minor seventh"}
   "m/ma7"       {:interval-names [:1 :m3 :5 :M7]                :aliases ["m/maj7","mM7","mMaj7","m/M7","-Δ7","mΔ","-^7"]  :name "minor/major seventh"}
   "m6"          {:interval-names [:1 :m3 :5 :M6]                :aliases ["-6"]                                            :name "minor sixth"}
   "m9"          {:interval-names [:1 :m3 :5 :m7 :M9]            :aliases ["-9"]                                            :name "minor ninth"}
   "mM9"         {:interval-names [:1 :m3 :5 :M7 :M9]            :aliases ["mMaj9","-^9"]                                   :name "minor/major ninth"}
   "m11"         {:interval-names [:1 :m3 :5 :m7 :M9 :11]        :aliases ["-11"]                                           :name "minor eleventh"}
   "m13"         {:interval-names [:1 :m3 :5 :m7 :M9 :M13]       :aliases ["-13"]                                           :name "minor thirteenth"}
   ;;; Diminished
   "dim"         {:interval-names [:1 :m3 :d5]                   :aliases ["°","o"]                                         :name "diminished"}
   "dim7"        {:interval-names [:1 :m3 :d5 :d7]               :aliases ["°7","o7"]                                       :name "diminished seventh"}
   "m7b5"        {:interval-names [:1 :m3 :d5 :m7]               :aliases ["ø","-7b5","h7","h"]                             :name "half-diminished"}
   ;; Dominant/Seventh
   ;;; Normal
   "7"           {:interval-names [:1 :M3 :5 :m7]                :aliases ["dom"]                                           :name "dominant seventh"}
   "9"           {:interval-names [:1 :M3 :5 :m7 :M9]            :aliases []                                                :name "dominant ninth"}
   "13"          {:interval-names [:1 :M3 :5 :m7 :M9 :M13]       :aliases []                                                :name "dominant thirteenth"}
   "7#11"        {:interval-names [:1 :M3 :5 :m7 :A11]           :aliases ["7#4"]                                           :name "lydian dominant seventh"}
   ;;; Altered
   "7b9"         {:interval-names [:1 :M3 :5 :m7 :m9]            :aliases []                                                :name "dominant flat ninth"}
   "7#9"         {:interval-names [:1 :M3 :5 :m7 :A9]            :aliases []                                                :name "dominant sharp ninth"}
   "alt7"        {:interval-names [:1 :M3 :m7 :m9]               :aliases []                                                :name "altered"}
   ;;; Suspended
   "sus4"        {:interval-names [:1 :4 :5]                     :aliases ["sus"]                                           :name "suspended fourth"}
   "sus2"        {:interval-names [:1 :M2 :5]                    :aliases []                                                :name "suspended second"}
   "7sus4"       {:interval-names [:1 :4 :5 :m7]                 :aliases ["7sus"]                                          :name "suspended fourth seventh"}
   "11"          {:interval-names [:1 :5 :m7 :M9 :11]            :aliases []                                                :name "eleventh"}
   "b9sus"       {:interval-names [:1 :4 :5 :m7 :m9]             :aliases ["phryg","7b9sus","7b9sus4"]                      :name "suspended fourth flat ninth"}
   ;; Other
   "5"           {:interval-names [:1 :5]                        :aliases []                                                :name "fifth"}
   "aug"         {:interval-names [:1 :M3 :A5]                   :aliases ["+","+5","^#5"]                                  :name "augmented"}
   "m#5"         {:interval-names [:1 :m3 :A5]                   :aliases ["-#5","m+"]                                      :name "minor augmented"}
   "maj7#5"      {:interval-names [:1 :M3 :A5 :M7]               :aliases ["maj7+5","+maj7","^7#5"]                         :name "augmented seventh"}
   "maj9#11"     {:interval-names [:1 :M3 :5 :M7 :M9 :A11]       :aliases ["Δ9#11","^9#11"]                                 :name "major sharp eleventh (lydian)"}
   "sus24"       {:interval-names [:1 :M2 :4 :5]                 :aliases ["sus4add9"]                                      :name ""}
   "maj9#5"      {:interval-names [:1 :M3 :A5 :M7 :M9]           :aliases ["Maj9#5"]                                        :name ""}
   "7#5"         {:interval-names [:1 :M3 :A5 :m7]               :aliases ["+7","7+","7aug","aug7"]                         :name ""}
   "7#5#9"       {:interval-names [:1 :M3 :A5 :m7 :A9]           :aliases ["7#9#5","7alt"]                                  :name ""}
   "9#5"         {:interval-names [:1 :M3 :A5 :m7 :M9]           :aliases ["9+"]                                            :name ""}
   "9#5#11"      {:interval-names [:1 :M3 :A5 :m7 :M9 :A11]      :aliases []                                                :name ""}
   "7#5b9"       {:interval-names [:1 :M3 :A5 :m7 :m9]           :aliases ["7b9#5"]                                         :name ""}
   "7#5b9#11"    {:interval-names [:1 :M3 :A5 :m7 :m9 :A11]      :aliases []                                                :name ""}
   "+add#9"      {:interval-names [:1 :M3 :A5 :A9]               :aliases []                                                :name ""}
   "M#5add9"     {:interval-names [:1 :M3 :A5 :M9]               :aliases ["+add9"]                                         :name ""}
   "M6#11"       {:interval-names [:1 :M3 :5 :M6 :A11]           :aliases ["M6b5","6#11","6b5"]                             :name ""}
   "M7add13"     {:interval-names [:1 :M3 :5 :M6 :M7 :M9]        :aliases []                                                :name ""}
   "69#11"       {:interval-names [:1 :M3 :5 :M6 :M9 :A11]       :aliases []                                                :name ""}
   "m69"         {:interval-names [:1 :m3 :5 :M6 :M9]            :aliases ["-69"]                                           :name ""}
   "7b6"         {:interval-names [:1 :M3 :5 :m6 :m7]            :aliases []                                                :name ""}
   "maj7#9#11"   {:interval-names [:1 :M3 :5 :M7 :A9 :A11]       :aliases []                                                :name ""}
   "M13#11"      {:interval-names [:1 :M3 :5 :M7 :M9 :A11 :M13]  :aliases ["maj13#11","M13+4","M13#4"]                      :name ""}
   "M7b9"        {:interval-names [:1 :M3 :5 :M7 :m9]            :aliases []                                                :name ""}
   "7#11b13"     {:interval-names [:1 :M3 :5 :m7 :A11 :m13]      :aliases ["7b5b13"]                                        :name ""}
   "7add6"       {:interval-names [:1 :M3 :5 :m7 :M13]           :aliases ["67","7add13"]                                   :name ""}
   "7#9#11"      {:interval-names [:1 :M3 :5 :m7 :A9 :A11]       :aliases ["7b5#9","7#9b5"]                                 :name ""}
   "13#9#11"     {:interval-names [:1 :M3 :5 :m7 :A9 :A11 :M13]  :aliases []                                                :name ""}
   "7#9#11b13"   {:interval-names [:1 :M3 :5 :m7 :A9 :A11 :m13]  :aliases []                                                :name ""}
   "13#9"        {:interval-names [:1 :M3 :5 :m7 :A9 :M13]       :aliases []                                                :name ""}
   "7#9b13"      {:interval-names [:1 :M3 :5 :m7 :A9 :m13]       :aliases []                                                :name ""}
   "9#11"        {:interval-names [:1 :M3 :5 :m7 :M9 :A11]       :aliases ["9+4","9#4"]                                     :name ""}
   "13#11"       {:interval-names [:1 :M3 :5 :m7 :M9 :A11 :M13]  :aliases ["13+4","13#4"]                                   :name ""}
   "9#11b13"     {:interval-names [:1 :M3 :5 :m7 :M9 :A11 :m13]  :aliases ["9b5b13"]                                        :name ""}
   "7b9#11"      {:interval-names [:1 :M3 :5 :m7 :m9 :A11]       :aliases ["7b5b9","7b9b5"]                                 :name ""}
   "13b9#11"     {:interval-names [:1 :M3 :5 :m7 :m9 :A11 :M13]  :aliases []                                                :name ""}
   "7b9b13#11"   {:interval-names [:1 :M3 :5 :m7 :m9 :A11 :m13]  :aliases ["7b9#11b13","7b5b9b13"]                          :name ""}
   "13b9"        {:interval-names [:1 :M3 :5 :m7 :m9 :M13]       :aliases []                                                :name ""}
   "7b9b13"      {:interval-names [:1 :M3 :5 :m7 :m9 :m13]       :aliases []                                                :name ""}
   "7b9#9"       {:interval-names [:1 :M3 :5 :m7 :m9 :A9]        :aliases []                                                :name ""}
   "Madd9"       {:interval-names [:1 :M3 :5 :M9]                :aliases ["2","add9","add2"]                               :name ""}
   "Maddb9"      {:interval-names [:1 :M3 :5 :m9]                :aliases []                                                :name ""}
   "Mb5"         {:interval-names [:1 :M3 :d5]                   :aliases []                                                :name ""}
   "13b5"        {:interval-names [:1 :M3 :d5 :M6 :m7 :M9]       :aliases []                                                :name ""}
   "M7b5"        {:interval-names [:1 :M3 :d5 :M7]               :aliases []                                                :name ""}
   "M9b5"        {:interval-names [:1 :M3 :d5 :M7 :M9]           :aliases []                                                :name ""}
   "7b5"         {:interval-names [:1 :M3 :d5 :m7]               :aliases []                                                :name ""}
   "9b5"         {:interval-names [:1 :M3 :d5 :m7 :M9]           :aliases []                                                :name ""}
   "7no5"        {:interval-names [:1 :M3 :m7]                   :aliases []                                                :name ""}
   "7b13"        {:interval-names [:1 :M3 :m7 :m13]              :aliases []                                                :name ""}
   "9no5"        {:interval-names [:1 :M3 :m7 :M9]               :aliases []                                                :name ""}
   "13no5"       {:interval-names [:1 :M3 :m7 :M9 :M13]          :aliases []                                                :name ""}
   "9b13"        {:interval-names [:1 :M3 :m7 :M9 :m13]          :aliases []                                                :name ""}
   "madd4"       {:interval-names [:1 :m3 :4 :5]                 :aliases []                                                :name ""}
   "mMaj7b6"     {:interval-names [:1 :m3 :5 :m6 :M7]            :aliases []                                                :name ""}
   "mMaj9b6"     {:interval-names [:1 :m3 :5 :m6 :M7 :M9]        :aliases []                                                :name ""}
   "m7add11"     {:interval-names [:1 :m3 :5 :m7 :11]            :aliases ["m7add4"]                                        :name ""}
   "madd9"       {:interval-names [:1 :m3 :5 :M9]                :aliases []                                                :name ""}
   "dim7M7"      {:interval-names [:1 :m3 :d5 :M6 :M7]           :aliases ["o7M7"]                                          :name ""}
   "dimM7"       {:interval-names [:1 :m3 :d5 :M7]               :aliases ["oM7"]                                           :name ""}
   "mb6M7"       {:interval-names [:1 :m3 :m6 :M7]               :aliases []                                                :name ""}
   "m7#5"        {:interval-names [:1 :m3 :m6 :m7]               :aliases []                                                :name ""}
   "m9#5"        {:interval-names [:1 :m3 :m6 :m7 :M9]           :aliases []                                                :name ""}
   "m11A"        {:interval-names [:1 :m3 :A5 :m7 :M9 :11]       :aliases []                                                :name ""}
   "mb6b9"       {:interval-names [:1 :m3 :m6 :m9]               :aliases []                                                :name ""}
   "m9b5"        {:interval-names [:1 :M2 :m3 :d5 :m7]           :aliases []                                                :name ""}
   "M7#5sus4"    {:interval-names [:1 :4 :A5 :M7]                :aliases []                                                :name ""}
   "M9#5sus4"    {:interval-names [:1 :4 :A5 :M7 :M9]            :aliases []                                                :name ""}
   "7#5sus4"     {:interval-names [:1 :4 :A5 :m7]                :aliases []                                                :name ""}
   "M7sus4"      {:interval-names [:1 :4 :5 :M7]                 :aliases []                                                :name ""}
   "M9sus4"      {:interval-names [:1 :4 :5 :M7 :M9]             :aliases []                                                :name ""}
   "9sus4"       {:interval-names [:1 :4 :5 :m7 :M9]             :aliases ["9sus"]                                          :name ""}
   "13sus4"      {:interval-names [:1 :4 :5 :m7 :M9 :M13]        :aliases ["13sus"]                                         :name ""}
   "7sus4b9b13"  {:interval-names [:1 :4 :5 :m7 :m9 :m13]        :aliases ["7b9b13sus4"]                                    :name ""}
   "4"           {:interval-names [:1 :4 :m7 :m10]               :aliases ["quartal"]                                       :name ""}
   "11b9"        {:interval-names [:1 :5 :m7 :m9 :11]            :aliases []                                                :name ""}))

;; Add semitone intervals for fuzzy search/distance
(def CHORD (reduce (fn [m [chord-type chord-details]]
                     (assoc-in m [chord-type :intervals] (mapv #(interval-name->semitone %) (:interval-names chord-details))))
                   BASE-CHORD
                   BASE-CHORD))

;; Keep chord order above for scale diatonic chords to go from less to more complex
(def CHORD-ORDER (into {} (map (fn [[i k]] [k i]) (map-indexed vector (keys BASE-CHORD)))))


;; From overtone/music/pitch
(def SCALE
  (let [ionian-sequence     [2 2 1 2 2 2 1]
        hex-sequence        [2 2 1 2 2 3]
        pentatonic-sequence [3 2 2 3 2]
        rotate (fn [scale-sequence offset]
                 (take (count scale-sequence)
                       (drop offset (cycle scale-sequence))))]
    {:major              ionian-sequence
     :dorian             (rotate ionian-sequence 1)
     :phrygian           (rotate ionian-sequence 2)
     :lydian             (rotate ionian-sequence 3)
     :mixolydian         (rotate ionian-sequence 4)
     :aeolian            (rotate ionian-sequence 5)
     :minor              (rotate ionian-sequence 5)
     :locrian            (rotate ionian-sequence 6)
     :hex-major6         (rotate hex-sequence 0)
     :hex-dorian         (rotate hex-sequence 1)
     :hex-phrygian       (rotate hex-sequence 2)
     :hex-major7         (rotate hex-sequence 3)
     :hex-sus            (rotate hex-sequence 4)
     :hex-aeolian        (rotate hex-sequence 5)
     :minor-pentatonic   (rotate pentatonic-sequence 0)
     :yu                 (rotate pentatonic-sequence 0)
     :major-pentatonic   (rotate pentatonic-sequence 1)
     :gong               (rotate pentatonic-sequence 1)
     :egyptian           (rotate pentatonic-sequence 2)
     :shang              (rotate pentatonic-sequence 2)
     :jiao               (rotate pentatonic-sequence 3)
     :pentatonic         (rotate pentatonic-sequence 4) ;; historical match
     :zhi                (rotate pentatonic-sequence 4)
     :ritusen            (rotate pentatonic-sequence 4)
     :whole-tone         [2 2 2 2 2 2]
     :chromatic          [1 1 1 1 1 1 1 1 1 1 1 1]
     :harmonic-minor     [2 1 2 2 1 3 1]
     :melodic-minor-asc  [2 1 2 2 2 2 1]
     :hungarian-minor    [2 1 3 1 1 3 1]
     :octatonic          [2 1 2 1 2 1 2 1]
     :messiaen1          [2 2 2 2 2 2]
     :messiaen2          [1 2 1 2 1 2 1 2]
     :messiaen3          [2 1 1 2 1 1 2 1 1]
     :messiaen4          [1 1 3 1 1 1 3 1]
     :messiaen5          [1 4 1 1 4 1]
     :messiaen6          [2 2 1 1 2 2 1 1]
     :messiaen7          [1 1 1 2 1 1 1 1 2 1]
     :super-locrian      [1 2 1 2 2 2 2]
     :hirajoshi          [2 1 4 1 4]
     :kumoi              [2 1 4 2 3]
     :neapolitan-major   [1 2 2 2 2 2 1]
     :bartok             [2 2 1 2 1 2 2]
     :bhairav            [1 3 1 2 1 3 1]
     :locrian-major      [2 2 1 1 2 2 2]
     :ahirbhairav        [1 3 1 2 2 1 2]
     :enigmatic          [1 3 2 2 2 1 1]
     :neapolitan-minor   [1 2 2 2 1 3 1]
     :pelog              [1 2 4 1 4]
     :augmented2         [1 3 1 3 1 3]
     :scriabin           [1 3 3 2 3]
     :harmonic-major     [2 2 1 2 1 3 1]
     :melodic-minor-desc [2 1 2 2 1 2 2]
     :romanian-minor     [2 1 3 1 2 1 2]
     :hindu              [2 2 1 2 1 2 2]
     :iwato              [1 4 1 4 2]
     :melodic-minor      [2 1 2 2 2 2 1]
     :marva              [1 3 2 1 2 2 1]
     :melodic-major      [2 2 1 2 1 2 2]
     :indian             [4 1 2 3 2]
     :spanish            [1 3 1 2 1 2 2]
     :prometheus         [2 2 2 5 1]
     :diminished         [1 2 1 2 1 2 1 2]  ; half-whole diminished
     :diminished2        [2 1 2 1 2 1 2 1]  ; whole-half diminished (mode)
     :todi               [1 2 3 1 1 3 1]
     :leading-whole      [2 2 2 2 2 1 1]
     :augmented          [3 1 3 1 3 1]
     :purvi              [1 3 2 1 1 3 1]
     :chinese            [4 2 1 4 1]
     :lydian-minor       [2 2 2 1 1 2 2]
     :minor-blues        [3 2 1 1 3 2]
     :major-blues        [2 1 1 3 2 3]}))

(def ENABLED-SCALES
  #{:major
    :minor
    :harmonic-minor
    :harmonic-major
    :melodic-minor
    :melodic-major
    :minor-pentatonic
    :major-pentatonic
    :diminished
    :whole-tone
    :minor-blues
    :major-blues})

(def NOTES {:C  0  :c  0  :b# 0  :B# 0
            :C# 1  :c# 1  :Db 1  :db 1  :DB 1  :dB 1
            :D  2  :d  2
            :D# 3  :d# 3  :Eb 3  :eb 3  :EB 3  :eB 3
            :E  4  :e  4
            :E# 5  :e# 5  :F  5  :f  5
            :F# 6  :f# 6  :Gb 6  :gb 6  :GB 6  :gB 6
            :G  7  :g  7
            :G# 8  :g# 8  :Ab 8  :ab 8  :AB 8  :aB 8
            :A  9  :a  9
            :A# 10 :a# 10 :Bb 10 :bb 10 :BB 10 :bB 10
            :B  11 :b  11 :Cb 11 :cb 11 :CB 11 :cB 11})

(def REVERSE-NOTES
  {0 :C
   1 :C#
   2 :D
   3 :Eb
   4 :E
   5 :F
   6 :F#
   7 :G
   8 :Ab
   9 :A
   10 :Bb
   11 :B})

(defn canonical-pitch-class-name
  "Returns the canonical version of the specified pitch class pc."
  [pc]
  (let [pc (keyword (name pc))]
    (REVERSE-NOTES (NOTES pc))))

(def MIDI-NOTE-RE-STR "([a-gA-G][#bB]?)([-0-9]+)")
(def MIDI-NOTE-RE (re-pattern MIDI-NOTE-RE-STR))
;; (def ONLY-MIDI-NOTE-RE (re-pattern (str "\\A" MIDI-NOTE-RE-STR "\\Z")))
(def ONLY-MIDI-NOTE-RE (re-pattern (str "^" MIDI-NOTE-RE-STR "$")))

(defn- midi-string-matcher
  "Determines whether a midi keyword is valid or not. If valid,
  returns a regexp match object"
  [mk]
  (re-find ONLY-MIDI-NOTE-RE (name mk)))

(defn- validate-midi-string!
  "Throws a friendly exception if midi-keyword mk is not
  valid. Returns matches if valid."
  [mk]
  (let [matches (midi-string-matcher mk)]
    (when-not matches
      (let [error-message (str "Invalid midi-string. " mk " does not appear to be in MIDI format i.e. C#4")]
        (throw #?(:clj (IllegalArgumentException. error-message)
                  :cljs (js/Error error-message)))))

    (let [[match pitch-class octave-str] matches
          octave (first octave-str)]
      (when (< (int octave) -1)
        (let [error-message (str "Invalid midi-string: " mk ". Octave is out of range. Lowest octave value is -1")]
          (throw #?(:clj (IllegalArgumentException. error-message)
                    :cljs (js/Error error-message))))))
    matches))

(defn octave-note
  "Convert an octave and interval to a midi note."
  [octave interval]
  (+ (* octave 12) interval 12))

(defn note-info
  "Takes a string representing a midi note such as C4 and returns a map
  of note info"
  [midi-string]
  (let [[match pitch-class octave] (validate-midi-string! midi-string)
        pitch-class                (canonical-pitch-class-name pitch-class)
        octave                     #?(:clj (Integer/parseInt octave)
                                      :cljs (js/parseInt octave))
        interval                   (NOTES (keyword pitch-class))]
    {:match       match
     :pitch-class pitch-class
     :octave      #?(:clj (Integer. octave) :cljs octave)
     :interval    interval
     :midi-note   (octave-note octave interval)}))

(defn note
  "Resolves note to MIDI number format. Resolves upper and lower-case
  keywords and strings in MIDI note format. If given an integer or
  nil, returns them unmodified. All other inputs will raise an
  exception.

  Usage examples:

  (note \"C4\")  ;=> 60
  (note \"C#4\") ;=> 61
  (note \"eb2\") ;=> 39
  (note :F#7)    ;=> 102
  (note :db5)    ;=> 73
  (note 60)      ;=> 60
  (note nil)     ;=> nil"
  [n]
  (cond
    (nil? n) nil
    (integer? n) (if (>= n 0)
                   n
                   (let [error-message (str "Unable to resolve note: " n ". Value is out of range. Lowest value is 0")]
                     (throw #?(:clj (IllegalArgumentException. error-message)
                               :cljs (js/Error error-message)))))
    (keyword? n) (note (name n))
    (string? n) (:midi-note (note-info n))
    :else (let [error-message (str "Unable to resolve note: " n ". Wasn't a recognised format (either an integer, keyword, string or nil)")]
            (throw #?(:clj (IllegalArgumentException. error-message)
                      :cljs (js/Error error-message))))))

(defn find-pitch-class-name
  "Given a midi number representing a note, returns the name of the note
  independent of octave.

  (find-pitch-class-name 62) ;=> :D
  (find-pitch-class-name 74) ;=> :D
  (find-pitch-class-name 75) ;=> :Eb"
  [note]
  (REVERSE-NOTES (mod note 12)))

(defn find-note-name
  [note]
  "Given a midi number representing a note, returns a keyword
  representing the note including octave number. Reverse of the fn note.

  (find-note-name 45) ;=> A2
  (find-note-name 57) ;=> A3
  (find-note-name 58) ;=> Bb3"
  (when note (let [octave (dec (int (/ note 12)))]
               (keyword (str (name (find-pitch-class-name note)) octave)))))

(defn note->octave [note]
  (dec (int (/ note 12))))

(defn interval->pitch
  [key interval]
  (-> key
      NOTES
      (+ interval)
      (mod 12)
      REVERSE-NOTES))

(defn construct-note
  "Pitch + octave -> note"
  [pitch octave]
  (note (keyword (str (name pitch) octave))))

