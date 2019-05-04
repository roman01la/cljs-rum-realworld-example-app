(ns conduit.controllers.form)

(def initial-state
  {:init-data   nil
   :init-fields nil
   :data        nil
   :errors      nil
   :fields      nil
   :pristine?   true
   :has-errors? false
   :validators  nil})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defn fields-description->form-init-data [fields-description]
  (->> fields-description
       keys
       (reduce
         #(assoc %1 %2 (get-in fields-description [%2 :initial-value] ""))
         {})))

(defn check-errors [validators value]
  (->> validators
       (filter (fn [[validator]] (-> value validator not)))
       (map second)))

(defn get-field-errors [form name value]
  (let [{:keys [validators]} form
        field-validators (get validators name)]
    (check-errors field-validators value)))

;; form updaters

(defn update-has-errors? [form]
  (->> (form :errors)
       vals (apply concat) (every? nil?) not
       (assoc form :has-errors?)))

(defn init-errors [form]
  (->> (:fields form)
       keys
       (reduce #(assoc %1 %2 nil) {})
       (assoc form :errors)))

(defn update-pristine? [form]
  (->> (form :data)
       (reduce-kv
         (fn [res k _]
           (and res (= (get-in form [:data k])
                       (get-in form [:init-data k]))))
         true)
       (assoc form :pristine?)))

(defn reset [form]
  (-> form
      (assoc :data (:init-data form))
      (assoc :fields (:init-fields form))
      init-errors
      update-has-errors?
      (assoc :pristine? false)
      update-pristine?))

;; -------------------------

(defmethod control :init-form [_ [form-description] state]
  (let [{:keys [fields validators]} form-description
        init-data (fields-description->form-init-data fields)]
    {:state (-> state
                (assoc :init-data init-data)
                (assoc :init-fields fields)
                (assoc :data init-data)
                (assoc :fields fields)
                (assoc :validators validators)
                init-errors
                update-has-errors?)}))

(defmethod control :validate [_ [name] state]
  {:state (-> state
              (assoc-in [:errors name] (get-field-errors state name (get-in state [:data name])))
              update-has-errors?)})

(defmethod control :change [_ [name value] state]
  {:state (-> state
              (assoc-in [:data name] value)
              (assoc-in [:errors name] (get-field-errors state name value))
              update-has-errors?
              update-pristine?)})

(defmethod control :focus [_ [name] state]
  {:state (assoc-in state [:fields name :touched?] true)})

(defmethod control :reset [_ _ state]
  {:state (reset state)})