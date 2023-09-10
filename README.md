# Why Does That Sound Good?

AKA Happy Accidentals

A front-end-only web app which listens to your MIDI keyboard and helps reverse-engineer/fuzzy-search what you played to help figure out the context (e.g. chord, scale).

## Demo

Click the image below to see the video

[![Demo video](https://img.youtube.com/vi/8pHL7TzVDfk/maxresdefault.jpg)](https://www.youtube.com/watch?v=8pHL7TzVDfk)

## Features

- Fuzzy chord and scale identification
  - No need to play exact notes, this will find the closest result(s)
  - Shows % similarity AKA overlap
  - Shows suggested chord over notes played
- MIDI in/out and preview
- Progression building
- Group chords into sections
- Private; LocalStorage saving, no back-end servers

## Rationale

I love playing the piano and often I will stumble upon a happy accident (a chord or line that sounds good) but I don't know enough to instantly recognize what it is.

My 'algorithm' would usually go like this:

1. Repeat for each chord:
    1. Look up chord by notes
    1. Keep track of all suggested chords
1. Repeat for each combination of suggested chords:
    1. Look up scale by chord roots
1. Pick scale which sounds best
1. Look up other chords in scale

This is usually a slow process which breaks creative flow. All of these lookups can be automated in real-time.

### Problems With Other Solutions

Some things I noticed with existing solutions which prompted me to create my own:

- Expectation that the user enters in the exact notes of a chord
  - No fuzzy search supported if I'm off by a note or two
- Expectation that the user enters in the key and/or scale before chord identification can take place
  - Again, no fuzzy search supported
  - If I don't know what chord it is, there's a good chance I also don't know what key/scale I'm in
  - I get the rationale since a collection of notes can have several names under various keys and filtering down ahead of time can help, but I think there are usually few enough that it's not a big deal and the ambiguity can actually stoke some new curiosity
    - E.g. a C major chord has the same notes as an E minor chord with a sharp 5. Both provide different contexts to think in!

## Goals & Non-Goals

- This app aims to serve as a map to tell you where you are (which chord/scale you're playing or close to playing) and where you might go from there (other chords/notes in scale).
  - It is not meant to limit you to the suggested results, it just provides context when you want it; play what sounds good to you!
- It is meant to help those who are learning piano/keyboard and have little to no music theory knowledge or just want a quick lookup.
- It is not an end-all-be-all music theory resource, just a helpful automation tool to serve as a launching pad.

## Getting Started

### Project Overview

* Architecture:
[Single Page Application (SPA)](https://en.wikipedia.org/wiki/Single-page_application)
  - There is no back-end, this is self-contained as a front-end app
* Languages
  - Front end is [ClojureScript](https://clojurescript.org/) with ([re-frame](https://github.com/day8/re-frame))
* Dependencies
  - UI framework: [re-frame](https://github.com/day8/re-frame)
  ([docs](https://github.com/day8/re-frame/blob/master/docs/README.md),
  [FAQs](https://github.com/day8/re-frame/blob/master/docs/FAQs/README.md)) ->
  [Reagent](https://github.com/reagent-project/reagent) ->
  [React](https://github.com/facebook/react)
  - Screen breakpoints tool: [BREAKING-POINT](https://github.com/gadfly361/breaking-point)
* Build tools
  - CLJS compilation, dependency management, REPL, & hot reload: [`shadow-cljs`](https://github.com/thheller/shadow-cljs)
  - Test framework: [cljs.test](https://clojurescript.org/tools/testing)
  - Test runner: [Karma](https://github.com/karma-runner/karma)
* Development tools
  - Debugging: [CLJS DevTools](https://github.com/binaryage/cljs-devtools),
  [`re-frame-10x`](https://github.com/day8/re-frame-10x)
  - Emacs integration: [CIDER](https://github.com/clojure-emacs/cider)
  - Linter: [clj-kondo](https://github.com/borkdude/clj-kondo)

#### Directory structure

* [`/`](/../../): project config files
* [`.clj-kondo/`](.clj-kondo/): lint config and cache files (cache files are not tracked; see
[`.gitignore`](.gitignore))
* [`dev/`](dev/): source files compiled only with the [dev](#running-the-app) profile
  - [`user.cljs`](dev/cljs/user.cljs): symbols for use during development in the
[ClojureScript REPL](#connecting-to-the-browser-repl-from-a-terminal)
* [`resources/public/`](resources/public/): SPA root directory;
[dev](#running-the-app) / [prod](#production) profile depends on the most recent build
  - [`index.html`](resources/public/index.html): SPA home page
    - Dynamic SPA content rendered in the following `div`:
        ```html
        <div id="app"></div>
        ```
    - Customizable; add headers, footers, links to other scripts and styles, etc.
  - Generated directories and files
    - Created on build with either the [dev](#running-the-app) or [prod](#production) profile
    - `js/compiled/`: compiled CLJS (`shadow-cljs`)
      - Not tracked in source control; see [`.gitignore`](.gitignore)
* [`src/why_does_that_sound_good/`](src/why_does_that_sound_good/): SPA source files (ClojureScript,
[re-frame](https://github.com/Day8/re-frame))
  - [`core.cljs`](src/why_does_that_sound_good/core.cljs): contains the SPA entry point, `init`
* [`test/why_does_that_sound_good/`](test/why_does_that_sound_good/): test files (ClojureScript,
[cljs.test](https://clojurescript.org/tools/testing))
  - Only namespaces ending in `-test` (files `*_test.cljs`) are compiled and sent to the test runner
* [`.github/workflows/`](.github/workflows/): contains the
[github actions](https://github.com/features/actions) pipelines.
  - [`test.yaml`](.github/workflows/test.yaml): Pipeline for testing.


### Editor/IDE

Use your preferred editor or IDE that supports Clojure/ClojureScript development. See
[Clojure tools](https://clojure.org/community/resources#_clojure_tools) for some popular options.

### Environment Setup

1. Install [JDK 8 or later](https://openjdk.java.net/install/) (Java Development Kit)
2. Install [Node.js](https://nodejs.org/) (JavaScript runtime environment) which should include
   [NPM](https://docs.npmjs.com/cli/npm) or if your Node.js installation does not include NPM also install it.
3. Install [Chrome](https://www.google.com/chrome/) or
[Chromium](https://www.chromium.org/getting-involved/download-chromium) version 59 or later
(headless test environment)
    * For Chromium, set the `CHROME_BIN` environment variable in your shell to the command that
    launches Chromium. For example, in Ubuntu, add the following line to your `.bashrc`:
        ```bash
        export CHROME_BIN=chromium-browser
       ```
4. Install [clj-kondo](https://github.com/borkdude/clj-kondo/blob/master/doc/install.md) (linter)
5. Clone this repo and open a terminal in the `why-does-that-sound-good` project root directory
6. (Optional) Setup [lint cache](https://github.com/borkdude/clj-kondo#project-setup):
    ```sh
    clj-kondo --lint "$(npx shadow-cljs classpath)"
    ```
7. Setup
[linting in your editor](https://github.com/borkdude/clj-kondo/blob/master/doc/editor-integration.md)

### Browser Setup

Browser caching should be disabled when developer tools are open to prevent interference with
[`shadow-cljs`](https://github.com/thheller/shadow-cljs) hot reloading.

Custom formatters must be enabled in the browser before
[CLJS DevTools](https://github.com/binaryage/cljs-devtools) can display ClojureScript data in the
console in a more readable way.

#### Chrome/Chromium

1. Open [DevTools](https://developers.google.com/web/tools/chrome-devtools/) (Linux/Windows: `F12`
or `Ctrl-Shift-I`; macOS: `⌘-Option-I`)
2. Open DevTools Settings (Linux/Windows: `?` or `F1`; macOS: `?` or `Fn+F1`)
3. Select `Preferences` in the navigation menu on the left, if it is not already selected
4. Under the `Network` heading, enable the `Disable cache (while DevTools is open)` option
5. Under the `Console` heading, enable the `Enable custom formatters` option

#### Firefox

1. Open [Developer Tools](https://developer.mozilla.org/en-US/docs/Tools) (Linux/Windows: `F12` or
`Ctrl-Shift-I`; macOS: `⌘-Option-I`)
2. Open [Developer Tools Settings](https://developer.mozilla.org/en-US/docs/Tools/Settings)
(Linux/macOS/Windows: `F1`)
3. Under the `Advanced settings` heading, enable the `Disable HTTP Cache (when toolbox is open)`
option

Unfortunately, Firefox does not yet support custom formatters in their devtools. For updates, follow
the enhancement request in their bug tracker:
[1262914 - Add support for Custom Formatters in devtools](https://bugzilla.mozilla.org/show_bug.cgi?id=1262914).

## Development

### Terms

Small distinction in the code itself with the music terms:

- Pitch: music letter of the keyboard key being played as a Clojure keyword (e.g. :C, :B)
  - Contains no octave information
- Note: midi integer of the keyboard key being played
  - Can derive pitch and octave from this
  - E.g. middle C = 60 = :C4
- Used notes: final notes of block to use for scale identification
  - Uses chord suggestion's notes if a chord was selected, otherwise use original input notes
- Block: collection of notes + some metadata
- Section: collection of blocks + some metadata
- Similarity: set overlap ([Jaccard Index](https://en.wikipedia.org/wiki/Jaccard_index)) of pitches between user input and target chord/scale
  - Represented as a percentage, higher is better/more similar
  - Weighted more if the chord/scale's root was played

### Running the App

Start a temporary local web server, build the app with the `dev` profile, and serve the app,
browser test runner and karma test runner with hot reload:

```sh
npm install
npx shadow-cljs watch app
```

Please be patient; it may take over 20 seconds to see any output, and over 40 seconds to complete.

When `[:app] Build completed` appears in the output, browse to
[http://localhost:8280/](http://localhost:8280/).

[`shadow-cljs`](https://github.com/thheller/shadow-cljs) will automatically push ClojureScript code
changes to your browser on save. To prevent a few common issues, see
[Hot Reload in ClojureScript: Things to avoid](https://code.thheller.com/blog/shadow-cljs/2019/08/25/hot-reload-in-clojurescript.html#things-to-avoid).

Opening the app in your browser starts a
[ClojureScript browser REPL](https://clojurescript.org/reference/repl#using-the-browser-as-an-evaluation-environment),
to which you may now connect.

#### Connecting to the browser REPL from Emacs with CIDER

Connect to the browser REPL:
```
M-x cider-jack-in-cljs
```

See
[Shadow CLJS User's Guide: Emacs/CIDER](https://shadow-cljs.github.io/docs/UsersGuide.html#cider)
for more information. Note that the mentioned [`.dir-locals.el`](.dir-locals.el) file has already
been created for you.

#### Connecting to the browser REPL from VS Code with Calva

See the [re-frame-template README](https://github.com/day8/re-frame-template) for [Calva](https://github.com/BetterThanTomorrow/calva) instuctions. See also https://calva.io for Calva documentation.


#### Connecting to the browser REPL from other editors

See
[Shadow CLJS User's Guide: Editor Integration](https://shadow-cljs.github.io/docs/UsersGuide.html#_editor_integration).
Note that `npm run watch` runs `npx shadow-cljs watch` for you, and that this project's running build ids is
`app`, `browser-test`, `karma-test`, or the keywords `:app`, `:browser-test`, `:karma-test` in a Clojure context.

Alternatively, search the web for info on connecting to a `shadow-cljs` ClojureScript browser REPL
from your editor and configuration.

For example, in Vim / Neovim with `fireplace.vim`
1. Open a `.cljs` file in the project to activate `fireplace.vim`
2. In normal mode, execute the `Piggieback` command with this project's running build id, `:app`:
    ```vim
    :Piggieback :app
    ```

#### Connecting to the browser REPL from a terminal

1. Connect to the `shadow-cljs` nREPL:
    ```sh
    lein repl :connect localhost:8777
    ```
    The REPL prompt, `shadow.user=>`, indicates that is a Clojure REPL, not ClojureScript.

2. In the REPL, switch the session to this project's running build id, `:app`:
    ```clj
    (shadow.cljs.devtools.api/nrepl-select :app)
    ```
    The REPL prompt changes to `cljs.user=>`, indicating that this is now a ClojureScript REPL.
3. See [`user.cljs`](dev/cljs/user.cljs) for symbols that are immediately accessible in the REPL
without needing to `require`.

### Running Tests

Build the app with the `prod` profile, start a temporary local web server, launch headless
Chrome/Chromium, run tests, and stop the web server:

```sh
npm install
npm run ci
```

Please be patient; it may take over 15 seconds to see any output, and over 25 seconds to complete.

Or, for auto-reload:
```sh
npm install
npm run watch
```

Then in another terminal:
```sh
karma start
```

### Running `shadow-cljs` Actions

See a list of [`shadow-cljs CLI`](https://shadow-cljs.github.io/docs/UsersGuide.html#_command_line)
actions:
```sh
npx shadow-cljs --help
```

Please be patient; it may take over 10 seconds to see any output. Also note that some actions shown
may not actually be supported, outputting "Unknown action." when run.

Run a shadow-cljs action on this project's build id (without the colon, just `app`):
```sh
npx shadow-cljs <action> app
```
### Debug Logging

The `debug?` variable in [`config.cljs`](src/cljs/why_does_that_sound_good/config.cljs) defaults to `true` in
[`dev`](#running-the-app) builds, and `false` in [`prod`](#production) builds.

Use `debug?` for logging or other tasks that should run only on `dev` builds:

```clj
(ns why-does-that-sound-good.example
  (:require [why-does-that-sound-good.config :as config])

(when config/debug?
  (println "This message will appear in the browser console only on dev builds."))
```

## Production

Build the app with the `prod` profile:

```sh
npm install
npm run release
```

Please be patient; it may take over 15 seconds to see any output, and over 30 seconds to complete.

The `resources/public/js/compiled` directory is created, containing the compiled `app.js` and
    `manifest.edn` files.
