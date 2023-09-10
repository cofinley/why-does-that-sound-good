(ns why-does-that-sound-good.pitch)

;; From overtone/music/pitch

(def CHORD
  {:maj         #{0 4 7}
   :min         #{0 3 7}
   :maj7        #{0 4 7 11}
   :dom7        #{0 4 7 10}
   :min7        #{0 3 7 10}
   :aug         #{0 4 8}
   :dim         #{0 3 6}
   :dim7        #{0 3 6 9}
   :+5          #{0 4 8}
   :m+5         #{0 3 8}
   :sus2        #{0 2 7}
   :sus4        #{0 5 7}
   :6           #{0 4 7 9}
   :m6          #{0 3 7 9}
   :7sus2       #{0 2 7 10}
   :7sus4       #{0 5 7 10}
   :7-5         #{0 4 6 10}
   :m7-5        #{0 3 6 10}
   :7+5         #{0 4 8 10}
   :m7+5        #{0 3 8 10}
   :9           #{0 4 7 10 14}
   :m9          #{0 3 7 10 14}
   :m7+9        #{0 3 7 10 14}
   :maj9        #{0 4 7 11 14}
   :9sus4       #{0 5 7 10 14}
   :6*9         #{0 4 7 9 14}
   :m6*9        #{0 3 9 7 14}
   :7-9         #{0 4 7 10 13}
   :m7-9        #{0 3 7 10 13}
   :7-10        #{0 4 7 10 15}
   :9+5         #{0 10 13}
   :m9+5        #{0 10 14}
   :7+5-9       #{0 4 8 10 13}
   :m7+5-9      #{0 3 8 10 13}
   :11          #{0 4 7 10 14 17}
   :m11         #{0 3 7 10 14 17}
   :maj11       #{0 4 7 11 14 17}
   :11+         #{0 4 7 10 14 18}
   :m11+        #{0 3 7 10 14 18}
   :13          #{0 4 7 10 14 17 21}
   :m13         #{0 3 7 10 14 17 21}})

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

