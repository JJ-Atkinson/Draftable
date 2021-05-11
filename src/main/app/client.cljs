(ns app.client
  (:require
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.mutations :as mut]
    [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
    [com.fulcrologic.fulcro.dom :as dom]
    [app.SPA :refer [SPA]]
    [com.fulcrologic.fulcro.react.hooks :as hooks]
    [com.fulcrologic.fulcro.mutations :as m]

    ["@codemirror/closebrackets" :refer [closeBrackets]]
    ["@codemirror/fold" :as fold]
    ["@codemirror/gutter" :refer [lineNumbers]]
    ["@codemirror/highlight" :as highlight]
    ["@codemirror/history" :refer [history historyKeymap]]
    ["@codemirror/state" :refer [EditorState]]
    ["@codemirror/view" :as view :refer [EditorView]]
    ["@codemirror/state" :as cm-state
     :refer [EditorState EditorSelection Extension StateCommand
             ChangeSet ChangeDesc TransactionSpec StrictTransactionSpec]]
    ["lezer" :as lezer]
    ["lezer-generator" :as lg]
    ["lezer-tree" :as lz-tree]
    [applied-science.js-interop :as j]
    [clojure.string :as str]
    [nextjournal.clojure-mode :as cm-clj]
    [nextjournal.clojure-mode.extensions.close-brackets :as close-brackets]
    [nextjournal.clojure-mode.extensions.formatting :as format]
    [nextjournal.clojure-mode.extensions.selection-history :as sel-history]
    [nextjournal.clojure-mode.keymap :as keymap]
    [nextjournal.clojure-mode.live-grammar :as live-grammar]
    [nextjournal.clojure-mode.node :as n]
    [nextjournal.clojure-mode.selections :as sel]
    [nextjournal.clojure-mode.test-utils :as test-utils]
    ))


(def theme
  (.theme EditorView
    (j/lit {".cm-content"             {:white-space "pre-wrap"
                                       :padding     "10px 0"}
            "&.cm-focused"            {:outline "none"}
            ".cm-line"                {:padding     "0 9px"
                                       :line-height "1.6"
                                       :font-size   "16px"
                                       :font-family "var(--code-font)"}
            ".cm-matchingBracket"     {:border-bottom "1px solid var(--teal-color)"
                                       :color         "inherit"}
            ".cm-gutters"             {:background "transparent"
                                       :border     "none"}
            ".cm-gutterElement"       {:margin-left "5px"}
            ;; only show cursor when focused
            ;".cm-cursor"              {:visibility "hidden"}
            "&.cm-focused .cm-cursor" {:visibility "visible"}})))

(defonce extensions #js[theme
                        (history)
                        highlight/defaultHighlightStyle
                        (view/drawSelection)
                        (lineNumbers)
                        (fold/foldGutter)
                        ;(.. EditorState -allowMultipleSelections (of true))
                        cm-clj/default-extensions
                        (.of view/keymap cm-clj/complete-keymap)
                        (.of view/keymap historyKeymap)])


(defn use-lifecycle [setup teardown]
  (hooks/use-effect (fn setup-effect* []
                      (when setup (setup))
                      (fn teardown-effect* [] (when teardown (teardown))))))

(defn mount-cm [element code onchange]
  (new EditorView
    (j/lit {:state  (.create EditorState
                      #js {:doc        code
                           :selection  js/undefined
                           :extensions #js[extensions]})
            :parent element})))

(defsc CodeMirrorComp [this {:keys [id code] :as props}]
  {:query         [:id :code]
   :initial-state {:id :param/id :code :param/code}
   :ident         :id
   :use-hooks?    true}
  (let [[cm-inst cm-inst!] (hooks/use-state nil)
        [textarea-inst textarea-inst!] (hooks/use-state nil)]
    (def some-inst cm-inst)
    (js/console.log cm-inst)
    ;(use-lifecycle nil #(when cm-inst (j/call cm-inst :destroy)))

    (dom/div 
      (dom/div {:classes ["rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"]
                :ref     #(when-not cm-inst (cm-inst! (mount-cm % code nil)))
                :style   {:maxHeight 400}}))
    ))

(def ui-code-mirror-comp (comp/factory CodeMirrorComp {:keyfn :id}))

(defsc Root [this
             {:keys [codemirror] :as props}]
  {:query         [{:codemirror (comp/get-query CodeMirrorComp)}]
   :initial-state {:codemirror [{:id 1 :code ";; I'm number 1"}
                                {:id 2 :code ";; I'm number 2 "}]}}

  (dom/div
    (map ui-code-mirror-comp codemirror)))


(defn ^:export init
  "Shadow-cljs sets this up to be our entry-point function. See shadow-cljs.edn `:init-fn` in the modules of the main build."
  []
  (app/mount! SPA Root "app")
  (js/console.log "Loaded"))

(defn ^:export refresh
  "During development, shadow-cljs will call this on every hot reload of source. See shadow-cljs.edn"
  []
  ;; re-mounting will cause forced UI refresh, update internals, etc.
  (app/mount! SPA Root "app")
  (js/console.log "Hot reload"))
