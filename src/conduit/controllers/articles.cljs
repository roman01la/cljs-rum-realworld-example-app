(ns conduit.controllers.articles
  (:require [scrum.core :as scrum]
            [conduit.api :as api]
            [promesa.core :as p]))

(def initial-state
  {:global []})

(defmulti control (fn [action] action))

(defmethod control :default [_ _ state]
  state)

(defmethod control :init []
  initial-state)

(defmethod control :load [_ [r] state]
  (-> (api/fetch :articles)
      (p/then #(scrum/dispatch! r :articles :load-ready %)))
  state)

(defmethod control :load-ready [_ [{:keys [articles]} state]]
  (assoc state :global articles))
