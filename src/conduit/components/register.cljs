(ns conduit.components.register
  (:require [rum.core :as rum]
            [conduit.components.forms :refer [RegisterForm]]))

(rum/defc Register [r route params]
  [:.auth-page
   [:.container.page
    [:.row
     [:.col-md-6.offset-md-3.col-xs-12
      [:h1.text-xs-center "Sign up"]
      [:p.text-xs-center
       [:a {:href "#/login"} "Have an account?"]]
      (RegisterForm r route params)]]]])
