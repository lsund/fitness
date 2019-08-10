(ns fitness.core
  "Component system map"
  (:require [com.stuartsierra.component :as c]
            [fitness.app :as app]
            [fitness.db :as db]
            [fitness.server :as server]))

(defn new-system [config]
  (c/system-map :server (c/using (server/new-server (:server config))
                                 [:app])
                :app (c/using (app/new-app (:app config))
                              [:db])
                :db (c/using (db/new-db (:db config))
                             [])))
