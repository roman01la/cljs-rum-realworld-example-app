(ns conduit.controllers.user)

(def initial-state
  {:current-user nil
   :loading?     false
   :token        nil
   :errors       nil})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :login [_ [{:keys [email password]}] _]
  {:state (assoc initial-state :loading? true)
   :http  {:endpoint :login
           :params   {:user {:email email :password password}}
           :method   :post
           :type     :json
           :on-load  :login-success
           :on-error :form-submit-error}})

(defmethod control :login-success [_ [{user :user {token :token} :user}] state]
  {:state         (assoc state :token token
                               :current-user user
                               :errors nil
                               :loading? false)
   :local-storage {:action :set
                   :id     "jwt-token"
                   :value  token}
   :redirect      ""})

(defmethod control :register [_ [{:keys [username email password]}] _]
  {:state (assoc initial-state assoc :loading? true)
   :http  {:endpoint :users
           :params   {:user {:username username :email email :password password}}
           :method   :post
           :type     :json
           :on-load  :register-success
           :on-error :form-submit-error}})

(defmethod control :register-success [_ [{user :user {token :token} :user}] state]
  {:state         (assoc state
                    :token token
                    :current-user user
                    :errors nil
                    :loading? false)
   :local-storage {:action :set
                   :id     "jwt-token"
                   :value  token}
   :redirect      ""})

(defmethod control :form-submit-error [_ [{errors :errors}] state]
  {:state (assoc state
            :errors errors
            :loading? false)})

(defmethod control :clear-errors [_ _ state]
  {:state (assoc state :errors nil)})

(defmethod control :check-auth [_ _ state]
  {:state         state
   :local-storage {:action     :get
                   :id         "jwt-token"
                   :on-success :load-user}})

(defmethod control :load-user [_ [token] state]
  {:state (assoc state :token token :loading? true)
   :http  {:endpoint :user
           :token    token
           :on-load  :load-user-success}})

(defmethod control :load-user-success [_ [{:keys [user]}] state]
  {:state (assoc state
            :current-user user
            :loading? false)})

(defmethod control :update-settings [_ [{:keys [username email password image bio]}] state]
  {:state (assoc state :loading? true)
   :http  {:endpoint :user
           :params   {:user {:username username
                             :email    email
                             :password password
                             :image    image
                             :bio      bio}}
           :method   :put
           :token    (:token state)
           :on-load  :update-settings-success
           :on-error :form-submit-error}})

(defmethod control :update-settings-success [_ [{user :user}] state]
  {:state    (assoc state :current-user user
                          :errors nil
                          :loading? false)
   :redirect ""})

(defmethod control :logout []
  {:state         initial-state
   :local-storage {:action :remove
                   :id     "jwt-token"}
   :redirect      ""})
