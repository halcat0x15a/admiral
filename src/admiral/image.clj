(ns admiral.image
  (:import [java.awt.image BufferedImage]
           [java.io File]
           [javax.imageio ImageIO]))

(defn image [name]
  (ImageIO/read (File. (str name ".png"))))

(def menu-sally (image "menu_sally"))
(def menu-supply (image "menu_supply"))

(def sally (image "sally_sally"))
(def expedition (image "sally_expedition"))

(def area (image "map_1"))
(def operation (image "map_1_1"))
(def full (image "full"))
(def decision (image "decision"))
(def start (image "start"))
(def advance (image "withdraw"))
(def chase (image "chase"))

(def checkbox (image "checkbox"))
(def supply (image "supply"))
(def supply-2 (image "supply_2"))
(def supply-3 (image "supply_3"))
(def supply-4 (image "supply_4"))

(def expeditions [(image "expedition_5") (image "expedition_6") (image "expedition_2")])
(def fleets [(image "fleet_3") (image "fleet_4")])
(def expedition-start (image "expedition_start"))

(def arsenal (image "arsenal"))
(def arsenal-scrap (image "arsenal_scrap"))
(def sort-new (image "sort_new"))
(def ^BufferedImage parameter-speed (image "parameter_speed"))
(def scrap (image "scrap"))

(def home (image "home"))

(def option (image "option"))
