(ns starter.core
  (:require [reagent.core :as r]))

(defn d [ang-rad] (* ang-rad (/ js/Math.PI 180)))
(def width 300)
(defonce angle-between (r/atom 10))
(defonce outter-angle (r/atom 60))
(defonce angle-count (r/atom 5))


;;       x-intercept
;;      / \
;;angl /___\  angr
;;  shared-width

(defn x-intercept [shared-width angl angr]
  (/ shared-width (+ 1 (/ (Math/tan (d angl)) (Math/tan (d angr))))))

;;         /|
;;        / | height
;;angle  /__|
;;      width
(defn height [angle width]
  (* width (Math/tan (d angle))))

(defn build-line-pair [angle]
  (let [x1 (x-intercept width @outter-angle angle)
        x2 (- width x1)
        h  (- width (height angle x2))]
    (list
      [:line {:class "stick"
              :fill  "red"
              :x1    0 :y1 width
              :x2    x2 :y2 h}]
      [:line {:class "stick"
              :fill  "red"
              :x1    width :y1 width
              :x2    x1 :y2 h}])))

(defn lines []
  (mapcat
    (fn [angle]
      (build-line-pair angle))
    (take @angle-count (iterate #(- % @angle-between) @outter-angle))))

(defn slider [atom text min max round?]
  [:div
   [:text (str text " " @atom)]
   [:input {:type      "range" :value @atom :min min :max max
            :style     {:width "100%"}
            :on-change (fn [e]
                         (reset! atom
                           (let [v (.-value (.-target e))]
                             (if round?
                               (js/Math.round v)
                               v))))}]])

(defn app []
  [:body
   [:style {:type "text/css"}
    ".stick {stroke: red; stroke-width: 3;}"]
   (slider angle-between "ang between" 4 15 false)
   (slider angle-count "ang count" 3 8 true)
   (slider outter-angle "outter" 40 80 false)
   [:svg {:style {:border     "none"
                  :background "white"
                  :width      (str width)
                  :height     (str width)}}
    (lines)
    #_[:path {:stroke-width 12
              :stroke       "white"
              :fill         "none"
              :d            "M 30,40 C 100,40 50,110 120,110"}]]])

(defn stop []
  (js/console.log "Stopping..."))

(defn start []
  (js/console.log "Starting...")
  (r/render [app]
    (.getElementById js/document "app")))

(defn ^:export init []
  (start))
