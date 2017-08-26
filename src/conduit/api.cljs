(ns conduit.api
  (:require [citrus.core :as citrus]
            [httpurr.client.xhr :as xhr]
            [promesa.core :as p]))

(defmulti ->endpoint (fn [id] id))

(defmethod ->endpoint :articles [_ _]
  "articles")

(defmethod ->endpoint :tags [_ _]
  "tags")

(defmethod ->endpoint :article [_ slug]
  (str "articles/" slug))

(defmethod ->endpoint :comments [_ slug]
  (str "articles/" slug "/comments"))


(defn- ->uri [path]
  (str "https://conduit.productionready.io/api/" path))

(defn- parse-body [res]
  (-> (:body res)
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn fetch
  ([endpoint]
   (fetch endpoint nil))
  ([endpoint params]
   (fetch endpoint params nil))
  ([endpoint params slug]
   (-> (->endpoint endpoint slug)
       ->uri
       (xhr/get {:query-params params})
       (p/then parse-body))))
