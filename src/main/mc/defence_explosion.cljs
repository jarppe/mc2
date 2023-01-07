(ns mc.defence-explosion
  (:require [mc.sprites :refer [sprite]]
            [mc.sprite :refer [Sprite newSprite]]))


(defn make-defence-explosion ^Sprite [parent x y]
  (newSprite {:kind   :defence-explosion
              :sprite (sprite :defence-explosion)
              :parent parent
              :x      x
              :y      y
              :s      0.01
              :s'     0.008
              :valid? (fn [^Sprite sprite] (< (.-s sprite) 1))}))