# template

This is a template for starting a clojure web project based on
stuartsierra/component

# Setup and Configure

Assume your new project is to be called `PROJECT_NAME`

1. In every file, the string `template` should be changed to `PROJECT_NAME`.
2. rename `src/{clj,cljs}/template` to `src/{clj,cljs}/PROJECT_NAME`
3. Launch a Clojure repl
4. Optionally specify the database configuration under `[:db :name]`
5. Configure new process:
    - Port
    - Database name
6. Start the webserver
7. GET http://localhost:port
8. Start hacking
