(ns mc.tick
  (:require ["pixi" :as pixi]
            [clojure.math :refer [sin cos atan2 PI]]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-height game-width]]
            [mc.sprites :refer [sprite]]))


(def margin 10)
(def min-x (- margin))
(def max-x (+ game-width margin))
(def min-y (- margin))
(def max-y (+ game-height margin))
(def game-center-x (/ game-width 2))
(def half-PI (* PI 0.5))

(defprotocol ISprite
  (init [this])
  (tick [this])
  (dispose [this]))


(deftype Sprite [sprite
                 ^:unsynchronized-mutable x ^:unsynchronized-mutable y
                 ^:unsynchronized-mutable d d'
                 ^:unsynchronized-mutable v v' max-v
                 ^:unsynchronized-mutable s s' max-s
                 data]
  ISprite
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
          a      (- next-d half-PI)
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
  (dispose [_this]
    (j/call sprite :removeFromParent)
    (j/call sprite :destroy (j/obj :children true))
    nil))


(def Inf ##Inf) ; Indent breaks with ##Inf


(def default-sprite-opts {:d      0
                          :d'     0
                          :v      0
                          :v'     0
                          :max-v  Inf
                          :s      1
                          :s'     0
                          :max-s  Inf
                          :limit? (constantly false)})


(defn newSprite [opts]
  (let [opts (merge default-sprite-opts opts)]
    (-> (->Sprite (:sprite opts) 
                  (:x opts) (:y opts) 
                  (:d opts) (:d' opts) 
                  (:v opts) (:v' opts) (:max-v opts) 
                  (:s opts) (:s' opts) (:max-s opts) 
                opts)
      (init))))


(defn make-defence-missile [parent target-x target-y]
  (let [delta-x (- target-x game-center-x)
        delta-y (- game-height target-y)
        d       (atan2 delta-x delta-y)
        limit?  (if (> (abs delta-x) (abs delta-y))
                  (if (pos? delta-x)
                    (fn [^Sprite sprite _state _time] (> (.-x sprite) target-x))
                    (fn [^Sprite sprite _state _time] (< (.-x sprite) target-x)))
                  (fn [^Sprite sprite _state _time] (< (.-y sprite) target-y)))]
    (newSprite {:parent   parent
                :sprite   (sprite :defence-arrow)
                :kind     :defence-missile
                :x        500
                :y        500
                :d        d
                :v        0
                :v'       0.02
                :max-v    2.0
                :target-x target-x
                :target-y target-y
                :limit?   limit?})))


(defn make-defence-explosion [parent x y]
  (newSprite {:parent parent
              :sprite (sprite :defence-explosion)
              :kind   :defence-explosion
              :x      x
              :y      y
              :s      0.1
              :s'     0.02
              :limit? (fn [^Sprite sprite _state _time] (> (.-s sprite) 3))}))

(defn click [state time x y]
  (update state :game update :objects conj (make-defence-missile (-> state :stage) x y)))


(defn limit-reached [^Sprite sprite]
  (case (-> sprite .-data :kind)
    :defence-missile (do (dispose sprite)
                         (let [data (.-data sprite)]
                           (make-defence-explosion (-> data :parent)
                                                   (-> data :target-x)
                                                   (-> data :target-y))))
    :defence-explosion (dispose sprite)))


(defn update-object [state time ^Sprite sprite]
  (tick sprite)
  (cond
    (or (not (< min-x (.-x sprite) max-x))
        (not (< min-y (.-y sprite) max-y)))
    (dispose sprite)

    (let [limit? (-> sprite .-data :limit?)]
      (limit? sprite))
    (limit-reached sprite)

    :else
    sprite))


(defn update-objects [objects state time]
  (let [update-object (partial update-object state time)]
    (-> (keep update-object objects)
        (doall))))


(defn update-game [state time]
  (-> state
      (update-in [:game :objects] update-objects state time)))
