(ns conduit.controllers.router)

(def initial-state
  {})

(defmulti control (fn [action] action))

(defmethod control :default [_ _ state]
  state)

(defmethod control :init []
  initial-state)

(defmethod control :push [_ [{:keys [handler route-params]}]]
  {:route handler
   :params route-params})
