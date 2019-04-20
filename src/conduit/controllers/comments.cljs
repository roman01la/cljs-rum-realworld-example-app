(ns conduit.controllers.comments)

(def initial-state
  {:error              nil
   :comments           []
   :loading?           false
   :comments-candidate nil})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :create [_ [article-id body token] state]
  {:state (assoc state :loading? true)
   :http  {:endpoint :comments
           :slug     article-id
           :method   :post
           :params   {:comment {:body body}}
           :token    token
           :on-load  :add-comment
           :on-error :save-error}})

(defmethod control :load [_ [{:keys [id]}] _]
  {:state (assoc initial-state :loading? true)
   :http  {:endpoint :comments
           :slug     id
           :on-load  :load-ready}})

(defmethod control :add-comment [_ [{:keys [comment]}] state]
  {:state    (-> state
                 (assoc :loading? false)
                 (update :comments #(conj % comment)))
   :dispatch {:form [:reset]}})

(defmethod control :delete-comment [_ [article-id comment-id token] state]
  {:state (assoc state :comments-candidate (filter #(not= (:id %) comment-id) (state :comments))
                       :loading? true)
   :http  {
           :endpoint :comment
           :slug     [article-id comment-id]
           :method   :delete
           :token    token
           :on-load  :use-comments-candidate
           :on-error :save-error}})

(defmethod control :load-ready [_ [{:keys [comments]}] state]
  {:state (assoc state :comments comments
                       :loading? false)})

(defmethod control :use-comments-candidate [_ _ state]
  {:state (assoc state :comments (state :comments-candidate)
                       :comments-candidate nil
                       :loading? false)})

(defmethod control :save-error [_ [{errors :errors}] state]
  {:state (assoc state :errors errors
                       :loading? false)})

