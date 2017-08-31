(ns conduit.components.forms
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [clojure.string :as cstr]
            [conduit.mixins :as mixin]
            [conduit.helpers.form :as form-helper]))

(rum/defc InputErrors [input-errors]
  [:ul.error-messages
   (for [err-msg input-errors] [:li {:key (.indexOf input-errors err-msg)} err-msg])])

(rum/defc ServerErrors [server-errors]
  [:ul.error-messages
   (map (fn [[k v]]
          [:li {:key k}
           (str (name k) " "
                (if (vector? v) (cstr/join ", " v) v))])
        server-errors)])

(rum/defc Input < {:key-fn (fn [{input-key :input-key}] input-key)}
  [{:keys [placeholder input-key input-type form-validators form-data form-errors validate-fn on-change]}]
  [:fieldset.form-group
   [:input.form-control.form-control-lg
    {:placeholder placeholder
     :on-change #(on-change (.. % -target -value))
     :on-blur validate-fn
     :on-key-up validate-fn
     :type (or input-type :text)
     :value (get form-data input-key)}]
   (when-let [input-errors (get form-errors input-key)]
     (InputErrors input-errors))])

(rum/defcs LoginForm < rum/reactive
  (mixin/form-state {:fields [{:key :email :placeholder "Email"}
                              {:key :password :placeholder "Password" :type "password"}]
                     :validators {:email [[form-helper/not-empty? "Please enter email"]
                                          [form-helper/email? "Invalid Email"]]
                                  :password [[form-helper/not-empty? "Please enter password"]]}
                     :submit-handler (fn [reconciler form-data form-errors form-validators]
                                       (fn [e]
                                         (.preventDefault e)
                                         (when (-> (form-helper/validate! form-validators @form-data form-errors)
                                                   form-helper/has-errors?)
                                           (let [{:keys [email password]} @form-data]
                                             (citrus/dispatch! reconciler :user :login {:email email
                                                                                        :password password})))))})
  {:will-unmount
   (fn [{[r] :rum/args :as state}]
     (citrus/dispatch! r :user :clear-errors)
     state)}
  [state r route params]
  (let [{:keys [form-fields form-validators form-data form-errors form-submit-handler]} state]
    [:form {:on-submit form-submit-handler}
     (when-let [server-errors (rum/react (citrus/subscription r [:user :errors]))]
       (ServerErrors server-errors))
     (for [{input-key :key placeholder :placeholder input-type :type} form-fields]
       (Input {:placeholder placeholder
               :input-key input-key
               :input-type input-type
               :form-validators form-validators
               :form-data (rum/react form-data)
               :form-errors (rum/react form-errors)
               :validate-fn #(form-helper/validate! form-validators @form-data form-errors input-key)
               :on-change #(swap! form-data assoc input-key %)}))
     [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign in"]]))
