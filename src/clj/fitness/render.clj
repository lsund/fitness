(ns fitness.render
  "Namespace for rendering views"
  (:require
   [fitness.db :as db]
   [taoensso.timbre :as logging]
   [hiccup.form :refer [form-to]]
   [hiccup.page :refer [html5 include-css include-js]]
   [fitness.util :as util]
   [fitness.html :as html]))


(defn index
  [db-data config]
  (html5
   [:head
    [:title "Fixme"]]
   [:body
    [:h1 "Hello. Fixme please"]
    (apply include-js (:javascripts config))
    (apply include-css (:styles config))]))

(def not-found (html5 "not found"))
