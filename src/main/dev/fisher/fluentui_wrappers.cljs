(ns dev.fisher.fluentui-wrappers
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
            [taoensso.encore :as enc]
            ["@fluentui/react" :refer (PrimaryButton Stack DefaultButton ThemeProvider PartialTheme
                                        createTheme Dropdown DropdownMenuItemType Text Layer)]
            [taoensso.timbre :as log]))


;; https://fabricweb.z5.web.core.windows.net/pr-deploy-site/refs/heads/master/theming-designer/index.html
(def dark-theme
  (createTheme
    #js {:palette #js {:themePrimary         "#b66eff"
                       :themeLighterAlt      "#07040a"
                       :themeLighter         "#1d1229"
                       :themeLight           "#37214d"
                       :themeTertiary        "#6d4299"
                       :themeSecondary       "#a060e0"
                       :themeDarkAlt         "#be7cff"
                       :themeDark            "#c891ff"
                       :themeDarker          "#d6aeff"
                       :neutralLighterAlt    "#1c1c1c"
                       :neutralLighter       "#252525"
                       :neutralLight         "#343434"
                       :neutralQuaternaryAlt "#3d3d3d"
                       :neutralQuaternary    "#454545"
                       :neutralTertiaryAlt   "#656565"
                       :neutralTertiary      "#e9e9e9"
                       :neutralSecondary     "#ececec"
                       :neutralPrimaryAlt    "#f0f0f0"
                       :neutralPrimary       "#dddddd"
                       :neutralDark          "#f7f7f7"
                       :black                "#fbfbfb"
                       :white                "#111111"}}))

(def theme-provider (react-interop/react-factory ThemeProvider))

(def primary-button (react-interop/react-factory PrimaryButton))

(def dd_item-header (.-Header DropdownMenuItemType))
(def dd_item-divider (.-Divider DropdownMenuItemType))


(def -cached-option-converter
  "Converts a dropdown option map into a js dropdown option map"
  (let [rw-key (fn [m] (update m :key (fn [k] (hash k))))]
    (enc/memoize 20 (* 1000 60 60)
      (fn [opts]
        (let [updated (map rw-key opts)]
          [(clj->js updated) (into {} (map vector (map :key updated) (map :key opts)))])))))


(let [dropdown-factory (react-interop/react-factory Dropdown)]
  (defn dropdown
    "Wraps FUI dropdown with a much nicer :options and :onChange interface.
    
     {:label       Label
      :options     A vector of maps, 
                    [{:key       Key for both :selected and :onChange
                      :text      Text on screen
                      ?:itemType `fui/dd_item-.*`, is the visual style of the item
                      ?:disabled Is the option disabled 
                     } ...]
      :selected    Key from your options map or nil
      :onChange    (fn [new-key] ...)
      :placeholder Visual placeholder
      :styles      Use `with-dropdown-styles` instead
      }"
    [{:keys [label options selected onChange placeholder styles] :as props}
     & children]
    (let [[js-opts hash->key] (-cached-option-converter options)]
      (apply dropdown-factory
        #js {:label       label
             :styles      styles
             :placeholder placeholder
             :selectedKey (if selected (hash selected) js/undefined)
             :options     js-opts
             :onChange    (fn [elem item]
                            (let [actual-key (get hash->key (.-key item))]
                              (onChange actual-key)))}
        children))))

(defn with-dropdown-styles
  ""
  [style opts]
  (assoc opts :styles style))

(def stack
  "https://developer.microsoft.com/en-us/fluentui#/controls/web/stack"
  (react-interop/react-factory Stack))

(def stack-item 
  (react-interop/react-factory (.-Item Stack)))

(defn vstack [opts & children] (apply stack (merge {:horizontal false} opts) children))
(defn hstack [opts & children] (apply stack (merge {:horizontal true} opts) children))

(def text (react-interop/react-factory Text))

(defn Stext [& strs] (apply text #js {:variant "small"} strs))
(defn Mtext [& strs] (apply text #js {:variant "medium"} strs))
(defn M+text [& strs] (apply text #js {:variant "mediumPlus"} strs))


;; see grouped list

(def layer (react-interop/react-factory Layer))