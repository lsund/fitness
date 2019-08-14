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

(defn duration-str->int [x]
  (when x
    (if-let [[_ minutes _ seconds _] (re-matches #"(\d+)(m)(\d+)(s)" x)]
      (+ (* (parse-int minutes) 60) (parse-int seconds))
      (when-let [[_ number unit] (re-matches #"(\d+)([ms])" x)]
        (case unit
          "m" (* (parse-int number) 60)
          "s" (parse-int number))))))

(defn update-keys [m ks f]
  (reduce #(update %1 %2 f) m ks))

(defn update-all [m f]
  (reduce #(update %1 %2 f) m (keys m)))

(defn empty->nil [x]
  (when (or (not (string? x)) (not-empty x))
    x))

(defn today [] (java.time.LocalDateTime/now))
