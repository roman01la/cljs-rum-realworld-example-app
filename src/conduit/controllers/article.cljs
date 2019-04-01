(ns conduit.controllers.article)

(def initial-state
  {:loading? false})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

;; TODO: really strange, how :on-load works exactly?
(defmethod control :load [_ [{:keys [id]}]]
  {:state {:loading? true}
   :http  {:endpoint :article
           :slug     id
           :on-load  :load-ready}})

(defmethod control :load-ready [_ [{:keys [article]}]]
  {:state article})

(defmethod control :update [_ [id transform data] state]
  {:state (merge state (transform data))})

(defmethod control :save [_ [{:keys [title description body tagList]} token id] state]
  (let [http-params (if id
                      {:endpoint :article
                       :slug     id
                       :method   :put}
                      {:endpoint :articles
                       :method   :post})]
    {:state (assoc state :loading? true)
     :http  (into http-params
                  {:params   {:article {:title       title
                                        :description description
                                        :body        body
                                        :tagList     tagList}}
                   :token    token
                   :on-load  :save-success
                   :on-error :save-error})}))

(defmethod control :save-success [_ [{article :article}] state]
  {:state    (assoc state :article article
                          :errors nil
                          :loading? false)
   :redirect (str "article/" (:slug article))})

;; TODO change loading status
(defmethod control :save-error [_ [{errors :errors}] state]
  {:state (assoc state :errors errors)})