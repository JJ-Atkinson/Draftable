## Draftable

A union of draft and table. Vision: to fulfill and maybe exceed light-table's original goals.

## Why?

At this point I have tried to some extent most of the available clojure editors. LightTable, IntelliJ+Cursive, Spacemacs+Cider, Atom+Proto REPL, Nightcode, and Calva. The only thing that stuck was a love for paredit with some custom vim keybindings. Every editor had something I liked, but none combined everything. For instance, I loved the idea of [code bubbles][2] in LightTable ([they were never implemented][1]). I love the embeddable graphs of proto-repl. Nightcode's simplicity is fantastic. Spacemacs is totally awesome for keyboard navigability. Cursive is part of IntelliJ, which has phenomenal code navigation features.

## Why start a new project?

I could try to extend Cider, Fireplace, [Liquid][3], or lighttable. Why not? Cider, FP, and Liquid are non starters because of the layout constraints they have, and one of the goals is to embed arbitrary html content into the editor itself. LightTable is a different issue, since it is fully CLJS and electron based. I have dug into the code a fair amount, but it is simply [too complicated at this point for me to add to it][4].  Atom appears to [support code bubbles][18], but some of the other features below would be difficult to impossible to implement. I’m not aware of any limitations around code bubbles in vs code, but it is a bit more locked down than atom and I can’t see more functionality being implemented. 

This is also a skill building tool. I want to explore Fulcro, Pluggable architectures, and management of an oss project. 

## Contributing

For now I'm not interested in code contributions. Unless this project takes off, I expect to be the only major contributor, and I don't know how far I can take this idea. I don't want to waste your time. If, however, you have ideas, please post an issue detailing it so I can add it to the plans.

## Functionality Desired

#### Keybindings with discoverability

Vim and spacemacs are the inspiration here. Every key, even single key strokes, should pass through a multi mode keybinding setup. The whole setup should function somewhat alike a finite state machine, where keybindings have the option of switching the mode. It will be similar to vim’s insert/edit/replace, or the displays at the bottom of spacemacs. One key should be reserved to show all current keybindings. The mode should be shown globally. I'm still debating some of the semantics of this feature.

#### [Code Bubbles][5]

This is my biggest dream here. I would like to move away from files with surrounding tool windows as the prevailing code editor model. My proposal isn't very new. The link above and the [video here][2] show a basic concept.
 
The proposed replacement is workspaces. Workspaces are a blank slate where any element can run. Repls, test visualizations, code blocks, code editors, html visualizations, and more could be added to the possible elements. Elements could be extended as well, like code blocks that find the code and its related tests and shows them together with visual pass/fail embedded. Possibilities abound here.

Problems also arise - primarily around layout generation. The simplest would be to add only what the user explicitly asks for, allowing them to drag and drop elements to the correct order. This is limiting though and seems like a poor ux choice. This could be improved by navigation modes that auto generate workspace layouts in response to user navigation. Possible modes:

- Test runner mode where the code block is shown next to all tests
- Related function mode where all call sites of a function are identified and shown nearby in the workspace
- Repl mode where arbitrary code can be pulled up around a running repl. Useful for integration tests?

#### Excellent navigation

Not much to say here - this is a must for large code bases. This flows from the workspace concept above, but navigation should have the option to kick off workspace restructuring.

#### Editors

The default method for code editing is raw text manipulation. Paredit and Parinfer bend the rules quite a bit, but most of the time you end up pushing your cursor around manually without using the ast. Things like lispy-ace-symbol would be awesome.


Editors should be pluggable and selectable by the user. Any mode combination of bare text + Vim, paredit or parinfer should be usable, as well as an ast editor.

- [Well polished ast editor for a clj-like language][10]
- [Calcit editor][11]
- [Example ast editor (very incomplete)][9]
- [Lispy ace symbol][6]
- [Ace code editor (from amazon)][7]
- [Paren soup (nice cljs-native clojure editor)][8]

## Research links

- [Lightemod - all in one clojure editor (education focused?)][12]
- [Rewrite-clj - useful for ast and paredit?][13]
- [liquid editor][14]
- [Complement code completion][15]
- [instaparse - fallback if rw-clj doesn't work][16]
- [clj-kondo][17]
- [Inspiring a Future Clojure editor _with fogotten lisp ux_ (clojure conj 2017)][19]


[1]: https://groups.google.com/forum/#!searchin/light-table-discussion/bubbles/light-table-discussion/AIx17mxKQmo/JiGFRzu6uxkJ
[2]: https://www.kickstarter.com/projects/ibdknox/light-table
[3]: https://github.com/mogenslund/liquid
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

