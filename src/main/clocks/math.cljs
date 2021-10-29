(ns clocks.math)

(defn round?
  "Check if the number `v` is close to be a round number"
  [v]
  (<= (.abs js/Math (- v (.round js/Math v))) 0.0001))


(defn floor
  "Given a float-point number gives the lowest nearest integer"
  [v]
  (.floor js/Math v))
