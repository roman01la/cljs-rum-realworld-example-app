(ns conduit.components.router
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [bidi.bidi :as b]
            [goog.events :as events]
            [clojure.string :as cstr]))

(defn start! [on-set-page routes]
  (letfn [(handle-route []
            (let [uri (cstr/replace js/location.hash "#" "")]
              (->> (if-not (empty? uri) uri "/")
                   (b/match-route routes)
                   on-set-page)))]
    (events/listen js/window "hashchange" handle-route)
    (handle-route)
    handle-route))

(defn stop! [handler]
  (events/unlisten js/window "hashchange" handler))

(def router-mixin
  {:will-mount
   (fn [{[r routes _ on-navigate] :rum/args
         :as state}]
     (->> routes
          (start! #(do (scrum/dispatch! r :router :push %)
                       (on-navigate r %)))
          (assoc state :conduit/history)))
   :will-unmount
   (fn [{history :conduit/history
         :as state}]
     (stop! history)
     (dissoc state :conduit/history))})

(rum/defc Router <
  rum/reactive
  router-mixin
  [r _ layouts _]
  (let [{:keys [route params]} (rum/react (scrum/subscription r [:router]))
        layout (get layouts route)]
    (when layout
      (layout r route params))))
