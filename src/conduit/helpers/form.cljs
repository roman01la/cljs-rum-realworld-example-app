(ns conduit.helpers.form
  (:import goog.format.EmailAddress))

(defn not-empty? [v]
  (cond
    (nil? v) false
    (= "" v) false
    :else true))

(defn email? [v]
  (.isValid (new EmailAddress v)))

(defn validate! [form-data form-errors validators]
  (doseq [[k v] validators]
    (reset! form-errors (assoc @form-errors k nil))
    (doall (->> v
                (map (fn [e]
                       (let [[validator err-msg] e
                             value (k @form-data)]
                         (when-not (validator value)
                           (reset! form-errors (assoc @form-errors k (vec (conj (k @form-errors) err-msg)))))))))))
    @form-errors)

(defn valid? [form-errors]
  (reduce-kv #(if (not-empty? %3) (reduced false) true) true form-errors))
