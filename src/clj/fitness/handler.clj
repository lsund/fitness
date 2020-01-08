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

(defn make-exercise [db params]
  (let [{:keys [new-name eid]}
        params

        new-exercise?
        (not-empty new-name)

        [name eid]
        (if new-exercise?
          [new-name (db/new-exerciseid db)]
          [(db/eid->name db (util/parse-int eid)) eid])

        exercise
        (-> params
            (dissoc :eid :new-name :new-check)
            (assoc :name name)
            (assoc :exerciseid eid)
            (assoc :active true)
            (assoc :day (util/today))
            (util/update-all util/empty->nil)
            (util/update-keys [:exerciseid :reps :sets :weight :level :distance :lowpulse :highpulse]
                              util/parse-int)
            (update :duration util/duration-str->int))]
    exercise))

(defn- app-routes [{:keys [db] :as config}]
  (routes
   (GET "/" m
        (render/workout {:config
                         config

                         :oldest-untouched-exercises
                         (db/oldest-untouched-exercises db)

                         :daily-standard-exercises
                         (db/daily-standard-exercises db)

                         :historic-exercises
                         (db/all-join-another db :exercise :exerciseid_name :exerciseid :name)

                         :indexed-exercises
                         (sort-by :name (db/all db :exerciseid_name))

                         :params (:params m)}))
   (POST "/add" {:keys [params]}
         (let [exercise (make-exercise db params)]
           (db/insert-row db :exercise (dissoc exercise :name))
           (db/insert-unique-exercise db :exerciseid_name exercise)
           ;; TODO remove this
           (db/hack-update-standard db))
         (redirect "/"))
   (GET "/squash" []
        (render/squash {:config config
                        :matches (db/all db :squash)
                        :opponents (db/squash-opponents db)}))
   (POST "/squash/add" [opponent myscore opponentscore new-opponent day]
         (db/insert-row db
                        :squash
                        {:myscore
                         (util/parse-int myscore)

                         :opponentscore
                         (util/parse-int opponentscore)

                         :day
                         (if (empty? day)
                           (util/today)
                           (util/->localdate day))

                         :opponent
                         (if (= opponent "New")
                           new-opponent
                           opponent)})
         (redirect "/squash"))
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
