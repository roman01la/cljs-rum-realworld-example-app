(ns conduit.core
  (:require [rum.core :as rum]
            [scrum.core :as scrum]
            [goog.dom :as dom]
            [conduit.controllers.router :as router]
            [conduit.controllers.articles :as articles]
            [conduit.controllers.tags :as tags]
            [conduit.controllers.tag-articles :as tag-articles]
            [conduit.components.router :refer [Router]]
            [conduit.components.home :refer [Home]]))

(def routes
  ["/" [["" :home]
        [["tag/" :id] :home]]])

(def layouts
  {:home Home})

;; create Reconciler instance
(defonce reconciler
  (scrum/reconciler
    {:state (atom {})
     :controllers
     {:router router/control
      :articles articles/control
      :tag-articles tag-articles/control
      :tags tags/control}}))

;; initialize controllers
(defonce init-ctrl (scrum/broadcast-sync! reconciler :init))

(defn on-navigate [r {:keys [handler route-params]}]
  (let [{:keys [id]} route-params]
    (cond
      (and (= handler :home) id)
      (do
        (scrum/dispatch! r :tag-articles :load r id)
        (scrum/dispatch! r :tags :load r))

      (= handler :home)
      (do
        (scrum/dispatch! r :tag-articles :reset)
        (scrum/dispatch! r :articles :load r)
        (scrum/dispatch! r :tags :load r)))))


(rum/mount (Router reconciler routes layouts on-navigate)
           (dom/getElement "app"))

