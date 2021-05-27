(ns dev.fisher.ui.keyboard.keyboard-constants
  (:require
    [goog.events :as ev]
    [clojure.string :as str]
    [clojure.set :as set]
    [cljs.spec.alpha :as s]
    [taoensso.encore :as enc]))


;; Partially lifted from re-pressed,
;; https://github.com/gadfly361/re-pressed/blob/master/src/main/re_pressed/impl.cljs 

(ev/unlistenByKey)

(def modifier-keys #{16                 ;; shift
                     17                 ;; ctrl
                     18                 ;; alt
                     91                 ;; windows (i.e. meta) 
                     })

(s/def ::alt? boolean?)
(s/def ::ctrl? boolean?)
(s/def ::meta? boolean?)
(s/def ::shift? boolean?)
(s/def ::in-input? boolean?)
(s/def ::key string?)
(s/def ::key-code int?)
(s/def ::key-combo
  (s/keys
    :req-un [::alt? ::ctrl? ::meta? ::in-input?
             (or ::key ::key-code)]
    :opt-un [::shift?]))

(s/def ::modifier-key? boolean?)
(s/def ::key-event
  (s/merge
    ::key-combo
    (s/keys :req-un [::modifier-key?])))

(def event-key-remappings
  "Rename some of the weird key names from goog.events. Only used when 
   intercepting raw events"
  {"Space"   "SPC"
   "Escape"  "ESC"
   "Meta"    "meta"
   "Shift"   "shift"
   "Alt"     "alt"
   "Control" "ctrl"})

(def modifier-shorthand-chars
  "Character map for `build-key-combo-matcher`"
  {"s" :shift
   "m" :meta
   "a" :alt
   "c" :ctrl
   "i" :in-input})

(def modifier-shorthand->evt-prop
  "Maps from a modifier shorthand (`:shift`) to the matching property on 
   keyboard events (`:shift?`)"
  {:shift    :shift?
   :meta     :meta?
   :alt      :alt?
   :ctrl     :ctrl?
   :in-input :in-input?})

(defonce
  ^{:doc "Simple map for us based keyboards for {[lower upper] key-code}"}
  lower-upper->key-code
  (let [lowerUPPER->key-code
                (into {} (map (fn [x] [(str (str/lower-case (char x)) (char x))
                                       x])
                           (range 65 91)))
        numbers (zipmap
                  (partition 2 "0)1!2@3#4$5%6^7&8*9(")
                  (range 48 58))
        special-chars
                (zipmap (partition 2 "=+,<-_.>/?`~")
                  (range 187 193))]
    (merge
      lowerUPPER->key-code
      numbers
      special-chars
      {"[{"  219
       "\\|" 220
       "]}"  221
       "'\"" 222})))

;; https://github.com/google/closure-library/blob/master/closure/goog/events/keycodes.js#L27
;; slightly modified for SPC, ESC, and DEL
(defonce simple-key->key-code
  ^{:doc "The simple key -> keycode combinations that don't care about case. e.g.
          F*, Enter, Space, Back, etc."}
  (merge
    (zipmap (map (fn [idx] (str "F" idx)) (range 1 13))
      (range 112 124))
    {"BACK"      8
     "TAB"       9
     "ENTER"     13
     ;; Modifier keys could go here, but since we don't care about any of them for 
     ;; single key shortcuts I've not bothered to enter them.
     "CAPS_LOCK" 20
     "SPC"       32
     "ESC"       27
     "END"       35
     "HOME"      36
     "LEFT"      37
     "UP"        38
     "RIGHT"     39
     "DOWN"      40
     "INSERT"    45
     "DEL"       46
     "PHANTOM"   255                    ;; Debugging?? not entirely sure this will be needed
     }))

(defonce
  ^{:doc
    "This is a translation map from keys (\"H\", \"(\", ...) to the appropriate
    base key match. Converts to keycode + (?space = true) as needed to enable 
    maches in most reasonable cases. This is *ONLY* designed for us based keyboard
    layouts."}
  key->keycode+modifier
  (into {}
    (map (fn [[[lower upper] key-code]]
           {lower {:key-code key-code}
            upper {:key-code key-code
                   :shift?   true}})
      lower-upper->key-code)))

(let [kc->lower-upper (set/map-invert lower-upper->key-code)
      kc->key         (set/map-invert simple-key->key-code)]
  (defn str-ify
    "Take an event (or key combo matcher built by `build-key-combo-matcher`) and 
     convert it to a string. Format is \"(modifiers-)?[key key-code]\". In the event
     a key-code + shift is used to indicate an upper case key, the key is used instead
     of the keycode and the shift modifier."
    [evt-or-combo-matcher]
    (let [{:keys [shift? key-code]} evt-or-combo-matcher
          [lower upper :as cased?] (get kc->lower-upper key-code)
          modifiers (str
                      (when (and shift? (not cased?)) "s")
                      (when (:meta? evt-or-combo-matcher) "m")
                      (when (:alt? evt-or-combo-matcher) "a")
                      (when (:ctrl? evt-or-combo-matcher) "c")
                      (when (:in-input? evt-or-combo-matcher) "i"))

          kc-or-key (or (when cased? (if shift? upper lower))
                      (:key evt-or-combo-matcher)
                      (kc->key key-code)
                      key-code)]
      (str
        modifiers
        (when (seq modifiers) "-")
        kc-or-key))))

(defn build-key-combo-matcher
  "Build a `combo-matcher` for `evt-matches`. If called with a map in single-arity, 
   returns the map. Converts upper case `k` (e.g. \"A\", \"+\") into keycodes with the modifier
   `:shift`, since the system ignores capslock. Works only on us-based keyboard
   layouts. See `key->keycode+modifier` for details.
   
   ```
   Props:
   k          Either a number (key-code), or a string (key), where key may be 
                re-written into a keycode + (?shift) if required
   modifiers  Either a vector of modifiers (`[:alt :ctrl :meta :shift :in-input]`),
                or a string for shorthand (\"acmsi\")
   ```"
  ([k]
   (if (map? k)
     k
     (build-key-combo-matcher nil k)))
  ([modifiers k]
   (let [modifiers    (if (string? modifiers)
                        (map modifier-shorthand-chars modifiers)
                        modifiers)
         base         {:alt?      false
                       :ctrl?     false
                       :meta?     false
                       :shift?    false
                       :in-input? false}
         with-mod     (reduce (fn [b k]
                                (assoc b
                                  (modifier-shorthand->evt-prop k) true))
                        base
                        modifiers)
         k->kc?       (get simple-key->key-code k k)
         type         (if (string? k->kc?) :key :key-code)
         cased-letter (get key->keycode+modifier k)
         ]
     (if cased-letter
       (merge with-mod cased-letter)
       (assoc with-mod type k->kc?)))))

(defn evt-matches? [evt combo-matcher]
  (and
    (if (contains? combo-matcher :key)
      (= (:key evt) (:key combo-matcher))
      (= (:key-code evt) (:key-code combo-matcher)))
    (= (:alt? evt) (:alt? combo-matcher))
    (= (:ctrl? evt) (:ctrl? combo-matcher))
    (= (:meta? evt) (:meta? combo-matcher))
    (= (:in-input? evt) (:in-input? combo-matcher))
    (or (not (contains? combo-matcher :shift?))
      (= (:shift? evt) (:shift? combo-matcher)))))

(defn build-key-combo-tree
  "Take a map of vector -> any, and build a new nested map based on the contents of the vector. 
   e.g. 
   ```
   {[\"r\" \"K\"] {:action :rK}
    [\"r\" \"N\"] {:action :rN}}
   => {\"r\" {\"K\" {:action :rK}
            \"N\" {:action :rN}}
   ```"
  [combos]
  (reduce (fn [acc [k v]]
            (assoc-in acc k v))
    {}
    combos))

(defn printable-key-combo-tree
  "Take the result of `build-key-combo-tree` and replace all instances of key-combo-matcher
   with `str-ify` so tis actually readable."
  [tree]
  (cond-> tree

    (map? tree)
    (->> (enc/map-keys printable-key-combo-tree)
      (enc/map-vals printable-key-combo-tree))

    (or (contains? tree :key-code) (contains? tree :key))
    (str-ify)))

(comment
  (printable-key-combo-tree
    (build-key-combo-tree
      (enc/map-keys (partial map (partial apply build-key-combo-matcher))
        {[["K"]]               :K
         [["c" "N"] ["N"]]     :c-N_N
         [["c" "N"] ["m" "R"]] :c-N_m-R
         [["L"] ["M"]]         :L_M
         [["L"] ["F"]]         :L_F})))
  {"K"   :K
   "c-N" {"N"   :c-N_N
          "m-R" :c-N_m-R}
   "L"   {"M" :L_M
          "F" :L_F}})