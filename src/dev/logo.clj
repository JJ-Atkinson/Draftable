(ns logo
  (:require
    [clojure.java.shell :refer [sh]]))

(defn W2 [w a th]
  (/ w (+ 1 (/ (Math/tan a) (Math/tan th)))))

(defn H [a w2]
  (* w2 (Math/tan a)))

(defn logo-points [w h turn-degrees]
  (let [th (Math/toRadians 70)]
    (concat
      (for [a (map #(Math/toRadians %)
                (range 20 70 turn-degrees))]
        (let [w2 (W2 w a th)]
          [[0 h] [w2 (- h (H a w2))]]))
      (for [a (map #(Math/toRadians %)
                (range 20 70 turn-degrees))]
        (let [w2 (W2 w a th)
              w1 (- w w2)]
          [[w h] [w1 (- h (H a w2))]])))))

(defn line [[[x1 y1] [x2 y2]]]
  (format "<line class=\"stick\" x1=\"%s\" y1=\"%s\" x2=\"%s\" y2=\"%s\" />" x1 y1 x2 y2))

(defn create-logo []
  (spit "logo.html"
    (format "<!DOCTYPE html><html><body><svg width=\"300\" height=\"300\"><style type=\"text/css\">.stick { stroke:#765373; stroke-width:3; } </style>%s</svg></body></html>"
      (apply str (mapv line (logo-points 100 100 10))))))

(do (create-logo)
  (sh "open" "logo.html"))
