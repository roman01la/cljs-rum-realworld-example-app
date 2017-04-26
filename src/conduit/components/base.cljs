(ns conduit.components.base
  (:require [rum.core :as rum]))

(rum/defc Button [{:keys [icon]} label]
  [:button.btn.btn-outline-primary.btn-sm.pull-xs-right
   (when icon [:i {:class icon}])
   (when icon " ")
   label])
