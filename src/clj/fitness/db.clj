(ns fitness.db
  "Database component"
  (:require [clojure.java.jdbc :as jdbc]
            [fitness.util :as util]
            [jdbc.pool.c3p0 :as pool]
            [environ.core :refer [env]]
            [clojure.string :as string]
            [clojure.set :refer [rename-keys]]
            [clj-time.coerce :refer [to-sql-date]]
            [com.stuartsierra.component :as c]))
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; DB SPEC


;; Heroku DB Spec
(def db-uri (java.net.URI. (or (env :database)
                               "postgresql://localhost:5432/trainer")))

(def user-and-password
  (if (nil? (.getUserInfo db-uri))
    nil
    (string/split (.getUserInfo db-uri) #":")))

(defn make-db-spec []
  (pool/make-datasource-spec
   {:classname "org.postgresql.Driver"
    :subprotocol "postgresql"
    :user (get user-and-password 0)
    :password (get user-and-password 1)
    :subname (if (= -1 (.getPort db-uri))
               (format "//%s%s" (.getHost db-uri) (.getPath db-uri))
               (format "//%s:%s%s" (.getHost db-uri) (.getPort db-uri) (.getPath db-uri)))}))

;; Local DB Spec
(defn pg-db [config]
  {:dbtype "postgresql"
   :dbname (:name config)
   :user "postgres"})

(def pg-uri
  {:dbtype "postgresql"
   :connection-uri "postgresql://localhost:5432/trainer"})

(def pg-db-val (pg-db {:name "fitness"}))

;; DB Component
(defrecord Db [db db-config]
  c/Lifecycle
  (start [component]
    (println "[Db] Starting database")
    (assoc component :db (pg-db db-config)))
  (stop [component]
    (println "[Db] Stopping database")
    component))

(defn new-db [config]
  (map->Db {:db-config config}))

(defn row [db table id]
  (first (jdbc/query db [(str "SELECT * FROM " (name table) " WHERE id=?") id])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query

(defn value [db table column id]
  (-> db
      (jdbc/query [(str "SELECT "
                        (name column)
                        " from "
                        (name table)
                        " where id = ?") id])
      first
      column))

(defn all [db table]
  (jdbc/query db [(str "SELECT * FROM " (name table))]))

(defn all-where [db table clause]
  (jdbc/query db [(str "SELECT * FROM " (name table) " WHERE " clause)]))

(defn id->name [db table id]
  (->> (all-where db table (str "id=" id))
       first
       :name))

(defn distinct-names [db table]
  (map :name
       (jdbc/query db [(str "SELECT distinct(name) from " (name table))])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modify

(defn update-row [db table update-map id]
  (jdbc/update! db table update-map ["id=?" id]))

(defn delete-row [db table id]
  (jdbc/delete! db table ["id=?" id]))

(defn insert-row [db table insert-map]
  (jdbc/insert! db table insert-map))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Trainer -> Finances

(defn import-trainer-exercise [db data]
  (doseq [record data]
    (insert-row db :exercise (-> record
                                 (update :day to-sql-date)
                                 (update :duration util/duration-str->int)))))

(defn import-trainer-squash [db data]
  (doseq [record data]
    (insert-row db :squash (-> record
                               (update :day to-sql-date)
                               (rename-keys {:name :opponent})))))
