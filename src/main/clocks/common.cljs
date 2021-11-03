(ns clocks.common)

(def ^:const distance-hour 0.85)
(def ^:const pomodoro-last 5)
(def ^:const initial-empty-state 
  {:show-slice-time nil
   :notification-permission "denied"
   :history [#_{:action :start :time date}]})