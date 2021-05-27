(ns dev.fisher.ui.keyboard.event-interceptor
  (:require
    [goog.events :as ev]
    [dev.fisher.ui.keyboard.keyboard-constants :as k-const]
    [taoensso.timbre :as log]
    [clojure.string :as str]))

(defn in-input?
  "Determines if the keyboard event was targeted at a known input method.
   e.g. returns `true` when capturing events while typing into a `dom/input`.
   This considers any element with `contentEditable` true to be an input. 
   (this addresses the fact that codemirror uses divs). "
  [e]
  (let [tag-name (.-tagName (.-target e))]
    (or (= (.-contentEditable (.-target e)) "true")
      (contains?
        #{"INPUT"
          "SELECT"
          "TEXTAREA"} tag-name))))

(defn evt->clj
  "Convert a key(up|down) event from goog.events into a clojure friendly map.
   Format maches everything in `d.f.u.k.keyboard-constants`"
  [e]
  {:alt?          (.-altKey e)
   :ctrl?         (.-ctrlKey e)
   :meta?         (.-metaKey e)
   :shift?        (.-shiftKey e)
   :key-code      (.-keyCode e)
   :key           (let [base (str/replace (.-key e)
                               " " "Space")]
                    (get k-const/event-key-remappings base base))
   :in-input?     (in-input? e)
   :modifier-key? (contains? k-const/modifier-keys (.-keyCode e))})

(defn register-document-listener
  "type    \"keyup\" or \"keydown\"
   f       (fn [clj-event] ...), see `evt->clj`"
  [type f]
  (ev/listen
    js/document
    type
    (fn [e] (f (evt->clj e)))))

(comment 
  (register-document-listener "keydown" (comp println k-const/str-ify)))