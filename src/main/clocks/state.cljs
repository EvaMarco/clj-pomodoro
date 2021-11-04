(ns clocks.state
  (:require
   [clocks.date :as dt]
   [clocks.math :as mth]
   [clocks.utils :as util]))

(def state (atom nil))


(add-watch state :watcher (fn [_ _ _ new-value]
                            ;; (pp/pprint new-value)
                            (.setItem js/localStorage "state" (util/clj->str new-value))
                            (let [time-label      (.getElementById js/document "time-left")]
                              (unchecked-set  time-label "textContent" (:label-text new-value))
                              )))


(add-watch state :stop (fn [_ _ _ new-value]
                         (let [stop-button (.getElementById js/document "stop-btn")
                               start-button (.getElementById js/document "start-btn")
                               start-rest-button (.getElementById js/document "start-rest-btn")]
                           ;;  (.log js/console (clj->js new-value))
                           (unchecked-set stop-button "disabled" (nil? (:show-slice-time new-value)))
                           (unchecked-set start-button "disabled" (some? (:show-slice-time new-value)))
                           (unchecked-set start-rest-button "disabled" (some? (:show-slice-time new-value))))))

(defn add-to-history [action time]
  (swap! state (fn [old-state]
                 (update old-state :history (fn [old-history]
                                              (conj old-history {:action action :time time}))))))
  ;; (swap! state update :history conj {:action action :time time})

(defn get-next-state [history actual-state])
  ;; (let [counter (+ (count history) 1)
  ;;       reminder (rem counter 12)]
  ;;   (.log js/console  "actual-state" (clj->js actual-state))
  ;;   (.log js/console  "history" (clj->js history))
  ;;   (.log js/console  "counter" counter)
  ;;   (.log js/console  "reminder" reminder)
  ;;   (cond
  ;;     (some #(= reminder %) [1 5 9]) (.log js/console  "start-counting")
  ;;     (some #(= reminder %) [3 7]) (.log js/console  "start-short-rest")
  ;;     (some #(= reminder %) [11]) (.log js/console  "start-long-rest")
  ;;     :else (.log js/console  "stop"))

  ;;   )


(defn check-time-end [duration]
  (let [diff-seconds    (- (* duration 60) (dt/seconds-diff (dt/now) (:show-slice-time @state)))]
    (when (<= diff-seconds 0)
      (swap! state #(assoc % :state :time-stop))
      (swap! state #(assoc % :show-slice-time nil))
      (add-to-history :finish (dt/now))
      (when (= (:notification-permission @state) "granted")
        (js/Notification. "Your pomodoro time has exprired, Take a rest!")))
    ))

(defn create-time-text [duration]
  (let [diff-seconds    (- (* duration 60) (dt/seconds-diff (dt/now) (:show-slice-time @state)))
        diff-min-result (mth/floor (/ diff-seconds 60))
        rest-sec-result (mth/floor (mod diff-seconds 60))]
    (swap! state #(assoc % :label-text (str "You have " diff-min-result " minutes and " rest-sec-result " seconds left")))
    ))

(defn update-label
  "Update label texto in state"
  ([]
   (update-label nil))
  ([duration]
   (case (:state @state)
     (:counting :resting) (create-time-text duration)
     :time-stop (swap! state #(assoc % :label-text "Your pomodoro time has exprired"))
     :user-stop (swap! state #(assoc % :label-text ""))
     nil)))
