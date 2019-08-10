(ns fitness.handler
  "Route handler"
  (:require [compojure.core :refer [GET routes]]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [fitness.render :as render]))

;; Datbase changes:
;;
;; One type of exercise with nullable fields
;; Exercise (Id, Name, Duration, Distance, Lowpulse, Highpulse,
;;           level, sets, reps, weight, step)
;;
;; The exercise record denotes a done exercise. No need for multiple tables
;; all data can be derived from this table.
;;
;; Still need table for SquashMatch and SquashOpponent
;;
;; Instead of trying to monitor exact growth, Show the weight max and
;; average reps + weight and simply let the user enter the data
;;
;; Instead of plans, have a scroll list of exercises, the user adds it
;; done exercises to the session and then clicks complete
;;
;; Plans are just lists, like suggestions: you don't need to follow them
;; and no complicated skip/increment/decrement etc.
;;

(defn- app-routes [{:keys [db] :as config}]
  (routes
   (GET "/" []
        (render/index {} config))
   (route/resources "/")
   (route/not-found render/not-found)))

(defn new-handler [config]
  (-> (app-routes config)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-defaults
       (-> site-defaults (assoc-in [:security :anti-forgery] false)))))
