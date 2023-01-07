(ns mc.sprite
  (:require [clojure.math :refer [sin cos]]
            [applied-science.js-interop :as j]
            [mc.object :refer [IObject init]]
            [mc.util :as u]))


(deftype Sprite [kind
                 sprite
                 ^:unsynchronized-mutable x ^:unsynchronized-mutable y
                 ^:unsynchronized-mutable d d'
                 ^:unsynchronized-mutable v v' max-v
                 ^:unsynchronized-mutable s s' max-s
                 valid?
                 data]
  IObject
  (kind [_this]
    kind)
  (init [this]
    (j/assoc! sprite :rotation d)
    (j/call-in sprite [:position :set] x y)
    (j/call-in sprite [:scale :set] s s)
    (when-let [parent (:parent data)]
      (j/call parent :addChild sprite))
    this)
  (tick [this]
    (let [next-v (min (+ v v') max-v)
          next-s (min (+ s s') max-s)
          next-d (+ d d')
          a      (- next-d u/half-PI)
          next-x (+ x (* next-v (cos a)))
          next-y (+ y (* next-v (sin a)))]
      (set! (.-v this) next-v)
      (when (not= d next-d)
        (j/assoc! sprite :rotation a)
        (set! (.-d this) next-d))
      (when (or (not= x next-x)
                (not= y next-y))
        (j/call-in sprite [:position :set] next-x next-y)
        (set! (.-x this) next-x)
        (set! (.-y this) next-y))
      (when (not= s next-s)
        (j/call-in sprite [:scale :set] next-s next-s)
        (set! (.-s this) next-s))
      this))
  (valid? [this]
    (valid? this))
  (dispose [_this]
    (j/call sprite :removeFromParent)
    (j/call sprite :destroy (j/obj :children true))
    nil))

(defn sprite-in-bounds [^Sprite sprite]
  (and (< u/min-x (.-x sprite) u/max-x)
       (< u/min-y (.-y sprite) u/max-y)))


(def default-sprite-opts {:d      0
                          :d'     0
                          :v      0
                          :v'     0
                          :max-v  u/Inf
                          :s      1
                          :s'     0
                          :max-s  u/Inf
                          :valid? sprite-in-bounds})


(defn newSprite [opts]
  (let [opts (merge default-sprite-opts opts)]
    (-> (->Sprite (:kind opts)
                  (:sprite opts)
                  (:x opts) (:y opts)
                  (:d opts) (:d' opts)
                  (:v opts) (:v' opts) (:max-v opts)
                  (:s opts) (:s' opts) (:max-s opts)
                  (:valid? opts)
                  opts)
        (init))))
