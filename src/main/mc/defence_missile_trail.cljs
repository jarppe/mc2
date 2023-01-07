(ns mc.defence-missile-trail
  (:require [mc.trail :refer [Trail newTrail]]))


(defn make-defence-missile-trail ^Trail [parent ^Sprite missile]
  (newTrail :defence-missile-trail parent missile {:color 0x67AD5B}))
