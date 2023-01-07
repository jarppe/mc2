(ns mc.util
  (:require [clojure.math :refer [PI]]))


(def game-width 1000)
(def game-height 500)
(def game-center-x (/ game-width 2))


(def margin 10)
(def min-x (- margin))
(def max-x (+ game-width margin))
(def min-y (- margin))
(def max-y (+ game-height margin))


(def Inf ##Inf) ; Indent breaks with ##Inf


(def half-PI (* PI 0.5))

(def ground-height 20)
(def ground-y (- game-height ground-height))

(def base-x game-center-x)
(def base-y ground-y)
