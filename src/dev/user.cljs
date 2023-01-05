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
  ;
  )
