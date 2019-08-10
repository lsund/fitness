(ns fitness.app
  "Application component"
  (:require [com.stuartsierra.component :as c]
            [fitness.handler :as handler]))

(defrecord App [handler app-config db]
  c/Lifecycle
  (start [component]
    (if handler
      component
      (do
        (println "[App] Starting, attaching handler")
        (assoc component :handler (handler/new-handler (merge app-config db))))))
  (stop [component]
    (println "[App] Stopping")
    (assoc component :handler nil)))

(defn new-app [config]
  (map->App {:app-config config}))
