(ns midi-cc-randomizer.core
  (:require [reagent.core :as r]))

(enable-console-print!)

(defonce state* (r/atom {:midi nil
                         :parameters (vec (repeat 0x80 nil))
                         :group-colors ["rgb(230,159,0)"
                                        "rgb(86,180,233)"
                                        "rgb(0,158,115)"
                                        "rgb(204,121,167)"]
                         :selected-group-index 0}))

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
     :else (let [output-map (maplike->map (.-outputs (:midi @state*)))]
             (if-not (pos? (count output-map))
               [:div "Midi connected, but no output ports found. :("]
               [:div
                [:div.output-port-area
                 [:h3 "MIDI output ports:"]
                 (into [:select#port]
                       (for [[k v] output-map]
                         [:option {:value k} (.-name v)]))]
                [:div.group-area
                 [:h3 "Groups"]
                 [:div {:style {:margin-bottom "4px"}}
                  [:button {:on-click (fn [e]
                                        (let [dropdown (js/document.getElementById "port")
                                              output (get output-map (.-value dropdown))]
                                          (doseq [cc (range 0x80)]
                                            (.send output (array 0xB0 cc (rand-int 0x80))))))}
                   "SEND ALL"]
                  " "
                  [:button {:on-click (fn [e]
                                        (let [dropdown (js/document.getElementById "port")
                                              output (get output-map (.-value dropdown))]
                                          (doseq [cc (range 0x80)]
                                            (when (get-in @state* [:parameters cc :group-index])
                                              (.send output (array 0xB0 cc (rand-int 0x80)))))))}
                   "SEND ALL GROUPED"]]
                 (into [:div.groups]
                       (for [[i c] (map-indexed vector (:group-colors @state*))]
                         [:div.group
                          [:span {:style {:vertical-align :middle
                                          :display :inline-block
                                          :width "2em" :height "2em" :background c
                                          :border (if (= i (:selected-group-index @state*))
                                                    "solid 2px black"
                                                    "solid 2px white")}
                                  :on-click (fn [e] (swap! state* assoc :selected-group-index i))}
                           " "]
                          " "
                          [:button {:on-click (fn [e]
                                                (let [dropdown (js/document.getElementById "port")
                                                      output (get output-map (.-value dropdown))]
                                                  (doseq [cc (range 0x80)]
                                                    (when (= i (get-in @state* [:parameters cc :group-index]))
                                                      (.send output (array 0xB0 cc (rand-int 0x80)))))))}
                           "SEND"]]))]
                [:div.parameter-area
                 [:h3 "Parameters:"]
                 [:table {:style {:border-collapse :collapse}}
                  [:thead]
                  (into [:tbody]
                        (for [row-index (range 32)]
                          (into [:tr]
                                (for [col-index (range 4)]
                                  (let [cc (+ col-index (* 4 row-index))]
                                    [:td {:style (merge
                                                  {:padding-bottom "3px"
                                                   :padding-right (if (not= 3 col-index) "8px" "0px")
                                                   :cursor :pointer}
                                                  (when-let [idx (get-in @state* [:parameters cc :group-index])]
                                                    {:background (get-in @state* [:group-colors idx])}))}
                                     [:tt {:on-click (fn [e] (swap! state* assoc-in [:parameters cc] {:group-index (:selected-group-index @state*)}))}
                                      (str "0x" (if (<= cc 0xf) "0" "") (.toString cc 16))]
                                     " "
                                     [:button {:on-click (fn [e]
                                                           (let [dropdown (js/document.getElementById "port")
                                                                 output (get output-map (.-value dropdown))]
                                                             (.send output (array 0xB0 cc (rand-int 0x80)))))}
                                      "TAP"]])))))]]])))])

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
