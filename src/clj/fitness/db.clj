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
(def db-uri (java.net.URI. (or (env :database-url)
                               "postgresql://localhost:5432/fitness")))

(def user-and-password
  (if (nil? (.getUserInfo db-uri))
    nil
    (string/split (.getUserInfo db-uri) #":")))

(defn make-heroku-spec []
  (pool/make-datasource-spec
   {:classname "org.postgresql.Driver"
    :subprotocol "postgresql"
    :user (get user-and-password 0)
    :password (get user-and-password 1)
    :subname (if (= -1 (.getPort db-uri))
               (format "//%s%s" (.getHost db-uri) (.getPath db-uri))
               (format "//%s:%s%s" (.getHost db-uri) (.getPort db-uri) (.getPath db-uri)))}))

(def docker-db-spec
  {:dbtype "postgresql"
   :dbname "fitness"
   :user "lsund"
   :host "db"
   :password "admin"})

(def local-db-spec
  {:dbtype "postgresql"
   :dbname "fitness"
   :user "postgres"})

(defn make-db-spec [{:keys [environment]}]
  (case environment
    :heroku (make-heroku-spec)
    :docker docker-db-spec
    local-db-spec))

;; DB Component
(defrecord Db [db db-config]
  c/Lifecycle
  (start [component]
    (println "[Db] Starting database with spec: " (make-db-spec db-config))
    (assoc component :db (make-db-spec db-config)))
  (stop [component]
    (println "[Db] Stopping database")
    component))

(defn new-db [config]
  (map->Db {:db-config config}))

(defn row [db table id]
  (first (jdbc/query db [(str "select * from " (name table) " where id=?") id])))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Query

(defn value [db table column id]
  (-> db
      (jdbc/query [(str "select "
                        (name column)
                        " from "
                        (name table)
                        " where id = ?") id])
      first
      column))

(defn all [db table]
  (jdbc/query db [(str "select * from " (name table))]))

(defn all-where [db table clause]
  (jdbc/query db [(str "select * from " (name table) " where " clause)]))

(defn id->name [db table id]
  (->> (all-where db table (str "id=" id))
       first
       :name))

(defn distinct-by-column [db column table]
  (map column (jdbc/query db [(str "select distinct(" (name column) ")"
                                   " from " (name table))])))

(defn eid->name [db eid]
  (-> (jdbc/query db ["select name from exercise where exerciseid = ?" eid])
      first
      :name))

(defn indexed-exercises [db]
  (jdbc/query db ["select distinct(name), exerciseid
                   from exercise
                   order by name"]))

(defn squash-opponents [db]
  (jdbc/query db ["select distinct(opponent)
                   from squash
                   order BY opponent"]))

(defn new-exerciseid [db]
  (if-let [id (-> (jdbc/query db ["select max(exerciseid) from exercise"])
                  first
                  :max)]
    (inc id)
    1))

(defn oldest-untouched-exercises [db]
  (jdbc/query db
              ["select name, max(day)
                from exercise
                where exerciseid not in(12, 19, 9, 4, 25)
                group by name
                order by max;"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modify

(defn update-row [db table update-map id]
  (jdbc/update! db table update-map ["id=?" id]))

(defn delete-row [db table id]
  (jdbc/delete! db table ["id=?" id]))

(defn insert-row [db table insert-map]
  (jdbc/insert! db table insert-map))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Migrations

;; 2019-08-13 Trainer -> Finances

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

;; 2019-08-14 Add exercise id

(defn update-indices [db]
  (doseq [[ename i] (map vector (distinct-by-column db :name :exercise) (range))]
    (doseq [e (all-where db :exercise (format "name = '%s'" ename))]
      (update-row db :exercise {:exerciseid i} (:id e)))))
