(ns conduit.mixins
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(defn dispatch-on-mount [events-fn]
  {:did-mount
   (fn [{[r] :rum/args :as state}]
     (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
       (apply citrus/dispatch! (into [r ctrl] event-vector)))
     state)
   :did-remount
   (fn [old {[r] :rum/args :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
         (apply citrus/dispatch! (into [r ctrl] event-vector))))
     state)})


(defn- check-errors [validators value]
  (->> validators
       (filter (fn [[validator]] (-> value validator not)))
       (map second)))

(defn form [{:keys [fields validators on-submit]}]
  (let [data-init (->> fields keys (reduce #(assoc %1 %2 "") {}))
        errors-init (->> fields keys (reduce #(assoc %1 %2 nil) {}))
        fields-init fields
        data (atom data-init)
        errors (atom errors-init)
        fields (atom fields-init)]
    {:will-mount
     (fn [{[r _ _ current-values] :rum/args
           comp :rum/react-component
           :as state}]
       (when current-values (reset! data (into {} (for [[k v] @data] {k (get current-values k)}))))
       (add-watch data ::form-data (fn [_ _ old-state next-state]
                                     (when-not (= old-state next-state)
                                       (rum/request-render comp))))
       (add-watch errors ::form-errors (fn [_ _ old-state next-state]
                                         (when-not (= old-state next-state)
                                           (rum/request-render comp))))
       state)
     :will-unmount
     (fn [state]
       (remove-watch data ::form-data)
       (remove-watch errors ::form-errors)
       (reset! data data-init)
       (reset! errors errors-init)
       (reset! fields fields-init)
       (assoc state ::form {}))
     :wrap-render
     (fn [render-fn]
       (fn [{[r] :rum/args :as state}]
         (let [state
               (assoc state ::form {:fields @fields
                                    :validators validators
                                    :validate #(swap! errors assoc %1 (check-errors (get validators %1) %2))
                                    :on-change #(swap! data assoc %1 %2)
                                    :on-submit #(on-submit r @data @errors validators)
                                    :on-focus #(swap! fields assoc-in [% :touched?] true)
                                    :data @data
                                    :errors @errors})]
           (render-fn state))))}))
