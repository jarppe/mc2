(ns mc.tick
  (:require [mc.object :refer [IObject kind tick valid? dispose]]
            [mc.defence-missile :refer [make-defence-missile]]
            [mc.defence-explosion :refer [make-defence-explosion]]
            [mc.defence-missile-trail :refer [make-defence-missile-trail]]))


(defn click [state _time x y]
  (let [stage   (-> state :stage)
        missile (make-defence-missile stage x y)
        trail   (make-defence-missile-trail stage missile)]
    (update state :game update :objects conj missile trail)))


(defn limit-reached [^IObject object]
  (case (kind object)
    :defence-missile (let [data (.-data object)]
                       (dispose object)
                       (make-defence-explosion (-> data :parent)
                                               (-> data :target-x)
                                               (-> data :target-y)))
    :defence-explosion (do (dispose object) nil)
    :defence-missile-trail (do (dispose object) nil)))


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
