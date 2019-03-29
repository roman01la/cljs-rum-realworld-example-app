(ns conduit.components.header
  (:require [rum.core :as rum]))

(def nav-items
  [{:label       "Home"
    :route       :home
    :link        "/"
    :display-for :always}
   {:label       "New Post"
    :route       :editor
    :icon        "ion-compose"
    :link        "/editor"
    :display-for :logged}
   {:label       "Settings"
    :route       :settings
    :icon        "ion-gear-a"
    :link        "/settings"
    :display-for :logged}
   {:label       "Sign in"
    :route       :login
    :link        "/login"
    :display-for :non-logged}
   {:label       "Sign up"
    :route       :sign-up
    :link        "/register"
    :display-for :non-logged}])

(rum/defc NavItem [curr-route {:keys [label icon route link]}]
  [:li.nav-item {:class (when (= route curr-route) "active")}
   [:a.nav-link {:href (str "#" link)}
    (when icon [:i {:class icon}])
    (when icon " ")
    label]])

(rum/defc Header [r route {:keys [loading? logged-in?]}]
  (let [user-nav-items (filter #(not= (if logged-in? :non-logged :logged) (:display-for %)) nav-items)]
    [:nav.navbar.navbar-light
     [:div.container
      [:a.navbar-brand {:href "#/"} "conduit"]
      (when-not loading?
        [:ul.nav.navbar-nav.pull-xs-right
         (map #(rum/with-key (NavItem route %) (:label %)) user-nav-items)])]]))
