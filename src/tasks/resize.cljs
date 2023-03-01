(ns tasks.resize
  "
  Based on tutorial from:
  https://eshop.macsales.com/blog/28492-create-your-own-custom-icons-in-10-7-5-or-later/

  Usage:
  npm run resize ./src/imgs/kitty-icon.png ./icons/kitty-terminal/png
  "
  (:require
    [promesa.core :as p]
    ["jimp$default" :as jimp]
    ["path" :as path]
    ["fs/promises" :as fs]))

(def sizes
  [512
   256
   128
   64
   32
   16])

(defn generate-size
  [src-img size]
  [{:name (str "icon_" size "x" size "@2x.png")
    :img (let [size-x2 (* size 2)]
           (-> src-img
               (.clone)
               (.resize size-x2 size-x2)))}
   {:name (str "icon_" size "x" size ".png")
    :img (-> src-img
             (.clone)
             (.resize size size))}])

(defn write-file
  [file dest-dir]
  (let [{:keys [img name]} file
        dest-path (.join path dest-dir name)]
   (p/do
     (.write img dest-path)
     dest-path)))

(defn resize-copies
  "
  Takes a src image path string, preferrably png, and a destination dir string
  Reads the src image and resizes it to make an iconset
  Writes the files to the destination dir
  Returns nil

  @TODO Maybe src-path should be a directory that may contain specific sizes
        and this should use those, scaling each available size down until the
        next manual png.

        That would support having a separate 16px src image from the 1024px src
        image.
  "
  [src-path dest-dir]
  (p/catch
    (p/let [src-img (.read jimp src-path)]
      dest-dir (.resolve path src-path dest-dir)
      (p/do
        (.rm fs dest-dir #js {:recursive true :force true})
        (.mkdir fs dest-dir #js {:recursive true})
        (p/->> sizes
               (mapcat #(generate-size src-img %))
               (map #(write-file % dest-dir))
               (p/all)
               (map println)
               (dorun))))

    (fn [err]
      (js/console.error err))))


(defn -main
  "
  Support invoking with clj -m tasks.resize
  "
  [src-path dest-dir]
  (resize-copies src-path dest-dir))



