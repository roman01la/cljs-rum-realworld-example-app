(ns conduit.components.forms
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.form.validators :as v]))

(rum/defc render-errors [errors input-key]
  (when-let [input-errors (get-in errors [input-key])]
    [:ul.error-messages
     (map (fn [e] [:li e]) input-errors)]))

(rum/defc render-server-errors < rum/reactive [r]
  (when-let [server-errors (rum/react (citrus/subscription r [:user :errors]))]
    [:ul.error-messages
     (map (fn [[k v]] [:li {:key k} (str (name k) " " v)]) server-errors)]))

(rum/defc input < rum/reactive
  [{:keys [placeholder input-key input-type form-data form-errors validators]}]
  (let [validate-field! #(v/validate! form-data form-errors (select-keys validators [input-key]))]
    [:fieldset.form-group
     [:input.form-control.form-control-lg
      {:placeholder placeholder
       :on-change #(reset! form-data (assoc @form-data input-key (.. % -target -value)))
       :on-blur validate-field!
       :on-key-down validate-field!
       :on-key-up validate-field!
       :type (or input-type :text)
       :value (get-in (rum/react form-data) [input-key])}]
     (render-errors (rum/react form-errors) input-key)]))
