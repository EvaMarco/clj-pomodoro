(ns clocks.utils)

(defn str->clj [data-str]
  (when data-str
    (let [parsed-map (js->clj (.parse js/JSON data-str) :keywordize-keys true)
          fix-item
          (fn [item] (update item :state keyword))]
      (-> parsed-map
          (update :history #(mapv fix-item %))
          (update :state keyword)))))


(defn clj->str [data]
  (.stringify js/JSON (clj->js data)))