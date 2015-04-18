(ns admiral
  (:import [java.awt Robot MouseInfo GraphicsEnvironment Rectangle]
           [java.awt.event InputEvent]
           [java.awt.image BufferedImage]
           [java.io File]
           [javax.imageio ImageIO])
  (:require [admiral.image :as image]))

(def ^Robot robot
  (doto (Robot.)
    (.setAutoDelay 10)))

(def ^Rectangle bounds
  (.getMaximumWindowBounds (GraphicsEnvironment/getLocalGraphicsEnvironment)))

(defn lookup [^BufferedImage image]
  (.delay robot 1000)
  (let [screen (.createScreenCapture robot bounds)
        points (for [x1 (range (- (.getWidth screen) (.getWidth image)))
                     y1 (range (- (.getHeight screen) (.getHeight image)))]
                 (for [x2 (range (.getWidth image))
                       y2 (range (.getHeight image))]
                   [x1 y1 x2 y2]))]
    (when-let [[x y _ _] (ffirst (filter #(every? (fn [[x1 y1 x2 y2]] (== (.getRGB screen (+ x1 x2) (+ y1 y2)) (.getRGB image x2 y2))) %) points))]
      [(+ x (/ (.getWidth image) 2) (.getX bounds)) (+ y (/ (.getHeight image) 2) (.getY bounds))])))

(defn interpolate [start end]
  (range start end (/ (- end start) 10)))

(defn move [x y]
  (let [current (.getLocation (MouseInfo/getPointerInfo))]
    (doseq [[x y] (map vector (interpolate (.getX current) x) (interpolate (.getY current) y))]
      (.mouseMove robot x y))
    (.mouseMove robot x y)))

(defn click
  ([image]
     (when-let [[x y] (lookup image)]
       (click x y)))
  ([x y]
     (move x y)
     (.mousePress robot InputEvent/BUTTON1_MASK)
     (.mouseRelease robot InputEvent/BUTTON1_MASK)
     (move 0 0)
     true))

(defmacro until [& f]
  `(loop [x# (do ~@f)] (if x# x# (recur (do ~@f)))))

(defn sally []
  (let [[x y] (until (lookup image/menu-sally))]
    (until (click x y) (click image/sally)))
  (click image/area)
  (click image/operation))

(defn battle []
  (click image/decision)
  (let [[x y] (until (lookup image/start))]
    (until (when-not (or (click image/advance) (click image/chase)) (click x y)) (lookup image/option))))

(defn home []
  (let [[x y] (until (lookup image/home))]
    (until (click x y) (lookup image/option))))

(defn supply []
  (let [[x y] (until (lookup image/menu-supply))]
    (until (click x y) (lookup image/home)))
  (click image/checkbox)
  (click image/supply)
  (letfn [(supply [n]
            (until (not (click n)))
            (click image/checkbox)
            (click image/supply))]
    (let [fleets [(supply image/supply-2) (supply image/supply-3) (supply image/supply-4)]]
      (home)
      fleets)))

(defn expedition [fleets]
  (let [[x y] (until (lookup image/menu-sally))]
    (until (click x y) (click image/expedition))
    (doseq [i (range (count fleets)) :when (fleets i)]
      (when-let [[x y] (until (lookup (image/expeditions i)))] (click x y))
      (click image/decision)
      (when (pos? i) (click (image/fleets (dec i))))
      (click image/expedition-start))
    (home)))

(defn arsenal []
  (click image/arsenal)
  (click image/arsenal-scrap)
  (until (when-let [[x y] (lookup image/parameter-speed)]
           (click (+ x (.getWidth image/parameter-speed)) y))
         (lookup image/sort-new))
  (until (when-let [[x y] (lookup image/parameter-speed)]
           (click x (+ y (.getHeight image/parameter-speed))))
         (let [result (click image/scrap)]
           (until (lookup image/parameter-speed))
           (not result)))
  (home))

(defn capture [^String name]
  (ImageIO/write (.createScreenCapture robot bounds) "PNG" (File. name)))

(defn -main [& args]
  (while true
    (let [fleets (supply)]
      (when (some identity fleets) (expedition fleets))
      (sally)
      (if (lookup image/full) (arsenal) (battle)))))
