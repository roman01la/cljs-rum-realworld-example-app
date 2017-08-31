(ns conduit.components.forms
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [clojure.string :as cstr]
            [conduit.mixins :as mixins]
            [conduit.components.base :as base]
            [conduit.helpers.form :as form-helper]))

(rum/defc InputErrors [input-errors]
  [:ul.error-messages
   (for [err-msg input-errors]
     [:li {:key err-msg} err-msg])])

(rum/defc ServerErrors [server-errors]
  [:ul.error-messages
   (for [[k v] server-errors]
     [:li {:key k}
      (str (name k)
           " "
           (if (vector? v)
             (cstr/join ", " v)
             v))])])

(rum/defc InputField
  [{:keys [placeholder type value errors on-blur on-focus on-change]}]
  [:fieldset.form-group
   [:input.form-control.form-control-lg
    {:placeholder placeholder
     :on-change #(on-change (.. % -target -value))
     :on-blur on-blur
     :on-focus on-focus
     :type (or type :text)
     :value value}]
   (when errors
     (InputErrors errors))])

(def login-form
  {:fields {:email {:placeholder "Email"}
            :password {:placeholder "Password" :type "password"}}
   :validators {:email [[#(not (empty? %)) "Please enter email"]
                        [form-helper/email? "Invalid Email"]]
                :password [[#(not (empty? %)) "Please enter password"]]}
   :on-submit
   (fn [reconciler data errors validators]
     (let [{:keys [email password]} data]
       (citrus/dispatch! reconciler :user :login {:email email
                                                  :password password})))})

(def register-form
  {:fields {:username {:placeholder "Username"}
             :email {:placeholder "Email"}
             :password {:placeholder "Password" :type "password"}}
   :validators {:username [[#(not (empty? %)) "Please enter username"]]
                :email [[#(not (empty? %)) "Please enter email"]
                        [form-helper/email? "Invalid Email"]]
                :password [[#(not (empty? %)) "Please enter password"]
                           [(form-helper/length? {:min 8}) "Password is too short (minimum is 8 characters)"]]}
   :on-submit
   (fn [reconciler data errors validators]
     (let [{:keys [username email password]} data]
       (citrus/dispatch! reconciler :user :register {:username username
                                                     :email email
                                                     :password password})))})

(defn- with-prevent-default [e]
  (.preventDefault e)
  e)

(rum/defcs LoginForm < rum/reactive
  (mixins/form login-form)
  {:will-unmount
   (fn [{[r] :rum/args :as state}]
     (citrus/dispatch! r :user :clear-errors)
     state)}
  [state r _ _]
  (let [{{:keys [fields data errors on-submit on-change on-focus validate]} ::mixins/form} state
        server-errors (rum/react (citrus/subscription r [:user :errors]))
        has-errors? (->> errors vals (apply concat) (every? nil?) not)
        disabled? (or has-errors? (->> fields vals (map :touched?) (every? nil?)))]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit with-prevent-default))}
     (when server-errors
       (ServerErrors server-errors))
     (for [[key {:keys [placeholder type]}] fields]
       (let [value (get data key)]
         (rum/with-key
           (InputField
             {:placeholder placeholder
              :type type
              :errors (-> (get errors key) seq)
              :on-blur #(validate key value)
              :on-focus #(on-focus key)
              :on-change #(do
                            (validate key %)
                            (on-change key %))
              :value value})
           key)))
     (base/Button
       {:class "pull-xs-right"
        :outline? false
        :disabled? disabled?
        :size :L}
       "Sign in")]))

(rum/defcs RegisterForm < rum/reactive
  (mixins/form register-form)
  {:will-unmount
   (fn [{[r] :rum/args :as state}]
     (citrus/dispatch! r :user :clear-errors)
     state)}
  [state r _ _]
  (let [{{:keys [fields data errors on-submit on-change on-focus validate]} ::mixins/form} state
        server-errors (rum/react (citrus/subscription r [:user :errors]))
        has-errors? (->> errors vals (apply concat) (every? nil?) not)
        disabled? (or has-errors? (->> fields vals (map :touched?) (every? nil?)))]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit with-prevent-default))}
     (when server-errors
       (ServerErrors server-errors))
     (for [[key {:keys [placeholder type]}] fields]
       (let [value (get data key)]
         (rum/with-key
           (InputField
             {:placeholder placeholder
              :type type
              :errors (-> (get errors key) seq)
              :on-blur #(validate key value)
              :on-focus #(on-focus key)
              :on-change #(do
                            (validate key %)
                            (on-change key %))
              :value value})
           key)))
     (base/Button
       {:class "pull-xs-right"
        :outline? false
        :disabled? disabled?
        :size :L}
       "Sign up")]))
