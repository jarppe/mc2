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


(defn init-sprites [state]
  (let [renderer            (-> state :app (j/get :renderer))
        make-sprite-factory (partial make-sprite-factory renderer)]
    (swap! sprites (fn [sprites]
                     (doseq [factory (vals sprites)]
                       (factory :destroy))
                     {:circle (make-sprite-factory 20 20 (doto (pixi/Graphics.)
                                                           (j/call :beginFill 0xf0f022)
                                                           (j/call :drawCircle 10 10 10)
                                                           (j/call :endFill)))
                      :arrow  (make-sprite-factory 7 7 (doto (pixi/Graphics.)
                                                         (j/call :beginFill 0x22f0e0)
                                                         (j/call :moveTo 0 0)
                                                         (j/call :lineTo 6 3)
                                                         (j/call :lineTo 0 6)
                                                         (j/call :closePath)
                                                         (j/call :endFill)))}))))


(defn sprite [id]
  (let [factory (get @sprites id)]
    (factory)))
