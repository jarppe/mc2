(ns mc.object)

(defprotocol IObject
  (kind [this])
  (init [this])
  (tick [this])
  (valid? [this])
  (dispose [this]))
