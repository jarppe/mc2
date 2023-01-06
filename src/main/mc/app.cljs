(ns mc.app
  (:require [applied-science.js-interop :as j]
            [mc.game :refer [reset-game init-game]]))


(goog-define DEV false)


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn before-load []
  (println "Re-loading..."))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn after-load []
  (println "Re-loaded")
  (reset-game))


#_{:clj-kondo/ignore [:clojure-lsp/unused-public-var]}
(defn ^:export run []
  (println "Init at" (if DEV "DEV" "PRODUCTION") "mode...")
  (let [parent (j/call js/document :getElementById "container")
        canvas (j/call js/document :getElementById "game")]
    (init-game parent canvas DEV)))
