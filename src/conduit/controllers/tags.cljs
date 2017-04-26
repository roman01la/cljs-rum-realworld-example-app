(ns conduit.controllers.tags
  (:require [scrum.core :as scrum]
            [conduit.api :as api]
            [promesa.core :as p]))

(def initial-state
  [])

(defmulti control (fn [action] action))

(defmethod control :default [_ _ state]
  state)

(defmethod control :init []
  initial-state)

(defmethod control :load [_ [r] state]
  (-> (api/fetch :tags)
      (p/then #(scrum/dispatch! r :tags :load-ready %)))
  state)

(defmethod control :load-ready [_ [{:keys [tags]}]]
  tags)
