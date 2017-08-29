(ns conduit.components.article
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [markdown.core :as md]
            [conduit.components.base :as base]
            [conduit.components.grid :as grid]
            [conduit.mixins :as mixins]
            [conduit.components.comment :as comment]))

(rum/defc Banner [{:keys [loading? title author createdAt favoritesCount]}]
  (let [{:keys [username image]} author]
    [:div.banner
     (if loading?
       [:div.container
        [:span "Loading article..."]]
       [:div.container
        [:h1 title]
        (base/ArticleMeta
          {:username username
           :createdAt createdAt
           :image image}
          (base/Button
            {:icon :plus-round
             :type :secondary}
            (str "Follow " username " "))
          [:span "  "]
          (base/Button
            {:icon :heart}
            (str "Favorite Post (" favoritesCount ")")))])]))

(rum/defc Actions [{:keys [author createdAt favoritesCount]}]
  (let [{:keys [username image]} author]
    [:div.article-actions
     (base/ArticleMeta
       {:username username
        :createdAt createdAt
        :image image}
       (base/Button
         {:icon :plus-round
          :type :secondary}
         (str "Follow " username " "))
       [:span "  "]
       (base/Button
         {:icon :heart}
         (str "Favorite Post (" favoritesCount ")")))]))

(rum/defc Page [r {:keys [body tagList] :as article} comments]
  [:div.container.page
   (grid/Row
     (grid/Column "col-md-12"
       [:div {:dangerouslySetInnerHTML
              {:__html (md/md->html body)}}]
       (base/Tags tagList)))
   [:hr]
   (Actions article)
   (grid/Row
     (grid/Column
       "col-xs-12 col-md-8 offset-md-2"
       (comment/Form)
       (map comment/Comment comments)))])

(rum/defc Article <
  rum/reactive
  (mixins/dispatch-on-mount
    {:article :load
     :comments :load})
  [r route params]
  (let [article (rum/react (citrus/subscription r [:article]))
        comments (rum/react (citrus/subscription r [:comments]))]
    [:div.article-page
     (Banner article)
     (Page r article comments)]))
