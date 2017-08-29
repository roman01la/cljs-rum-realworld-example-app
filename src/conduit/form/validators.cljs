(ns conduit.form.validators)

(def email-regex #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")

(defn not-empty? [v]
  (cond
    (nil? v) false
    (= "" v) false
    :else true))

(defn email? [v]
  (not (nil? (re-matches email-regex (str v)))))

(defn validate! [form-data form-errors validators]
  (let [valid? (atom true)]
    (doseq [[k v] validators]
      (reset! form-errors (assoc @form-errors k nil))
      (doall (->> v
                  (map (fn [e]
                         (let [[validator err-msg] e
                               value (k @form-data)]
                           (when (not (validator value))
                             (reset! valid? false)
                             (reset! form-errors (assoc @form-errors k (vec (conj (k @form-errors) err-msg)))))))))))
    @valid?))
