(ns fitness.util
  "Namespace for utilities"
  (:require [clojure.string :as string]
            [clj-time.core :as clj-time]
            [clj-time.format :as format])
  (:import [java.time LocalDateTime LocalDate]
           [java.sql Date Timestamp]))

(defn stringify [k] (-> k name string/capitalize))

(defn parse-int [s]
  {:pre [(or (integer? s) (re-matches #"-?\d+" s))]}
  (if (integer? s)
    s
    (Integer/parseInt s)))

(defn parse-float [s]
  {:pre [(or (float? s) (re-matches #"(-?\d+\.\d+|-?\d+)" s))]}
  (try
    (Double/parseDouble s)))

(defn format-date [date]
  (format/unparse (format/formatters :date) date))

(defn format-today []
  (format-date (clj-time/now)))
