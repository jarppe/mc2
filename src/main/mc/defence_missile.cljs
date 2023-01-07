(ns mc.defence-missile
  (:require [clojure.math :refer [atan2]]
            [mc.sprites :refer [sprite]]
            [mc.sprite :refer [Sprite newSprite]]
            [mc.util :as u]))


(defn make-defence-missile ^Sprite [parent target-x target-y]
  (let [delta-x (- target-x u/base-x)
        delta-y (- u/base-y target-y)
        d       (atan2 delta-x delta-y)
        valid?  (if (> (abs delta-x) (abs delta-y))
                  (if (pos? delta-x)
                    (fn [^Sprite sprite _state _time] (< (.-x sprite) target-x))
                    (fn [^Sprite sprite _state _time] (> (.-x sprite) target-x)))
                  (fn [^Sprite sprite _state _time] (> (.-y sprite) target-y)))]
    (newSprite {:kind     :defence-missile
                :sprite   (sprite :defence-arrow)
                :parent   parent
                :x        u/base-x
                :y        u/base-y
                :d        d
                :v        0
                :v'       0.02
                :max-v    2.0
                :target-x target-x
                :target-y target-y
                :valid?   valid?})))