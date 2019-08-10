(ns fitness.main
  "Namespace for running the program once"
  (:gen-class)
  (:require [com.stuartsierra.component :as c]
            [fitness.config :as config]
            [fitness.core :refer [new-system]]))

(defn -main [& args]
  (c/start (new-system (config/from-file)))
  (println "Server up and running"))
