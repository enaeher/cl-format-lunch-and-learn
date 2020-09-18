(ns cl-format-l&l
  (:require [clojure.pprint :refer [cl-format]]
            [clojure.string :as str]))

;; cl-format: uses and abuses
;;
;; # What is it?
;;
;; - A DSL for formatting output
;; - Like C printf or Java String.format, but more powerful
;; - at its simplest:

(cl-format nil "a string ~a and a number ~d" "foo" 12)

;; # Where did this thing come from?
;;
;; - clojure.pprint/cl-format is a straightforward (if buggy) copy of
;;   Common Lisp format
;; - which is adapted from Lisp Machine Lisp [1]
;; - which in turn was inspired by the Multics _ioa subroutine
;;   - example: call ioa_ ("a=^d ^[b=^d^;^s^] c=^d",5,sw,7,9); [2]
;;
;; 1. http://www.bitsavers.org/pdf/mit/cadr/chinual_4thEd_Jul81.pdf
;; 2. http://www.bitsavers.org/pdf/honeywell/multics/F15C_pgmgSubrsCourse_Sep83.pdf

;; # Should you use cl-format?
;;
;; Probably not! (most of the time)
;;
;; - Write-only, even once you are proficient
;; - Un-Lispy: all syntax, not extensible
;; - Not widely used by Clojurists
;;
;; But there are some situations where it comes in handy
;;
;; - Formatting things for English-speaking humans
;;   - number names
;;   - pluralization and number agreement
;;   - "x, y, and z"
;; - Tabular data output

;; # Structure of a directive
;;
;; ~       indicates beginning of directive
;; 10,'0   (optional) prefix parameters, comma-separated, integer or char types
;; @       (optional) modifiers: @, :, or both. indicates directive "mode"
;; d       single character indicating directive type
;;
;; The meanings of any prefix parameters and modifiers are determined
;; by the type of the directive.
;;
;; In this case:
;; - ~d takes up to four prefix params: mincol, padchar, commachar, and
;;   comma-interval
;; - @ modifier forces sign to always be printed
;; - : modifier prints commachar (defaults to ,) between groups of
;;   comma-interval (defaults to 3) digits

(cl-format nil "~d" 1234) ; simple directive
(cl-format nil "~10d" 1234) ; single integer prefix parameter
(cl-format nil "~10,'0d" 1234) ; two prefix parameters, one int, one char
(cl-format nil "~@d" 1234) ; single modifier
(cl-format nil "~@:d" 1234) ; both modifiers
(cl-format nil "~10,'0,'.,1:d" 1234) ; four prefix parameters and a modifier

;; # Fun with numbers

;; base 42? why not?

(cl-format nil "~42R" 1234)

;; English numbers

(cl-format nil "~R" 1234)
(cl-format nil "~R" 1606938044258990275541962092341162602522202993782792835301376)

;; Ordinals

(cl-format nil "~:R" 1234)

;; Roman numerals

(cl-format nil "~@R" 1234)

;; Two kinds!

(cl-format nil "~:@R" 1234)

;; # Beyond printf: Iteration

(def animals [12 "cat"
              1 "bird"
              3 "dog"])

;; Simple iteration with ~{ ... ~}

(cl-format nil "~{~d ~a ~}" animals)

;; Wouldn't it be nice to print this list with commas?
;; ~^ returns early from the iteration if there are no more arguments

(cl-format nil "~{~d ~a~^, ~}" animals)

;; That's better. What about pluralization?
;; ~p prints an "s" if and only if the the arg is not equal to 1
;; ~* jumps forward in the arguments, ~:* jumps backwards, and both
;; take a prefix parameter allowing you to jump multiple units

(cl-format nil "~{~d ~a~2:*~p~*~^, ~}" animals)

;; # Conditionals

;; Let's make the output a little more English-y: x, y, and z.
;;
;; You can express conditional clauses with ~[ ... ~; ...  ~]
;; It consumes an integer argument n and uses the (zero-indexed) nth clause.
;; ~; separates the clauses.

(cl-format nil "~[first clause~;second clause~;third clause~]" 0)

;; instead of consuming an argument, you can pass the index as a prefix parameter

(cl-format nil "~1[first clause~;second clause~;third clause~]")

;; you can use # in place of any prefix parameter to use the number of
;; remaining arguments as the parameter value
;; ~:; indicates a default clause within ~[ ... ~]

(cl-format nil "~{~d ~a~2:*~p~*~#[~;~;, and ~:;, ~]~}" animals)

;; This works for lists of 3+ items--lots of examples online extending
;; it to handle the case of lists with 0-2 items

;; # Tabular data

(def data [[:foo 23.4 8]
           [:bar 5.94 4]
           [:quux 618 2]
           [:fnord 0.3 4]])

(cl-format true "~:{~%~a~10t~6,2f ~v~~30t~:*~d~}" data)

;; # Word wrap

(def litany-against-fear
  (str/split
   "I must not fear.
    Fear is the mind-killer.
    Fear is the little-death that brings total obliteration.
    I will face my fear.
    I will permit it to pass over me and through me.
    And when it has gone past I will turn the inner eye to see its path.
    Where the fear has gone there will be nothing. Only I will remain."
   #"\s+"))

(cl-format true "~%~%~{~<~%~0,20:;~a ~>~}" litany-against-fear)

;; # Further reading
;;
;; - From Peter Seibel's _Practical Common Lisp_:
;;   "A Few FORMAT Recipes"
;;   http://www.gigamonkeys.com/book/a-few-format-recipes.html
;;
;; - From Guy Steele's _Common Lisp the Language_:
;;   "Formatted Output to Character Streams"
;;   https://www.cs.cmu.edu/Groups/AI/html/cltl/clm/node200.html
;;
;; - From the Common Lisp HyperSpec:
;;   "Formatted Output"
;;   http://www.ai.mit.edu/projects/iiip/doc/CommonLISP/HyperSpec/Body/sec_22-3.html
;;
;; - By Gene Michael Stover:
;;   "Advanced Use of Lisp’s FORMAT Function"
;;   http://cybertiggyr.com/fmt/fmt.pdf
