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

(defn- remove-hidden-fields [fields]
  (reduce-kv
    (fn [m k v]
      (if-not (contains? v :hidden)
        (assoc m k v)
        m))
    {}
    fields))

(defn form [{:keys [fields validators on-submit]}]
  (let [data-init (->> fields keys (reduce
                                     #(assoc %1 %2 (get-in fields [%2 :initial-value] ""))
                                     {}))
        errors-init (->> fields keys (reduce #(assoc %1 %2 nil) {}))
        data (atom data-init)
        errors (atom errors-init)
        fields-init (->> fields
                         (reduce-kv
                           (fn [m k v]
                             (assoc m k (-> v
                                            (#(if (contains? % :container)
                                                (assoc % :container ((:container %) data errors k)) %))
                                            (#(if (contains? % :events)
                                                (assoc % :events
                                                         (into {} (for [[evt-name evt-fn] (:events %)]
                                                                    {evt-name (evt-fn data errors k)}))) %)))))
                           {}))
        fields (atom fields-init)
        has-errors? (->> @errors vals (apply concat) (every? nil?) not)
        pristine? (->> @fields vals (map :touched?) (every? nil?))
        foreign-data (atom {})]
    {:will-mount
     (fn [{[r _ _ current-values] :rum/args
           comp                   :rum/react-component
           :as                    state}]
       (when current-values
         (do
           (reset! data (into {} (for [[k v] @data] {k (or (get current-values k)
                                                           v)})))
           (reset! foreign-data current-values)))
       (add-watch data ::form-data (fn [_ _ old-state next-state]
                                     (when-not (= old-state next-state)
                                       (rum/request-render comp))))
       (add-watch errors ::form-errors (fn [_ _ old-state next-state]
                                         (when-not (= old-state next-state)
                                           (rum/request-render comp))))
       state)
     :will-update
     (fn [{[_ _ _ current-values] :rum/args
           :as                    state}]
       (when (and current-values (not= current-values @foreign-data))
         (do
           (reset! data (into {}
                              (for [[k v] @data] {k (or (get current-values k) v)})))
           (reset! foreign-data current-values)))
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
               (assoc state ::form {:fields      (remove-hidden-fields @fields)
                                    :validators  validators
                                    :validate    #(swap! errors assoc %1 (check-errors (get validators %1) %2))
                                    :on-change   #(swap! data assoc %1 %2)
                                    :on-submit   #(on-submit r @data @errors validators %)
                                    :on-focus    #(swap! fields assoc-in [% :touched?] true)
                                    :data        @data
                                    :errors      @errors
                                    :has-errors? has-errors?
                                    :pristine?   pristine?})]
           (render-fn state))))}))
