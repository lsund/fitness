(ns fitness.config
  "Configuration handling"
  (:require [clojure.edn :as edn]))

(defn from-file []
  (edn/read-string (slurp "resources/edn/config.edn")))
