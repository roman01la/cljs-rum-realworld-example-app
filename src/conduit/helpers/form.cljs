(ns conduit.helpers.form
  (:import goog.format.EmailAddress))

(defn not-empty? [v]
  (not= true (empty? v)))

(defn email? [v]
  (.isValid (EmailAddress. v)))

(defn check-errors
  ([validators form-data]
   (->> validators
        (reduce-kv (fn [m k input-validation-pairs]
                     (let [input-val (get form-data k)]
                       (assoc m k (check-errors input-validation-pairs input-val true)))) {})))
  ([input-validation-pairs input-val _]
   (->> input-validation-pairs
        (reduce (fn [input-err input-validation-pair]
                  (let [[input-check err-msg] input-validation-pair]
                    (when-not (input-check input-val)
                      (conj input-err err-msg)))) []))))

(defn validate!
  ([validators form-data form-errors]
   (reset! form-errors (check-errors validators form-data))
   @form-errors)
  ([validators form-data form-errors input-key]
   (let [input-validators (select-keys validators [input-key])
         input-errors (-> (check-errors input-validators form-data) input-key)]
     (swap! form-errors assoc input-key input-errors)
     @form-errors)))

(defn has-errors? [form-errors]
  (reduce-kv #(if (not-empty? %3) (reduced false) true) true form-errors))
