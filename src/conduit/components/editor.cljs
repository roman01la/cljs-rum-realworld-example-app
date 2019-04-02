(ns conduit.components.editor
  (:require [rum.core :as rum]
            [conduit.components.forms.article :refer [ArticleForm]]))

(rum/defc Editor [r route params]
  [:.editor-page
   [:.container.page
    [:.row
     [:.col-md-10.offset-md-1.col-xs-12
      (ArticleForm r route params)]]]])
