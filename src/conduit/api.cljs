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

(defn- ->json [params]
  (.stringify js/JSON (clj->js params)))

(defn- ->xhr [uri xhr-fn params]
  (-> uri
      (xhr-fn params)
      (p/then (fn [{status :status body :body :as response}]
                (condp = status
                  status/ok (p/resolved (parse-body body))
                  (p/rejected (parse-body body)))))))

(defn fetch [{:keys [endpoint params slug method type headers]}]
  (let [xhr-fn (case method
                 :post xhr/post
                 :put xhr/put
                 :patch xhr/patch
                 xhr/get)
         xhr-params {:query-params (when-not (= method :post) params)
                     :body (when (= method :post) (->json params))
                     :headers (case type
                                :json (merge headers {"Content-Type" "application/json"})
                                headers)}]
     (-> (->endpoint endpoint slug)
         ->uri
         (->xhr xhr-fn xhr-params))))
