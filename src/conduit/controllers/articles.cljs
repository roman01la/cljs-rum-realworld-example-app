(ns conduit.controllers.articles)

(def initial-state
  {:articles    []
   :page        0
   :pages-count 0
   :loading?    false})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ _ state]
  {:state (assoc state :loading? true)
   :http  {:endpoint :articles
           :on-load  :load-ready}})

(defmethod control :load-ready [_ [{:keys [articles articlesCount]}] state]
  {:state
   (-> state
       (assoc :articles articles)
       (assoc :page 1)
       (assoc :pages-count (-> articlesCount (/ 10) Math/round))
       (assoc :loading? false))})

(defmethod control :favorite [_ [id token callback]]
  {:http {:endpoint :favorite
          :slug     id
          :method   :post
          :token    token
          :on-load  callback
          :on-error callback}})

(defmethod control :favorite-ready [_ _ state]
  {:state state})

(defmethod control :favorite-error [_ _ state]
  {:state state})

(defmethod control :unfavorite [_ [id token callback]]
  {:http {:endpoint :favorite
          :slug     id
          :method   :delete
          :token    token
          :on-load  callback
          :on-error callback}})

(defmethod control :unfavorite-ready [_ _ state]
  {:state state})

(defmethod control :unfavorite-error [_ _ state]
  {:state state})
