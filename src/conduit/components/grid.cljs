(ns conduit.components.grid
  (:require [rum.core :as rum]))

(rum/defc Row [& children]
  (apply vector :div.row children))

(rum/defc Column [size & children]
  (apply vector :div {:class (str "col-md-" size)} children))
