(ns clocks.common)

(def ^:const distance-hour 0.85)
(def ^:const initial-empty-state
  {:show-slice-time nil
   :notification-permission "denied"
   :state nil
   :history [#_{:action :start :time date}]
   :label-text ""})
(def ^:const slice-types {
                          :pomodoro-slice {:size 25 :color "rgba(255,0,0,0.2)"} 
                          :short-rest-slice {:size 5 :color "rgba(0,255,0,0.2)"}
                          :long-rest-slice {:size 12 :color "rgba(0,0,255,0.2)"}})