## Fisher

> "You seem to know all about him," observed March, with a rather bewildered laugh, "and about a good many other people."  
> Fisher's bald brow became abruptly corrugated, and a curious expression came into his eyes.  
> "I know too much," he said. "That's what's the matter with me. That's what's the matter with all of us, and the whole show; we know too much. Too much about one another; too much about ourselves. That's why I'm really interested, just now, about one thing that I don't know."  
> "And that is?" inquired the other.  
> "Why that poor fellow is dead."  
-- **The Man Who Knew Too Much**, G.K. Chesterton.

<br/>

***Status:*** I have 3 projects in progress testing apis for various componenents listed below.

## Why?

At this point, I have tried to some extent most of the available Clojure editors. LightTable, IntelliJ+Cursive, Spacemacs+Cider, Atom+Proto REPL, Nightcode, and Calva. The only thing that stuck was a love for paredit with some custom vim keybindings. Every editor had something I liked, but none combined everything. For instance, I loved the idea of [cards][2] in LightTable ([they were never implemented][1]). I love the embeddable graphs of proto-repl. Nightcode's simplicity is fantastic. Spacemacs is totally awesome for keyboard navigability. Cursive is part of IntelliJ, which has phenomenal code navigation features.

## Why start a new project?

I could try to extend Cider, Fireplace, [Liquid][14], or lighttable. Why not? Cider, FP, and Liquid are non-starters because of the layout constraints they have, and one of the goals is to embed arbitrary HTML content into the editor itself. LightTable is a different issue since it is fully CLJS and electron based. I have dug into the code a fair amount, but it is simply [too complicated at this point for me to add to it][4].  Atom appears to [support cards][18], but some of the other features below would be difficult to impossible to implement. I’m not aware of any limitations around cards in vs code, but it is a bit more locked down than atom and I can’t see more functionality being implemented. 

This is also a skill-building tool. I want to explore Fulcro (after research now [Ful-Frame][32]), Pluggable architectures, and management of an OSS project. 

## Achievability 

I outline a ton of functionality below that will take a lot of work - quite possibly more work than I could put in myself. Naturally, that means if it is to be completed help is required. I am researching quite a bit about plugin architectures that I could use. The plan is to create a minimal core that includes critical functionality - keyboard routing, frameworks for managing cards, window state, modals, etc. Every other bit of functionality will be a plugin - including all cards. Hopefully, I can design a system of plugins with extension points - i.e. the ability to add plugins to plugins. This would allow for instance cards that are used to open Clojure code to have the editor type exchanged from plain text to an AST one while using largely the same mechanics. I don't think I can design a system that is pluggable in every facet. Some functionality will need to remain baked in. ***Any ideas around this would be greatly appreciated!***

## Contributing

For now, I'm not interested in code contributions. Unless this project takes off, I expect to be the only major contributor, and I don't know how far I can take this idea. I don't want to waste your time. If, however, you have ideas, please post an issue detailing it so I can add it to the plans.

## Functionality Desired

#### Keybindings with discoverability

Vim and spacemacs are the inspiration here. Every key, even single keystrokes, should pass through a multi-mode keybinding setup. The whole setup should function somewhat like a finite state machine, where keybindings have the option of switching the mode. It will be similar to vim’s insert/edit/replace, or the displays at the bottom of spacemacs. One key should be reserved to show all current keybindings. The mode should be shown globally. I'm still debating some of the semantics of this feature.

#### [Cards (Code Bubbles)][5]

This is my biggest dream here. I would like to move away from files with surrounding tool windows as the prevailing code editor model. My proposal isn't very new. The link above and the [video here][2] show a basic concept.
 
The proposed replacement is workspaces. Workspaces are a blank slate where any element can run. REPLs, test visualizations, code blocks, code editors, HTML visualizations, and more could be added to the possible elements. Elements could be extended as well, like code blocks that find the code and its related tests and show them together with visual pass/fail embedded. Possibilities abound here.

Problems also arise - primarily around layout generation. The simplest would be to add only what the user explicitly asks for, allowing them to drag and drop elements to the correct order. This is limiting though and seems like a poor UX choice. This could be improved by navigation modes that auto-generate workspace layouts in response to user navigation. Possible modes:

- Test runner mode where the code block is shown next to all tests
- Related function mode where all call sites of a function are identified and shown nearby in the workspace
- Repl mode where arbitrary code can be pulled up around a running repl. Useful for integration tests?
- Not an entire card, but a way to pull tests out of the repl. I.e. a keycommand that says the current visible output is correct and go store the command with its output. 
- Literate programming files - designed for what I call integration files. Integration files are a namespace with lots of commented code that demonstrates how to flow through a namespace and how the functions can interact. Can integrate some of the same outputs as Proto REPL.

#### Excellent navigation

Not much to say here - this is a must for large codebases. This flows from the workspace concept above, but navigation should have the option to kick off workspace restructuring.

#### Editors

The default method for code editing is raw text manipulation. Paredit and Parinfer bend the rules quite a bit, but most of the time you end up pushing your cursor around manually without using the AST. Things like lispy-ace-symbol would be awesome.


Editors should be pluggable and selectable by the user. Any mode combination of bare text + Vim, paredit or parinfer should be usable, as well as an AST editor.

- [Well polished ast editor for a clj-like language][10]
- [Calcit editor][11]
- [Example ast editor (very incomplete)][9]
- [Lispy ace symbol][6]
- [Ace code editor (from amazon)][7]
- [Paren soup (nice cljs-native clojure editor)][8]

### Plugin system

This is core to the editor, allowing the development of the various corners to happen in a decoupled manner. Also, what is a code editor without plugins?

- [SO Question][20]
- [component library][21]
- [mount (alt to component)][23] - [why not use component?][24]
- [pomegranate - for jvm side class loading][22]
- [The clean architecture - article][25]
- [A simple plugin system in clojure - article][26]
- [Plugins in lein][27]
- [Plugins in IntelliJ][28]
- [Plugins in VSCode][29] 
- [Plugins in Atom][30]


## General Research links

- [Lightemod - all in one clojure editor (education focused?)][12]
- [Rewrite-clj - useful for ast and paredit?][13]
- [liquid editor][14]
- [Complement code completion][15]
- [instaparse - fallback if rw-clj doesn't work][16]
- [clj-kondo][17]
    - note to self - it can print out an ns overview with definitions and line numbers
- [Inspiring a Future Clojure editor _with forgotten lisp ux_ (Clojure conj 2017)][19]
- [REPL integration for js projects][31]
- [React component for grid systems][33]
  - If needed, [React draggable components][34]
- [React Tree View][35]. [Another][36]

Thanks to @jlmr in slack for mentioning his name of cards - much easier to say than code bubbles.

[1]: https://groups.google.com/forum/#!searchin/light-table-discussion/bubbles/light-table-discussion/AIx17mxKQmo/JiGFRzu6uxkJ
[2]: https://www.kickstarter.com/projects/ibdknox/light-table
[4]: https://groups.google.com/forum/#!msg/light-table-discussion/2csnnNA1pfo/693EWDJVhuwJ
[5]: http://cs.brown.edu/~spr/codebubbles/
[6]: http://oremacs.com/lispy/#lispy-ace-symbol
[7]: https://ace.c9.io/
[8]: https://github.com/oakes/paren-soup/
[9]: https://github.com/Hendekagon/iiiiioiooooo-dom
[10]: http://kevinmahoney.co.uk/articles/structural-editor-prototype/
[11]: https://github.com/Cirru/calcit-editor
[12]: https://github.com/oakes/Lightmod
[13]: https://github.com/xsc/rewrite-clj
[14]: https://github.com/mogenslund/liquid
[15]: https://github.com/alexander-yakushev/compliment
[16]: https://github.com/Engelberg/instaparse
[17]: https://github.com/borkdude/clj-kondo
[18]: https://github.com/moodyloo/atombubble
[19]: https://www.youtube.com/watch?v=K0Tsa3smr1w
[20]: https://stackoverflow.com/questions/17969926/architecture-for-plugins-to-be-loaded-in-runtime
[21]: https://github.com/stuartsierra/component
[22]: https://github.com/clj-commons/pomegranate
[23]: https://github.com/tolitius/mount
[24]: https://github.com/tolitius/mount/blob/master/doc/differences-from-component.md#differences-from-component
[25]: https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html
[26]: https://yogthos.net/posts/2015-01-15-A-Plugin-System-in-Clojure.html
[27]: https://github.com/technomancy/leiningen/blob/master/doc/PLUGINS.md#writing-a-plugin
[28]: http://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/plugin_extensions.html#how-to-get-the-extension-points-list
[29]: https://code.visualstudio.com/api/extension-capabilities/overview
[30]: https://github.com/atom/flight-manual.atom.io
[31]: https://github.com/mauricioszabo/repl-tooling
[32]: https://github.com/JJ-Atkinson/Ful-Frame
[33]: https://github.com/STRML/react-grid-layout
[34]: https://github.com/strml/react-draggable
[35]: https://reactjsexample.com/a-hierarchical-object-tree-component-for-react/
[36]: https://github.com/chenglou/react-treeview
