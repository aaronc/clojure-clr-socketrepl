(ns clr-socketrepl.server
  (:use [clr-socketrepl.common])
  (:import [System.Net.Sockets TcpListener]) 
  (:import [|System.Byte[]|]))

(defn- eval-message
  [msg]
  (eval (read-string msg)))

(defn start-server
  [port]
  (let [server (TcpListener. port)
        bLen 256
        buffer  (|System.Byte[]|. bLen)]
    (.Start server) 
    (try (let [client (.AcceptTcpClient server)
               stream (.GetStream client)
               reader (System.IO.StreamReader. stream)
               read-buf (fn [] (recv-msg stream buffer bLen))]
           (try (loop [msg (.ReadLine reader)]
                  (when msg
                    (let [res (eval-message msg)]
                                        ;(println msg)
                                        ;(println res)
                      (send-msg stream (pr-str res))
                      (recur (read-buf)))))
                (finally (.Close client))))
         (finally (.Stop server)))))


(defn start-server-repl
  [port]
  (let [server (TcpListener. port)]
    (try (.Start server) 
         (let [client (.AcceptTcpClient server)
               stream (.GetStream client)
               reader (System.IO.StreamReader. stream)
               writer (System.IO.StreamWriter. stream)]
           (set! (.AutoFlush writer) true)
           (in-ns 'user)
                (try (let [write-fn #(.WriteLine writer %)]
                  (read-eval-print-loop
                   {:reader #(.ReadLine reader)
                    :writer write-fn
                    :err write-fn
                    :evalfn (fn [expr] (let [value (eval (read-string expr))]
                              (set! *3 *2)
                              (set! *2 *1)
                              (set! *1 value)
                              (pr-str value)))
                              }))
                     (catch Exception ex (set! *e ex)
                            (.ToString ex))
                     (finally (do
                                  (.Dispose reader)
                                  (.Dispose writer)
                                  (.Close client)))))
           (finally (.Stop server)))))