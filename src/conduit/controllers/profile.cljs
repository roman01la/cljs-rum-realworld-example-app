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

(defmethod control :load-profile [_ [id token] state]
  {:state (assoc state :loading? true)
   :http {:endpoint :profile
          :slug     id
          :on-load  :load-profile-ready
          :on-error :load-profile-error
          :token    token}})

(defmethod control :load-profile-ready [_ [{profile :profile}] state]
  {:state (assoc state
                 :profile  profile
                 :loading? false)})

(defmethod control :load-profile-error [_ _ state]
  {:state (assoc state
                 :profile  nil
                 :loading? false)})

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

(defmethod control :update [_ [{profile :profile}] state]
  {:state (assoc state :profile profile)})
