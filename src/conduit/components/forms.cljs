(ns conduit.components.forms
  (:import goog.format.EmailAddress)
  (:require [rum.core :as rum]
            [clojure.string :as cstr]
            [citrus.core :as citrus]))

(def initial-form-state {:data {} :errors {}})

(def form-state (atom initial-form-state))

(defn not-empty? [v]
  (cond
    (nil? v) false
    (= "" v) false
    :else true))

(defn email? [v]
  (.isValid (new EmailAddress v)))

(defn validate! [form-data form-errors validators]
  (let [valid? (atom true)]
    (doseq [[k v] validators]
      (reset! form-errors (assoc @form-errors k nil))
      (doall (->> v
                  (map (fn [e]
                         (let [[validator err-msg] e
                               value (k @form-data)]
                           (when (not (validator value))
                             (reset! valid? false)
                             (reset! form-errors (assoc @form-errors k (vec (conj (k @form-errors) err-msg)))))))))))
    @valid?))

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
        validate-input! #(validate! form-data form-errors input-validators)]
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

(rum/defc UserFormBase <
  {:will-unmount
   (fn [{[r] :rum/args :as state}]
     (reset! form-state initial-form-state)
     (citrus/dispatch! r :user :clear-errors)
     state)}
  [r route params form-fields form-validators form-submit-handler form-buttons]
  (let [form-data (rum/cursor-in form-state [:data])
        form-errors (rum/cursor-in form-state [:errors])]
    (reset! form-data (reduce #(assoc %1 %2 "") {} (map #(:key %) form-fields)))
    [:form {:on-submit (form-submit-handler form-data form-errors)}
     (ServerErrors r)
     (for [{input-key :key placeholder :placeholder input-type :type} form-fields]
       (Input {:placeholder placeholder
               :input-key input-key
               :input-type input-type
               :validators form-validators
               :form-data form-data
               :form-errors form-errors}))
     form-buttons]))

(rum/defc LoginForm
  [r route params]
  (let [fields [{:key :email :placeholder "Email"}
                {:key :password :placeholder "Password" :type "password"}]
        validators {:email [[not-empty? "Please enter email"]
                            [email? "Invalid Email"]]
                    :password [[not-empty? "Please enter password"]]}
        buttons [:button.btn.btn-lg.btn-primary.pull-xs-right "Sign in"]
        submit-handler (fn [form-data form-errors]
                         (fn [e]
                           (.preventDefault e)
                           (when (validate! form-data form-errors validators)
                             (let [{:keys [email password]} (deref form-data)]
                               (citrus/dispatch! r :user :login {:email email
                                                                 :password password})))))]
    (UserFormBase r route params fields validators submit-handler buttons)))
