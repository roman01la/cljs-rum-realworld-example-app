(ns conduit.helpers.form
  (:import goog.format.EmailAddress))

(defn email? [v]
  (.isValid (EmailAddress. v)))
