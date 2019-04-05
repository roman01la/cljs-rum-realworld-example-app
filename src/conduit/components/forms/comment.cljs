(ns conduit.components.forms.comment
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.base :as base]))

(rum/defc CommentForm < rum/reactive [r]
  (let [avatar-url (rum/react (citrus/subscription r [:user :current-user :image]))]
    [:form.card.comment-form
     [:div.card-block
      [:textarea.form-control
       {:placeholder "Write a comment..."
        :rows        3}]]
     [:div.card-footer
      [:img.comment-author-img {:src avatar-url}]
      (base/Button "Post Comment")]]))