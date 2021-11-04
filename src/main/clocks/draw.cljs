(ns clocks.draw
    (:require
     [clocks.common :as cmm]))

(def PI (.-PI js/Math))
(def TWO_PI (* 2 PI))

(defn deg->rad [angle]
  "Convers degrees to radians"
  (* angle (/ PI 180)))

(defn get-point
  "Given the coordinates of a circle
   - center (cx,cy)
   - radius Returns a point in the angle and a distance (from 0 to 1) where 0 is the center and 1 is the perimeter"
  ([cx cy radius angle]
   (get-point cx cy radius angle 1))
  ([cx cy radius angle dist]
   (let [angle (deg->rad angle)]
     {:x (+ cx (* (.sin js/Math angle) (* radius dist)))
      :y (- cy (* (.cos js/Math angle) (* radius dist)))})))

(defn circle
  "Draws a circle in the canvas context `ctx`"
  [ctx cx cy radius]
  (aset ctx "strokeStyle" "black")
  (aset ctx "lineWidth" 4)
  (.beginPath ctx)
  (.arc ctx cx cy radius 0 TWO_PI)
  (.stroke ctx))

(defn text
  "Draws text in the canvas context `ctx`"
  [ctx {:keys [x y]} text]
  (aset ctx "font" "24px Arial")
  (aset ctx "textAlign" "center")
  (aset ctx "textBaseline" "middle")
  (aset ctx "fillStyle" "black")
  (.fillText ctx text x y))

(defn line
  "Draws a line in the context `ctx` from point `p1` to point `p2`.
  Optionaly can receive a `width` and a `color`"
  ([ctx p1 p2]
   (line ctx p1 p2 1 "black"))

  ([ctx {x1 :x y1 :y} {x2 :x y2 :y} width color]
   (aset ctx "strokeStyle" color)
   (aset ctx "lineWidth" width)
   (.beginPath ctx)
   (.moveTo ctx x1 y1)
   (.lineTo ctx x2 y2)
   (.stroke ctx)))
  
(defn slice
  "Draws a \"pizza slice\" shape"
  [ctx cx cy radius from-angle color size]
  (aset ctx "fillStyle" color)
  (let [to-angle (+ from-angle (* 6 size))
        from-angle (deg->rad (- from-angle 90))
        to-angle (deg->rad (- to-angle 90))]
    (.beginPath ctx)
    (.moveTo ctx cx cy)
    (.arc ctx cx cy radius from-angle to-angle)
    (.fill ctx)))


(defn draw-hands [ctx x-center y-center radius {:keys [hour minute second]}]
  (let [hour-angle      (* (/ 360 12) hour)
        minute-angle    (* (/ 360 60) minute)
        second-angle    (* (/ 360 60) second)
        center-clock    {:x x-center :y y-center}
        hour-hand-end   (get-point x-center y-center radius hour-angle 0.5)
        minute-hand-end (get-point x-center y-center radius minute-angle 0.8)
        second-hand-end (get-point x-center y-center radius second-angle 0.9)]
    (line ctx center-clock hour-hand-end  5 "gray")
    (line ctx center-clock minute-hand-end  3 "silver")
    (line ctx center-clock second-hand-end  1 "red")))

(defn draw-slice [ctx x-center y-center radius {:keys [minute]} {:keys [size color]}]
  (let [from-angle (* (/ 360 60) minute)]
    (slice ctx x-center y-center radius from-angle color size)))

(defn draw-clock [ctx x-center y-center radius time-now]
  (circle ctx x-center y-center radius)
  (doseq [i (range 0 12)]
    (let [current-angle (* (/ 360 12) i)
          hour-start    (get-point x-center y-center radius current-angle cmm/distance-hour)
          hour-end      (get-point x-center y-center radius current-angle 1)
          time-place    (get-point x-center y-center radius current-angle 0.8)]
      (line ctx hour-start hour-end)
      (text ctx time-place (if (= i 0)
                             "12"
                             (str i)))))
  (doseq [i (range 0 60)]
    (let [current-angle (* (/ 360 60) i)
          minute-start  (get-point x-center y-center radius current-angle 0.9)
          minute-end    (get-point x-center y-center radius current-angle 1)]
      (when-not (= (mod i 5) 0)
        (line ctx minute-start minute-end 1 "pink"))))
  (draw-hands ctx x-center y-center radius time-now))