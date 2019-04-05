(ns conduit.controllers.comments)

(def initial-state
  {:error    nil
   :comments []
   :loading? false})

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
  (.log js/console comment state)
  {:state (-> state
              (assoc :loading false)
              (update :comments #(conj % comment)))})

(defmethod control :load-ready [_ [{:keys [comments]}] state]
  {:state (assoc state :comments comments)})

(defmethod control :save-error [_ [{errors :errors}] state]
  {:state (assoc state :errors errors
                       :loading? false)})

