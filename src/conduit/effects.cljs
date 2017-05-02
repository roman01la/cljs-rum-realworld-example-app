(ns conduit.effects
  (:require [scrum.core :as scrum]
            [conduit.api :as api]
            [promesa.core :as p]))

(defn http [r c {:keys [endpoint params slug on-load on-error]}]
  (-> (api/fetch endpoint params slug)
      (p/then #(scrum/dispatch! r c on-load %))
      (p/catch #(scrum/dispatch! r c on-error %))))
