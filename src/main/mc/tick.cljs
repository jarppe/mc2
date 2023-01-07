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
(def Inf ##Inf) ; Indent breaks with ##Inf
(def half-PI (* PI 0.5))


(defprotocol IObject
  (kind [this])
  (init [this])
  (tick [this])
  (valid? [this])
  (dispose [this]))


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
  (valid? [this]
    (valid? this))
  (dispose [_this]
    (j/call sprite :removeFromParent)
    (j/call sprite :destroy (j/obj :children true))
    nil))


(deftype Trail [kind graphics ^IObject follow data]
  IObject
  (kind [_this]
    kind)
  (init [this]
    (doto graphics
      (j/call :lineStyle (-> data :width) (-> data :color)))
    (when-let [parent (:parent data)] 
      (j/call parent :addChild graphics)) 
    this)
  (tick [this]
    (let [x (.-x follow)
          y (.-y follow)]
      (doto graphics
        (j/call :clear)
        (j/call :lineStyle (-> data :width) (-> data :color))
        (j/call :moveTo 500 500)
        (j/call :lineTo x y))
      this))
  (valid? [_this]
    (valid? follow))
  (dispose [this]
    (doto graphics
      (j/call :removeFromParent)
      (j/call :destroy true))
    this))


(defn sprite-in-bounds [^Sprite sprite]
  (and (< min-x (.-x sprite) max-x)
       (< min-y (.-y sprite) max-y)))


(def default-sprite-opts {:d      0
                          :d'     0
                          :v      0
                          :v'     0
                          :max-v  Inf
                          :s      1
                          :s'     0
                          :max-s  Inf
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


(defn make-defence-missile [parent target-x target-y]
  (let [delta-x (- target-x game-center-x)
        delta-y (- game-height target-y)
        d       (atan2 delta-x delta-y)
        valid?  (if (> (abs delta-x) (abs delta-y))
                  (if (pos? delta-x)
                    (fn [^Sprite sprite _state _time] (< (.-x sprite) target-x))
                    (fn [^Sprite sprite _state _time] (> (.-x sprite) target-x)))
                  (fn [^Sprite sprite _state _time] (> (.-y sprite) target-y)))]
    (newSprite {:kind     :defence-missile
                :sprite   (sprite :defence-arrow)
                :parent   parent
                :x        500
                :y        500
                :d        d
                :v        0
                :v'       0.02
                :max-v    2.0
                :target-x target-x
                :target-y target-y
                :valid?   valid?})))


(defn make-defence-explosion [parent x y]
  (newSprite {:kind     :defence-explosion
              :sprite   (sprite :defence-explosion)
              :parent   parent
              :x        x
              :y        y
              :s        0.01
              :s'       0.008
              :valid? (fn [^Sprite sprite] (< (.-s sprite) 1))}))


(defn make-defence-missile-trail [parent ^Sprite missile]
  )


(defn click [state _time x y]
  (let [stage   (-> state :stage)
        missile (make-defence-missile stage x y)
        trail   (make-defence-missile-trail stage missile)]
    (update state :game update :objects conj missile #_trail)))


(defn limit-reached [^IObject object]
  (case (kind object)
    :defence-missile (let [data (.-data object)]
                       (dispose object) 
                       (make-defence-explosion (-> data :parent)
                                               (-> data :target-x)
                                               (-> data :target-y)))
    :defence-explosion (dispose object)))


(defn update-object [_state _time ^IObject object]
  (-> (tick object)
      (valid?)
      (if object (limit-reached object))))


(defn update-objects [objects state time]
  (let [update-object (partial update-object state time)]
    (-> (keep update-object objects)
        (doall))))


(defn update-game [state time]
  (-> state
      (update-in [:game :objects] update-objects state time)))
