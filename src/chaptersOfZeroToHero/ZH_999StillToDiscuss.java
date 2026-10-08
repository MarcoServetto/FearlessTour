package chaptersOfZeroToHero;

import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_999StillToDiscuss {
/*START
--CHAPTER-- Still to discuss
--SECTION-- Still to discuss

# Still to discuss

This chapter lists the topics that the guide still has to discuss.
Each entry says what the discussion has to cover.
When a topic is written up in its proper chapter, its entry leaves this list.

## Variants of match

Fearless has no special syntax for pattern matching: a type offers a `.match`-like method taking a matcher object, and the matcher has one method for each case.
The guide uses this encoding everywhere, but it comes in several variants, and each variant has its own strengths.
This section has to present them side by side.

### Match as in `Bool`: every case is a named type

````
Bool: Sealed,DataType[Bool,Bool] { .if[R:**](m: mut ThenElse[R]): R; ... }
ThenElse[R:**]: { mut .then: R; mut .else: R; }
True: Bool { .if m -> m.then; ... }
False: Bool { .if m -> m.else; ... }
````
Every case is a top level type, and each case implements the match method by selecting its own method of the matcher.
The cases carry no data, so each case is a single object, and the code can mention a case by name: `True`, `False`.

### Match as in `Opt` and `Stack`: some cases have no named type

````
Opt[E:*]: _Opt[E] { .match m -> m.empty; ... }
Opts: { #[E:*](x: E): mut Opt[E] -> { .match m -> m.some(x) }; }
OptMatch[E:*,R:**]: { mut .some(E): R; mut .empty: R; }
````
The empty optional is `Opt` itself, while the case `.some` has no named type: it is an object literal created by `Opts#`, capturing the value.
The `Stack` of Chapter 2 has the same shape: `Stack` is the empty stack, and `+` creates the non empty case as an object literal.
The discussion has to cover when a case deserves a name and when a factory method is enough.

### Match as in `Action`: the match runs a computation

````
Action[T:*]: {
  mut .run[R:*](m: mut ActionMatch[T,R]): R;
  mut .map[R:*](f: mut MF[T,R]): mut Action[R] -> {m -> this.run{
    .ok   x -> m.ok(f#x);
    .info i -> m.info(i);
    }};
  ...
  }
ActionMatch[T:*,R:*]: { mut .ok(x: T): R; mut .info(info: Info): R; }
````
An `Action` is not a value that already is in one of its cases: it is a computation, and the case is decided only when `.run` executes it.
Methods like `.map` and `.andThen` return new actions without running anything.
The discussion has to cover what lazy matching means: when the computation runs, what happens when an action is run twice, and how this differs from composing matches over data that already exists.

### Match as in `Order`: an interface, but no cases

````
Order: {
  read #[R:**](m: mut OrderMatch[R]): R;
  read &&(onEq: mut MF[Order]): Order -> this#{ .lt -> {::.lt}; .eq -> onEq#; .gt -> {::.gt} };
  }
OrderMatch[R:**]: { mut .lt: R; mut .eq: R; mut .gt: R; }
````
There are no `Lt`, `Eq` or `Gt` types: the outcomes are the object literals `{::.lt}`, `{::.eq}` and `{::.gt}`.
Combined outcomes, like the result of `&&`, are just more implementations of the same interface.
The discussion has to compare this with `Bool`, where the cases have names.

### Match and `Block`: return the outcome instead of taking the matcher

A method that takes a matcher and returns the `R:**` produced by the matcher can not compute its result with `Block`:
````
.cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R -> Block#
  .if {t0.x < (t1.x)}.return {m.lt}
  ...
````
does not compile, since `Block#` requires its `R` to be `R:*`.
`Block` needs `R:*` because an early return (`.if{..}.return{..}`) stores the result in an object for the rest of the chain, and an object can not store a hygienic reference.
The solution is for the method to return a function from the matcher to the result, as `Order` does, instead of taking the matcher.
The block then computes an ordinary value, and applying it to the matcher produces the `R:**`:
````
.cmp(t0: read T, t1: read T): Order -> Block#
  .if {t0.x < (t1.x)}.return {{::.lt}}
  ...
  .return {{::.eq}};
````
This works for any matcher, not only for comparisons.

### Match carrying `iso` and hygienic references on the side

A matcher is an object literal, and object literals can not capture hygienic references (`mutH`, `readH`), while an `iso` reference is captured as `imm`.
Thus code matching on a value while holding a `mutH` or an `iso` reference may not compile.
For example, in a parser consuming a `mutH Lexer` and building an `iso Exp`:
````
.parsePlus(l: mutH Lexer, left: iso Exp): iso Exp -> l.nextToken.match{
  .plus   -> Exps.sum(Vars#(left), Vars#(this.parseNum(l)));
  .eof    -> left;
  .num(n) -> Error.msg "unexpected num";
  }
````
the matcher would need to capture `l` and `left`.
The solution is a matcher whose match method takes the extra references as parameters, and passes them to each case:
````
CaseToken[A,B,R:**]: { .match(a: iso A, b: mutH B, m: mut CaseTokenMatch[A,B,R]): R }
CaseTokenMatch[A,B,R:**]: {
  mut .eof(a: iso A, b: mutH B): R;
  mut .plus(a: iso A, b: mutH B): R;
  mut .num(a: iso A, b: mutH B, n: Nat): R;
  }
CaseTokens: { #[A,B,R:**](t: Token): mut CaseToken[A,B,R] -> t.match{
  .eof    -> {a,b,m -> m.eof(a,b)};
  .plus   -> {a,b,m -> m.plus(a,b)};
  .num(n) -> {a,b,m -> m.num(a,b,n)};
  }}
````
````
.parsePlus(l: mutH Lexer, left: iso Exp): iso Exp ->
  CaseTokens#(l.nextToken).match(left, l, {
    .plus(left', l') -> Exps.sum(Vars#(left'), Vars#(this.parseNum(l')));
    .eof(left', _)   -> left';
    .num(_, _, n)    -> Error.msg "unexpected num";
    })
````
The data flows through parameters instead of captures, so the type system can track it precisely.
This variant needs the hygienic reference capabilities, discussed below.

## Hygienic reference capabilities

`**` stands for all the reference capabilities, including `iso`, `mutH` and `readH`, that the guide has not discussed yet.
The discussion has to cover:
- what `mutH` and `readH` are, and why object literals can not capture them;
- what `iso` is, how promotion produces it, and why capturing an `iso` reference gives an `imm` one;
- `R:*` versus `R:**` in generic types, and why `Block` takes `R:*`;
- `Block.openIso`.

## Promised by earlier chapters

- The rules for valid type names (Chapter 1, tanks).
- `Float` and `Num`, the numeric types for fractions (Chapter 1, finite numbers).
- How the checks on numbers, like overflow and underflow, can be tuned (Chapter 1, finite numbers).
- Ways to hide auxiliary methods, like `._rightSub` (Chapter 1, finite numbers).
- Fluent libraries where the continuation changes the receiver to a different value or type (Chapter 2, locals).
- Testing, to supplement the visualisation of how code reduces (Chapter 2, stack).
- Why tail recursive algorithms are not a concern in Fearless (Chapter 2, stack).
- `DataType` in detail (Chapter 3).
- `Try` in detail (Chapter 3).
- Flows in detail, including parallel flows and the difference between `.flow` and `.seqFlow` (Chapter 3).
- `EList[E]` and `ESet[E]` (Chapter 3, collections).
- Graphics, to render the tanks as images (Chapter 3).
- Deterministic and non deterministic errors (Chapter 4, action).
- The techniques supporting offensive programming when working with mutable data (Chapter 4, action).

## Helping the inference

The inference is going to improve over time, so the guide talks about it in terms of what may or may not compile.
The discussion has to cover the ways to help it:
explicit type arguments (`Foo#[Bar](bar)`), literals with an explicit type (`OrderBy[Tank,Direction]{::.imm.heading}`), and the difference between arguments, where an expected type is known, and receivers, where it may not be.
OMIT_START
-------------------------*/@Test void blockRejectsResultWithHygienicReferences() { run("""
use base.Nat as Nat;
use base.Block as Block;
use base.OrderMatch as OrderMatch;
Point:{ .x: Nat; .y: Nat;
  .cmp[R:**](t0: Point, t1: Point, m: mut OrderMatch[R]): R -> Block#
    .if {t0.x < (t1.x)}.return {m.lt}
    .if {t0.x > (t1.x)}.return {m.gt}
    .return {m.eq};
  }
//ERROR|In file: [###]_test/_rank_app111.fear
//ERROR|
//ERROR|005|   .cmp[R:**](t0: Point, t1: Point, m: mut OrderMatch[R]): R -> Block#
//ERROR|   |                                                                ^^^^^^
//ERROR|   | ... 2 lines ...
//ERROR|008|     .return {m.eq};
//ERROR|
//ERROR|While inspecting method call "#" > ".cmp(_,_,_)" line 5
//ERROR|The call to "#" is invalid.
//ERROR|Type argument 1 ("R") does not satisfy the bounds
//ERROR|for type parameter "R" in "Block#".
//ERROR|Here "R" can only use capabilities "imm" or "mut" or "read".
//ERROR|But type argument "R" can use capabilities "imm" or "iso" or "mut" or "mutH" or "read" or "readH".
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|Block#[imm,R]
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------
-------------------------*/@Test void blockComputesAnOrderThatIsAppliedToTheMatcher() { run("""
use base.Nat as Nat;
use base.Block as Block;
use base.Order as Order;
Point:{ .x: Nat; .y: Nat; }
Points:{ #(x: Nat, y: Nat): Point -> { .x -> x; .y -> y } }
Compare:{
  #(t0: Point, t1: Point): Order -> Block#
    .if {t0.x < (t1.x)}.return {{::.lt}}
    .if {t0.x > (t1.x)}.return {{::.gt}}
    .return {{::.eq}};
  }
Name:{ #(o: Order): base.Str -> o#{.lt->"lt";.eq->"eq";.gt->"gt"} }
Test:base.Main {sys -> Block#
  .do{base.Debug#(Name#(Compare#(Points#(1,2),Points#(2,0))))}
  .do{base.Debug#(Name#(Compare#(Points#(3,2),Points#(2,0))))}
  .do{base.Debug#(Name#(Compare#(Points#(2,2),Points#(2,0))))}
  .return{base.Void}}
//PRINT|lt
//PRINT|gt
//PRINT|eq
"""); }/*--------------------------------------------
-------------------------*/@Test void matchShapes() { run("""
use base.Nat as Nat;
use base.Block as Block;
use base.Debug as Debug;
use base.Error as Error;
use base.Opt as Opt;
use base.Opts as Opts;
use base.Order as Order;
Name:{ #(o: Order): base.Str -> o#{.lt->"lt";.eq->"eq";.gt->"gt"} }
Test:base.Main {sys -> Block#
  .do{Debug#(base.True.if{.then->"True case";.else->"False case"})}
  .do{Debug#(base.False.if{.then->"True case";.else->"False case"})}
  .do{Debug#(Opts#(7).match{.empty->0; .some x->x})}
  .do{Debug#(Opt[Nat].match{.empty->0; .some x->x})}
  .do{Debug#(Name#(Order{::.lt} && {Error.msg "never evaluated"}))}
  .do{Debug#(Name#(Order{::.gt} && {Error.msg "never evaluated"}))}
  .do{Debug#(Name#(Order{::.eq} && {Order{::.gt}}))}
  .do{Debug#(Name#(Order{::.eq} && {Order{::.eq}}))}
  .return{base.Void}}
//PRINT|True case
//PRINT|False case
//PRINT|7
//PRINT|0
//PRINT|lt
//PRINT|gt
//PRINT|gt
//PRINT|eq
"""); }/*--------------------------------------------
-------------------------*/@Test void matcherCannotCaptureHygienicReferences() { run("""
use base.Nat as Nat;
use base.Error as Error;
Exp:{ .val: Nat }
Exps:{
  #(n: Nat): iso Exp -> { .val -> n };
  .sum(a: Exp, b: Exp): iso Exp -> { .val -> a.val + (b.val) };
  }
Token:{ .match[R:**](m: mut TokenMatch[R]): R }
TokenMatch[R:**]:{ mut .plus: R; mut .eof: R; mut .num(n: Nat): R; }
Lexer:{ mut .nextToken: Token; }
Parser:{
  .parseNum(l: mutH Lexer): Exp -> Exps#(1);
  .parsePlus(l: mutH Lexer, left: iso Exp): iso Exp -> l.nextToken.match{
    .plus   -> Exps.sum(left, this.parseNum(l));
    .eof    -> left;
    .num(n) -> Error.msg "unexpected num";
    }
  }
//ERROR|In file: [###]_test/_rank_app111.fear
//ERROR|
//ERROR|013|   .parsePlus(l: mutH Lexer, left: iso Exp): iso Exp -> l.nextToken.match{
//ERROR|014|     .plus   -> Exps.sum(left, this.parseNum(l));
//ERROR|   |     ----------------------------------------^^-
//ERROR|   | ... 2 lines ...
//ERROR|017|     }
//ERROR|
//ERROR|While inspecting parameter "l" > ".plus" line 14 > ".parsePlus(_,_)" line 13
//ERROR|parameter "l" has type "mutH Lexer".
//ERROR|The type of parameter "l" is hygienic (readH or mutH)
//ERROR|and thus it cannot be captured in the object literal instance of "mut TokenMatch[iso Exp]" (line 13).
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|l
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------
-------------------------*/@Test void matcherCarriesHygienicReferencesAsParameters() { run("""
use base.Nat as Nat;
use base.Error as Error;
Exp:{ .val: Nat }
Exps:{
  #(n: Nat): iso Exp -> { .val -> n };
  .sum(a: Exp, b: Exp): iso Exp -> { .val -> a.val + (b.val) };
  }
Token:{ .match[R:**](m: mut TokenMatch[R]): R }
TokenMatch[R:**]:{ mut .plus: R; mut .eof: R; mut .num(n: Nat): R; }
Lexer:{ mut .nextToken: Token; }
CaseToken[A,B,R:**]: { .match(a: iso A, b: mutH B, m: mut CaseTokenMatch[A,B,R]): R }
CaseTokenMatch[A,B,R:**]: {
  mut .eof(a: iso A, b: mutH B): R;
  mut .plus(a: iso A, b: mutH B): R;
  mut .num(a: iso A, b: mutH B, n: Nat): R;
  }
CaseTokens: { #[A,B,R:**](t: Token): mut CaseToken[A,B,R] -> t.match{
  .eof    -> {a,b,m -> m.eof(a,b)};
  .plus   -> {a,b,m -> m.plus(a,b)};
  .num(n) -> {a,b,m -> m.num(a,b,n)};
  }}
Parser:{
  .parseNum(l: mutH Lexer): Exp -> Exps#(1);
  .parsePlus(l: mutH Lexer, left: iso Exp): iso Exp ->
    CaseTokens#(l.nextToken).match(left, l, {
      .plus(left', l') -> Exps.sum(left', this.parseNum(l'));
      .eof(left', _)   -> left';
      .num(_, _, n)    -> Error.msg "unexpected num";
      })
  }
Tokens:{
  .plus: Token -> { .match m -> m.plus };
  .eof: Token -> { .match m -> m.eof };
  .num(n: Nat): Token -> { .match m -> m.num(n) };
  }
Lexers:{ #(t: Token): mut Lexer -> { .nextToken -> t } }
Test:base.Main {sys -> base.Block#
  .do{base.Debug#(Parser.parsePlus(Lexers#(Tokens.plus), Exps#(5)).val)}
  .do{base.Debug#(Parser.parsePlus(Lexers#(Tokens.eof), Exps#(5)).val)}
  .do{base.Debug#(base.Try#{Parser.parsePlus(Lexers#(Tokens.num 3), Exps#(5)).val}.info!.getMsg)}
  .return{base.Void}}
//PRINT|6
//PRINT|5
//PRINT|unexpected num
"""); }/*--------------------------------------------
OMIT_END
END*/
}
