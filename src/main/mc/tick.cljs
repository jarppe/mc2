(ns mc.tick
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-height]]
            [mc.sprites :refer [sprite]]))


(defn make-circle [state x y]
  (update state :game update :circles conj {:graphics (sprite :circle x y)
                                            :x        x
                                            :y        y
                                            :vx       (* (- 500 x) 0.01)
                                            :vy       -2.5
                                            :vvx      0
                                            :vvy      0.02}))


(defn click [state x y]
  (make-circle state x y))


(defn update-circle [state
                     {:keys [x y vx vy vvx vvy graphics]
                      :as   circle}]
  (let [x' (+ x vx)
        y' (+ y vy)]
    (if (< y' (+ game-height 10))
      (do (j/call-in graphics [:position :set] x' y')
          (assoc circle :x x' :y y' :vx (+ vx vvx) :vy (+ vy vvy)))
      (do (j/call (:stage state) :removeChild graphics)
          nil))))


(defn update-circles [circles state]
  (let [update-circle (partial update-circle state)]
    (doall (keep update-circle circles))))


(defn update-game [state]
  (-> state
      (update-in [:game :circles] update-circles state)))
