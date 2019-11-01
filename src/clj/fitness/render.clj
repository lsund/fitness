(ns fitness.render
  "Namespace for rendering views"
  (:require [hiccup.form :refer [form-to]]
            [hiccup.page :refer [html5 include-css]]
            [fitness.util :as util]
            [fitness.html :as html]))

(defn weight-table []
  [:table
   [:thead
    [:tr
     [:th "sets (#)"]
     [:th "reps (#)"]
     [:th "weight (kg)"]]]
   [:tbody
    [:tr
     [:td [:input {:name "sets" :type :number :value 3 :min "0"}]]
     [:td [:input {:name "reps" :type :number :min "0"}]]
     [:td [:input {:name "weight" :type :number :min "0"}]]]]])

(defn cardio-table []
  [:table
   [:thead
    [:tr
     [:th "duration (time)"]
     [:th "distance (m)"]
     [:th "highpulse (bpm)"]
     [:th "lowpulse (bpm)"]
     [:th "level (#)"]]]
   [:tbody
    [:tr
     [:td [:input {:name "duration" :type :text}]]
     [:td [:input {:name "distance" :type :number :min "0"}]]
     [:td [:input {:name "lowpulse" :type :number :min "0"}]]
     [:td [:input {:name "highpulse" :type :number :min "0"}]]
     [:td [:input {:name "level" :type :number :min "0"}]]]]])

(defn squash-table []
  [:table
   [:thead
    [:tr
     [:th "my score (#)"]
     [:th "opponent score (#)"]]]
   [:tbody
    [:tr
     [:td [:input {:name "myscore" :type :number :min "0"}]]
     [:td [:input {:name "opponentscore" :type :number :min "0"}]]]]])

(defn exercise->str [{:keys [name reps weight sets duration distance level]}]
  (cond
    (and reps weight sets)
    (str name ": " reps "/" weight "x" sets)

    (and duration distance level)
    (str name ": "
         "level " level ", "
         distance "m, " (util/int->duration-str duration))))

(defn workout [{:keys [config  indexed-exercises]}]
  (html5
   [:head
    [:title "Workout"]]
   [:body
    (html/navbar)
    (form-to [:post "/add"]
             [:select {:name "eid"}
              (for [x (conj indexed-exercises {:exerciseid -1 :name "New"})]
                [:option {:value (:exerciseid x)} (:name x)])]
             [:input {:type :text
                      :name "new-name"
                      :placeholder "New name"}]
             (weight-table)
             (cardio-table)
             [:div
              [:input {:type :submit :value "Add"}]])
    (apply include-css (:styles config))]))

(defn history [{:keys [exercises]}]
  (html5
   [:head
    [:title "History"]]
   [:body
    (html/navbar)
    (let [grouped (->> exercises (group-by :day) sort reverse)]
      (for [[x es] grouped]
        [:div
         [:p x]
         [:ul
          (for [e es]
            [:li (exercise->str e)])]]))]))

(defn match->str [{:keys [day opponent myscore opponentscore]}]
  (str day ": " opponent " " myscore "-" opponentscore))

(defn squash [{:keys [opponents matches]}]
  (html5
   [:head
    [:title "History"]]
   [:body
    (html/navbar)
    (form-to [:post "/squash/add"]
             [:select {:name "opponent"}
              (for [x (conj opponents {:opponent "New"})]
                [:option {:value (:opponent x)} (:opponent x)])]
             [:input {:type :text
                      :name "new-opponent"
                      :placeholder "New opponent"}]
             [:input {:type :date :name "day"}]
             [:input {:type :submit :value "Add"}]
             (squash-table))
    [:ul
     (for [match (reverse (sort-by :day matches))]
       [:li (match->str match)])]]))

(def not-found (html5 "not found"))
