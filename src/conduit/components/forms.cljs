(ns conduit.components.forms
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [clojure.string :as cstr]
            [conduit.mixins :as mixin]
            [conduit.helpers.form :as form-helper]))

(rum/defc InputErrors [errors input-key]
  (when-let [input-errors (get-in errors [input-key])]
    [:ul.error-messages
     (map (fn [e] [:li e]) input-errors)]))

(rum/defc ServerErrors < rum/reactive [r]
  (when-let [server-errors (rum/react (citrus/subscription r [:user :errors]))]
    [:ul.error-messages
     (map (fn [[k v]]
            [:li {:key k}
             (str (name k) " "
                  (if (vector? v) (cstr/join ", " v) v))])
          server-errors)]))

(rum/defc Input < rum/reactive
  {:key-fn (fn [{input-key :input-key}] input-key)}
  [{:keys [placeholder input-key input-type validators form-data form-errors]}]
  (let [input-validators (select-keys validators [input-key])
        validate-input! #(form-helper/validate! form-data form-errors input-validators)]
    [:fieldset.form-group
     [:input.form-control.form-control-lg
      {:placeholder placeholder
       :on-change #(reset! form-data (assoc @form-data input-key (.. % -target -value)))
       :on-blur validate-input!
       :on-key-down validate-input!
       :on-key-up validate-input!
       :type (or input-type :text)
       :value (get-in (rum/react form-data) [input-key])}]
     (InputErrors (rum/react form-errors) input-key)]))

(rum/defcs LoginForm <
  (mixin/form-state {:fields [{:key :email :placeholder "Email"}
                              {:key :password :placeholder "Password" :type "password"}]
                     :validators {:email [[form-helper/not-empty? "Please enter email"]
                                          [form-helper/email? "Invalid Email"]]
                                  :password [[form-helper/not-empty? "Please enter password"]]}
                     :submit-handler (fn [reconciler form-data form-errors form-validators]
                                       (fn [e]
                                         (.preventDefault e)
                                         (when (-> (form-helper/validate! form-data form-errors form-validators)
                                                   form-helper/valid?)
                                           (let [{:keys [email password]} (deref form-data)]
                                             (citrus/dispatch! reconciler :user :login {:email email
                                                                                        :password password})))))})
  {:will-unmount
   (fn [{[r] :rum/args :as state}]
     (citrus/dispatch! r :user :clear-errors)
     state)}
  [state r route params]
  (let [{:keys [form-fields form-validators form-data form-errors form-submit-handler]} state]
    [:form {:on-submit form-submit-handler}
     (ServerErrors r)
     (for [{input-key :key placeholder :placeholder input-type :type} form-fields]
       (Input {:placeholder placeholder
               :input-key input-key
               :input-type input-type
               :validators form-validators
               :form-data form-data
               :form-errors form-errors}))
     [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign in"]]))
