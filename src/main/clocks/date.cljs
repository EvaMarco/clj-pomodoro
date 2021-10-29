(ns clocks.date)

(defn parse-date [date]
  {:time   (.getTime date)
   :hour   (.getHours date)
   :minute (.getMinutes date)
   :second (.getSeconds date)})

(defn now
  []
  (parse-date (js/Date.)))

(defn seconds-diff
  "Return the difference in seconds of two timestamps"
  [{t1-time :time} {t2-time :time}]
  (/ (- t1-time t2-time) 1000))
