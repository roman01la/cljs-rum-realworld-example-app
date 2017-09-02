(ns conduit.components.root
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.mixins :as mixins]
            [conduit.components.home :as home]
            [conduit.components.article :as article]
            [conduit.components.login :as login]
            [conduit.components.register :as register]
            [conduit.components.profile :as profile]
            [conduit.components.header :refer [Header]]
            [conduit.components.footer :refer [Footer]]))

(rum/defc Root < rum/reactive
  (mixins/dispatch-on-mount
   (fn [] {:user [:check-auth]}))
  [r]
  (let [{route :handler params :route-params}
        (rum/react (citrus/subscription r [:router]))
        {:keys [current-user loading?]} (rum/react (citrus/subscription r [:user]))]
    [:div
     (Header r route {:loading? loading?
                      :current-user current-user})
     (case route
       :home (home/Home r route params)
       :tag (home/HomeTag r route params)
       :article (article/Article r route params)
       :sign-in (login/Login r route params)
       :sign-up (register/Register r route params)
       :profile (profile/Profile r route params current-user)
       [:div "404"])
     (Footer)]))
