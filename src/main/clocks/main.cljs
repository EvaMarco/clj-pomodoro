(ns clocks.main
  (:require
   [clocks.draw :as d]
   [clocks.date :as dt]
   [clocks.common :as cmm]
   [clocks.utils :as util]
   [clocks.state :as st]))

(declare init)

(defn ^:dev/before-load stop
  [])

(defn ^:dev/after-load reload
  []
  (init))


;; LIVE CODING

(defn ^:export handle-start [event]
  (let [date-now    (dt/now)
        next-state (st/get-next-state (:history @st/state))]
    (swap! st/state #(assoc % :show-slice-time date-now))
    (swap! st/state #(assoc % :state next-state))))


(defn ^:export handle_stop [event]
  (swap! st/state #(assoc % :show-slice-time nil))
  (swap! st/state #(assoc % :state :user-stop))
  (st/update-label))

(defn ^:export handle-reset [event]
  (reset! st/state cmm/initial-empty-state))
  

(defn update-clock [ctx width height]
  (let [x-center (/ width 2)
        y-center (/ height 2)
        radius   (* (/ width 2) 0.9)]
    (.clearRect ctx 0 0 width height)
    (d/draw-clock ctx x-center y-center radius (dt/now))

    (when-let [show-slice-time (:show-slice-time @st/state)]
      (let [type (case (:state @st/state)
                   :counting (:pomodoro-slice cmm/slice-types)
                   :resting (:short-rest-slice cmm/slice-types)
                   :long-resting (:long-rest-slice cmm/slice-types))]
        (d/draw-slice ctx x-center y-center radius show-slice-time type)
        (st/check-time-end (:size type))
        (st/update-label (:size type)))))
)

(defn start-timer [cb]
  (.setTimeout js/window #(do (cb) (start-timer cb)) 1000))

(defn init []
  (let [canvas          (.getElementById js/document "clock")
        ctx             (.getContext canvas "2d")
        rect            (.getBoundingClientRect canvas)
        width           (.-width rect)
        height          (.-height rect)
        initial-state   (or (util/str->clj (.getItem js/localStorage "state")) cmm/initial-empty-state)
        update
        (fn [] (update-clock ctx width height))]
    (-> (.requestPermission js/Notification)
        (.then (fn [permission]
                 (swap! st/state #(assoc % :notification-permission permission)))))
    (reset! st/state initial-state)
    (unchecked-set canvas "width" width)
    (unchecked-set canvas "height" height)
    (update)
    (start-timer update)))
  
