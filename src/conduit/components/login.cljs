(ns conduit.components.login
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.form.validators :as v]
            [conduit.components.forms :as f]))

(def initial-form-state {:data {} :errors {}})

(def form-state (atom initial-form-state))

(rum/defc Login < rum/reactive
  {:will-unmount (fn [{[r] :rum/args :as state}]
                   (reset! form-state initial-form-state)
                   (citrus/dispatch! r :user :clear-errors)
                   state)}
  [r route params]
  (let [form-data (rum/cursor-in form-state [:data])
        form-errors (rum/cursor-in form-state [:errors])
        validators {:email [[v/not-empty? "Please enter email"]
                             [v/email? "Invalid Email"]]
                    :password [[v/not-empty? "Please enter password"]]}
        handle-submit (fn [e]
                        (.preventDefault e)
                        (when (v/validate! form-data form-errors validators)
                          (citrus/dispatch! r :user :login {:email (:email @form-data) :password (:password @form-data)})))]
    [:.auth-page
     [:.container.page
      [:.row
       [:.col-md-6.offset-md-3.col-xs-12
        [:h1.text-xs-center "Sign in"]
        [:p.text-xs-center
         [:a {:href "#/register"} "Need an account?"]]
        (f/render-server-errors r)
        [:form {:on-submit handle-submit}
         (for [{input-key :key placeholder :placeholder input-type :type}
               [{:key :email :placeholder "Email"}
                {:key :password :placeholder "Password" :type "password"}]]
           (f/input {:placeholder placeholder :input-key input-key :input-type input-type :form-data form-data :form-errors form-errors :validators validators}))
         [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign in"]]]]]]))
