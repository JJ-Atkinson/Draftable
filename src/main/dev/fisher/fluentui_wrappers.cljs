(ns dev.fisher.fluentui-wrappers
  (:require [com.fulcrologic.fulcro.algorithms.react-interop :as react-interop]
            [taoensso.encore :as enc]
            ["@fluentui/react" :refer (PrimaryButton Stack DefaultButton ThemeProvider PartialTheme
                                        createTheme Dropdown DropdownMenuItemType Text Layer
                                        TextField Pivot PivotItem Label TooltipHost
                                        DirectionalHint)]
            ["@fluentui/react/lib/Icons" :refer (initializeIcons )]
            ["@fluentui/react/lib/Icon" :refer (FontIcon )]
            [taoensso.timbre :as log]
            [com.fulcrologic.fulcro.dom :as dom]))


;; IF TRY_PREVENT_FIRST_ARG_NOT_MAP_ERROR is enabled,
;;  WARN_FIRST_ARG_NOT_MAP *MUST* be enabled too
(goog-define WARN_FIRST_ARG_NOT_MAP false)
(goog-define TRY_PREVENT_FIRST_ARG_NOT_MAP_ERROR false)

(def -interop-factory
  "Use `-interop-factory` for user-facing safety checking. It *will not* recognize 
   #js maps as first args, and will error. In cases where you know #js props are being 
   passed in, use `react-interop/react-factory` instead"
  (if WARN_FIRST_ARG_NOT_MAP
    (fn [react-factory]
      (if-not react-factory
        (constantly (dom/div "YOU FORGOT TO IMPORT THE FACTORY"))
        (let [fulcro-factory (react-interop/react-factory react-factory)]
          (fn [& args]
            (let [error? (not (map? (first args)))]
              (when error?
                (log/warn "Factory is not being called with props, check your call."))
              (if (and error? TRY_PREVENT_FIRST_ARG_NOT_MAP_ERROR)
                (dom/div "CHECK PROPS")
                (apply fulcro-factory args)))))))
    react-interop/react-factory))

(def -prop-merge
  (if WARN_FIRST_ARG_NOT_MAP
    (fn [& maps]
      (if (every? map? maps)
        (apply merge maps)
        (do
          (log/error "Calling merge on non-existent map")
          {})))
    merge))

;; https://github.com/microsoft/fluentui/wiki/Using-icons
(defonce -icon-loading
  (do (initializeIcons)))

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

(def theme-provider (-interop-factory ThemeProvider))

(def button "Props: `{:text str :onClick fn}`" (-interop-factory DefaultButton))
(def primary-button "Props: `{:text str :onClick fn}`" (-interop-factory PrimaryButton))

(def label (-interop-factory Label))

(def text-field
  "Props
  ```
  {:label    Label, not inline unless specified
   :value    String value
   :onChange (fn [element ?new-str] ...)
   :styles   https://developer.microsoft.com/en-us/fluentui#/controls/web/textfield
   ...}```"
  (-interop-factory TextField))
(def input
  "Props
  ```
  {:label    Label, not inline unless specified
   :value    String value
   :onChange (fn [element ?new-str] ...)
   :styles   https://developer.microsoft.com/en-us/fluentui#/controls/web/textfield
   ...}```"
  (-interop-factory TextField))

(def dd_item-header (.-Header DropdownMenuItemType))
(def dd_item-divider (.-Divider DropdownMenuItemType))

(def -cached-option-converter
  "Converts a dropdown option map into a js dropdown option map"
  (let [rw-key (fn [m] (update m :key hash))]
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
  (-interop-factory Stack))

(def nogap-stack "Map defining no gaps in stacks" {:tokens {:childrenGap 0}})
(def lowgap-stack "Map defining low gaps in stacks" {:tokens {:childrenGap 10}})
(def medgap-stack "Map defining medium gaps in stacks" {:tokens {:childrenGap 25}})

(def stack-item
  (-interop-factory (.-Item Stack)))

(defn vstack [opts & children] (apply stack (-prop-merge {:horizontal false} opts) (or children [])))
(defn hstack [opts & children] (apply stack (-prop-merge {:horizontal true} opts) (or children [])))

(def text (-interop-factory Text))


(let [-text (react-interop/react-factory Text)]
  (defn Stext [& strs] (apply -text #js {:variant "small"} strs))
  (defn Mtext [& strs] (apply -text #js {:variant "medium"} strs))
  (defn M+text [& strs] (apply -text #js {:variant "mediumPlus"} strs)))

(def pivot (-interop-factory Pivot))
(def pivot-item (-interop-factory PivotItem))

(def icon (-interop-factory FontIcon))

(def tooltip-host
  "https://developer.microsoft.com/en-us/fluentui#/controls/web/tooltip"
  (-interop-factory TooltipHost))

(def tooltip-bottom 
  "Map props describing direction hint bottom"
  {:directionalHint (.-bottomCenter DirectionalHint)})


;; see grouped list

(def layer (-interop-factory Layer))
