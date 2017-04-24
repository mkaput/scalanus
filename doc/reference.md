The Scalanus Language Reference
===============================

# Introduction

Scalanus is a simple, dynamically and strongly typed, interpreted language aimed at
being as easy as possible to implement. It is continuation of the
[Janus](https://github.com/mkaput/janus) ([Reference](https://github.com/mkaput/janus/blob/master/doc/Language-Specification.md))
programming language with much more features and cleaned up grammar & semantics.

This document is the primary reference for the Scalanus programming language
grammar and semantics.

This document has been greatly inspired by and based on
[The Rust Reference](https://doc.rust-lang.org/reference.html) and
[Rust Grammar](https://doc.rust-lang.org/grammar.html) (including ordinary
copy-pasting tedious paragraphs).

# Notation

Scalanus' grammar is defined over Unicode codepoints, each conventionally denoted
`U+XXXX`, for 4 or more hexadecimal digits `X`. _Most_ of Scalanus' grammar is
confined to the ASCII range of Unicode, and is described in this document by a
dialect of Extended Backus-Naur Form (EBNF) which can be defined
self-referentially as follows:

```antlr
grammar        : rule+
rule           : nonterminal ':' productionrule
productionrule : production [ '|' production ]*
production     : term+
term           : element repeats
element        : LITERAL | IDENTIFIER | '[' productionrule ']'
repeats        : [ '*' | '+' ] NUMBER? | NUMBER? | '?'
```

Where:

- Whitespace in the grammar is ignored.
- Square brackets are used to group rules.
- `LITERAL` is a single printable ASCII character, or an escaped hexadecimal
  ASCII code of the form `\xQQ`, in single quotes, denoting the corresponding
  Unicode codepoint `U+00QQ`.
- `IDENTIFIER` is a nonempty string of ASCII letters and underscores.
- The `repeats` forms apply to the adjacent `element`, and are as follows:
  - `?` means zero or one repetition
  - `*` means zero or more repetitions
  - `+` means one or more repetitions
  - NUMBER trailing a repeat symbol gives a maximum repetition count
  - NUMBER on its own gives an exact repetition count

This EBNF dialect should hopefully be familiar to many readers.

## Unicode productions

A few productions in Scalanus' grammar permit Unicode codepoints outside the ASCII
range. We define these productions in terms of character properties specified
in the Unicode standard, rather than in terms of ASCII-range codepoints. The
section [Special Unicode Productions](#special-unicode-productions) lists these
productions.

## String table productions

Some rules in the grammar &mdash; notably unary operators, binary operators,
and [keywords](#keywords) &mdash; are given in a simplified form: as a listing
of a table of unquoted, printable whitespace-separated strings. These cases form
a subset of the rules regarding the [token](#tokens) rule, and are assumed to be
the result of a lexical-analysis phase feeding the parser, driven by a DFA,
operating over the disjunction of all such string table entries.

When such a string enclosed in double-quotes (`"`) occurs inside the grammar,
it is an implicit reference to a single member of such a string table
production. See [tokens](#tokens) for more information.

# Lexical structure

## Input format

Scalanus input is interpreted as a sequence of Unicode codepoints encoded in UTF-8.
Most Scalanus grammar rules are defined in terms of printable ASCII-range
codepoints, but a small number are defined in terms of Unicode properties or
explicit codepoint lists.

## Special Unicode Productions

The following productions in the Scalanus grammar are defined in terms of Unicode
properties: `ident`, `non_null`, `non_eol`, `non_single_quote` and
`non_double_quote`.

### Identifiers

The `ident` production is any nonempty Unicode string of the following form:

- The first character has property `XID_start`
- The remaining characters have property `XID_continue`

that does _not_ occur in the set of [keywords](#keywords).

> **Note**: `XID_start` and `XID_continue` as character properties cover the
> character ranges used to form the more familiar C and Java language-family
> identifiers.

### Delimiter-restricted productions

Some productions are defined by exclusion of particular Unicode characters:

- `non_null` is any single Unicode character aside from `U+0000` (null)
- `non_eol` is `non_null` restricted to exclude `U+000A` (`'\n'`)
- `non_single_quote` is `non_null` restricted to exclude `U+0027`  (`'`)
- `non_double_quote` is `non_null` restricted to exclude `U+0022` (`"`)

## Miscellaneous productions

These productions do not have any special Scalanus grammar meaning, but are
defined in order to simplify definitions of more sophisticated productions.

```antlr
hex_digit   : 'a' | 'b' | 'c' | 'd' | 'e' | 'f'
            | 'A' | 'B' | 'C' | 'D' | 'E' | 'F'
            | dec_digit
oct_digit   : '0' | '1' | '2' | '3' | '4' | '5' | '6' | '7'
dec_digit   : '0' | nonzero_dec
nonzero_dec : '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9'
```

## Comments

```antlr
comment            : block_comment | line_comment
block_comment      : '/*' block_comment_body '*/'
block_comment_body : [ block_comment | character ]*
line_comment       : '//' non_eol*
```

## Whitespace

```antlr
whitespace_char : '\x20' | '\x09' | '\x0a' | '\x0d'
whitespace      : [ whitespace_char | comment ]+
```

## Tokens

```antlr
token : [ keyword | op | ident | literal | symbol ] whitespace
```

### Keywords

|           |           |           |           |           |
|-----------|-----------|-----------|-----------|-----------|
| _         | and       | as        | break     | case      |
| class     | const     | continue  | do        | else      |
| enum      | False     | fn        | for       | if        |
| import    | in        | loop      | mod       | or        |
| return    | trait     | True      | type      | while     |
| yield     |


Keywords are case-sensitive. Each of these has special meaning in its grammar,
and all of them are excluded from the `ident` rule.

Not all of these keywords are used by the language as of now. Some of them were
reserved to make space for possible future features.

### Literals

```antlr
literal : string_lit | char_lit | num_lit | bool_lit | unit_lit
```

#### Character and string literals

```antlr
char_lit   : '\x27' char_body '\x27'
string_lit : '"' string_body* '"'

char_body : non_single_quote
          | '\x5c' [ '\x27' | common_escape ]

string_body : non_double_quote
            | '\x5c' [ '"' | common_escape ]

common_escape  : '\x5c' | 'n' | 'r' | 't' | '0'
               | 'x' hex_digit 2
               | 'u' '{' hex_digit+ 6 '}'
```

#### Number literals

```antlr
num_lit : nonzero_dec [ dec_digit | '_' ]* float_suffix?
        | '0' [       [ dec_digit | '_' ]* float_suffix?
              | 'b'   [ '1' | '0' | '_' ]+
              | 'o'   [ oct_digit | '_' ]+
              | 'x'   [ hex_digit | '_' ]+  ]

float_suffix : exponent | '.' dec_lit exponent?

exponent : [ 'E' | 'e' ] [ '-' | '+' ]? dec_lit

dec_lit : [ dec_digit | '_' ]+
```

#### Boolean literals

```antlr
bool_lit : 'True' | 'False'
```

The two values of the boolean type are written `True` and `False`.

#### Unit literal

```antlr
unit_lit : '()'
```

### Symbols

```antlr
symbol : '[' | ']' | '(' | ')' | '{' | '}' | ',' | ';'
```

Symbols are a general class of printable [tokens](#tokens) that play structural
roles in a variety of grammar productions. They are cataloged here for
completeness as the set of remaining miscellaneous printable tokens that do not
otherwise appear as [operators](#operators) or [keywords](#keywords).

# Elementary language constructs and grammar

The entry rule of Scalanus source file is called `program`.

```antlr
program : whitespace? stmt*
```

## Paths

```antlr
path : ident
```

A *path* is an unique name of an [item](#items) or variable.

## Statements

```antlr
stmt : [ decl_stmt | expr ] ';'?
```

### Declaration statements

```antlr
decl_stmt   : assign_stmt | item
assign_stmt : pattern '=' expr
```

Assignment statement tries to match given expression with given pattern.
If matching fails, `MatchError` exception is thrown.

## Patterns

```antlr
pattern          : simple_pattern [ ',' simple_pattern ]*

simple_pattern   : wildcard_pattern
                 | mem_acc_pattern
                 | path_pattern
                 | mut_pattern
                 | value_pattern
                 
wildcard_pattern : '_'
mem_acc_pattern  : mem_acc_expr
path_pattern     : path
value_pattern    : '^' expr
```

Pattern matching mechanism allows destructuring complex data, perform
some assumptions on its values and mutate them. A *pattern* is
a sequence of one or more [*simple patterns*](#simple-patterns).
Each simple pattern has special meanings which have been described later.

If matching one of simple patterns fails, the whole process of 
pattern matching does so, and whole pattern is treated as *unmatched*.
Handling of unmatched patterns depends on context (usually `MatchError`
exception is thrown).

If pattern consists of multiple simple pattern, it is assumed that
matching is performed against a [tuple](#tuples) (otherwise, pattern
is unmatched). `i`-th simple pattern corresponds to `i`-th tuple element.

**Example**

```
> a, _, c = (1, 2, 3)
a == 1 and c == 3   // 2 is ignored

> a = 3
a == 3              // path pattern mutates variable/item if it already exists

> arr = [1, 2]
> arr[0], arr[1] = (3, 4)
arr[0] == 3 and arr[1] == 4

> x = 1
> ^x, ^2 + 2 = (1, 4) // this matches
> ^2 + 2 = 5          // but this throws MatchError
```

### Simple patterns

#### Path pattern

Path pattern has different semantics depending on execution context.
If path points to undefined variable, a new variable is created with
matching value. Otherwise pattern tries to mutate pointed existing variable.

#### Value pattern

A value pattern evaluates expression and compares result with matched value.
If they are different, pattern does not match.

#### Wildcard pattern

A wildcard pattern, denoted `_`, ignores matched value (matches anything). 

#### Member access pattern

A member access pattern tries to mutate specified element of given object.

If object does not have element with given key, `MemberAccessError` is
thrown.

If mutation is impossible, then `MutationError` is thrown.

## Items

```antlr
item : fn_item
```

### Functions

```antlr
fn_item : 'fn' ident '(' pattern? ')' block
```

### Blocks

A block is a sequence of statements, possibly ending with an expression.
The return value of the block is the value of the last expression
statement, or `()` otherwise.

```antlr
block : '{' stmt* '}'
```

## Expressions

```antlr
expr : literal_expr
     | tuple_expr
     | array_expr
     | dict_expr
     | block_expr
     | op_expr
     | if_expr
     | for_expr
     | while_expr
     | loop_expr
     | break_expr
     | continue_expr
     | return_expr

literal_expr : literal
block_expr   : block
tuple_expr   : tuple
```

### Operators

The special `op_expr` production means unary and binary expression
with operator, and for brevity, it is denoted in this document using
following precedence table:

| Precedence | Operator          | Associativity | Operation |
|------------|-------------------|---------------|---|
| 20         | `(...)`           | n/a           | Grouping |
| 19         | `... . ...`       | left-to-right | [Member access](#member-access) |
|            | `... [ ... ]`     | left-to-right | [Computed member access](#member-access) |
| 18         | `... ( ... )`     | left-to-right | [Function call](#function-call) |
| 17         | `... ++`          | n/a           | Postfix increment |
|            | `... --`          | n/a           | Postfix decrement |
| 16         | `! ...`           | right-to-left | Logical NOT |
|            | `~ ...`           | right-to-left | Bitwise NOT |
|            | `+ ...`           | right-to-left | Unary plus |
|            | `- ...`           | right-to-left | Unary minus |
|            | `++ ...`          | n/a           | Prefix increment |
|            | `-- ...`          | n/a           | Prefix decrement |
| 15         | `... ** ...`      | right-to-left | Exponentation |
| 14         | `... * ...`       | left-to-right | Multiplication |
|            | `... / ...`       | left-to-right | Division |
|            | `... mod ...`     | left-to-right | Remainder |
| 13         | `... + ...`       | left-to-right | Addition |
|            | `... - ...`       | left-to-right | Substraction |
| 12         | `... << ...`      | left-to-right | Bitwise left shift |
|            | `... >> ...`      | left-to-right | Bitwise right shift |
| 11         | `... & ...`       | left-to-right | Bitwise AND |
| 10         | `... ^ ...`       | left-to-right | Bitwise XOR |
| 9          | `... | ...`       | left-to-right | Bitwise OR |
| 8          | `... == ...`      | left-to-right | Equality |
|            | `... != ...`      | left-to-right | Inequality |
|            | `... < ...`       | left-to-right | Less than |
|            | `... > ...`       | left-to-right | Greater than or equal |
|            | `... <= ...`      | left-to-right | Less than or equal |
|            | `... >= ...`      | left-to-right | Greater than or equal |
| 7          | -                 | -             | - |
| 6          | `... and ...`     | left-to-right | Logical AND |
| 5          | `... or ...`      | left-to-right | Logical OR |
| 4          | -                 | -             | - |
| 3          | -                 | -             | - |
| 2          | -                 | -             | - |
| 1          | -                 | -             | - |
| 0          | -                 | -             | - |

#### Member access

```antlr
mem_acc_expr : expr [ '.' ident | '[' expr ']' ]
```

*Member access* and *compound member access* expressions allow
to access members of compound data structures, such as
[tuples](#tuples), [arrays](#arrays) and [dictionaries](#dictionaries).

TODO: Member access lookups in object method namespace

If object does not have requested member, `MemberAccessError` is thrown.

#### Function call

```antlr
fn_call : expr '(' [ expr [ ',' expr ]* ]? ')'
```

### Tuples

```antlr
tuple : '(' expr ',' [ [ expr ',' ]* expr ','? ]? ')
```

In order to differentiate it from grouping expression, if tuple
contains one value, it has to end with extra comma. Otherwise,
dangling comma is optional:

```
(2 + 2)      // grouping
(2 + 2,)     // 1-tuple
(2 + 2, 3)   // pair
(2 + 2, 3,)  // pair
```

Tuple is an immutable, lightweight, ordered collection of one
or more values. Its elements are indexed starting with 0.

**Example**

```
> tup = (1, 2, 3)
> a, 2, _ = tup   // tuples can be unpacked with pattern matching
tup[0] == 1     // or their elements can be accessed directly
```

### Arrays

Arrays is a mutable (it can be resized), ordered collection of
zero or more values. Its elements are indexed starting with 0.
In contrast to tuples, arrays cannot be unpacked.

Arrays do not have dedicated syntax for construction. One
can create new array using `Array.of` function which instantiates
new array of all passed arguments.

**Example**

```
> arr = Array.of(1, 2, 3)
arr[0] == 1
```

### Dictionaries

```antlr
dict      : '#{' [ dict_elem [ ',' dict_elem ]* ','? ]? '}'
dict_elem : expr '=' expr
```

Dictionary is a mutable, unordered collection of key-value pairs.
In Scalanus dictionaries are implemented using hashmaps.

**Example**

```
> dict = #{ "hi" = 123, 0 = "foobar" }
dict.hi == 123
dict[0] == "foobar"
```

### If expressions

```antlr
if_expr : 'if' expr
          block
          else_tail?

else_tail : 'else' [ if_expr | block ]
```

The return value of the `if-else` expression is either the result
of the *if* block, or the *else* one. If the latter one was not
provided, it evaluates to `()`, e.g.:

```
> a = if False { 1234 }
a == ()
```

### For loops

```antlr
for_expr : 'for' pattern 'in' expr
           block
```

The `for` loop iterates over an iterable value, for example array or list.

The `for` loop is syntactic sugar for a while loop which consumes an iterator:

```
it = /* expr */.iterator()
while it.hasNext(it) {
    /* pattern */ = it.next()
    /* body */
}
```

### While loops

```antlr
while_expr : 'while' expr
             block
```

The `while` loop is also similar to constructs in other languages, and it also
always returns `()`.

The `while` loop is syntactic sugar for following snippet:

```
loop {
    if /* condition */ { break }
    /* body */
}
```

### Infinite loops

```antlr
loop_expr : 'loop' block
```

`loop` always returns `()`.

### Break expressions

```antlr
break_expr : 'break'
```

`break` does not evaluate as it performs jump, but technically
it should evaluate to `()`.

### Continue expressions

```antlr
continue_expr : 'continue'
```

`continue` does not evaluate as it performs jump, but technically
it should evaluate to `()`.

### Return expressions

```antlr
return_expr : 'return' expr
```

`return` does not evaluate as it performs jump, but technically
it should evaluate to `()`.
