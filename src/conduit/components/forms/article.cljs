(ns conduit.components.forms.article
  (:require [rum.core :as rum]
            [citrus.core :as citrus]
            [conduit.mixins :as mixins]
            [conduit.components.base :as base]
            [conduit.components.forms :refer [InputField
                                              TextareaField
                                              ServerErrors
                                              TagInputFieldContainer
                                              with-prevent-default]]
            [conduit.helpers.form :as form-helper]))

(defn- handle-keydown [data errors key]
  #(when (= 13 (.-keyCode %))
     (.preventDefault %)
     (let [tagList (or (:tagList @data) [])
           tag (clojure.string/trim (key @data))]
       (swap! data assoc
              :tagList (if (and (not (empty? tag)) (= -1 (.indexOf tagList tag)))
                         (conj (vec tagList) tag) tagList)
              key ""))))

(defn- handle-submit [reconciler data errors validators [token id]]
  (let [{:keys [title description body tagList]} data]
    (citrus/dispatch! reconciler :article :save
                      {:title       title
                       :description description
                       :body        body
                       :tagList     tagList}
                      token)))

(def article-form
  {:fields     {:title       {:placeholder "Article Title"}
                :description {:placeholder "What's this article about?"}
                :body        {:placeholder "Write your article (in markdown)"}
                :tag         {:placeholder "Enter tags"
                              :container   TagInputFieldContainer
                              :events      {:on-key-down handle-keydown}}
                :tagList     {:hidden        true
                              :initial-value []}}
   :validators {:title [[form-helper/present? "Please enter title"]]
                :body  [[form-helper/present? "Please enter body"]]}
   :on-submit  handle-submit})

(rum/defcs ArticleForm < rum/reactive
                         (mixins/form article-form)
                         {:will-unmount
                          (fn [{[r] :rum/args :as state}]
                            (citrus/dispatch! r :article :init)
                            state)}
  [state r _ _ _]
  (let [{{:keys [fields data errors on-submit on-change on-focus validate pristine? has-errors?]} ::mixins/form} state
        token (rum/react (citrus/subscription r [:user :token]))
        server-errors (rum/react (citrus/subscription r [:article :errors]))
        loading? (rum/react (citrus/subscription r [:article :loading?]))
        disabled? (or has-errors? pristine? loading?)]
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