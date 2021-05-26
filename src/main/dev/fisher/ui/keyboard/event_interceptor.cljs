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

(defn evt->clj [e]
  {
   :alt?          (.-altKey e)
   :ctrl?         (.-ctrlKey e)
   :meta?         (.-metaKey e)
   :shift?        (.-shiftKey e)
   :key-code      (.-keyCode e)
   :key           (let [base (str/replace (.-key e)
                               " " "Space")]
                    (get k-const/event-key-remappings base base))
   :in-input?     (in-input? e)
   :modifier-key? (contains? k-const/modifier-keys (.-keyCode e))})

(def lut
  {:space (k-const/build-key-combo-matcher "SPC")
   :HI    (k-const/build-key-combo-matcher "ca" "H")
   :hi    (k-const/build-key-combo-matcher "ca" "h")
   :==    (k-const/build-key-combo-matcher "ca" "=")
   :=+    (k-const/build-key-combo-matcher "ca" "+")
   :bye   (k-const/build-key-combo-matcher "i" "ESC")
   :cra   (k-const/build-key-combo-matcher "ma" "*")})

(defn receive-event [e]
  ;(js/console.log e)
  ;(js/console.log (evt->clj e))
  (let [e   (evt->clj e)
        res (some (fn [[k p]]
                    (when (k-const/evt-matches? e p) k))
              lut)]
    ;(k-const/evt-matches? lastt (:space lut))
    (def lastt e)
    (js/console.log (or res (k-const/str-ify e))))
  )

(defn register-listener []
  (ev/listen
    js/document
    "keydown"
    (fn [e]
      (receive-event e))))

(defonce listener-debounce (register-listener))