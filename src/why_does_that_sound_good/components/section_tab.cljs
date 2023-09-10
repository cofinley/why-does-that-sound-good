(ns why-does-that-sound-good.components.section-tab
  (:require
   [re-frame.core :as re-frame]
   [why-does-that-sound-good.events :as events]
   [why-does-that-sound-good.utils :refer [in?]]))

(defn section-tab [section & {:keys [selected? editing-name?]
                              :or {selected? false editing-name? false}}]
  ;; TODO: bring editing-name? into this component with reagent atom
  (let [event (if section [::events/on-section-select (:id section)] [::events/on-section-add])]
    [:div
     {:class (str "flex gap-x-4 justify-between pb-1 pt-1 px-2 rounded-t-lg " (if selected? "dark:bg-neutral-700" "dark:bg-transparent"))}
     (if editing-name?
       [:input
        {:type "text"
         :class "bg-transparent dark:text-neutral-100 max-w-fit"
         :auto-focus true
         :size (max 10 (- (count (:name section)) 5))
         :on-change #(re-frame/dispatch [::events/on-section-name-change (:id section) (-> % .-target .-value)])
         :on-key-down #(when (in? ["Enter" "Escape"] (.-key %)) (.blur (.-target %)))
         :on-blur #(re-frame/dispatch [::events/on-section-name-edit-toggle nil])
         :value (:name section)}]
       [:button
        {:on-click #(re-frame/dispatch event)
         :on-double-click #(re-frame/dispatch [::events/on-section-name-edit-toggle section])}
        (if section (:name section) "+ Add Section")])
     (when section
       [:button
        {:on-click #(when (js/confirm (str "Are you sure you want to delete section \"" (:name section) "\"?"))
                      (re-frame/dispatch [::events/on-section-delete section]))}
        "×"])]))
