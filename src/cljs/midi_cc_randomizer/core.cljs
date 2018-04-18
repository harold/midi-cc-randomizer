(ns midi-cc-randomizer.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce state* (r/atom {:midi nil
                         :parameters []}))

(defn maplike->map
  [maplike]
  (reduce (fn [eax k]
            (assoc eax k (.get maplike k)))
          {}
          (js/Array.from (.keys maplike))))

(defn page
  [state*]
  [:div.page
   [:h2 "MIDI-CC-RANDOMIZER"]
   (cond
     (:error-message @state*) [:div "ERROR: " (:error-message @state*)]
     (nil? (:midi @state*)) [:div "Loading..."]
     true (let [output-map (maplike->map (.-outputs (:midi @state*)))]
            (if-not (pos? (count output-map))
              [:div "No MIDI output ports found. :("]
              [:div
               [:h3 "MIDI output ports:"]
               (into [:select#port]
                     (for [[k v] output-map]
                       [:option {:value k} (.-name v)]))
               [:div.parameter-area
                [:h3 "Parameters:"]
                (into [:div.parameters]
                      (for [{:keys [cc min max]} (:parameters @state*)]
                        [:div
                         [:tt (str "CC: 0x" (.toString cc 16) " Min: " min " Max: " max " ")]
                         [:button {:on-click (fn [e]
                                               (let [dropdown (js/document.getElementById "port")
                                                     output (get output-map (.-value dropdown))]
                                                 (let [a (array 0xB0 cc (+ min (rand-int (inc (- max min)))))]
                                                   (.send output a))))}
                          "LEARN"]]))
                [:button {:on-click (fn [e]
                                      (let [parameter-count (count (:parameters @state*))]
                                        (swap! state* update :parameters conj {:cc (+ 0x66 parameter-count)
                                                                               :min 0x00
                                                                               :max 0x7F})))}
                 "ADD"]]
               [:h3 "Go:"]
               [:div [:button {:on-click (fn [e]
                                           (let [dropdown (js/document.getElementById "port")
                                                 output (get output-map (.-value dropdown))]
                                             (doseq [{:keys [cc min max]} (:parameters @state*)]
                                               (.send output (array 0xB0 cc (+ min (rand-int (inc (- max min)))))))))}
                      "SEND"]]])))])

(defn- on-midi
  [midi]
  (swap! state* assoc :midi midi))

(defn- on-fail
  [message]
  (swap! state* assoc :error-message message))

(defn reload
  []
  (.then (.requestMIDIAccess js/navigator) on-midi on-fail)
  (->> (.getElementById js/document "app")
       (r/render [page state*])))

(defn ^:export main
  []
  (reload))
