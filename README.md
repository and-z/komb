# komb

A tiny Babashka utility for sorting JSON.
It reads JSON content, sorts object keys and (array) values alphanumerically and pretty-prints the result.

## Features

- Sorts nested JSON objects and arrays
- Reads JSON from a file or from `stdin`
- Optional semantic sort preserves order of array elements
- Optional pretty-printing
- Runs locally/offline by Node.js or GraalVM
- Returns bash exit code on success/error

## Motivation

I was searching for an offline tool, but only thing I've found was [jsonabc][jsonabc].
This utility does almost the same but it's implemented using a [sane language][clojure] 😃.
It also fixes some issues of `jsonabc` like sorting numbers.

This tool reads JSON from a file or from `stdin` if no argument provided.
By default it will sort semantically, keeping order of array elements as is and pretty-print the result.

The basic idea was to have a handy tool to compare JSON files.
With additional CLI options it's also possible to sort JSON files without loosing semantic order.

`komb` uses proper shell exit codes to communicate successful or failed execution.

See usage examples down below.

## Command Line Interface

```shell
komb - sort JSON & pretty-print

Usage: komb [options] [path]

Reads content of a JSON file from *stdin* if no path provided.

Options:
      --[no-]semantic  true  Enable to preserve order of array elements
      --[no-]pretty    true  Pretty-print the result
  -h, --help                 Print this help.

Arguments:
  path - path to JSON file
```

## Installation

`komb` supports two different runtimes: Native GraalVM and Node.js.
The user can decide which flavor suits better.

### Run on Node.js

Any modern Node.js version should be sufficient.
Ensure `node/npm/npx` are available before proceed next.
`komb` is written using Clojure and it brings `nbb` as a dependency to interpret code with it,
so no additional libraries besides Node.js itself are required.

Try `komb` out without installation via
```shell
npx komb some-json-file.json

# or provide JSON directly
cat <<'JSON' | npx komb --no-semantic
{
  "alias": "bb",
  "full-name": "Babashka",
  "author": "borkdude",
  "unordered-numbers": [2, 5, 1, 2, 9]
}
JSON
```

or install it from public `NPM`.
```shell
npm install komb
```

Verify installation succeeded:
```shell
komb --help
```

Please note this utility has no JS interface and therefore it cannot be used from JS code as a library.

### Run on GraalVM

This scenario is probably for Clojure (savvy) people.
Currently there is no pre-build binary available for this tool, but it can be installed from source.

Make sure you've installed [Babashka][babashka].
You probably also will need a recent Java, cause Babashka uses Java-based tools for dependency management.

#### Clone the repository
```shell
git clone git@github.com:and-z/komb.git
cd komb
```

Assume executing following commands from the local project directory.

#### (Optional) Install using `bbin` to local path

The most convenient way to use this utility is to install it as a tool using [bbin][bbin-install].

With `bbin` in place you can install `komb` from the local git repository:
```shell
bb install
```

It's also easily uninstalled with:
```shell
bb uninstall
```

Make sure to uninstall old version before installing a more recent one.

Tip: List available tasks with `bb tasks`.

#### (Optional) Use as Babashka task

It's also possible to use this utility without installation. The API should be consistent.

##### List available tasks
```shell
bb tasks
```

##### Sort some JSON file using Babashka task
```shell
bb komb test/it/zimpel/komb/unsorted.json
```

##### Sort some JSON from `stdin` using Babashka task
```shell
cat test/it/zimpel/komb/unsorted.json | bb komb
```

## Usage examples

Once `komb` is on your path it can be executed providing a path to a JSON file:

Sort using default behaviour:
```shell
komb test/it/zimpel/komb/unsorted.json
```

If no file is provided `komb` reads JSON content from `stdin`:

```shell
cat test/it/zimpel/komb/unsorted.json | komb
```

Sometimes it's handy to sort everything (including arrays), e.g. to compare JSON payloads visually:
```shell
cat <<'JSON' | komb --no-semantic --no-pretty
{
  "a": [99,3,4,22,33,99]
}
JSON
```

Output:
```json
{"a":[3,4,22,33,99,99]}
```

## Use `komb` with code editor (Emacs/Spacemacs/etc)

Having a programmable editor is actually pretty nice. I'm not an Emacs specialist at all, but with some
guidance from the excellent article ["Executing Shell Commands in Emacs"][emacs-shell-commands] by Mickey Petersen
I was able to easily embed `komb` into my workflow.
In this example I'm using Spacemacs but the idea should be transferrable to other editors as well.

I've defined my custom elisp function like shown below:
```elisp
(defun sort-json ()
  "Sorts and pretty-prints JSON using `komb` Babashka tool"
  (interactive)
  (shell-command-on-region
   ;; beginning and end of buffer
   (point-min)
   (point-max)
   ;; command and param
   "komb"
   ;; output buffer
   (current-buffer)
   ;; replace?
   t
   ;; name of the error buffer
   "*komb Error Buffer*"
   ;; show error buffer
   t))
```

For more convenience I've also defined a custom key binding for JSON major mode:
```elisp
(defun dotspacemacs/user-config()
  ; some existing user configuration
  ; ...
  (spacemacs/set-leader-keys-for-major-mode 'json-mode "o=" 'sort-json)
```

With this small adjustments in place sorting JSON files became a breeze:
- Open unsorted JSON file in Emacs buffer
- Press `, o =`
- Profit

## Development

It is possible to start a standard Clojure, Babashka or nbb REPL to play with source code.
Nothing special to mention here.

### Run tests
```shell
bb test:bb
```

## Startup time

Here are some numbers (in descending order) to give an idea of how quick/slow different versions may run.
The measurements are not scientific and don't pretent to be a valid benchmark.
I executed different variants of starting `komb` and wrapped the commands with `time` on my linux dev machine.

| runtime | note | cmd | time <cmd> |
|---------|------|-----|------------|
| nodejs | not installed | npx komb test/it/zimpel/komb/unsorted.json | 0,37s user 0,05s system 72% cpu 0,581 total |
| nodejs | not installed | npx nbb -m it.zimpel.komb.main test/it/zimpel/komb/unsorted.json | 0,27s user 0,08s system 132% cpu 0,262 total |
| nodejs | not installed | nbb --classpath src -m it.zimpel.komb.main test/it/zimpel/komb/unsorted.json | 0,15s user 0,02s system 131% cpu 0,127 total |
| nodejs | `npm install -g komb` | komb test/it/zimpel/komb/unsorted.json | 0,14s user 0,03s system 132% cpu 0,128 total |
| graalvm | not installed | bb komb test/it/zimpel/komb/unsorted.json | 0,01s user 0,02s system 90% cpu 0,029 total |
| graalvm | `bb install` | komb test/it/zimpel/komb/unsorted.json | 0,01s user 0,03s system 94% cpu 0,040 total |

`not installed` means `komb` is not installed as a tool via `npm install` for Node.js flavor or `bb install` for GraalVM flavor.
When properly installed there is no performance penalty for dynamic lookup like in case of `npx`.

[jsonabc]: https://github.com/ShivrajRath/jsonabc
[clojure]: https://clojure.org/
[babashka]: https://babashka.org/
[bbin-install]: https://github.com/babashka/bbin/?tab=readme-ov-file#installation
[emacs-shell-commands]: https://www.masteringemacs.org/article/executing-shell-commands-emacs
