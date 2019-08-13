(ns fitness.html
  "HTML components")

(defn navbar []
  [:div.mui-appbar
   [:table {:width "100%"}
    [:tr {:style "vertical-align:middle;"}
     [:td.mui--appbar-height [:button [:a {:href "/"} "Workout"]]]
     [:td.mui--appbar-height [:button [:a {:href "/history"} "History"]]]
     [:td.mui--appbar-height [:button [:a {:href "/squash"} "Squash"]]]]]])
