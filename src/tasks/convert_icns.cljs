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
  (let [[strs# args#] (partition-by string? args)
        result# (cons (clj->js (conj (vec strs#) "")) args#)]
    `(list ~@result#)))

(defmacro $
  [& args]
  `(apply ~zx (js-template-str ~@args)))

(comment
  (js/process.cwd)
  (macroexpand-1
    '(js-template-str "iconutil -c icns " (.join path (js/process.cwd) "icons/kitty-terminal/kitty.iconset")))

  (apply zx (js-template-str "iconutil -c icns " (.join path (js/process.cwd) "icons/kitty-terminal/kitty.iconset")))
  (macroexpand
    '($ "iconutil -c icns " (.join path (js/process.cwd) "icons/kitty-terminal/kitty.iconset")))
  ($ "iconutil -c icns " (.join path (js/process.cwd) "icons/kitty-terminal/kitty.iconset")))

(defn create-iconset
  ^:private
  [src-dir dest-name]
  (let [dirname (.dirname path src-dir)
        dest-file (str dest-name ".iconset")
        dest-path (.resolve path dirname dest-file)]
    (p/do
      (.rm fs dest-path #js {:force true :recursive true})
      (.cp fs src-dir dest-path #js {:recursive true})
      dest-path)))

(defn iconset->icns
  ^:private
  [src-path]
  (p/do
    ($ "iconutil -c icns " src-path)
    (s/replace src-path #"iconset$" "icns")))


(defn dir->icns
  "
  Main transform pipeline
  Takes a src directory containing pngs of all required sizes
  Copies src directory into {dest-name}.iconset
  Uses macOS iconutil binary to convert iconset -> .icns file
  "
  [src-dir dest-name]
  (p/-> src-dir
        (create-iconset dest-name)
        (iconset->icns)))


(defn -main
  "
  Support cli invoking via nbb -m tasks.convert-icns
  "
  [src-dir dest-name]
  (dir->icns src-dir dest-name))


