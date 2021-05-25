(ns dev.fisher.ui.editor.codemirror-one-dark-theme
  (:require
    ["@codemirror/highlight" :as highlight :refer [HighlightStyle tags]]
    ["@codemirror/view" :as view :refer [EditorView]]
    [applied-science.js-interop :as j]))



;; Stolen from https://github.com/codemirror/theme-one-dark/blob/main/src/one-dark.ts
;; didn't use the package because the original code was borked and I didn't want to take time and figure
;; out how to contribute the fix back.

(def chalky "#e5c07b")
(def coral "#e06c75")
(def cyan "#56b6c2")
(def invalid "#ffffff")
(def ivory "#abb2bf")
(def stone "#7d8799")                   ;;  Brightened compared to original to increase contrast
(def malibu "#61afef")
(def sage "#98c379")
(def whiskey "#d19a66")
(def violet "#c678dd")
(def darkBackground "#21252b")
(def highlightBackground "#2c313a")
(def background "#282c34")
(def selection "#3E4451")
(def cursor "#528bff")
;; match-brackets has a base theme


(def one-dark-theme-obj
  (clj->js {".cm-panels.cm-panels-bottom"                 {"borderTop" "2px solid black"}
            ".cm-editor" {"border" "none"}
            ".cm-tooltip"                                 {"border"          "1px solid #181a1f"
                                                           "backgroundColor" darkBackground}

            "&.cm-focused .cm-selectionBackground, .cm-selectionBackground, ::selection"
                                                          {"backgroundColor" selection}
            ".cm-searchMatch.cm-searchMatch-selected"     {"backgroundColor" "#6199ff2f"}
            "&.cm-focused .cm-cursor"                     {"borderLeftColor" cursor}
            ".cm-panels.cm-panels-top"                    {"borderBottom" "2px solid black"}
            ".cm-activeLineGutter"                        {"backgroundColor" highlightBackground}
            ".cm-gutters"                                 {"backgroundColor" background
                                                           "color"           stone
                                                           "border"          "none"}
            ".cm-panels"                                  {"backgroundColor" darkBackground
                                                           "color"           ivory}
            ".cm-activeLine"                              {"backgroundColor" highlightBackground}
            ".cm-tooltip-autocomplete"                    {"& > ul > li[aria-selected]" {"backgroundColor" highlightBackground
                                                                                         "color"           ivory}}
            ".cm-selectionMatch"                          {"backgroundColor" "#aafe661a"}
            ".cm-searchMatch"                             {"backgroundColor" "#72a1ff59"
                                                           "outline"         "1px solid #457dff"}
            ".cm-content"                                 {"caretColor" cursor}
            "&"                                           {"color"           ivory
                                                           "backgroundColor" background}
            ".cm-foldPlaceholder"                         {"backgroundColor" "transparent"
                                                           "border"          "none"
                                                           "color"           "#ddd"}
            ".cm-matchingBracket, .cm-nonmatchingBracket" {"backgroundColor" "#bad0f847"
                                                           "outline"         "1px solid #515a6b"}}))
(def one-dark-theme
  (.theme EditorView
    one-dark-theme-obj
    #js {:dark true}))

(def one-dark-highlight-style
  (.define HighlightStyle
    (clj->js [{:tag   (.-keyword tags)
               :color violet}
              {:tag   [(.-name tags), (.-deleted tags), (.-character tags), (.-propertyName tags), (.-macroName tags)],
               :color coral},
              {:tag   [(.function tags (.-variableName tags)), (.-labelName tags)],
               :color malibu},
              {:tag   [(.-color tags), (.constant tags (.-name tags)), (.standard tags (.-name tags))],
               :color whiskey},
              {:tag   [(.definition tags (.-name tags)), (.-separator tags)],
               :color ivory},
              {:tag   [(.-typeName tags), (.-className tags), (.-number tags), (.-changed tags)
                       (.-annotation tags), (.-modifier tags), (.-self tags), (.-namespace tags)],
               :color chalky},
              {:tag   [(.-operator tags), (.-operatorKeyword tags), (.-url tags)
                       (.-escape tags), (.-regexp tags), (.-link tags), (.special tags (.-string tags))],
               :color cyan},
              {:tag   [(.-meta tags), (.-comment tags)],
               :color stone},
              {:tag        (.-strong tags),
               :fontWeight "bold"},
              {:tag       (.-emphasis tags),
               :fontStyle "italic"},
              #_{:tag            (.-strikethrough tags),
                 :textDecoration "line-through"},
              {:tag            (.-link tags),
               :color          stone,
               :textDecoration "underline"},
              {:tag        (.-heading tags),
               :fontWeight "bold",
               :color      coral},
              {:tag   [(.-atom tags), (.-bool tags), (.special tags (.-variableName tags))],
               :color whiskey},
              {:tag   [(.-processingInstruction tags), (.-string tags), (.-inserted tags)],
               :color sage},
              {:tag   (.-invalid tags),
               :color invalid}])))