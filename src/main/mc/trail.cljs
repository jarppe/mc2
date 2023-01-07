(ns mc.trail
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]
            [mc.object :refer [IObject valid? init]]
            [mc.util :as u]))


(deftype Trail [kind graphics ^IObject follow data]
  IObject
  (kind [_this]
    kind)
  (init [this]
    (when-let [parent (:parent data)]
      (j/call parent :addChild graphics))
    this)
  (tick [this]
    (let [x (.-x follow)
          y (.-y follow)]
      (doto graphics
        (j/call :clear)
        (j/call :lineStyle (-> data :width) (-> data :color))
        (j/call :moveTo u/base-x u/base-y)
        (j/call :lineTo x y))
      this))
  (valid? [_this]
    (valid? follow))
  (dispose [this]
    (doto graphics
      (j/call :removeFromParent)
      (j/call :destroy true))
    this))


(defn newTrail [kind parent missile opts]
  (-> (->Trail kind
               (pixi/Graphics.)
               missile
               (merge {:width 1
                       :color 0xffffff}
                      opts
                      {:parent parent}))
      (init)))
