(ns conduit.components.forms.comment
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.components.base :as base]
            [conduit.helpers.form :as form-helper]
            [conduit.mixins :as mixins]))

(def comment-form
  {:fields     {:comment {:placeholder "Write a comment"}}
   :validators {:comment [[form-helper/present? "Please enter a comment"]]}})

(rum/defcs CommentForm < rum/reactive
                         (mixins/dispatch-on-mount
                              (fn []
                                {:form [:init-form]}))
  [state r]
  (let [{avatar-url :image token :token} (rum/react (citrus/subscription r [:user :current-user]))
        {params :route-params} (rum/react (citrus/subscription r [:router]))
        comments (rum/react (citrus/subscription r [:comments]))
        form (rum/react (citrus/subscription r [:form]))
        disabled? (or (:has-errors? form) (:pristine? form) (:loading? comments))
        placeholder (get-in form [:fields :comment :placeholder])
        handle-submit (fn [e]
                        (.preventDefault e)
                        (citrus/dispatch! r :comments :create
                                          (:id params)
                                          (get-in form [:data :comment])
                                          token))]
    [:form.card.comment-form
     {:on-submit handle-submit}
     [:div.card-block
      [:textarea.form-control
       {:placeholder placeholder
        :rows        3
        :value       (get-in form [:data :comment])
        :on-change   #(->> % .-target .-value (citrus/dispatch! r :form :change :comment))
        :on-blur     #(citrus/dispatch! r :form :validate :comment)
        :on-focus    #(citrus/dispatch! r :form :focus :comment)}]]
     [:div.card-footer
      [:img.comment-author-img {:src avatar-url}]
      (base/Button {:disabled? disabled?} "Post Comment")]]))