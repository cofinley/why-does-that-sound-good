(ns why-does-that-sound-good.components.similarity
  (:require
   [clojure.string :as str]))

(defn similarity-badge [s]
  [:span
   {:title "Similarity or overlap"
    :class (str/join " "
                     ["text-xs py-0.5 px-1.5 rounded font-semibold dark:text-white"
                      (str "dark:bg-"
                           (cond
                             (>= s 0.9) "green"
                             (>= s 0.8) "lime"
                             (>= s 0.7) "yellow"
                             (>= s 0.6) "amber"
                             (>= s 0.5) "orange"
                             (>= s 0.2) "red"
                             :else "neutral") "-600")])}
   (str (Math/round (* 100 s)) "%")])
