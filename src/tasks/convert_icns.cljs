(ns tasks.convert-icns
  "
  Based on tutorial from:
  https://eshop.macsales.com/blog/28492-create-your-own-custom-icons-in-10-7-5-or-later/

  Usage
  npm run convert-icns ./icons/kitty-terminal/png ./icons/kitty-terminal/kitty.icns
  "
  (:require
    [clojure.string :as s]
    [promesa.core :as p]
    ["path" :as path]
    ["fs/promises" :as fs]
    ["zx" :refer [$] :rename {$ zx}]))

(defmacro js-template-str
  [& args]
  (let [[strs# args#] (partition-by string? args)]
    `(cons [(clj->js ~strs#)] ~args#)))

(comment
  (macroexpand-1
    (js-template-str "iconutil -c icns " (str "hello"))))

(defn create-iconset
  [src-dir dest-name]
  (let [dirname (.dirname path src-dir)
        dest-file (str dest-name ".iconset")
        dest-path (.resolve path dirname dest-file)]
    (p/do
      (.rm fs dest-path #js {:force true :recursive true})
      (.cp fs src-dir dest-path #js {:recursive true})
      dest-path)))

(defn iconset->icns
  [src-path]
  (println "src-path:" src-path)
  (p/do
    (zx #js ["iconutil -c icns " ""] src-path)))


(defn -main
  [src-dir dest-name]
  (p/-> src-dir
        (create-iconset dest-name)
        (iconset->icns)))

