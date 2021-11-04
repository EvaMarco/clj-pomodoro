(ns clocks.state
  (:require
   [clocks.date :as dt]
   [clocks.math :as mth]
   [clocks.utils :as util]
   [cljs.pprint :as pp]))

(def state (atom nil))

(defn add-to-history [slice-state time]
  (swap! state (fn [old-state]
                 (update old-state :history (fn [old-history]
                                              (conj old-history {:state slice-state :time time}))))))
  ;; (swap! state update :history conj {:action action :time time})

(remove-watch state :watcher)
(add-watch state :watcher (fn [_ _ old-value new-value]                            
                            (.setItem js/localStorage "state" (util/clj->str new-value))
                            (when (not= (:state old-value) (:state new-value))
                              (pp/pprint new-value)
                              (add-to-history (:state new-value) (dt/now)))
                            (let [time-label      (.getElementById js/document "time-left")]
                              (unchecked-set  time-label "textContent" (:label-text new-value)))))

(remove-watch state :stop)
(add-watch state :stop (fn [_ _ _ new-value]
                         (let [stop-button (.getElementById js/document "stop-btn")
                               start-button (.getElementById js/document "start-btn")]
                           ;;  (.log js/console (clj->js new-value))
                           (unchecked-set stop-button "disabled" (nil? (:show-slice-time new-value)))
                           (unchecked-set start-button "disabled" (some? (:show-slice-time new-value))))))

(defn get-next-state [history]
  (let [is-stop-time? #(and (not= % :time-stop) (not= % :user-stop))
        is-long-resting? (fn [[_ item]] (= item :long-resting))
        index-after?
        (fn [from-index]
          (fn [[index _]] (or (nil? from-index) (> index from-index))))
        is-counting? (fn [[_ item]] (= item :counting))
        clean-history (->>  history
                            (map :state)
                            (filter is-stop-time?))
        clean-indexed-history (map-indexed vector clean-history)
        last-long-index (->> clean-indexed-history
                             (filter is-long-resting?)
                             (last)
                             (first))
        num-counting (->> clean-indexed-history
                          (filter (index-after? last-long-index))
                          (filter is-counting?)
                          (count))
        last-state (last clean-history)]
    (if (or (= last-state :long-resting) (= last-state :resting) (nil? last-state))
      :counting
      (if (> num-counting 2)
        :long-resting
        :resting))))

(defn check-time-end [duration]
  (let [diff-seconds    (- (* duration 60) (dt/seconds-diff (dt/now) (:show-slice-time @state)))]
    (when (<= diff-seconds 0)
      (swap! state #(assoc % :state :time-stop :show-slice-time nil))
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
     (:counting :resting :long-resting) (create-time-text duration)
     :time-stop (swap! state #(assoc % :label-text "Your pomodoro time has exprired"))
     :user-stop (swap! state #(assoc % :label-text ""))
     nil)))
