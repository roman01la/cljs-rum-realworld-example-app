(ns conduit.components.login
  (:require [rum.core :as rum]
            [conduit.components.forms :refer [LoginForm]]))

(rum/defc Login [r route params]
  [:.auth-page
   [:.container.page
    [:.row
     [:.col-md-6.offset-md-3.col-xs-12
      [:h1.text-xs-center "Sign in"]
      [:p.text-xs-center
       [:a {:href "#/register"} "Need an account?"]]
      (LoginForm r route params)]]]])
