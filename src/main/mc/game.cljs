(ns mc.game
  (:require ["pixi" :as pixi]
            [goog.events :as gevents]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-state game-width]]
            [mc.tick :refer [click update-game]]
            [mc.sprites :as sprites]))


(defn on-click [e]
  (let [x (j/get e :offsetX)
        y (j/get e :offsetY)]
    (swap! game-state (fn [state]
                        (let [scale (-> state :view :scale)
                              x     (/ x scale)
                              y     (/ y scale)]
                          (click state x y))))))


(defn on-resize [_]
  (when-let [state @game-state]
    (let [app       (-> state :app)
          parent    (-> state :view :parent)
          new-scale (-> parent (j/get :offsetWidth) (/ game-width))]
      (j/call-in app [:stage :scale :set] new-scale new-scale)
      (swap! game-state update :view assoc :scale new-scale))))

(defn on-tick [_]
  (swap! game-state update-game))


(defn reset-game []
  (swap! game-state (fn [state]
                      (j/call (-> state :stage) :removeChildren)
                      (sprites/init-sprites state)
                      (sprites/sprite :circle 0 0)
                      (sprites/sprite :circle 500 250)
                      (sprites/sprite :circle 1000 500)
                      (dissoc state :game))))


(defn init-game [parent canvas dev-mode?]
  (let [app       (pixi/Application. (j/obj :hello dev-mode?
                                            :autoDensity true
                                            :antialias true
                                            :view canvas
                                            :resizeTo parent))
        listeners [(gevents/listen js/window goog.events.EventType.RESIZE on-resize)
                   (gevents/listen canvas goog.events.EventType.MOUSEDOWN on-click)]]
    (reset! game-state {:dev?      dev-mode?
                        :view      {:parent parent
                                    :canvas canvas}
                        :listeners listeners
                        :app       app
                        :stage     (j/get app :stage)})
    (on-resize nil)
    (reset-game)
    (j/call-in app [:ticker :add] on-tick)))
