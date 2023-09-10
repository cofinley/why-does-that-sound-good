(ns why-does-that-sound-good.components.button
  (:require [clojure.string :as str]))

(defn button [{:keys [on-click class title]
               :or {title ""}} & children]
  [:button
   {:class (str/join " " ["px-2 py-1 text-sm border hover:dark:bg-neutral-100 hover:dark:text-neutral-900 rounded group" class])
    :on-click on-click
    :title title}
   children])
