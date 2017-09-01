(ns conduit.effects
  (:require [citrus.core :as citrus]
            [conduit.api :as api]
            [promesa.core :as p]))

(defmulti http (fn [_ _ params] (:method params)))

(defmethod http :post [r c {:keys [endpoint params slug on-load on-error]}]
  (-> (api/post endpoint params slug)
      (p/then #(citrus/dispatch! r c on-load %))
      (p/catch #(citrus/dispatch! r c on-error %))))

(defmethod http :default [r c {:keys [endpoint params slug on-load on-error headers]}]
  (-> (api/fetch endpoint params slug headers)
      (p/then #(citrus/dispatch! r c on-load %))
      (p/catch #(citrus/dispatch! r c on-error %))))

(defmulti local-storage (fn [_ _ params] (:action params)))

(defmethod local-storage :get [r c {:keys [id on-success on-error]}]
  (if-let [token (.getItem js/localStorage id)]
    (citrus/dispatch! r c on-success token)
    (citrus/dispatch! r c on-error)))

(defmethod local-storage :set [_ _ {:keys [id value]}]
  (.setItem js/localStorage id value))

(defmethod local-storage :remove [_ _ {:keys [id]}]
  (.removeItem js/localStorage id))

(defn redirect [_ _ path]
  (set! (.-hash js/location) (str "#/" path)))
