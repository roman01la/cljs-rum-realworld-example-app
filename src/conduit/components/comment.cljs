(ns conduit.components.comment
  (:require [rum.core :as rum]
            [conduit.components.base :as base]))

(rum/defc Form []
  [:form.card.comment-form
   [:div.card-block
    [:textarea.form-control
     {:placeholder "Write a comment..."
      :rows 3}]]
   [:div.card-footer
    [:img.comment-author-img {:src ""}]
    (base/Button "Post Comment")]])

(rum/defc Options []
  [:div.mod-options
   (base/Icon :edit)
   (base/Icon :trash-a)])

(rum/defc Comment [{:keys [body author createdAt]}]
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
      (Options)]]))
