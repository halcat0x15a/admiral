(ns admiral.robot
  (:import [java.awt Robot Rectangle GraphicsEnvironment MouseInfo]
           [java.awt.event InputEvent]
           [java.awt.image BufferedImage]
           [java.io File]
           [javax.imageio ImageIO]))

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
    (letfn [(match? [[x1 y1 x2 y2]]
              (== (.getRGB screen (+ x1 x2) (+ y1 y2))
                  (.getRGB image x2 y2)))]
      (if-let [[x y _ _] (ffirst (filter #(every? match? %) points))]
        [(+ x (/ (.getWidth image) 2) (.getX bounds))
         (+ y (/ (.getHeight image) 2) (.getY bounds))]))))

(defn interpolate [start end]
  (range start end (/ (- end start) 10)))

(defn move [x y]
  (let [current (.getLocation (MouseInfo/getPointerInfo))]
    (doseq [[x y] (map vector (interpolate (.getX current) x) (interpolate (.getY current) y))]
      (.mouseMove robot x y)))
  (.mouseMove robot x y)
  [x y])

(defn click
  ([image]
     (if-let [[x y] (lookup image)]
       (click x y)))
  ([x y]
     (move x y)
     (.mousePress robot InputEvent/BUTTON1_MASK)
     (.mouseRelease robot InputEvent/BUTTON1_MASK)
     (move 0 0)))

(defmacro until [& f]
  `(loop [x# (do ~@f)] (if x# x# (recur (do ~@f)))))

(defn capture [^String name]
  (ImageIO/write (.createScreenCapture robot bounds) "PNG" (File. name)))
