(ns mc.tick
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-height game-width]]
            [mc.sprites :refer [sprite]]))


(def margin 10)
(def min-x (- margin))
(def max-x (+ game-width margin))
(def min-y (- margin))
(def max-y (+ game-height margin))


(defprotocol ISprite
  (init [this] [this parent])
  (tick [this])
  (dispose [this]))


(deftype Sprite [sprite
                 kind
                 created
                 ^:unsynchronized-mutable x
                 ^:unsynchronized-mutable y
                 ^:unsynchronized-mutable d ^:unsynchronized-mutable d'
                 ^:unsynchronized-mutable v ^:unsynchronized-mutable v'
                 ^:unsynchronized-mutable s ^:unsynchronized-mutable s']
  ISprite
  (init [this]
    (init this nil))
  (init [this parent]
    (j/assoc! sprite :rotation d)
    (j/call-in sprite [:position :set] x y)
    (j/call-in sprite [:scale :set] s s)
    (when parent
      (j/call parent :addChild sprite))
    this)
  (tick [this]
    (let [next-d (+ d d')
          next-v (+ v v')
          next-s (+ s s')
          next-x (+ x (* next-v (js/Math.cos next-d)))
          next-y (+ y (* next-v (js/Math.sin next-d)))]
      (set! (.-v this) next-v)
      (when (not= d next-d)
        (j/assoc! sprite :rotation next-d)
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
  (dispose [_this]
    (j/call sprite :removeFromParent)
    (j/call sprite :destroy (j/obj :children true))
    nil))


(defn make-circle [parent time x y]
  (init (->Sprite (sprite :circle)
                  :circle
                  time
                  x y
                  (* js/Math.PI 0.5) 0
                  0.01 0.01
                  0.6 0)
        parent))


(defn make-arrow [parent time x y]
  (init (->Sprite. (sprite :arrow)
                   :arrow
                   time
                   x y
                   (* js/Math.PI -0.25) 0.02
                   0.01 0.01
                   1 0)
        parent))


(defn click [state time x y {:keys [shift?]}]
  (update state :game update :objects conj (if shift?
                                             (make-arrow (-> state :stage) time x y)
                                             (make-circle (-> state :stage) time x y))))


(defn update-object [state time ^Sprite object]
  (let [^Sprite object (tick object)]
    (if (and (< min-x (.-x object) max-x)
             (< min-y (.-y object) max-y))
      object
      (dispose object))))


(defn update-objects [objects state time]
  (let [update-object (partial update-object state time)]
    (doall (keep update-object objects))))


(defn update-game [state time]
  (-> state
      (update-in [:game :objects] update-objects state time)))
