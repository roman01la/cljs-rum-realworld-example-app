(ns conduit.components.root
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.home :as home]
            [conduit.components.article :refer [Article]]
            [conduit.components.header :refer [Header]]
            [conduit.components.footer :refer [Footer]]
            [conduit.components.login :refer [Login]]
            [conduit.components.register :refer [Register]]))

(rum/defc Root < rum/reactive
  {:did-mount
   (fn [{[r] :rum/args :as state}]
     (when-let [token (.getItem js/localStorage "jwt-token")]
       (citrus/dispatch! r :user :set-token token))
     state)}
  [r]
  (let [{route :handler params :route-params}
        (rum/react (citrus/subscription r [:router]))]
    [:div
     (Header r route)
     (case route
       :home (home/Home r route params)
       :tag (home/HomeTag r route params)
       :article (Article r route params)
       :login (Login r route params)
       :register (Register r route params)
       [:div "404"])
     (Footer)]))
