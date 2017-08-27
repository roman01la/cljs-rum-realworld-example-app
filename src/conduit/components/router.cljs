(ns conduit.components.router
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [bidi.bidi :as b]
            [goog.events :as events]
            [clojure.string :as cstr]))

(defn mixin [events]
  {:did-mount
   (fn [{[r _ params] :rum/args
         :as state}]
     (doseq [[ctrl event] events]
       (citrus/dispatch! r ctrl event params))
     state)
   :did-remount
   (fn [old
        {[r _ params] :rum/args
         :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event] events]
         (citrus/dispatch! r ctrl event params)))
     state)})
