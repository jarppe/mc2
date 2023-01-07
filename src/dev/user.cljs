(ns user
  (:require [applied-science.js-interop :as j]))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn init []
  (println "user.cljs: init"))


(comment

  (require '[mc.state :as state])
  (require '[mc.game :as game])
  (require '["pixi" :as pixi])

  (update {:foo [1 2]} :foo conj 4 5 6)
  game/e
  (-> @state/game-state :app (j/get :renderer))

  (js/console.dir (j/call pixi/RenderTexture :create (j/obj :width 10 :height 10)))

  (let [i (-> game/app .-renderer .-plugins .-interaction)
        p (pixi/Point. 0 0)]
    (.mapPositionToPoint i p 0 0)
    p)

  (pixi/Point. 0 0)

  (defprotocol IFoo (valid? [this]))

  (deftype Foo [^:unsynchronized-mutable x valid?]
    IFoo
    (valid? [this]
      (valid?)))

  (def foo (->Foo 1 (constantly :fofo)))
  (.-x foo)
  (valid? foo)
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
