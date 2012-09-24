(ns nightweb.browser
  (:require [splendid.jfx :as jfx])
  (:import (javafx.scene.layout VBox HBox)
           (javafx.scene.control ToolBar TabPane Tab Button TextField)
           javafx.scene.web.WebView
           javafx.scene.layout.Priority
           javafx.scene.text.Font
           javafx.beans.value.ChangeListener))

(defn truncate-title
  "Limits how long the title can be."
  [title max-size]
  (if (> (.length title) max-size)
    (str (subs title 0 max-size) "...")
    title))

(defn get-browser-title
  "Returns the best title for the given browser."
  [web-engine]
  (if-let [title (.getTitle web-engine)]
    title
    (.getLocation web-engine)))

(defn create-browser-tab
  "Create a new browser tab."
  []
  (let [new-tab (Tab. "Browser")
        tab-view (VBox.)
        nav-bar (ToolBar.)
        reload-icon "⟳"
        stop-icon "x"
        back-btn (Button. "<")
        for-btn (Button. ">")
        reload-btn (Button. reload-icon)
        url-field (TextField.)
        web-view (WebView.)
        web-engine (.getEngine web-view)]
    ; resize the controls
    (.setFont back-btn (Font. 16))
    (.setFont for-btn (Font. 16))
    (.setFont reload-btn (Font. 16))
    (.setMinHeight url-field 28)
    (HBox/setHgrow url-field Priority/ALWAYS)
    (VBox/setVgrow web-view Priority/ALWAYS)
    ; set actions for the controls
    (jfx/defhandler :onAction back-btn
                    (if (> (.getCurrentIndex (.getHistory web-engine)) 0)
                      (.go (.getHistory web-engine) -1)))
    (jfx/defhandler :onAction for-btn
                    (if (< (+ (.getCurrentIndex (.getHistory web-engine)) 1)
                           (.size (.getEntries (.getHistory web-engine))))
                      (.go (.getHistory web-engine) 1)))
    (jfx/defhandler :onAction reload-btn
                    (if (= (.getText reload-btn) reload-icon)
                      (.reload web-engine)
                      (.cancel (.getLoadWorker web-engine))))
    (jfx/defhandler :onAction url-field
                    (.load web-engine (.getText url-field)))
    ; things to do the loading state changes
    (.addListener
      (.stateProperty
        (.getLoadWorker web-engine))
      (proxy [ChangeListener] []
        (changed
          [ov oldState newState]
          (if (= newState javafx.concurrent.Worker$State/RUNNING)
            (do
              (.setText reload-btn stop-icon)
              (.setText url-field (.getLocation web-engine)))
            (do
              (.setText reload-btn reload-icon)
              (.setText new-tab (truncate-title (get-browser-title web-engine) 15)))))))
    ; add everything to the window
    (.setAll (.getItems nav-bar) [back-btn for-btn reload-btn url-field])
    (jfx/add tab-view [nav-bar web-view])
    (.setContent new-tab tab-view)
    new-tab))