(defproject fitness "1.10.0-SNAPSHOT"
  :description "TODO"
  :url "https://github.com/lsund/fitness"
  :min-lein-version "2.8.3"
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [org.clojure/tools.namespace "0.2.11"]
                 [org.clojure/java.jdbc "0.7.9"]
                 [clojure.jdbc/clojure.jdbc-c3p0 "0.3.3"]
                 [org.postgresql/postgresql "42.2.5"]
                 [cheshire "5.9.0"]
                 [environ "1.0.0"]
                 [ring/ring-json "0.5.0"]
                 [clj-time "0.15.1"]
                 [http-kit "2.3.0"]
                 [ring/ring-defaults "0.3.2"]
                 [compojure "1.6.1"]
                 [hiccup "1.0.5"]
                 [com.stuartsierra/component "0.4.0"]
                 [com.taoensso/timbre "4.10.0"]]
  :source-paths ["src/clj"]
  :ring {:handler fitness.core/new-handler}
  :main fitness.main
  :uberjar-name "fitness-standalone.jar"
  :figwheel {:css-dirs ["resources/public/css"]}
  :repl-options {:init-ns user
                 :timeout 120000}
  :profiles {:dev {:source-paths ["src/clj" "dev"]
                   :clean-targets ^{:protect false} ["resources/public/js/compiled"
                                                     :target-path]}})
