(ns clocks.main
  (:require
   [clocks.draw :as d]
   [clocks.date :as dt]
   [clocks.math :as mth]
   [clocks.common :as cmm]))


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


(def state (atom nil))

(defn str->clj [data-str]
  (js->clj (.parse js/JSON data-str) :keywordize-keys true))

(defn clj->str [data]
  (.stringify js/JSON (clj->js data)))

(add-watch state :watcher (fn [_ _ old-value new-value]
                            (.setItem js/localStorage "state" (clj->str new-value))
                            (js/console.log "State changed" (clj->js old-value) (clj->js new-value))
                            ))

(add-watch state :stop (fn [_ _ _ new-value]
                         (let [stop-button (.getElementById js/document "stop-btn")]
                           (unchecked-set stop-button "disabled" (nil? (:show-slice-time new-value))))))

(defn add-to-history [action time]
  (swap! state (fn [old-state] (update old-state :history (fn [old-history] (conj old-history {:action action :time time})))))
  ;; (swap! state update :history conj {:action action :time time})
  )

(defn clear-time-left
  "Remove timer text."
  []
  (let [time-label (.getElementById js/document "time-left")]
    (unchecked-set time-label "textContent" "")))


(defn ^:export handle-start [event]
  (let [date-now    (dt/now)]
    (swap! state #(assoc % :show-slice-time date-now))
    (add-to-history :start date-now)))

(defn ^:export handle_stop [event]
  (swap! state #(assoc % :show-slice-time nil))
  (clear-time-left)
  (add-to-history :stop (dt/now)))

(defn draw [ctx width height]
  (let [x-center (/ width 2)
        y-center (/ height 2)
        radius (* (/ width 2) 0.9)
        time-now (dt/now)]
    (.clearRect ctx 0 0 width height)
    (d/circle ctx x-center y-center radius)
    (doseq [i (range 0 12)]
      (let [current-angle (* (/ 360 12) i)
            hour-start    (d/get-point x-center y-center radius current-angle cmm/distance-hour)
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

    (when-let [show-slice-time (:show-slice-time @state)]
      (draw-slice ctx x-center y-center radius show-slice-time)
      (show-time-left show-slice-time time-now))))

(defn draw-hands [ctx x-center y-center radius {:keys [hour minute second]}]
  (let [hour-angle      (* (/ 360 12) hour)
        minute-angle    (* (/ 360 60) minute)
        second-angle    (* (/ 360 60) second)
        center-clock    {:x x-center :y y-center}
        hour-hand-end   (d/get-point x-center y-center radius hour-angle 0.5)
        minute-hand-end (d/get-point x-center y-center radius minute-angle 0.8)
        second-hand-end (d/get-point x-center y-center radius second-angle 0.9)]
    (d/line ctx center-clock hour-hand-end  5 "gray")
    (d/line ctx center-clock minute-hand-end  3 "silver")
    (d/line ctx center-clock second-hand-end  1 "red")))

(defn draw-slice [ctx x-center y-center radius {:keys [minute]}]
  (let [from-angle (* (/ 360 60) minute)]
    (d/slice ctx x-center y-center radius from-angle)))

(defn time-end [label]
  (unchecked-set label "textContent" "Pomodoro time has expired")
  (swap! state #(assoc % :show-slice-time nil))
  (clear-time-left)
  (add-to-history :finish (dt/now))
  (when (= (:notification-permission @state) "granted") (js/Notification. "Your pomodoro time has exprired, Take a rest!")))

(defn show-time-left [time-start now]
              (let [time-label      (.getElementById js/document "time-left")                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         diff-seconds    (- (* cmm/pomodoro-last 60) (dt/seconds-diff now time-start))
        diff-min-result (mth/floor (/ diff-seconds 60))
        rest-sec-result (mth/floor (mod diff-seconds 60))]
    (if (< diff-seconds 0)
      (time-end time-label)
      (unchecked-set time-label "textContent" (str "You have " diff-min-result " minutes and " rest-sec-result " seconds left")))))


(defn start-timer [cb]
  (.setTimeout js/window #(do (cb) (start-timer cb)) 1000))

(defn init []
  (let [canvas      (.getElementById js/document "clock")
        ctx         (.getContext canvas "2d")
        rect        (.getBoundingClientRect canvas)
        width           (.-width rect)
        height          (.-height rect)
        initial-state   (or (str->clj (.getItem js/localStorage "state")) cmm/initial-empty-state)
        do-draw
        (fn [] (draw ctx width height))]
    (-> (.requestPermission js/Notification)
        (.then (fn [permission]
                 (swap! state #(assoc % :notification-permission permission)))))
    (reset! state initial-state)
    (unchecked-set canvas "width" width)
    (unchecked-set canvas "height" height)
    (do-draw)
    (start-timer do-draw)))
