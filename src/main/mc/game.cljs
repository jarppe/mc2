(ns mc.game
  (:require ["pixi" :as pixi]
            [applied-science.js-interop :as j]
            [mc.state :refer [game-state]]
            [mc.util :refer [game-width]]
            [mc.tick :refer [click update-game]]
            [mc.sprites :as sprites]))


(defn on-click [x y]
  (swap! game-state (fn [state]
                      (let [scale (-> state :view :scale)
                            time  (js/Date.now)
                            x     (/ x scale)
                            y     (/ y scale)]
                        (click state time x y)))))


(defn on-mousedown [^js e]
  (on-click (j/get e :offsetX)
            (j/get e :offsetY)))


(defn on-touchstart [^js e]
  (let [touches (j/get e :changedTouches)]
    (when (pos? (j/get touches :length))
      (let [touch  (j/call touches :item 0)
            target (j/get e :target)
            bounds (j/call target :getBoundingClientRect)
            top    (j/get bounds :top)
            left   (j/get bounds :left)]
        (on-click (-> (j/get touch :offsetX)
                      (- left))
                  (-> (j/get touch :offsetY)
                      (- top)))))))


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
  (let [app        (pixi/Application. (j/obj :hello dev-mode?
                                             :autoDensity true
                                             :antialias true
                                             :view canvas
                                             :resizeTo parent))
        controller (js/AbortController.)
        opts       (j/obj :signal (j/get controller :signal))]
    (j/call js/window :addEventListener "resize" on-resize opts)
    (j/call canvas :addEventListener "mousedown" on-mousedown opts)
    (j/call canvas :addEventListener "touchstart" on-touchstart opts)
    (reset! game-state {:dev?  dev-mode?
                        :view  {:parent  parent
                                :canvas  canvas
                                :cleanup (fn [] (j/call controller :abort))}
                        :app   app
                        :stage (j/get app :stage)})
    (on-resize nil)
    (reset-game)
    (j/call-in app [:ticker :add] on-tick)))
