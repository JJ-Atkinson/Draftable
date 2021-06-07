(ns logo
  (:require
    [clojure.string :as str]
    [clojure.java.shell :refer [sh]]))

(defn W2 [w a th]
  (/ w (+ 1 (/ (Math/tan a) (Math/tan th)))))

(defn H [a w2]
  (* w2 (Math/tan a)))

(def colors
  ["#7969e1" "#7263d4" "#6b5dc8" "#6457bb" "#5d51ae" "#504695"])

(defn inc-points [n [color [x1 y1] [x2 y2]]]
  [color
   [(+ n x1) (+ n y1)]
   [(+ n x2) (+ n y2)]])

(defn logo-points [w h turn-degrees]
  (let [thd 60
        th (Math/toRadians thd)
        range-a (map vector
                  colors
                  (map #(Math/toRadians %)
                    (range (* 2 turn-degrees) (+ thd turn-degrees) turn-degrees)))]
    (mapv (partial inc-points 2)
      (interleave
        (for [[color a] range-a]
          (let [w2 (W2 w a th)]
            [color [0 h] [w2 (- h (H a w2))]]))
        (for [[color a] range-a]
          (let [w2 (W2 w a th)
                w1 (- w w2)]
            [color [w h] [w1 (- h (H a w2))]]))))))

(defn polyline [points]
  (format "<polyline stroke=\"%s\" stroke-width=\"3\" fill=\"none\" stroke-linecap=\"round\" stroke-linejoin=\"miter\" points=\"%s\">" (last colors) (str/join " " (flatten points))))

(defn line [[color [x1 y1] [x2 y2]]]
  (format "<line class=\"stick\" style=\"stroke:%s;\" x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" />" color x1 y1 x2 y2))

(defn create-logo [w h turn]
  (spit "logo.html"
    (format "<!DOCTYPE html><html><body><svg width=\"%s\" height=\"%s\"><style type=\"text/css\">.stick { stroke-width:3; } </style>%s%s</svg></body></html>"
      (+ 5 w) (+ 5 h)
      (str/join (mapv line (logo-points w h turn)))
      (polyline (mapv (let [n 2] (fn [[x y]] [(+ n x) (+ n y)]))
                  [[0 h] [(/ w 2) (- (H 60 (/ 100 2)) 3)] [w h]])))))

(comment
  (do (logo-points 100 100 10)
    (create-logo 100 100 10)
    (sh "open" "-g" "logo.html")))
