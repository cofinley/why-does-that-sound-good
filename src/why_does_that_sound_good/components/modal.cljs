(ns why-does-that-sound-good.components.modal)

(defn modal [active? content & {:keys [header footer]}]
  [:div ; container
   {:class (str (if active? "flex" "hidden") " items-center flex-col justify-center overflow-hidden inset-0 fixed z-10")}
   [:div
    {:class "bg-neutral-900 opacity-80 inset-0 absolute"}] ; background
   [:div ; card
    {:class "flex flex-col rounded-lg dark:bg-neutral-700 max-h-[80vh] w-96 mx-auto overflow-hidden relative"}
    (when header
      [:div ; header
       {:class "flex justify-start p-6 relative border-b dark:border-neutral-500 rounded-t-md items-center"}
       header])
    [:div ; content
     {:class "p-6 overflow-auto"}
     content]
    (when footer
      [:div ; footer
       {:class "flex justify-end items-center relative p-6 border-t dark:border:neutral-500 rounded-b-md"}
       footer])]])
