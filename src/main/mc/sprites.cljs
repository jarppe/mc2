(ns mc.sprites
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]))


(defn make-texture [renderer width height source]
  (let [texture (j/call pixi/RenderTexture :create (j/obj :width width :height height))]
    (j/call renderer :render source (j/obj :renderTexture texture))
    texture))


(defn make-sprite [texture]
  (doto (pixi/Sprite. texture)
    (j/call-in [:anchor :set] 0.5)))


(defn make-sprite-factory [renderer width height source]
  (let [texture (make-texture renderer width height source)]
    (fn
      ([]
       (make-sprite texture))
      ([_]
       (j/call texture :destroy)))))


(defonce sprites (atom {}))


(defn make-arrow [color]
  (doto (pixi/Graphics.)
    (j/call :beginFill color)
    (j/call :moveTo 0 6)
    (j/call :lineTo 3 0)
    (j/call :lineTo 6 6)
    (j/call :closePath)
    (j/call :endFill)))


(defn init-sprites [state]
  (let [renderer            (-> state :app (j/get :renderer))
        make-sprite-factory (partial make-sprite-factory renderer)]
    (swap! sprites (fn [sprites]
                     (doseq [factory (vals sprites)]
                       (factory :destroy))
                     {:defence-explosion (make-sprite-factory 60 60 (doto (pixi/Graphics.)
                                                                      (j/call :beginFill 0xffffff)
                                                                      (j/call :drawCircle 30 30 30)
                                                                      (j/call :endFill)))
                      :attack-arrow      (make-sprite-factory 7 7 (make-arrow 0xEC685D))
                      :defence-arrow     (make-sprite-factory 7 7 (make-arrow 0x67AD5B))}))))


(defn sprite [id]
  (let [factory (get @sprites id)]
    (factory)))
