(ns conduit.effects
  (:require [citrus.core :as citrus]
            [conduit.api :as api]
            [promesa.core :as p]))

(defn http [r c {:keys [endpoint params slug on-load on-error]}]
  (-> (api/fetch endpoint params slug)
      (p/then #(citrus/dispatch! r c on-load %))
      (p/catch #(citrus/dispatch! r c on-error %))))
