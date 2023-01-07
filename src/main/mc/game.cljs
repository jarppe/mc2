(ns mc.game
  (:require ["pixi" :as pixi]
            [goog.events :as gevents]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-state game-width]]
            [mc.tick :refer [click update-game]]
            [mc.sprites :as sprites]))


(defn on-click [x y]
  (swap! game-state (fn [state]
                      (let [scale (-> state :view :scale)
                            time  (js/Date.now)
                            x     (/ x scale)
                            y     (/ y scale)]
                        (click state time x y)))))


(defn on-mousedown [e]
  (on-click (j/get e :offsetX)
            (j/get e :offsetY)))


(defn on-touchstart [e]
  (let [touch-item (-> (j/get e :touches)
                       (j/call :item 0))]
    (on-click (j/get touch-item :clientX)
              (j/get touch-item :clientY))))


(defn on-resize [_]
  (when-let [state @game-state]
    (let [app       (-> state :app)
          parent    (-> state :view :parent)
          new-scale (-> parent (j/get :offsetWidth) (/ game-width))]
      (j/call-in app [:stage :scale :set] new-scale new-scale)
      (swap! game-state update :view assoc :scale new-scale))))


(defn on-tick [_]
  (swap! game-state update-game (js/Date.now)))


(defn reset-game []
  (swap! game-state (fn [state]
                      (j/call (-> state :stage) :removeChildren)
                      (sprites/init-sprites state)
                      (dissoc state :game))))


(defn init-game [parent canvas dev-mode?]
  (let [app       (pixi/Application. (j/obj :hello dev-mode?
                                            :autoDensity true
                                            :antialias true
                                            :view canvas
                                            :resizeTo parent))
        listeners [(gevents/listen js/window "resize" on-resize)
                   (gevents/listen canvas "mousedown" on-mousedown)
                   (gevents/listen canvas "touchstart" on-touchstart)]]
    (reset! game-state {:dev?      dev-mode?
                        :view      {:parent parent
                                    :canvas canvas}
                        :listeners listeners
                        :app       app
                        :stage     (j/get app :stage)})
    (on-resize nil)
    (reset-game)
    (j/call-in app [:ticker :add] on-tick)))
