(ns conduit.components.forms.user-settings
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.mixins :as mixins]
            [conduit.components.base :as base]
            [conduit.components.forms :refer [InputField TextareaField ServerErrors with-prevent-default]]
            [conduit.helpers.form :as form-helper]))

(def user-settings-form
  {:fields     {:image    {:placeholder "URL of profile picture"}
                :username {:placeholder "Username"}
                :bio      {:placeholder "Short bio about you"
                           :component   :textarea}
                :email    {:placeholder "Email"
                           :type        "email"}
                :password {:placeholder "Password"
                           :type        "password"}}
   :validators {:username [[form-helper/present? "Please enter username"]]
                :email    [[form-helper/present? "Please enter email"]
                           [form-helper/email? "Invalid Email"]]
                :password [[form-helper/present? "Please enter password"]
                           [(form-helper/length? {:min 8}) "Password is too short (minimum is 8 characters)"]]}
   :on-submit
               (fn [reconciler data errors validators [token id]]
                 (let [{:keys [image username bio email password]} data]
                   (citrus/dispatch! reconciler :user :update-settings
                                     {:image    image
                                      :username username
                                      :bio      bio
                                      :email    email
                                      :password password})))})

(def get-field
  {:input    InputField
   :textarea TextareaField})

(rum/defcs UserSettings < rum/reactive
                          (mixins/form user-settings-form)
                          {:will-unmount
                           (fn [{[r] :rum/args :as state}]
                             (citrus/dispatch! r :user :clear-errors)
                             state)}
  [state r _ _ _]
  (let [{{:keys [fields data errors on-submit on-change on-focus validate has-errors? pristine?]} ::mixins/form} state
        token (rum/react (citrus/subscription r [:user :token]))
        server-errors (rum/react (citrus/subscription r [:user :errors]))
        disabled? (or has-errors? pristine?)]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit (fn [] [token]) with-prevent-default))}
     [:fieldset
      (when server-errors
        (ServerErrors server-errors))
      (for [[key {:keys [placeholder type container events component]}] fields]
        (let [value (get data key)
              Field (get-field (or component :input))]
          (rum/with-key
            (Field
              {:placeholder placeholder
               :type        type
               :value       value
               :errors      (-> (get errors key) seq)
               :on-blur     #(validate key value)
               :on-focus    #(on-focus key)
               :on-change   #(do
                               (validate key %)
                               (on-change key %))
               :container   container
               :events      events})
            key)))
      (base/Button
        {:class     "pull-xs-right"
         :outline?  false
         :disabled? disabled?
         :size      :L}
        "Update Settings")]]))
