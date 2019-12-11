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

(defn exercise->str [{:keys [name reps weight sets duration distance level highpulse lowpulse] :as e}]
  (cond
    (and reps weight sets)
    (str name ": " reps "/" weight "x" sets)

    (and duration distance level)
    (str name ": "
         "level " level ", "
         distance "m, " (util/int->duration-str duration))

    (and duration highpulse lowpulse)
    (str name ": "
         "highpulse " highpulse ", "
         "lowpulse " lowpulse ", "
         (util/int->duration-str duration))

    (and distance level)
    (str name ": "
         "level " level ", "
         distance "m")

    :else
    (throw (Exception. (str "Cannot parse exercise: " e)))))

(defn textfield [label input]
  [:div.mui-textfield
   [:label label]
   input])

(defn workout [{:keys [config
                       indexed-exercises
                       historic-exercises
                       oldest-untouched-exercises]}]
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
               "Register new exercise"]]
             [:div.mui-select
              {:id "selectField"}
              [:select {:name "eid"}
               (for [x indexed-exercises]
                 [:option {:value (:exerciseid x)} (:name x)])]]
             [:div.mui-textfield
              {:id "newField"}
              [:input#newFieldText {:name "new-name"
                                    :type :text
                                    :placeholder "New"}]]
             (textfield "Sets"
                        [:input.mui-number {:id "sets"
                                            :name "sets"
                                            :type :number
                                            :value 3
                                            :min "0"}])
             (textfield "Reps"
                        [:input {:id "reps"
                                 :name "reps"
                                 :type :number
                                 :min "0"}])
             (textfield "Weight"
                        [:input {:id "weight"
                                 :name "weight"
                                 :type :number
                                 :min "0"}])
             [:h3 "Add Cardio"]
             (textfield "Duration"
                        [:input {:id "duration"
                                 :name "duration"
                                 :type :text}])
             (textfield "Distance"
                        [:input {:id "distance"
                                 :name "distance"
                                 :type :number
                                 :min "0"}])
             (textfield "Lowpulse"
                        [:td [:input {:id "lowpulse"
                                      :name "lowpulse"
                                      :type :number
                                      :min "0"}]])
             (textfield "Highpulse"
                        [:input {:id "highpulse"
                                 :name "highpulse"
                                 :type :number
                                 :min "0"}])
             (textfield "Level"
                        [:input {:id "level"
                                 :name "level"
                                 :type :number
                                 :min "0"}])
             [:div
              [:input.mui-btn.mui-btn--raised {:type :submit :value "Add"}]])
    [:div
     [:h3 "Consider these"]
     [:ul
      (for [e oldest-untouched-exercises]
        [:li (exercise->str e)])]]
    [:div
     [:h3 "History"]
     (let [grouped (->> historic-exercises (group-by :day) sort reverse)]
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
