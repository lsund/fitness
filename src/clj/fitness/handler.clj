(ns fitness.handler
  "Route handler"
  (:require [compojure.core :refer [GET POST routes]]
            [compojure.route :as route]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [fitness.render :as render]
            [fitness.db :as db]))

;; Datbase changes:
;;
;; One type of exercise with nullable fields
;; Exercise (Id, Name, Duration, Distance, Lowpulse, Highpulse,
;;           level, sets, reps, weight)
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

(defn today [] (java.time.LocalDateTime/now))

(defn parse-int [s]
  {:pre [(or (integer? s) (re-matches #"-?\d+" s))]}
  (if (integer? s)
    s
    (Integer/parseInt s)))

(defn update-keys [m ks f]
  (reduce #(update %1 %2 f) m ks))

(defn update-all [m f]
  (reduce #(update %1 %2 f) m (keys m)))

(defn empty->nil [x]
  (when (or (not (string? x)) (not-empty x))
    x))

(defn wrap-add-exercise [{:keys [db session params]}]
  (let [{:keys [id sets reps weight
                duration distance lowpulse highpulse level]
         :as exercise-params}
        params

        exercise
        (-> exercise-params
            (assoc :name
                   (db/id->name db :exercise id))
            (dissoc :id)
            (update-keys [:reps :sets :weight] parse-int)
            (update-all empty->nil))

        new-session
        (assoc session
               :exercises
               (conj (:exercises session []) exercise))]
    (-> (redirect "/")
        (assoc :session new-session))))

(defn- app-routes [{:keys [db] :as config}]
  (routes
   (GET "/" {:keys [session]}
        (render/workout {:config config
                         :exercises (db/all db :exercise)
                         :session-exercises (:exercises session)}))
   (POST "/add" resp (-> resp
                         (assoc :db db)
                         wrap-add-exercise))
   (POST "/save" {:keys [session params]}
         (doseq [x (:exercises session)]
           (db/insert-row db :exercise (assoc x :day (today))))
         (-> (redirect "/")
             (assoc :session nil)))
   (route/resources "/")
   (route/not-found render/not-found)))

(defn new-handler [config]
  (-> (app-routes config)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-defaults
       (-> site-defaults (assoc-in [:security :anti-forgery] false)))))
