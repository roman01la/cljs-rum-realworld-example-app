(ns conduit.components.header
  (:require [rum.core :as rum]))

(def nav-items
  [{:label "Home"
    :route :home}
   {:label "New Post"
    :route :new-post
    :icon "ion-compose"}
   {:label "Settings"
    :route :settings
    :icon "ion-gear-a"}
   {:label "Sign up"
    :route :sign-up}])

(rum/defc NavItem [curr-route {:keys [label icon route]}]
  [:li.nav-item {:class (when (= route curr-route) "active")}
   [:a.nav-link {:href ""}
    (when icon [:i {:class icon}])
    (when icon " ")
    label]])

(rum/defc Header [r route]
  [:nav.navbar.navbar-light
   [:div.container
    [:a.navbar-brand {:href "#/"} "conduit"]
    [:ul.nav.navbar-nav.pull-xs-right
     (map #(rum/with-key (NavItem route %) (:label %)) nav-items)]]])
