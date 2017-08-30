(ns conduit.mixins
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(defn dispatch-on-mount [events]
  {:did-mount
   (fn [{[r _ params] :rum/args
         :as state}]
     (doseq [[ctrl event] events]
       (citrus/dispatch! r ctrl event params))
     state)
   :did-remount
   (fn [old
        {[r _ params] :rum/args
         :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event] events]
         (citrus/dispatch! r ctrl event params)))
     state)})

(defn form-state [{:keys [fields validators submit-handler]}]
  {:will-mount
   (fn [{[r _ params] :rum/args
         :as state}]
     (let [initial-form-state {:data (reduce #(assoc %1 %2 "") {} (map #(:key %) fields))
                               :errors (reduce #(assoc %1 %2 nil) {} (map #(:key %) fields))}
           form-state (atom initial-form-state)
           form-data (rum/cursor-in form-state [:data])
           form-errors (rum/cursor-in form-state [:errors])]
       (assoc state
              :form-fields fields
              :form-validators validators
              :form-submit-handler (submit-handler r form-data form-errors validators)
              :form-state form-state
              :form-data form-data
              :form-errors form-errors
              :initial-form-state initial-form-state)))
   :will-unmount
   (fn [{form-state :form-state initial-form-state :initial-form-state :as state}]
     (reset! form-state initial-form-state) state)})
