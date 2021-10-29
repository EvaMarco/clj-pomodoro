(ns clocks.main
  (:require
   [clocks.draw :as d]
   [clocks.date :as dt]
   [clocks.math :as mth]))


(declare init)

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load reload
  []
  (init))


;; LIVE CODING
(declare draw-hands)
(declare draw-slice)
(declare show-time-left)

(def ^:const distance-hour 0.85)

(def show-slice-time (atom nil))

(defn draw [ctx width height]
  (let [x-center (/ width 2)
        y-center (/ height 2)
        radius (* (/ width 2) 0.9)
        time-now (dt/now)]
    (.clearRect ctx 0 0 width height)
    (d/circle ctx x-center y-center radius)
    (doseq [i (range 0 12)]
      (let [current-angle (* (/ 360 12) i)
            hour-start    (d/get-point x-center y-center radius current-angle distance-hour)
            hour-end      (d/get-point x-center y-center radius current-angle 1)
            time-place    (d/get-point x-center y-center radius current-angle 0.8)]
        (d/line ctx hour-start hour-end)
        (d/text ctx time-place (if (= i 0)
                                 "12"
                                 (str i)))))
    (doseq [i (range 0 60)]
      (let [current-angle (* (/ 360 60) i)
            minute-start  (d/get-point x-center y-center radius current-angle 0.9)
            minute-end    (d/get-point x-center y-center radius current-angle 1)]
        (when-not (= (mod i 5) 0)
          (d/line ctx minute-start minute-end 1 "pink"))))
    (draw-hands ctx x-center y-center radius time-now)
    (when @show-slice-time
      (draw-slice ctx x-center y-center radius @show-slice-time)
      (show-time-left @show-slice-time time-now))))

(defn draw-hands [ctx x-center y-center radius {:keys [hour minute second]}]
  (let [hour-angle      (* (/ 360 12) hour)
        minute-angle    (* (/ 360 60) minute)
        second-angle    (* (/ 360 60) second)
        center-clock    {:x x-center :y y-center}
        hour-hand-end   (d/get-point x-center y-center radius hour-angle 0.5)
        minute-hand-end (d/get-point x-center y-center radius minute-angle 0.8)
        second-hand-end (d/get-point x-center y-center radius second-angle 0.9)]
    (d/line ctx center-clock hour-hand-end  5 "#fabada")
    (d/line ctx center-clock minute-hand-end  3 "silver")
    (d/line ctx center-clock second-hand-end  1 "red")))

(defn draw-slice [ctx x-center y-center radius {:keys [minute]}]
  (let [from-angle (* (/ 360 60) minute)]
    (d/slice ctx x-center y-center radius from-angle)))

(defn start-timer [cb]
  (.setTimeout js/window #(do (cb) (start-timer cb)) 1000))

(defn show-time-left [time-start now]
  (let [time-label      (.getElementById js/document "time-left")
        diff-seconds    (- (* 25 60) (dt/seconds-diff now time-start))
        diff-min-result (mth/floor (/ diff-seconds 60))
        rest-sec-result (mth/floor (mod diff-seconds 60))]
    (unchecked-set time-label "textContent" (str "you have " diff-min-result " minutes and " rest-sec-result " seconds left"))))

(defn clear-time-left []
  (let [time-label (.getElementById js/document "time-left")]
    (unchecked-set time-label "textContent" "")))

(defn ^:export handle-start [event]
  (let [stop-button (.getElementById js/document "stop-btn")
        date-now (dt/now)]
    (unchecked-set stop-button "disabled" false)
    (reset! show-slice-time date-now)))

(defn ^:export handle_stop [event]
  (let [stop-button (.getElementById js/document "stop-btn")]
    (unchecked-set stop-button "disabled" true)
    (reset! show-slice-time nil)
    (clear-time-left)))

(defn init []
  (let [canvas (.getElementById js/document "clock")
        ctx    (.getContext canvas "2d")
        rect   (.getBoundingClientRect canvas)
        width  (.-width rect)
        height (.-height rect)
        do-draw
        (fn [] (draw ctx width height))]
    (unchecked-set canvas "width" width)
    (unchecked-set canvas "height" height)
    (do-draw)
    (start-timer do-draw)))