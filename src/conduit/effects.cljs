(ns conduit.effects
  (:require [scrum.core :as scrum]
            [conduit.api :as api]
            [promesa.core :as p]))

(defn http [r c {:keys [endpoint params on-load]}]
  (-> (api/fetch endpoint params)
      (p/then #(scrum/dispatch! r c on-load %))))
