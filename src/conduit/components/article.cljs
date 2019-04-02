(ns conduit.components.article
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [markdown.core :as md]
            [conduit.components.base :as base]
            [conduit.components.grid :as grid]
            [conduit.mixins :as mixins]
            [conduit.components.comment :as comment]))

(rum/defc Banner
  [{:keys [loading? title author createdAt favoritesCount]}
   {:keys [on-follow
           on-unfollow
           following?
           on-favorite
           on-unfavorite
           favorited?]}]
  (let [{:keys [username image]} author]
    [:div.banner
     (if loading?
       [:div.container
        [:span "Loading article..."]]
       [:div.container
        [:h1 title]
        (base/ArticleMeta
          {:username  username
           :createdAt createdAt
           :image     image}
          (base/Button
            {:icon     :plus-round
             :type     :secondary
             :on-click (if following? on-unfollow on-follow)}
            (if following?
              (str "Unfollow " username " ")
              (str "Follow " username " ")))
          [:span "  "]
          (base/Button
            {:icon     :heart
             :on-click (if favorited? on-unfavorite on-favorite)}
            (if favorited?
              (str "Unfavorite Post (" favoritesCount ")")
              (str "Favorite Post (" favoritesCount ")"))))])]))

(rum/defc Actions
  [{:keys [author createdAt favoritesCount]}
   {:keys [on-follow
           on-unfollow
           following?
           on-favorite
           on-unfavorite
           favorited?]}]
  (let [{:keys [username image]} author]
    [:div.article-actions
     (base/ArticleMeta
       {:username  username
        :createdAt createdAt
        :image     image}
       (base/Button
         {:icon     :plus-round
          :type     :secondary
          :on-click (if following? on-unfollow on-follow)}
         (if following?
           (str "Unfollow " username " ")
           (str "Follow " username " ")))
       [:span "  "]
       (base/Button
         {:icon     :heart
          :on-click (if favorited? on-unfavorite on-favorite)}
         (if favorited?
           (str "Unfavorite Post (" favoritesCount ")")
           (str "Favorite Post (" favoritesCount ")"))))]))

(rum/defc Page [r {:keys [body tagList] :as article} comments actions]
  [:div.container.page
   (grid/Row
     (grid/Column "col-md-12"
                  [:div {:dangerouslySetInnerHTML
                         {:__html (md/md->html body)}}]
                  (base/Tags tagList)))
   [:hr]
   (Actions article actions)
   (grid/Row
     (grid/Column
       "col-xs-12 col-md-8 offset-md-2"
       (comment/Form r)
       (map comment/Comment comments)))])

(rum/defc Article <
  rum/reactive
  (mixins/dispatch-on-mount
    (fn [_ _ {:keys [id]}]
      {:article  [:load {:id id}]
       :comments [:load {:id id}]}))
  [r route params]
  (let [article (rum/react (citrus/subscription r [:article :article]))
        comments (rum/react (citrus/subscription r [:comments]))
        token (rum/react (citrus/subscription r [:user :token]))
        {id :slug favorited? :favorited} article
        {user-id :username following? :following} (:author article)
        profile->author (fn [p] {:author (:profile p)})
        on-follow #(citrus/dispatch! r :profile :follow user-id token {:dispatch [:article :update id profile->author]})
        on-unfollow #(citrus/dispatch! r :profile :unfollow user-id token {:dispatch [:article :update id profile->author]})
        on-favorite #(citrus/dispatch! r :articles :favorite id token {:dispatch [:article :update id :article]})
        on-unfavorite #(citrus/dispatch! r :articles :unfavorite id token {:dispatch [:article :update id :article]})
        actions {:on-follow     on-follow
                 :on-unfollow   on-unfollow
                 :following?    following?
                 :on-favorite   on-favorite
                 :on-unfavorite on-unfavorite
                 :favorited?    favorited?}]
    [:div.article-page
     (Banner article actions)
     (Page r article comments actions)]))
