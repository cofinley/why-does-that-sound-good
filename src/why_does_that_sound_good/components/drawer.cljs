(ns why-does-that-sound-good.components.drawer
  (:require [clojure.string :as str]))

(defn drawer [{:keys [show? side on-close title]} & children]
  [:div
   {:class (str/join " " [(if (= side :left) "left-0" "right-0")
                          (when-not show? (if (= side :left) "-translate-x-full" "translate-x-full"))
                          "fixed top-0 z-40 h-screen p-4 overflow-y-auto transition-transform bg-white min-w-80 dark:bg-neutral-800 shadow-xl dark:shadow-neutral-900"])}
   [:h2
    {:class "mb-4 text-xl font-semibold text-gray-500 dark:text-gray-300"}
    title]
   [:button
    {:type "button"
     :on-click on-close
     :class "text-gray-400 bg-transparent hover:bg-gray-200 hover:text-neutral-900 rounded-lg text-sm w-8 h-8 absolute top-2.5 right-2.5 inline-flex items-center justify-center dark:hover:bg-neutral-600 dark:hover:text-white"}
    [:svg
     {:class "w-3 h-3" :fill "none" :view-box "0 0 14 14"}
     [:path {:stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round" :stroke-width "2" :d "m1 1 6 6m0 0 6 6M7 7l6-6M7 7l-6 6"}]]
    [:span
     {:class "sr-only"} "Close menu"]]
   children])
