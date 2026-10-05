package chaptersOfZeroToHero;

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
  .plus   -> Exps.sum(Vars#(left), Vars#(this.parseNum(l))),
  .eof    -> left,
  .num(n) -> Error.msg "unexpected num",
  }
````
the matcher would need to capture `l` and `left`.
The solution is a matcher whose match method takes the extra references as parameters, and passes them to each case:
````
CaseToken[A,B,R:**]: { .match(a: iso A, b: mutH B, m: mut CaseTokenMatch[A,B,R]): R }
CaseTokenMatch[A,B,R:**]: {
  .eof(a: iso A, b: mutH B): R;
  .plus(a: iso A, b: mutH B): R;
  .num(a: iso A, b: mutH B, n: Nat): R;
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

- `Float` and `Num`, the numeric types for fractions (Chapter 1, finite numbers).
- How the checks on numbers, like overflow and underflow, can be tuned (Chapter 1, finite numbers).
- Fluent libraries where the continuation changes the receiver to a different value or type (Chapter 2, locals).
- `DataType` in detail (Chapter 3).
- `Try` in detail (Chapter 3).
- Flows in detail, including parallel flows and the difference between `.flow` and `.seqFlow` (Chapter 3).
- `EList[E]` and `ESet[E]` (Chapter 3, collections).
- Graphics, to render the tanks as images (Chapter 3).
- Deterministic and non deterministic errors (Chapter 4, action).

## Helping the inference

The inference is going to improve over time, so the guide talks about it in terms of what may or may not compile.
The discussion has to cover the ways to help it:
explicit type arguments (`Foo#[Bar](bar)`), literals with an explicit type (`OrderBy[Tank,Direction]{::.imm.heading}`), and the difference between arguments, where an expected type is known, and receivers, where it may not be.
END*/
}
