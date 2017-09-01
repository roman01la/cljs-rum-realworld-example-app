(ns conduit.effects
  (:require [citrus.core :as citrus]
            [conduit.api :as api]
            [promesa.core :as p]))

(defmulti dispatch! (fn [_ _ effect]
                      (type effect)))

(defmethod dispatch! Keyword [r c event & args]
  (apply citrus/dispatch! r c event args))

(defmethod dispatch! PersistentArrayMap [r c effects & oargs]
  (doseq [[effect [c event & args]] effects]
    (apply dispatch! r c event (concat args oargs))))

(defn http [r c {:keys [endpoint params slug on-load on-error method type headers token]}]
  (-> (api/fetch {:endpoint endpoint
                  :params   params
                  :slug     slug
                  :method   method
                  :type     type
                  :headers  headers
                  :token    token})
      (p/then #(dispatch! r c on-load %))
      (p/catch #(dispatch! r c on-error %))))


(defmulti local-storage (fn [_ _ params] (:action params)))

(defmethod local-storage :get [r c {:keys [id on-success on-error]}]
  (if-let [token (.getItem js/localStorage id)]
    (dispatch! r c on-success token)
    (dispatch! r c on-error)))

(defmethod local-storage :set [_ _ {:keys [id value]}]
  (.setItem js/localStorage id value))

(defmethod local-storage :remove [_ _ {:keys [id]}]
  (.removeItem js/localStorage id))


(defn redirect [_ _ path]
  (set! (.-hash js/location) (str "#/" path)))


(defn dispatch [r _ events]
  (doseq [[ctrl event-vector] events]
    (apply citrus/dispatch! (into [r ctrl] event-vector))))
