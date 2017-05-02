(ns conduit.components.router
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [bidi.bidi :as b]
            [goog.events :as events]
            [clojure.string :as cstr]))

(defn- start! [on-set-page routes]
  (letfn [(handle-route []
            (let [uri (cstr/replace js/location.hash "#" "")]
              (->> (if-not (empty? uri) uri "/")
                   (b/match-route routes)
                   on-set-page)))]
    (events/listen js/window "hashchange" handle-route)
    (handle-route)
    handle-route))

(defn- stop! [handler]
  (events/unlisten js/window "hashchange" handler))

(defn mixin [events]
  {:did-mount
   (fn [{[r _ params] :rum/args
         :as state}]
     (doseq [[ctrl event] events]
       (scrum/dispatch! r ctrl event params))
     state)
   :did-remount
   (fn [old
        {[r _ params] :rum/args
         :as state}]
     (when (not= (:rum/args old) (:rum/args state))
       (doseq [[ctrl event] events]
         (scrum/dispatch! r ctrl event params)))
     state)})

(def ^:private router-mixin
  {:did-mount
   (fn [{[r routes _] :rum/args
         route ::route
         :as state}]
     (->> routes
          (start! #(reset! route %))
          (assoc state ::history)))
   :will-unmount
   (fn [{history ::history
         :as state}]
     (stop! history)
     (dissoc state ::history))})

(rum/defcs Router <
  rum/reactive
  router-mixin
  (rum/local {} ::route)
  [{route ::route} r _ layouts]
  (let [{:keys [handler route-params]} (rum/react route)
        layout (get layouts handler)]
    (when layout
      (layout r handler route-params))))
