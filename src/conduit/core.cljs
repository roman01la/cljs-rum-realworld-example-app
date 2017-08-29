(ns conduit.core
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [goog.dom :as dom]
            [conduit.effects :as effects]
            [conduit.router :as router]
            [conduit.controllers.articles :as articles]
            [conduit.controllers.tags :as tags]
            [conduit.controllers.tag-articles :as tag-articles]
            [conduit.controllers.article :as article]
            [conduit.controllers.comments :as comments]
            [conduit.controllers.router :as router-controller]
            [conduit.controllers.user :as user]
            [conduit.components.root :refer [Root]]))

(def routes
  ["/" [["" :home]
        ["login" :login]
        ["logout" :logout]
        ["register" :register]
        [["tag/" :id] :tag]
        [["article/" :id] :article]]])

;; create Reconciler instance
(defonce reconciler
  (citrus/reconciler
    {:state (atom {})
     :controllers
     {:articles articles/control
      :tag-articles tag-articles/control
      :tags tags/control
      :article article/control
      :comments comments/control
      :router router-controller/control
      :user user/control}
     :effect-handlers {:http effects/http
                       :local-storage effects/local-storage
                       :redirect effects/redirect}}))

;; initialize controllers
(defonce init-ctrl (citrus/broadcast-sync! reconciler :init))

(router/start!
 (fn [route]
   (doall
    [(citrus/dispatch! reconciler :router :push route)
     (when (= (:handler route) :logout)
       (citrus/dispatch! reconciler :user :logout))]))
 routes)

(rum/mount (Root reconciler)
           (dom/getElement "app"))
