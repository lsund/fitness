(ns fitness.handler
  "Route handler"
  (:require [compojure.core :refer [GET POST routes]]
            [fitness.util :as util]
            [compojure.route :as route]
            [ring.middleware.json :refer [wrap-json-params]]
            [ring.util.response :refer [redirect]]
            [ring.middleware.defaults :refer [site-defaults wrap-defaults]]
            [ring.middleware.keyword-params :refer [wrap-keyword-params]]
            [ring.middleware.params :refer [wrap-params]]
            [fitness.render :as render]
            [fitness.db :as db]))

;; Instead of trying to monitor exact growth, Show the weight max and
;; average reps + weight and simply let the user enter the data
;;
;; Instead of plans, have a scroll list of exercises, the user adds it
;; done exercises to the session and then clicks complete
;;
;; Plans are just lists, like suggestions: you don't need to follow them
;; and no complicated skip/increment/decrement etc.
;;

(defn wrap-session-add-exercise [{:keys [db session params]}]
  (let [{:keys [new-name eid]}
        params

        new-exercise?
        (= (util/parse-int eid) -1)

        new-exercise-count
        (if new-exercise?
          (inc (or (:new-exercise-count session) 0))
          (:new-exercise-count session))

        [name eid]
        (if new-exercise?
          [new-name (db/new-exerciseid db new-exercise-count)]
          [(db/eid->name db (util/parse-int eid)) eid])

        exercise
        (-> params
            (dissoc :eid :new-name)
            (assoc :name name)
            (assoc :exerciseid eid)
            (util/update-keys [:exerciseid :reps :sets :weight :level :distance]
                              util/parse-int)
            (update :duration util/duration-str->int)
            (util/update-all util/empty->nil))

        new-session
        (assoc session
               :exercises
               (conj (:exercises session []) exercise)
               :new-exercise-count
               new-exercise-count)]
    (-> (redirect "/")
        (assoc :session new-session))))

(defn- app-routes [{:keys [db] :as config}]
  (routes
   (GET "/" {:keys [session]}
        (render/workout {:config config
                         :exercises (db/all db :exercise)
                         :indexed-exercises (db/indexed-exercises db)
                         :session-exercises (:exercises session)}))
   (GET "/history" []
        (render/history {:config config
                         :exercises (db/all db :exercise)}))
   (POST "/add" resp (-> resp
                         (assoc :db db)
                         wrap-session-add-exercise))
   (POST "/save" {:keys [session params]}
         (doseq [x (:exercises session)]
           (db/insert-row db :exercise (assoc x :day (util/today))))
         (-> (redirect "/")
             (assoc :session nil)))
   ;; Imports
   (POST "/import-trainer-exercise" req
         (db/import-trainer-exercise db (get-in req [:params :data]))
         "Exercises imported.")
   (POST "/import-trainer-squash" req
         (db/import-trainer-squash db (get-in req [:params :data]))
         "Squash records imported.")
   (route/resources "/")
   (route/not-found render/not-found)))

(defn new-handler [config]
  (-> (app-routes config)
      (wrap-keyword-params)
      (wrap-params)
      (wrap-json-params)
      (wrap-defaults
       (-> site-defaults (assoc-in [:security :anti-forgery] false)))))
