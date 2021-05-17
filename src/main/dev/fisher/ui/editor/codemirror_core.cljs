(ns dev.fisher.ui.editor.codemirror-core
  (:require
    [com.fulcrologic.fulcro.components :as comp :refer [defsc]]
    [com.fulcrologic.fulcro.dom :as dom]
    [goog.object :as gobj]

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
    [com.fulcrologic.fulcro.mutations :as m :refer [defmutation]]
    [taoensso.timbre :as log]))

(def theme
  "Default CM css theme (per component)"
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

(defn extensions [onchange-handler]
  #js[theme
      (history)
      highlight/defaultHighlightStyle
      (view/drawSelection)
      (lineNumbers)
      (fold/foldGutter)
      ;(.. EditorState -allowMultipleSelections (of true))
      cm-clj/default-extensions
      (.of view/keymap cm-clj/complete-keymap)
      (.of view/keymap historyKeymap)

      ;; onchange-handler receives https://codemirror.net/6/docs/ref/#view.ViewUpdate 
      (-> EditorView .-updateListener (.of onchange-handler))])


;; TASK: move this out
#_(defn use-lifecycle [setup teardown]
    (hooks/use-effect (fn setup-effect* []
                        (when setup (setup))
                        (fn teardown-effect* [] (when teardown (teardown))))
      #js [0]))

(defn -mount-cm
  "Element is the parent in the dom, code is the initiail string contents, onchange is a (fn [view-update] )
   https://codemirror.net/6/docs/ref/#view.ViewUpdate"
  [element code onchange]
  (new EditorView
    (j/lit {:state  (.create EditorState
                      #js {:doc        code
                           :selection  js/undefined
                           :extensions #js[(extensions onchange)]})
            :parent element})))

(defn -doc-of [cm-or-txn]
  (-> cm-or-txn (.-state) (.-doc)))

(defn -text-of [cm-or-txn]
  (-> cm-or-txn -doc-of (.toString)))

(defmutation update-text-object [{:as props ::keys [id doc-object]}]
  (action [{:keys [state app]}]
    (js/console.log "doc-object" doc-object)
    (swap! state assoc-in [::id id ::doc-object] doc-object)))

(defn text-of*
  "Read out the current text from a mounted code mirror. This will convert the ropes to a string,
   so use sparingly."
  [state id]
  (.toString (get-in state [::id id ::doc-object])))

(defmutation save-text [{:as params ::keys [id]}]
  (remote [{:as env :keys [state]}]
    (-> env
      (m/with-server-side-mutation 'server.api.editor/save-text)
      (m/with-params {:file (::source-file params)
                      :text (text-of* @state id)}))))

(defsc CodeMirror [this props]
  {:query                [::id                ;; id
                          ::source-file       ;; path from which the code comes from
                          ::initial-code      ;; string of the code to populate the editor with, only used on first render
                          ::doc-object]       ;; the codemirror document, updated on state change https://codemirror.net/6/docs/ref/#text.Text
   :initial-state        {::id           :param/id
                          ::source-file  :param/source-file
                          ::initial-code ";; PLACEHOLDER"}
   :ident                ::id
   :initLocalState       (fn [this {::keys [initial-code id]}]
                           {:save-ref (fn [ref]
                                        (when-not (gobj/get this "cm-inst")
                                          (gobj/set this "cm-inst"
                                            (-mount-cm ref initial-code
                                              #(comp/transact! this [(update-text-object {::id id ::doc-object (-doc-of %)})])))))})
   :componentWillUnmount (fn [this]
                           (j/call (gobj/get this "cm-inst") :destroy))}
  (dom/div
    (dom/input {:onChange #(m/set-string!! this ::source-file :event %)
                :value (::source-file props)})
    (dom/button {:onClick #(df/load! this :text nil
                             {:params {:file (::source-file props)}
                              ;; TASK: update text object ???
                              })}
      "LOAD FROM DISK")
    (dom/button {:onClick #(comp/transact! this [(save-text props)])}
      (str "SAVE-" (::id props)))
    (dom/div
      (dom/div {:classes ["rounded-md mb-0 text-sm monospace overflow-auto relative border shadow-lg bg-white"]
                :ref     (comp/get-state this :save-ref)
                :style   {:maxHeight 400}}))))

(def ui-code-mirror (comp/factory CodeMirror {:keyfn ::id}))
