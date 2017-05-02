(ns conduit.core
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [goog.dom :as dom]
            [conduit.effects :as effects]
            [conduit.controllers.articles :as articles]
            [conduit.controllers.tags :as tags]
            [conduit.controllers.tag-articles :as tag-articles]
            [conduit.controllers.article :as article]
            [conduit.controllers.comments :as comments]
            [conduit.components.router :refer [Router]]
            [conduit.components.home :as home]
            [conduit.components.article :refer [Article]]))

(def routes
  ["/" [["" :home]
        [["tag/" :id] :tag]
        [["article/" :id] :article]]])

;; create Reconciler instance
(defonce reconciler
  (scrum/reconciler
    {:state (atom {})
     :controllers
     {:articles articles/control
      :tag-articles tag-articles/control
      :tags tags/control
      :article article/control
      :comments comments/control}
     :effect-handlers {:http effects/http}}))

;; initialize controllers
(defonce init-ctrl (scrum/broadcast-sync! reconciler :init))

(rum/mount (Router reconciler routes {:home home/Home
                                      :tag home/HomeTag
                                      :article Article})
           (dom/getElement "app"))

