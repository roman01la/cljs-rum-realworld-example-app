(ns conduit.components.grid
  (:require [rum.core :as rum]))

(rum/defc Row [& children]
  (apply vector :div.row children))

(rum/defc Column [class & children]
  (apply vector :div {:class class} children))
