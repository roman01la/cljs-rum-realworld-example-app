(ns server
  (:require [compojure.core :refer :all]
            [compojure.route :as r]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :as res]))

(defroutes app
  (r/resources "/" {:root "public"})
  (GET "*" [] (-> (res/resource-response "index.html" {:root "public"})
                  (res/content-type "text/html"))))

(def handler (wrap-defaults #'app site-defaults))
