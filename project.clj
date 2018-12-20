(defproject conduit "0.1.0-SNAPSHOT"
  :description "FIXME: write this!"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :min-lein-version "2.7.1"

  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/clojurescript "1.10.439"]
                 [rum "0.11.2"]
                 [org.roman01la/citrus "3.0.0"]
                 [bidi "2.1.2"]
                 [funcool/promesa "1.9.0"]
                 [funcool/httpurr "1.0.0"]
                 [markdown-clj "1.0.1"]]

  :plugins [[lein-figwheel "0.5.17"]
            [lein-cljsbuild "1.1.7" :exclusions [[org.clojure/clojure]]]]

  :source-paths ["src"]

  :cljsbuild {:builds
              [{:id "dev"
                :source-paths ["src"]
                :figwheel true
                :compiler {:main conduit.core
                           :asset-path "/js/compiled/out"
                           :output-to "resources/public/js/compiled/conduit.js"
                           :output-dir "resources/public/js/compiled/out"
                           :source-map-timestamp true
                           :preloads [devtools.preload]}}
               {:id "min"
                :source-paths ["src"]
                :compiler {:output-to "resources/public/js/compiled/conduit.js"
                           :main conduit.core
                           :optimizations :advanced
                           :parallel-build true
                           :closure-defines {"goog.DEBUG" false}
                           :pretty-print false}}]}

  :figwheel {:css-dirs ["resources/public/css"]
             :server-port 3000
             :ring-handler server/handler}

  :profiles {:dev {:dependencies [[binaryage/devtools "0.9.4"]
                                  [figwheel-sidecar "0.5.17"]
                                  [com.cemerick/piggieback "0.2.1"]
                                  [ring "1.5.1"]
                                  [ring/ring-defaults "0.2.1"]
                                  [compojure "1.5.0"]]

                   :source-paths ["src" "dev"]
                   ;; for CIDER
                   ;; :plugins [[cider/cider-nrepl "0.12.0"]]
                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
