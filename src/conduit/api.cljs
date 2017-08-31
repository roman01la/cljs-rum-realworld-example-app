(ns conduit.api
  (:require [citrus.core :as citrus]
            [httpurr.client.xhr :as xhr]
            [httpurr.status :as status]
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

(defmethod ->endpoint :users [_ _]
  "users")

(defmethod ->endpoint :login [_ _]
  "users/login")

(defmethod ->endpoint :user [_ _]
  "user")

(defn- ->uri [path]
  (str "https://conduit.productionready.io/api/" path))

(defn- parse-body [res]
  (-> res
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn- ->xhr [uri xhr-fn params]
  (-> uri
      (xhr-fn params)
      (p/then (fn [{status :status body :body :as response}]
                (condp = status
                  status/ok (p/resolved (parse-body body))
                  (p/rejected (parse-body body)))))))

(defn fetch
  ([endpoint]
   (fetch endpoint nil))
  ([endpoint params]
   (fetch endpoint params nil))
  ([endpoint params slug]
   (fetch endpoint params slug nil))
  ([endpoint params slug headers]
   (-> (->endpoint endpoint slug)
       ->uri
       (->xhr xhr/get {:query-params params
                       :headers headers}))))

(defn post
  ([endpoint]
   (fetch endpoint nil))
  ([endpoint params]
   (fetch endpoint params nil))
  ([endpoint params slug]
   (-> (->endpoint endpoint slug)
       ->uri
       (->xhr xhr/post {:body (.stringify js/JSON (clj->js params))
                        :headers {"Content-Type" "application/json"}}))))
