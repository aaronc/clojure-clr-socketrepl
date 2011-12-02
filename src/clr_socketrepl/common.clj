(ns clr-socketrepl.common)

(defn from-bytes [buf len] (.GetString System.Text.Encoding/ASCII buf 0 len))

(defn to-bytes [msg] (.GetBytes System.Text.Encoding/ASCII msg))

(defn send-msg [stream msg] (let [data (to-bytes msg)] (.Write stream data 0 (.Length data))))

(defn recv-msg [stream buf len] (let [ i (.Read stream buf 0 len)] (if (zero? i) nil (from-bytes buf i))))

(defn read-eval-print-loop
  [{:keys [reader evalfn writer err]}]
  (do
    (loop [msg (reader)]
      (when msg
                                        ;(println (str "Received: " msg))
        (try (let [res (evalfn msg)]
                                        ;(println (str "Writing: " res))
               (writer res))
             (catch Exception ex (do
                                        ;(println (str "Exception: " (.ToString ex)))
                                   (err (.ToString ex)))))
        (recur (reader))))))