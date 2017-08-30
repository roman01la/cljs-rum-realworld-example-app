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

(defmethod ->endpoint :login [_ _]
  "users/login")

(defn- ->uri [path]
  (str "https://conduit.productionready.io/api/" path))

(defn- parse-body [res]
  (-> res
      js/JSON.parse
      (js->clj :keywordize-keys true)))

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- ->xhr [uri xhr-fn params]
  (-> uri
      (xhr-fn params)
      (p/then (fn [{status :status body :body :as response}]
                (condp = status
                  status/ok (p/resolved (parse-body body))
                  (p/rejected (parse-body body)))))))

(defn fetch [{:keys [endpoint params slug method json authorized xhr-params]}]
  (let [xhr-fn (case method
                 :post xhr/post
                 :put xhr/put
                 :patch xhr/patch
                 (if (not= nil json) xhr/post xhr/get))
        xhr-params (-> xhr-params
                       (update-in [:query-params] #(if (not= nil params) (merge % params) %))
                       (update-in [:body] #(if (not= nil json) (->json json) %))
                       (update-in [:headers "Content-Type"] #(if (not= nil json) "application/json" %))
                       (update-in [:headers "Authorization"] #(if (= true authorized)
                                                                (str "Token " (.getItem js/localStorage "jwt-token")) %))
                       (update-in [:headers] (fn [h] (into {} (filter #(-> % val (not= nil)) h)))))]
     (-> (->endpoint endpoint slug)
         ->uri
         (->xhr xhr-fn xhr-params))))
