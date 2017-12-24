(ns conduit.controllers.tag-articles)

(def initial-state
  {:articles []
   :pages-count 0
   :loading? true})

(defmulti control (fn [event] event))

(defmethod control :default [_ _ state]
  {:state state})

(defmethod control :init []
  {:state initial-state})

(defmethod control :load [_ [{:keys [tag page]}] state]
  {:state (assoc state :loading? true)
   :http  {:endpoint :articles
           :params {:tag tag
                    :limit 10
                    :offset (* (-> page (or 1) js/parseInt dec) 10)}
           :on-load :load-ready}})

(defmethod control :load-ready [_ [{:keys [articles articlesCount]}] state]
  {:state
   (-> state
       (assoc :articles articles)
       (assoc :pages-count (-> articlesCount (/ 10) Math/round))
       (assoc :loading? false))})

(defmethod control :reset []
  {:state initial-state})

(defmethod control :update [_ [id transform data] state]
  {:state (update state :articles (fn [articles]
                                    (map
                                      (fn [article]
                                        (if (= (:slug article) id)
                                          (transform data)
                                          article))
                                      articles)))})
