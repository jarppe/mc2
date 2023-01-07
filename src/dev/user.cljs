(ns user
  (:require [applied-science.js-interop :as j]))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init []
  (println "user.cljs: init"))


(comment

  (require '[mc.state :as state])
  (require '["pixi" :as pixi])

  (-> @state/game-state :app (j/get :renderer))

  (js/console.dir (j/call pixi/RenderTexture :create (j/obj :width 10 :height 10)))

  (let [i (-> game/app .-renderer .-plugins .-interaction)
        p (pixi/Point. 0 0)]
    (.mapPositionToPoint i p 0 0)
    p)

  (pixi/Point. 0 0)

  (deftype Foo [^:unsynchronized-mutable x
                ^:unsynchronized-mutable y])

  ##Inf
    (def foo (->Foo 1 2))
   (.-x foo)
  (set! (.-x foo) 2)
  (assoc foo :y 32)

  (require '[clojure.math :refer [atan2 to-degrees PI sin cos]])

  (let [x 400
        y 400]
    (let [x (- x 500)
          y (- 500 y)
          d (-> (atan2 x y)
                (to-degrees))]
      [(sin d) (cos d)]))

  (< 10 20 30)

  ;
  )
