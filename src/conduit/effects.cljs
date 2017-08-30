(ns conduit.effects
  (:require [citrus.core :as citrus]
            [conduit.api :as api]
            [promesa.core :as p]))

(defn http [r c {:keys [endpoint params slug on-load on-error method json authorized xhr-params]}]
  (-> (api/fetch {:endpoint endpoint
                  :params params
                  :slug slug
                  :method method
                  :json json
                  :authorized authorized
                  :xhr-params xhr-params})
      (p/then #(citrus/dispatch! r c on-load %))
      (p/catch #(citrus/dispatch! r c on-error %))))

(defmulti local-storage (fn [_ _ params] (:action params)))

(defmethod local-storage :set [_ _ {:keys [id value]}]
  (.setItem js/localStorage id value))

(defmethod local-storage :remove [_ _ {:keys [id]}]
  (.removeItem js/localStorage id))

(defn redirect [_ _ path]
  (set! (.-hash js/location) (str "#/" path)))
