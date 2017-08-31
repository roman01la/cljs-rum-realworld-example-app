(ns conduit.mixins
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(defn dispatch-on-mount [events-fn]
  {:did-mount
   (fn [{[r] :rum/args :as state}]
     (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
       (apply citrus/dispatch! (into [r ctrl] event-vector)))
     state)
   :did-remount
   (fn [old {[r] :rum/args :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event-vector] (apply events-fn (:rum/args state))]
         (apply citrus/dispatch! (into [r ctrl] event-vector))))
     state)})
