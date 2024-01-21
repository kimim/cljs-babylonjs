(ns main.core
  (:require
   ["babylonjs" :refer [Scene
                        Engine
                        Vector3
                        Color3
                        ArcRotateCamera
                        HemisphericLight
                        MeshBuilder
                        StandardMaterial
                        Texture
                        SceneLoader]]
   ["@babylonjs/assets" :as Assets]
   ["babylonjs-loaders" :as loaders]
   ["babylonjs-gui" :as GUI]
   [applied-science.js-interop :as j]
   [cljs.core.async :as a :refer [go <!]]
   [cljs.core.async.interop :refer-macros [<p!]]))

(defonce db #js {})

(defn create-scene [engine]
  (let [s (Scene. engine)]
    (j/assoc! db :scene s)
    s))

(defn create-engine [canvas]
  (let [e (Engine. canvas true #js {:preserveDrawingBuffer true
                                    :stencil true})]
    (j/assoc! db :engine e :canvas canvas)
    e))

(defn v3
  ([]
   (v3 0))
  ([n]
   (Vector3. n n n))
  ([x y z]
   (Vector3. x y z)))

(defn set-v3 [v x y z]
  (j/call v :set x y z))

(defn color
  ([c]
   (color c c c))
  ([r g b]
   (Color3. r g b)))

(defn start-scene []
  (go
    (try
      (let [canvas (js/document.getElementById "renderCanvas")
            engine (create-engine canvas)
            scene (create-scene engine)
            camera (-> (ArcRotateCamera. "camera" (/ js/Math.PI -4)
                                         (/ js/Math.PI 3) 10 (v3))
                       (j/call :attachControl canvas true))
            light (HemisphericLight. "light" (v3 0 1 0))

            color (-> (StandardMaterial. "color")
                      (j/assoc! :diffuseColor
                                (j/call Color3 :Yellow)))
            texture (Texture.
                     (.. Assets -textures
                         -checkerboard_basecolor_png -rootUrl)
                     scene)
            ground (-> (j/call MeshBuilder :CreateGround
                               "ground" #js {:width 4 :height 4})
                       (j/assoc-in! [:position :y] -1)
                       (j/assoc! :material color)
                       (j/assoc! :diffuseTexture texture))

            _ (j/call SceneLoader :ImportMesh ""
                      (.. Assets -meshes -Yeti -rootUrl)
                      (.. Assets -meshes -Yeti -filename)
                      scene
                      (fn [mesh]
                        (j/assoc-in! mesh [0 :position :y] -1)
                        (j/assoc-in! mesh [0 :scaling] (v3 0.08 0.08 0.08))))

            - (j/call SceneLoader :ImportMesh ""
                      (.. Assets -meshes -fish -rootUrl)
                      (.. Assets -meshes -fish -filename)
                      scene
                      (fn [mesh]
                        (j/assoc-in! mesh [0 :position :x] 3)
                        (j/assoc-in! mesh [0 :scaling] (v3 0.1 0.1 0.1))))]
        (j/call engine :runRenderLoop #(j/call scene :render)))
      (catch js/Error e
        (js/console.error e)))))

(defn ^:dev/before-load stop []
  (js/console.clear)
  (js/console.log "before-load"))

(defn ^:dev/after-load start []
  (js/console.log "after-load")
  (start-scene))

(defn init []
  (js/console.log "init")
  (start))
