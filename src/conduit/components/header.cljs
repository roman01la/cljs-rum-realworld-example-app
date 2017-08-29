(ns conduit.components.header
  (:require [rum.core :as rum]
            [citrus.core :as citrus]))

(def nav-items
  [{:label "Home"
    :route :home}
   {:label "New Post"
    :route :new-post
    :icon "ion-compose"}
   {:label "Settings"
    :route :settings
    :icon "ion-gear-a"}
   {:label "Sign in"
    :route :login}
   {:label "Sign up"
    :route :register}
   {:label "Sign out"
    :route :logout}])

(rum/defc NavItem [curr-route {:keys [label icon route]}]
  [:li.nav-item {:class (when (= route curr-route) "active")}
   [:a.nav-link {:href (->> route (name) (str "#/"))}
    (when icon [:i {:class icon}])
    (when icon " ")
    label]])

(rum/defc Header < rum/reactive [r route]
  (let [current-user (rum/react (citrus/subscription r [:user :current-user]))
        curent-user? (not (nil? current-user))
        user-nav-items (if curent-user?
                         (->> {:label (str "Hi, " (:username current-user)) :route :home}
                              (conj (filter #(contains? #{:new-post :settings :logout} (:route %)) nav-items)))
                         (filter #(contains? #{:home :login :register} (:route %)) nav-items))]
    [:nav.navbar.navbar-light
     [:div.container
      [:a.navbar-brand {:href "#/"} "conduit"]
      [:ul.nav.navbar-nav.pull-xs-right
       (map #(rum/with-key (NavItem route %) (:label %)) user-nav-items)]]]))
