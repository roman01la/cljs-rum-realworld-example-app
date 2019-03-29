(ns conduit.controllers.articles)

(def initial-state
  {:articles    []
   :pages-count 0
   :loading?    false})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ [{:keys [page]}] state]
  {:state (assoc state :loading? true)
   :http  {:endpoint :articles
           :params   {:limit  10
                      :offset (* (-> page (or 1) js/parseInt dec) 10)}
           :on-load  :load-ready}})

(defmethod control :load-ready [_ [{:keys [articles articlesCount]}] state]
  {:state
   (-> state
       (assoc :articles articles)
       (assoc :pages-count (-> articlesCount (/ 10) Math/round))
       (assoc :loading? false))})

;; TODO why we have the same effect both for sucess and failure?
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

;; TODO why we have the same effect both for sucess and failure?
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
