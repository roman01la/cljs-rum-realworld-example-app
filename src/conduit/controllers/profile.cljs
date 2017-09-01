(ns conduit.controllers.profile)

(def initial-state
  {})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ _ state]
  {:state state})

(defmethod control :follow [_ [id token callback]]
  {:http {:endpoint :follow
          :slug     id
          :method   :post
          :token    token
          :on-load  callback
          :on-error callback}})

(defmethod control :follow-ready [_ _ state]
  {:state state})

(defmethod control :follow-error [_ _ state]
  {:state state})

(defmethod control :unfollow [_ [id token callback]]
  {:http {:endpoint :follow
          :slug     id
          :method   :delete
          :token    token
          :on-load  callback
          :on-error callback}})

(defmethod control :unfollow-ready [_ _ state]
  {:state state})

(defmethod control :unfollow-error [_ _ state]
  {:state state})
