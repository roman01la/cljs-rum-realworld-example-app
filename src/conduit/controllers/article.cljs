(ns conduit.controllers.article)

(def initial-state
  {})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ [{:keys [id]}] state]
  {:state {:loading? true}
   :http {:endpoint :article
          :slug id
          :on-load :load-ready}})

(defmethod control :load-ready [_ [{:keys [article]}]]
  {:state article})
