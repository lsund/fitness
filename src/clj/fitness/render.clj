(ns fitness.render
  "Namespace for rendering views"
  (:require
   [fitness.db :as db]
   [taoensso.timbre :as logging]
   [hiccup.form :refer [form-to]]
   [hiccup.page :refer [html5 include-css include-js]]
   [fitness.util :as util]
   [fitness.html :as html]))

(defn weight-table []
  [:table
   [:thead
    [:tr
     [:th "sets"]
     [:th "reps"]
     [:th "weight"]]]
   [:tbody
    [:tr
     [:td [:input {:name "sets" :type :number :value 3 :min "0"}]]
     [:td [:input {:name "reps" :type :number :min "0"}]]
     [:td [:input {:name "weight" :type :number :min "0"}]]]]])

(defn cardio-table []
  [:table
   [:thead
    [:tr
     [:th "duration"]
     [:th "distance"]
     [:th "highpulse"]
     [:th "lowpulse"]
     [:th "level"]]]
   [:tbody
    [:tr
     [:td [:input {:name "duration" :type :number :min "0"}]]
     [:td [:input {:name "distance" :type :number :min "0"}]]
     [:td [:input {:name "lowpulse" :type :number :min "0"}]]
     [:td [:input {:name "highpulse" :type :number :min "0"}]]
     [:td [:input {:name "level" :type :number :min "0"}]]]]])

(defn exercise->str [{:keys [name reps sets weight]}]
  (str name ": " reps "/" weight "x" sets))

(defn index
  [{:keys [config session-exercises exercises]}]
  (html5
   [:head
    [:title "Workout"]]
   [:body
    [:ul
     (for [x session-exercises]
       [:li (exercise->str x)])]
    (form-to [:post "/add"]
             [:select {:name "id"}
              (for [x exercises]
                [:option {:value (:id x)} (:name x)])]
             (weight-table)
             (cardio-table)
             [:input {:type :submit :value "Add"}])
    (form-to [:post "/save"]
             [:input {:type :submit :value "Save training session"}])
    (apply include-css (:styles config))]))

(def not-found (html5 "not found"))
