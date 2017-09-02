(ns conduit.components.header
  (:require [rum.core :as rum]))

(def nav-items
  [{:label "Home"
    :route :home
    :link "/"
    :display-for :always}
   {:label "New Post"
    :route :new-post
    :icon "ion-compose"
    :link "/new-post"
    :display-for :logged}
   {:label "Settings"
    :route :settings
    :icon "ion-gear-a"
    :link "/settings"
    :display-for :logged}
   {:label "Sign in"
    :route :sign-in
    :link "/login"
    :display-for :non-logged}
   {:label "Sign up"
    :route :sign-up
    :link "/register"
    :display-for :non-logged}])

(rum/defc NavItem [curr-route {:keys [label icon route link]}]
  [:li.nav-item {:class (when (= route curr-route) "active")}
   [:a.nav-link {:href (str "#" link)}
    (when icon [:i {:class icon}])
    (when icon " ")
    label]])

(rum/defc Header [r route {:keys [loading? current-user]}]
  (let [user-nav-items (->> nav-items
                            (filter #(not= (if current-user :non-logged :logged) (:display-for %)))
                            (#(if current-user
                                (into (vec %) [{:label (:username current-user)
                                                :route :profile
                                                :link  (str "/profile/" (:username current-user))}]) %)))]
    [:nav.navbar.navbar-light
     [:div.container
      [:a.navbar-brand {:href "#/"} "conduit"]
      (when-not loading?
        [:ul.nav.navbar-nav.pull-xs-right
         (map #(rum/with-key (NavItem route %) (:label %)) user-nav-items)])]]))
