(ns mc.sprites
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]))


(defn make-texture [renderer width height source]
  (let [texture (j/call pixi/RenderTexture :create (j/obj :width width :height height))]
    (j/call renderer :render source (j/obj :renderTexture texture))
    texture))


(defn make-sprite [stage texture x y]
  (let [sprite (doto (pixi/Sprite. texture)
                 (j/call-in [:position :set] x y)
                 (j/call-in [:anchor :set] 0.5))]
    (j/call stage :addChild sprite)
    sprite))


(defn make-sprite-factory [stage renderer width height source]
  (let [texture (make-texture renderer width height source)]
    (fn
      ([]
       (j/call texture :destroy))
      ([x y]
       (make-sprite stage texture x y)))))


(defonce sprites (atom {}))


(defn init-sprites [state]
  (let [stage               (-> state :app (j/get :stage))
        renderer            (-> state :app (j/get :renderer))
        make-sprite-factory (partial make-sprite-factory stage renderer)]
    (swap! sprites (fn [sprites]
                     (doseq [factory (vals sprites)]
                       (factory))
                     {:circle (make-sprite-factory 20 20 (doto (pixi/Graphics.)
                                                           (j/call :beginFill 0xf0f022)
                                                           (j/call :drawCircle 10 10 10)
                                                           (j/call :endFill)))}))))


(defn sprite [id x y]
  (let [factory (get @sprites id)]
    (factory x y)))
