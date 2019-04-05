(ns conduit.components.forms.comment
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.base :as base]))

(rum/defcs CommentForm < rum/reactive (rum/local "" ::text)
  [state r]
  (let [{avatar-url :image token :token} (rum/react (citrus/subscription r [:user :current-user]))
        {params :route-params} (rum/react (citrus/subscription r [:router]))
        textarea-value (::text state)
        handle-submit (fn [e]
                        (.preventDefault e)
                        (citrus/dispatch! r :comments :create
                                          (:id params)
                                          @textarea-value
                                          token))
        handle-change #(->> % .-target .-value (reset! textarea-value))]
    [:form.card.comment-form
     {:on-submit handle-submit}
     [:div.card-block
      [:textarea.form-control
       {:placeholder "Write a comment..."
        :rows        3
        :on-change   handle-change}]]
     [:div.card-footer
      [:img.comment-author-img {:src avatar-url}]
      (base/Button "Post Comment")]]))