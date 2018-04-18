(defproject midi-cc-randomizer "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.238"]
                 [reagent "0.7.0"]]

  :source-paths ["src/clj"]

  :plugins [[lein-cljsbuild "1.1.7"]]

  :clean-targets ^{:protect false} ["resources/public/js"
                                    "target"]

  :figwheel {:css-dirs ["resources/public/css"]}

  :profiles {:dev {:plugins      [[lein-figwheel "0.5.15"]]}}

  :cljsbuild {:builds
              [{:id           "dev"
                :source-paths ["src/cljs"]
                :figwheel     {:on-jsload "midi-cc-randomizer.core/reload"}
                :compiler     {:main                 midi-cc-randomizer.core
                               :optimizations        :none
                               :output-to            "resources/public/js/app.js"
                               :output-dir           "resources/public/js/dev"
                               :asset-path           "js/dev"
                               :source-map-timestamp true}}

               {:id           "min"
                :source-paths ["src/cljs"]
                :compiler     {:main            midi-cc-randomizer.core
                               :optimizations   :advanced
                               :output-to       "resources/public/js/app.js"
                               :output-dir      "resources/public/js/min"
                               :elide-asserts   true
                               :closure-defines {goog.DEBUG false}
                               :pretty-print    false}}]})
