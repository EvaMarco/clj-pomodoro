(ns clocks.draw)

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
  [ctx cx cy radius from-angle]
  (aset ctx "fillStyle" "rgba(255,0,0,0.2)")
  
  (let [to-angle (+ from-angle (* 30 5))
        from-angle (deg->rad (- from-angle 90))
        to-angle (deg->rad (- to-angle 90))]
    (.beginPath ctx)
    (.moveTo ctx cx cy)
    (.arc ctx cx cy radius from-angle to-angle)
    (.fill ctx)))
