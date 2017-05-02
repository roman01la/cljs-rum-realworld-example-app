(ns conduit.controllers.articles)

(def initial-state
  {:articles []
   :page 0
   :pages-count 0})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ _ state]
  {:http {:endpoint :articles
          :on-load :load-ready}})

(defmethod control :load-ready [_ [{:keys [articles articlesCount]}] state]
  {:state
   (-> state
       (assoc :articles articles)
       (assoc :page 1)
       (assoc :pages-count (-> articlesCount (/ 10) Math/round)))})
