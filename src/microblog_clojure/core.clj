(ns microblog-clojure.core
  (:require [ring.adapter.jetty :as j]
            [compojure.core :as c]
            [hiccup.core :as h]
            [ring.middleware.params :as p]
            [ring.util.response :as r])
  (:gen-class))

(defonce messages (atom []))
(defonce server (atom nil)) ; so doesn't run agian and rewrite code when reload
; to persist on disk save to edn file and read it, / route triggers add-watch to run
(add-watch messages :save-to-disk
  (fn [_ _ _ _] ; _ b/c dont care about arguments
    (spit "messages.edn" (pr-str @messages))))


(c/defroutes app
  (c/GET "/" request
    (h/html [:html
             [:body
              [:form {:action "/add-message" :method "post"}
               [:input {:type "text" :placeholder "Enter message" :name "message"}]
               [:button {:type "submit"} "Add message"]]
              [:ol
               (map (fn [message]
                      [:li message])
                 @messages)]]]))
  (c/POST "/add-message" request
    (let [message (get (:params request) "message")]
       (swap! messages conj message)
       (r/redirect "/"))))


(defn -main []
  ; read from disk and parse
  (try
    (let [messages-str (slurp "messages.edn")
           messages-vec (read-string messages-str)] 
      (reset! messages messages-vec))
    (catch Exception _))
 (when @server
   (.stop @server))
 (reset! server (j/run-jetty (p/wrap-params app) {:port 3000 :join? false})))
