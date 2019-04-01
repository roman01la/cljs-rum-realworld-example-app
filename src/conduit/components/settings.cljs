(ns conduit.components.settings
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.forms.user-settings :refer [UserSettings]]))

(rum/defc Settings < rum/reactive
  [r route params]
  (when-let [current-user (rum/react (citrus/subscription r [:user :current-user]))]
    [:div.settings-page
     [:div.container.page
      [:div.row
       [:div.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Your Settings"]
        (UserSettings r route params current-user)
        [:hr]
        [:a.btn.btn-outline-danger {:href "#/logout"} "Or click here to logout."]]]]]))
