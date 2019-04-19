(ns conduit.controllers.form)

(def initial-state
  {:init-data   nil
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

(defn fields-description->form-init-errors [fields-description]
  (->> fields-description
       keys
       (reduce #(assoc %1 %2 nil) {})))

(defn check-errors [validators value]
  (->> validators
       (filter (fn [[validator]] (-> value validator not)))
       (map second)))

(defmethod control :init-form [_ [form-description] state]
  (let [{:keys [fields validators]} form-description
        init-data (fields-description->form-init-data fields)]
    {:state (-> state
                (assoc :init-data init-data)
                (assoc :data init-data)
                (assoc :errors (fields-description->form-init-errors fields))
                (assoc :fields fields)
                (assoc :validators validators))}))

(defmethod control :validate [_ [name] state]
  (let [{:keys [validators data]} state
        field-validators (get validators name)
        field-value (get data name)
        field-errors (check-errors field-validators field-value)]
    {:state (assoc-in state [:errors name] field-errors)}))

(defmethod control :change [_ [name value] state]
  {:state (assoc-in state [:data name] value)})

(defmethod control :focus [_ [name] state]
  {:state (assoc-in state [:fields name :touched?] true)})

(defmethod control :reset [_ _ state]
  {:state (assoc state :data (state :init-data))})
