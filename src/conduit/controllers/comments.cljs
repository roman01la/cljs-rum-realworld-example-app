(ns conduit.controllers.comments)

(def initial-state
  [])

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ [{:keys [id]}] _]
  {:state initial-state
   :http  {:endpoint :comments
           :slug     id
           :on-load  :load-ready}})

(defmethod control :load-ready [_ [{:keys [comments]}]]
  {:state comments})
