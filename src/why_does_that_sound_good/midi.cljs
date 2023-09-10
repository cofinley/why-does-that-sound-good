(ns why-does-that-sound-good.midi)

(defn parse-midi-data [data]
  (let [status (first data)]
    (cond
      (<= 0x80 status 0x89)  {:command :note-off
                              :note (second data)}
      (<= 0x90 status 0x99) (if (zero? (aget data 2))
                              {:command :note-off
                               :note (second data)}
                              {:command :note-on
                               :note (second data)})
      (<= 0xB0 status 0xB9) {:command :control-change
                             :cc (second data)
                             :value (aget data 2)})))

(defn now [] (.. js/window -performance now))

(defn note-on
  ([out chan note vel] (note-on out chan note vel (now)))
  ([out chan note vel at]
   (.send out (clj->js [(+ 143 chan) note vel]) at)))

(defn note-off
  ([out chan note] (note-off out chan note (now)))
  ([out chan note at]
   (.send out (clj->js [(+ 127 chan) note 127]) at)))

(defn play-note
  [out note & {:keys [chan vel dur at now?]
               :or {chan 1 vel 60 dur 1000 at nil now? true}}]
  (when out
    (let [at (if now? (+ at (now)) at)]
      (note-on out chan note vel at)
      (note-off out chan note (+ at dur)))))

(defn play-chord [out notes & {:keys [broken? broken-delay broken-duration]
                               :or {broken? false broken-delay 150 broken-duration 1250}}]
  (if broken?
    (let [now (now)]
      (doseq [[note at duration] (map list
                                      notes
                                      (map #(+ now (* broken-delay %)) (range (count notes)))
                                      (map #(+ (* broken-delay %) broken-duration) (range (dec (count notes)) -1 -1)))]
        (play-note out note :dur duration :at at :now? false)))
    (doseq [note notes] (play-note out note))))

(defn play-scale [out notes & {:keys [broken-delay broken-duration]
                               :or {broken-delay 300 broken-duration 1000}}]
  (let [now (now)]
      ;; TODO: improve scheduling of individual notes, maybe with setTimeouts
      ;; otherwise, notes have a swing to them
      ;; https://web.dev/audio-scheduling/
    (doseq [[note at] (map list
                           notes
                           (map #(+ now (* broken-delay %)) (range (count notes))))]
      (play-note out note :dur broken-duration :at at :now? false))))
