(ns conduit.api
  (:require [httpurr.client.xhr :as xhr]
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

(defmethod ->endpoint :follow [_ slug]
  (str "profiles/" slug "/follow"))

(defmethod ->endpoint :favorite [_ slug]
  (str "articles/" slug "/favorite"))

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

(defn- method->xhr-fn [method]
  (case method
    :post xhr/post
    :put xhr/put
    :patch xhr/patch
    :delete xhr/delete
    xhr/get))

(defn- type->header [type]
  (case type
    :text {"Content-Type" "text/plain"}
    {"Content-Type" "application/json"}))

(defn- token->header [token]
  (if token
    {"Authorization" (str "Token " token)}
    {}))

(defn fetch [{:keys [endpoint params slug method type headers token]}]
  (let [xhr-fn (method->xhr-fn method)
        xhr-params {:query-params (when-not (contains? #{:post :put :patch} method) params)
                    :body         (when (contains? #{:post :put :patch} method) (->json params))
                    :headers      (merge headers (type->header type) (token->header token))}]
    (-> (->endpoint endpoint slug)
        ->uri
        (->xhr xhr-fn xhr-params))))
