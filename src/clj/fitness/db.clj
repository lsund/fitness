(ns fitness.db
  "Database component"
  (:require [clojure.java.jdbc :as jdbc]
            [clojure.edn :as edn]
            [clj-time.coerce :refer [to-sql-date]]
            [com.stuartsierra.component :as c]))

(defn pg-db [config]
  {:dbtype "postgresql"
   :dbname (:name config)
   :user "postgres"})

(def pg-db-val (pg-db {:name "fitness"}))

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

(defn update-row [db table update-map id]
  (jdbc/update! db table update-map ["id=?" id]))

(defn delete-row [db table id]
  (jdbc/delete! db table ["id=?" id]))

(defn insert-row [db table insert-map]
  (jdbc/insert! db table insert-map))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;  Trainer -> Finances

(defn parse-int [s]
  {:pre [(or (integer? s) (re-matches #"-?\d+" s))]}
  (if (integer? s)
    s
    (Integer/parseInt s)))

(defn duration-str->int [x]
  (when x
    (if-let [[_ minutes _ seconds _] (re-matches #"(\d+)(m)(\d+)(s)" x)]
      (+ (* (parse-int minutes) 60) (parse-int seconds))
      (when-let [[_ number unit] (re-matches #"(\d+)([ms])" x)]
        (case unit
          "m" (* (parse-int number) 60)
          "s" (parse-int number))))))

(defn deserialize-edn [db file]
  (-> file
      slurp
      edn/read-string))

(defn migrate-trainer-exercise [db data]
  (doseq [record data]
    (insert-row db :exercise (-> record
                                 (update :day to-sql-date)
                                 (update :duration duration-str->int)))))
