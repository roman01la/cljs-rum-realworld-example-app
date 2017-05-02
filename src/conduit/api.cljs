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

(defn fetch
  ([endpoint]
   (fetch endpoint nil))
  ([endpoint params]
   (-> (get endpoints endpoint)
       ->uri
       (xhr/get {:query-params params})
       (p/then parse-body))))
