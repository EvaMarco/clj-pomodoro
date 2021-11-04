(ns clocks.utils)

(defn str->clj [data-str]
  (when data-str
    (let [parsed-map (js->clj (.parse js/JSON data-str) :keywordize-keys true)
          fix-item
          (fn [item] (update item :state keyword))]
      (update parsed-map :history #(mapv fix-item %))
      (update parsed-map :state keyword))))


(defn clj->str [data]
  (.stringify js/JSON (clj->js data)))