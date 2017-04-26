(ns conduit.controllers.router)

(def initial-state
  nil)

(defmulti control (fn [action] action))

(defmethod control :default [_ _ state]
  state)

(defmethod control :init []
  initial-state)

(defmethod control :push [_ [route]]
  route)
