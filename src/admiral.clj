(ns admiral
  (:require [admiral.robot :refer :all]
            [admiral.image :as image]))

(defn sally []
  (let [[x y] (until (lookup image/menu-sally))]
    (until (click x y)
           (click image/sally)))
  (click image/area)
  (click image/operation))

(defn battle []
  (click image/decision)
  (let [[x y] (until (lookup image/start))]
    (until (if-not (or (click image/advance)
                       (click image/pursuit))
             (click x y))
           (lookup image/menu-sally))))

(defn home []
  (let [[x y] (until (lookup image/home))]
    (until (click x y)
           (lookup image/menu-sally))))

(defn supply []
  (let [[x y] (until (lookup image/menu-supply))]
    (until (click x y)
           (lookup image/home)))
  (and (click image/checkbox)
       (click image/supply))
  (letfn [(supply [n]
            (until (not (click n)))
            (and (click image/checkbox)
                 (click image/supply)))]
    (let [fleets [(supply image/supply-2) (supply image/supply-3) (supply image/supply-4)]]
      (home)
      fleets)))

(defn expedition [fleets]
  (let [[x y] (until (lookup image/menu-sally))]
    (until (click x y)
           (click image/expedition))
    (doseq [i (range (count fleets)) :when (fleets i)]
      (if-let [[x y] (until (click (image/expeditions i))
                            (lookup image/decision))]
        (click x y))
      (if (pos? i)
        (click (image/fleets (dec i))))
      (click image/expedition-start))
    (home)))

(defn arsenal []
  (click image/arsenal)
  (click image/arsenal-scrap)
  (until (if-let [[x y] (lookup image/parameter-speed)]
           (click (+ x (.getWidth image/parameter-speed)) y))
         (lookup image/sort-new))
  (until (if-let [[x y] (lookup image/parameter-speed)]
           (click x (+ y (.getHeight image/parameter-speed))))
         (let [result (click image/scrap)]
           (until (lookup image/parameter-speed))
           (not result)))
  (home))

(defn -main [& args]
  (while true
    (let [fleets (supply)]
      (if (some identity fleets)
        (expedition fleets)))
    (sally)
    (if (lookup image/full)
      (arsenal)
      (battle))))
