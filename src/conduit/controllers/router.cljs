(ns conduit.controllers.router)

(defmulti control (fn [event] event))

(defmethod control :init [_ [route]]
  {:state route })

(defmethod control :push [_ [route]]
  {:state route})
