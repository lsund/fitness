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

(defn all-join-another [db table join-table join-column other]
  (jdbc/query db [(->> ["select %s.*, %s.%s from %s inner join %s on %s.%s = %s.%s"
                        table
                        join-table
                        other
                        table
                        join-table
                        table
                        join-column
                        join-table
                        join-column]
                       (map name)
                       (apply format))]))

(defn all-where [db table clause]
  (jdbc/query db [(str "select * from " (name table) " where " clause)]))

(defn distinct-by-column
  [db table column & join-columns]
  (jdbc/query db [(str "select distinct(" (name column) ")," (string/join "," (map name join-columns))
                       " from " (name table))]))

(defn eid->name [db eid]
  (-> (jdbc/query db ["select name from exerciseid_name where exerciseid = ?" eid])
      first
      :name))

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
              ["select  exerciseid_name.name,
                        main.exerciseid,
                        main.sets,
                        main.reps,
                        main.weight,
                        main.duration,
                        main.lowpulse,
                        main.highpulse,
                        main.level,
                        sub.maxd
                from (select exerciseid,
                             max(day) as maxd
                from exercise group by exerciseid) as sub
                join exercise main
                on main.exerciseid = sub.exerciseid
                and sub.maxd = main.day
                inner join exerciseid_name
                on exerciseid_name.exerciseid = main.exerciseid
                where active = true
                order by maxd, exerciseid_name.name
                limit 8;"]))

(defn daily-standard-exercises [db]
  (jdbc/query db
              ["select * from (
                    select exerciseid_name.name, e1.* from exercise as e1
                    inner join exerciseid_name
                    on exerciseid_name.exerciseid = e1.exerciseid
                    where not exists (select * from exercise as e2 where e2.exerciseid = e1.exerciseid and e2.day > e1.day)
                    and standard = true) as sub
                where sub.day != now()::date"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Modify

(defn update-row [db table update-map id]
  (jdbc/update! db table update-map ["id=?" id]))

(defn delete-row [db table id]
  (jdbc/delete! db table ["id=?" id]))

(defn insert-row [db table insert-map]
  (jdbc/insert! db table insert-map))

(defn insert-unique-exercise [db table insert-map]
  (jdbc/execute! db ["insert into exerciseid_name (exerciseid, name) values (?, ?) on conflict do nothing"
                     (:exerciseid insert-map)
                     (:name insert-map)]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Migrations

;; 2019-08-13 Trainer -> Fitness

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
  (doseq [[ename i] (map vector (map :name (distinct-by-column db :exercise :name)) (range))]
    (doseq [e (all-where db :exercise (format "name = '%s'" ename))]
      (update-row db :exercise {:exerciseid i} (:id e)))))

;; 2019-12-23 Add table exerciseid_name

(defn exercise->exerciseid_name [db]
  (doseq [e (distinct-by-column db :exercise :exerciseid :name)]
    (insert-row db :exerciseid_name e)))
