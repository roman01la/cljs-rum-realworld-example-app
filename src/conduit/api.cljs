(ns conduit.api
  (:require [scrum.core :as scrum]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p]))

(defn ->uri [path]
  (str "https://conduit.productionready.io/api/" path))

(def endpoints
  {:articles "articles"
   :tags "tags"})

(defn parse-body [res]
  (-> (:body res)
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn fetch [endpoint]
  (-> (get endpoints endpoint)
      ->uri
      xhr/get
      (p/then parse-body)))

(defn mixin [controller->endpoint]
  {:did-mount
   (fn [{[r] :rum/args
         :as state}]
     (doseq [[controller endpoint] controller->endpoint]
       (->> (fetch endpoint)
            (scrum/dispatch! r controller :load r)))
     state)})
