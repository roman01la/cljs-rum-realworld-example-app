(ns conduit.components.comment
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.base :as base]))

(rum/defc Form < rum/reactive [r]
  (let [avatar-url (rum/react (citrus/subscription r [:user :current-user :image]))]
    [:form.card.comment-form
     [:div.card-block
      [:textarea.form-control
       {:placeholder "Write a comment..."
        :rows        3}]]
     [:div.card-footer
      [:img.comment-author-img {:src avatar-url}]
      (base/Button "Post Comment")]]))

(rum/defc Options < rum/reactive [r comment]
  (let [user (rum/react (citrus/subscription r [:user]))
        author-username (get-in comment [:author :username])
        {params :route-params} (rum/react (citrus/subscription r [:router]))
        handle-delete #(citrus/dispatch! r :comments :delete-comment (:id params) (:id comment) (:token user))]
    (when (= author-username (get-in user [:current-user :username]))
      [:div.mod-options
       (base/Icon {:on-click handle-delete} :trash-a)])))

(rum/defc Comment [r {:keys [body author createdAt] :as comment}]
  (let [{:keys [username image]} author]
    [:div.card
     [:div.card-block
      [:p.card-text body]]
     [:div.card-footer
      [:a.comment-author {:href (str "#/profile/" username)}
       [:img.comment-author-img {:src image}]]
      [:span " "]
      [:a.comment-author {:href (str "#/profile/" username)}
       username]
      [:span.date-posted
       (-> createdAt js/Date. .toDateString)]
      (Options r comment)]]))
