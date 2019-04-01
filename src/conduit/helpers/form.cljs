(ns conduit.helpers.form
  (:import goog.format.EmailAddress))

(defn email? [v]
  (.isValid (EmailAddress. v)))

(defn length? [{:keys [min max]}]
  (fn [v]
    (let [length (.-length v)]
      (and (if min (if (>= length min) true false) true)
           (if max (if (<= length max) true false) true)))))

(defn present? [v]
  (not (empty? v)))