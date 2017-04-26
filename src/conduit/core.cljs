(ns conduit.core
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [goog.dom :as dom]
            [conduit.controllers.router :as router]
            [conduit.controllers.articles :as articles]
            [conduit.controllers.tags :as tags]
            [conduit.components.router :refer [Router]]
            [conduit.components.home :refer [Home]]))

(def routes
  ["/" [["" :home]
        [["tag/" :id] :tag]]])

(def layouts
  {:home Home})

;; create Reconciler instance
(defonce reconciler
  (scrum/reconciler {:state (atom {})
                     :controllers {:router router/control
                                   :articles articles/control
                                   :tags tags/control}}))

;; initialize controllers
(defonce init-ctrl (scrum/broadcast-sync! reconciler :init))

(rum/mount (Router reconciler routes layouts)
           (dom/getElement "app"))

