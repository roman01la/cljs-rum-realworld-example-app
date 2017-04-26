(ns conduit.components.footer
  (:require [rum.core :as rum]))

(rum/defc Footer []
  [:footer
   [:div.container
    [:a.logo-font {:href "/"} "conduit"]
    [:span.attribution
     "An interactive learning project from "
     [:a {:href "https://thinkster.io"} "Thinkster"]
     ". "
     "Code & design licensed under MIT."]]])
