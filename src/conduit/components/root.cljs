(ns conduit.components.root
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.home :as home]
            [conduit.components.article :as article]
            [conduit.components.login :as login]
            [conduit.components.register :as register]))

(rum/defc Root < rum/reactive [r]
  (let [{route :handler params :route-params}
        (rum/react (citrus/subscription r [:router]))]
    (case route
      :home (home/Home r route params)
      :tag (home/HomeTag r route params)
      :article (article/Article r route params)
      :login (login/Login r route params)
      :register (register/Register r route params)
      [:div "404"])))
