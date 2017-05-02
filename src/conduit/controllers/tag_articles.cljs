(ns conduit.controllers.tag-articles)

(def initial-state
  {:articles []
   :page 0
   :pages-count 0
   :loading? true})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ [{:keys [id]}] state]
  {:state (assoc state :loading? true)
   :http {:endpoint :articles
          :params {:tag id}
          :on-load :load-ready}})

(defmethod control :load-ready [_ [{:keys [articles articlesCount]}] state]
  {:state
   (-> state
       (assoc :articles articles)
       (assoc :page 1)
       (assoc :pages-count (-> articlesCount (/ 10) Math/round))
       (assoc :loading? false))})

(defmethod control :reset []
  {:state initial-state})
