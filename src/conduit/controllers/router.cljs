(ns conduit.controllers.router)

(def initial-state
  {})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :push [_ [{:keys [handler route-params]}]]
  {:state
   {:route handler
    :params route-params}})
