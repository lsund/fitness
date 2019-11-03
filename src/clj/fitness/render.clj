(ns fitness.render
  "Namespace for rendering views"
  (:require [hiccup.form :refer [form-to]]
            [hiccup.page :refer [html5 include-css include-js]]
            [fitness.util :as util]
            [fitness.html :as html]))

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

(defn textfield [label input]
  [:div.mui-textfield
   [:label label]
   input])

(defn workout [{:keys [config indexed-exercises exercises]}]
  (html5
   [:head
    [:title "Workout"]]
   [:body
    (html/navbar)
    (form-to {:class "mui-form"}
             [:post "/add"]
             [:h3 "Add weightlift"]
             [:div.mui-radio
              [:label]
              [:div.mui-radio
               [:input {:type :checkbox
                        :name "new-check"
                        :id "newCheck"}]
               "New"]]
             [:div.mui-select
              {:id "selectField"}
              [:select {:name "eid"}
               (for [x indexed-exercises]
                 [:option {:value (:exerciseid x)} (:name x)])]]
             [:div.mui-textfield
              {:id "newField"}
              [:input {:name "new-name"
                       :type :text
                       :placeholder "New fooname"}]]
             (textfield "Sets"
                        [:input.mui-number {:name "sets"
                                            :type :number
                                            :value 3
                                            :min "0"}])
             (textfield "Reps"
                        [:input {:name "reps" :type :number :min "0"}])
             (textfield "Weight"
                        [:input {:name "weight" :type :number :min "0"}])
             [:h3 "Add Cardio"]
             (textfield "Duration"
                        [:input {:name "duration" :type :text}])
             (textfield "Distance"
                        [:input {:name "distance" :type :number :min "0"}])
             (textfield "Lowpulse"
                        [:td [:input {:name "lowpulse" :type :number :min "0"}]])
             (textfield "Highpulse"
                        [:input {:name "highpulse" :type :number :min "0"}])
             (textfield "Level"
                        [:input {:name "level" :type :number :min "0"}])
             [:div
              [:input.mui-btn.mui-btn--raised {:type :submit :value "Add"}]])
    [:div
     (let [grouped (->> exercises (group-by :day) sort reverse)]
       (for [[x es] grouped]
         [:div
          [:p x]
          [:ul
           (for [e es]
             [:li (exercise->str e)])]]))]
    (apply include-css (:styles config))
    (apply include-js (:javascripts config))]))

(defn match->str [{:keys [day opponent myscore opponentscore]}]
  (str day ": " opponent " " myscore "-" opponentscore))

(defn squash [{:keys [opponents matches]}]
  (html5
   [:head
    [:title "Squash Results"]]
   [:body
    (html/navbar)
    (form-to
     [:post "/squash/add"]
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
