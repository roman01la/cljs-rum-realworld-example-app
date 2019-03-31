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

(rum/defc InputFieldContainer [value & children]
  (apply vector :fieldset.form-group children))

(defn TagInputFieldContainer [data errors key]
  (fn [value & children]
    (let [{:keys [tagList]} @data]
      (apply vector :fieldset.form-group children
             #{[:.tag-list
                (map (fn [t]
                       [:span.tag-default.tag-pill {:key t}
                        [:i.ion-close-round
                         {:on-click (fn [e] (swap! data assoc :tagList (remove #(= % t) tagList)))}]
                        t])
                     tagList)
                ]}))))

(rum/defc InputField
  [{:keys [placeholder type value errors on-blur on-focus on-change container events]}]
  (let [input-container (or container InputFieldContainer)
        input-events (merge {:on-change #(on-change (.. % -target -value))
                             :on-blur   on-blur
                             :on-focus  on-focus}
                            events)]
    (input-container
      value
      [:input.form-control.form-control-lg
       (into
         {:placeholder placeholder
          :type        (or type :text)
          :value       value}
         input-events)]
      (when errors
        (InputErrors errors)))))

(rum/defc TextareaField
  [{:keys [placeholder type value errors on-blur on-focus on-change container events]}]
  (let [input-container (or container InputFieldContainer)
        input-events (merge {:on-change #(on-change (.. % -target -value))
                             :on-blur   on-blur
                             :on-focus  on-focus}
                            events)]
    (input-container
      value
      [:textarea.form-control.form-control-lg
       (into
         {:placeholder placeholder
          :type        (or type :text)
          :value       value}
         input-events)]
      (when errors
        (InputErrors errors)))))

(def login-form
  {:fields     {:email    {:placeholder "Email"}
                :password {:placeholder "Password" :type "password"}}
   :validators {:email    [[#(not (empty? %)) "Please enter email"]
                           [form-helper/email? "Invalid Email"]]
                :password [[#(not (empty? %)) "Please enter password"]]}
   :on-submit
               (fn [reconciler data errors validators]
                 (let [{:keys [email password]} data]
                   (citrus/dispatch! reconciler :user :login {:email    email
                                                              :password password})))})

(def register-form
  {:fields     {:username {:placeholder "Username"}
                :email    {:placeholder "Email"}
                :password {:placeholder "Password" :type "password"}}
   :validators {:username [[#(not (empty? %)) "Please enter username"]]
                :email    [[#(not (empty? %)) "Please enter email"]
                           [form-helper/email? "Invalid Email"]]
                :password [[#(not (empty? %)) "Please enter password"]
                           [(form-helper/length? {:min 8}) "Password is too short (minimum is 8 characters)"]]}
   :on-submit
               (fn [reconciler data errors validators]
                 (let [{:keys [username email password]} data]
                   (citrus/dispatch! reconciler :user :register {:username username
                                                                 :email    email
                                                                 :password password})))})

(def article-form
  {:fields     {:title       {:placeholder "Article Title"}
                :description {:placeholder "What's this article about?"}
                :body        {:placeholder "Write your article (in markdown)"}
                :tag         {:placeholder "Enter tags"
                              :container   TagInputFieldContainer
                              :events      {:on-key-down
                                            (fn [data errors key]
                                              #(when (= 13 (.-keyCode %))
                                                 (.preventDefault %)
                                                 (let [tagList (or (:tagList @data) [])
                                                       tag (cstr/trim (key @data))]
                                                   (swap! data assoc
                                                          :tagList (if (and (not (empty? tag)) (= -1 (.indexOf tagList tag)))
                                                                     (conj (vec tagList) tag) tagList)
                                                          key ""))))}}}
   :validators {:title [[#(not (empty? %)) "Please enter title"]]
                :body  [[#(not (empty? %)) "Please enter body"]]}
   :on-submit
               (fn [reconciler data errors validators [token id]]
                 (let [{:keys [title description body tagList]} data]
                   (citrus/dispatch! reconciler :article :save
                                     {:title       title
                                      :description description
                                      :body        body
                                      :tagList     tagList}
                                     token)))})

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
        loading? (rum/react (citrus/subscription r [:user :loading?]))
        disabled? (or has-errors?
                      loading?
                      (not (->> fields vals (map #(contains? % :touched?)) (every? true?)))
                      (->> fields vals (map :touched?) (every? nil?)))]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit with-prevent-default))}
     (when server-errors
       (ServerErrors server-errors))
     (for [[key {:keys [placeholder type]}] fields]
       (let [value (get data key)]
         (rum/with-key
           (InputField
             {:placeholder placeholder
              :type        type
              :errors      (-> (get errors key) seq)
              :on-blur     #(validate key value)
              :on-focus    #(on-focus key)
              :on-change   #(do
                              (validate key %)
                              (on-change key %))
              :value       value})
           key)))
     (base/Button
       {:class     "pull-xs-right"
        :outline?  false
        :disabled? disabled?
        :size      :L}
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
        loading? (rum/react (citrus/subscription r [:user :loading?]))
        disabled? (or has-errors?
                      loading?
                      (not (->> fields vals (map #(contains? % :touched?)) (every? true?)))
                      (->> fields vals (map :touched?) (every? nil?)))]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit with-prevent-default))}
     (when server-errors
       (ServerErrors server-errors))
     (for [[key {:keys [placeholder type]}] fields]
       (let [value (get data key)]
         (rum/with-key
           (InputField
             {:placeholder placeholder
              :type        type
              :errors      (-> (get errors key) seq)
              :on-blur     #(validate key value)
              :on-focus    #(on-focus key)
              :on-change   #(do
                              (validate key %)
                              (on-change key %))
              :value       value})
           key)))
     (base/Button
       {:class     "pull-xs-right"
        :outline?  false
        :disabled? disabled?
        :size      :L}
       "Sign up")]))

(rum/defcs ArticleForm < rum/reactive
                         (mixins/form article-form)
                         {:will-unmount
                          (fn [{[r] :rum/args :as state}]
                            (citrus/dispatch! r :article :init)
                            state)}
  [state r _ _]
  (let [{{:keys [fields data errors on-submit on-change on-focus validate]} ::mixins/form} state
        token (rum/react (citrus/subscription r [:user :token]))
        server-errors (rum/react (citrus/subscription r [:article :errors]))
        has-errors? (->> errors vals (apply concat) (every? nil?) not)
        disabled? (or has-errors? (->> fields vals (map :touched?) (every? nil?)))]
    [:form {:on-submit (when-not has-errors?
                         (comp on-submit (fn [] [token]) with-prevent-default))}
     (when server-errors
       (ServerErrors server-errors))
     (for [[key {:keys [placeholder type container events]}] fields]
       (let [value (get data key)]
         (rum/with-key
           (InputField
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
       "Publish Article")]))


(def user-settings-form
  {:fields     {:image    {:placeholder "URL of profile picture"}
                :username {:placeholder "Username"}
                :bio      {:placeholder "Short bio about you"
                           :component   :textarea}
                :email    {:placeholder "Email"
                           :type        "email"}
                :password {:placeholder "Password"
                           :type        "password"}}
   ;; TODO add email validation
   :validators {:username [[#(not (empty? %)) "Shouldn't be blank"]]
                :password [[#(not (empty? %)) "Shouldn't be blank"]]
                :email    [[#(not (empty? %)) "Shouldn't be blank"]]}
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
  {
   :input    InputField
   :textarea TextareaField})

(rum/defcs UserSettings < rum/reactive
                          (mixins/form user-settings-form)
  [state r _ _]
  ;; TODO: why ::mixins/form and not ::form?
  (let [{{:keys [fields data errors on-submit on-change on-focus validate]} ::mixins/form} state
        token (rum/react (citrus/subscription r [:user :token]))
        server-errors (rum/react (citrus/subscription r [:user :errors]))
        ;; TODO why has-errors? and disabled? on this level?
        has-errors? (->> errors vals (apply concat) (every? nil?) not)
        disabled? (or has-errors? (->> fields vals (map :touched?) (every? nil?)))]
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

