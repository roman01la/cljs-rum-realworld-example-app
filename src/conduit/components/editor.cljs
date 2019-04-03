(ns conduit.components.editor
  (:require [rum.core :as rum]
            [conduit.components.forms.article :refer [ArticleForm]]
            [conduit.mixins :as mixins]
            [citrus.core :as citrus]))

(rum/defc Editor < rum/reactive
                   (mixins/dispatch-on-mount
                     (fn [_ _ params]
                       (if params
                         {:article [:load {:id (params :slug)}]}
                         {:article [:init]})))
  [r route params]
  (let [article (rum/react (citrus/subscription r [:article :article]))]
    [:.editor-page
     [:.container.page
      [:.row
       [:.col-md-10.offset-md-1.col-xs-12
        (ArticleForm r route params article)]]]]))
