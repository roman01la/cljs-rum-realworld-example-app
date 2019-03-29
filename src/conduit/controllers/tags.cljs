(ns conduit.controllers.tags)

(def initial-state
  [])

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ _ state]
  {:http {:endpoint :tags
          :on-load  :load-ready}})

(defmethod control :load-ready [_ [{:keys [tags]}]]
  {:state tags})
