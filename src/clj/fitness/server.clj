(ns fitness.server
  "Server Component"
  (:require [com.stuartsierra.component :as component]
            [org.httpkit.server :refer [run-server]]))

(defrecord Server [app port server]
  component/Lifecycle
  (start [component]
    (println "[Server] Starting HttpKit on port" port)
    (if server
      component
      (do
        (->> (run-server (:handler app) {:port port})
             (assoc component :server)))))
  (stop [component]
    (if-not server
      component
      (do
        (println "Stopping HttpKit")
        (server :timeout 10)
        (assoc component :server nil)))))

(defn new-server [config]
  (map->Server {:port (:port config)}))
