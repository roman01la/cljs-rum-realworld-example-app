(ns conduit.controllers.user)

(def initial-state
  {:current-user nil
   :token nil
   :errors nil})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :login [_ [{:keys [email password]}] _]
  {:state initial-state
   :http {:endpoint :login
          :params {:user {:email email :password password}}
          :method :post
          :type :json
          :on-load :login-success
          :on-error :form-submit-error}})

(defmethod control :login-success [_ [{user :user {token :token} :user}] state]
  {:state (assoc state :token token :current-user user :errors nil)
   :local-storage {:action :set
                   :id "jwt-token"
                   :value token}
   :redirect ""})

(defmethod control :form-submit-error [_ [{errors :errors}] state]
  {:state (assoc state :errors errors)})

(defmethod control :clear-errors [_ _ state]
  {:state (assoc state :errors nil)})
