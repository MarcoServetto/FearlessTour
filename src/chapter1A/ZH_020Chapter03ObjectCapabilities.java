package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_020Chapter03ObjectCapabilities {
/*START
--CHAPTER-- Chapter 3
--SECTION-- ObjectCapabilities

### Object capabilities and Main

Finally we have discussed all of the knowledge needed to make our first Fearless program.

-------------------------*/@Test void finallyMain() { run("""
package test
alias base.Main as Main,
alias base.caps.UnrestrictedIO as UnrestrictedIO,

Test:Main {sys -> UnrestrictedIO#sys.println(`Hello, World!`)}
//prints Hello, World!
"""); }/*--------------------------------------------

This program will print `Hello, World!`.

That is, if we run `Fearless test.Test` over a folder containing just that file, we will see `Hello, World!` on the console.

Any type implementing `base.Main` can be the starting point for the execution.
Main is declared in base as follows:
```
Main:{ .main(sys: mut System): Void }
```

The parameter `sys` refers to an **object capability**: an object able to do external side effects.
`base.UnrestrictedIO` is a function creating a restricted object capability from the system capability.
The result of `UnrestrictedIO#sys` is an object of type `mut base.IO`: an object that can do input output side effects, like writing on the console or reading/writing files.

That is, Object capabilities and Reference capabilities are two different concepts.
- Reference capabilities are a type system feature, while
- Object capabilities are just a programming style that is embraced by the standard library.

### Updating F, Opt and matches. Capturing reference capabilities.

Up to now we have seen a simplified form of `F`, `Opt`, `ThenElse` and other kinds of matchers.
The code we have shown before is not wrong, just limited.
Since we were not using reference capabilities explicitly, everything was immutable.
Immutable object literals can capture immutable bindings, so everything worked.
But, when we add mutability in the mix, the world gets more complicated. More colourful, if you prefer.

An `imm` method in an immutable object literal can only capture `imm` parameters.
Also `iso` parameters can be captured, but this is because they are transparently converted into `imm` parameters.
This means that with the type `F` as we defined it before:
```
  F[R]:{ #: R }
  F[A,R]:{ #(a: A): R }
  F[A,B,R]:{ #(a: A,b:B): R }
```
Method `F#` can not capture `mut` and `read` parameters.

To work flexibly with RCs, we need to modify `F` as below:
```
F[R:**]: {read #: R}
F[A:**,R:**]: {read #(a: A): R}
F[A:**,B:**,R:**]: {read #(a: A, b: B): R} //and a few more overloads

MF[R:**]: {mut #: R}
MF[A:**,R:**]: {mut #(a: A): R}
MF[A:**,B:**,R:**]: {mut #(a: A, b: B): R} //and a few more overloads
```
Note the use of `T:**`. We can use `*` and `**` as shortcut for large generic bounds:
`*` is equivalent to `imm,mut,read`; `**` is equivalent to all of the reference capabilities; including some more unusual ones that we have not discussed yet.

`F#` sees the world as `read` and supports generics with any RC. In particular, this means that a
`read F[X]` can capture mut references as `read`. `MF` (mutable function) sees the world as `mut`. This
means that it can capture `mut` references as `mut`.
Those two types are present in the Fearless standard library.
A method requiring a function as a parameter can leverage RCs to enforce different guarantees
on it.

1. The behaviour of `imm F[X]` can not observe or perform any mutation.
2. The behaviour of
`read F[X]` can not cause mutation, but could observe mutation in the outer scope.
3. The behaviour
of `mut MF[X]` can actively mutate captured references.

-------------
TODO: Should we show this code later, just before list?
Not sure; when we want to talk about the new tanks,
when we want to talk about lists?
----

Below we show the full standard library code for optionals and booleans.
You may not be able to understand all the details yet, but it is good to familiarize yourself with it.

````
Opts: {
  #[T:*](x: T): mut Opt[T] -> {.match(m) -> m.some(x)},
  }
````
`Opts` is the factory for `Opt[T]`. This is exactly what we have seen bebore. Note how `Opts#` returns a `mut Opt[T]`.
This is what gives the most flexibility to the user.
If the user needs an `imm Opt[T]`, promotion can be transparently used.

````
Opt[T:*]: _Opt[T]{
  .match(m)   -> m.empty,
  .map(f)     -> this.match(f),
  .flatMap(f) -> this.match(f),
  ||(default) -> this.match{.some(x) -> x, .empty -> default#},
  |(default)  -> this.match{.some(x) -> x, .empty -> default},
  !           -> this.match{.some(x) -> x, .empty -> Error.msg "Opt was empty"},
  .flow       -> this.match{.empty -> Flow#, .some(x) -> Flow#x},
  .ifSome(f)  -> this.match{.some(x) -> f#x, .empty -> {}},

  read .isEmpty: Bool  -> this.match{.empty -> True,  .some(_)  -> False},
  read .isSome: Bool   -> this.match{.empty -> False, .some(_) -> True},
  imm .imm: Opt[imm T] -> this.match{.empty -> {}, .some(x) -> Opts#x},
  read .ifEmpty(f: mut MF[Void]): Void -> this.match{.some(_) -> {}, .empty -> f#},
  }
````
Via `T:*`, `Opt` works for `imm,read,mut` references.
`_Opt[T]` is used to separate the type signatures from the method implementations. It is a common pattern in Fearless code; it helps to focus on the behaviours and the types separatly,
and, as we will show while discussing `_Opt[T]`, it avoids a lot of repeated code.

In addition to `.match`, the full `Opt[T]` supports other useful methods
`.isEmpty` and `.isSome` simply returns a boolean stating
if the optional was empty or not.
`!` is a convenience method that returns the optional content or
produces and error. Calling this method is equivalent to claim
> I, the programmer, know that in this case the optional will definitivelly have a value inside.
> If not, this is an observed bug.

The two methods `Opt[T].or` and `Opt[T]||` are similar to `Bool.or` and `Bool||`:
- `Opt[T].or` returns the value stored in the optional,
or the parameter value as a default result if the optional is empty.
- `Opt[T]||` returns the value stored in the optional,
or it executes the lazy parameter value to get a default result if the optional is empty.

Finally, `.flow` returns a `Flow[T]`. Flows are a very important data type in the fearless standard libraries and we will discuss them later.
      
> No, .map(f) .flatMap(f) should be under flow
> should .ifSome be also under flow.forEach?
  
Finally, the method `.as`....
  
  imm .imm: Opt[imm T] -> this.match{.empty -> {}, .some(x) -> Opts#x},
  read .ifEmpty(f: mut MF[Void]): Void -> this.match{.some(_) -> {}, .empty -> f#},


````
_Opt[T:*]: Sealed{
  mut  .match[R:**](m: mut OptMatch[T, R]): R,
  read .match[R:*](m: mut OptMatch[read/imm T, R]): R,
  imm  .match[R:*](m: mut OptMatch[imm T, R]): R,

  mut  .map[R:*](f: mut OptMap[T, R]): mut Opt[R],
  read .map[R:*](f: mut OptMap[read/imm T, R]): mut Opt[R],
  imm  .map[R:*](f: mut OptMap[imm T, R]): mut Opt[R],

  mut  .flatMap[R:*](f: mut OptFlatMap[T, R]): mut Opt[R],
  read .flatMap[R:*](f: mut OptFlatMap[read/imm T, R]): mut Opt[R],
  imm  .flatMap[R:*](f: mut OptFlatMap[imm T, R]): mut Opt[R],

  mut  ||(default: mut MF[T]): T,
  read ||(default: mut MF[read/imm T]): read/imm T,
  imm  ||(default: mut MF[imm T]): imm T,

  mut  |(default: T): T,
  read |(default: read/imm T): read/imm T,
  imm  |(default: imm T): imm T,

  mut  !: T,
  read !: read/imm T,
  imm  !: imm T,

  mut  .flow: mut Flow[T],
  read .flow: mut Flow[read/imm T],
  imm  .flow: mut Flow[imm T],

  mut  .ifSome(f: mut MF[T, Void]): Void,
  read .ifSome(f: mut MF[read/imm T, Void]): Void,
  imm  .ifSome(f: mut MF[imm T, Void]): Void,
  }

OptMatch[T:*, R:**]: {mut .some(x: T): R, mut .empty: R}
OptMap[T:*,R:*]: OptMatch[T, mut Opt[R]]{
  mut #(t: T): R,
  .some(x) -> Opts#(this#x),
  .empty -> {}
  }
OptFlatMap[T:*,R:*]: OptMatch[T, mut Opt[R]]{
  mut #(t: T): mut Opt[R],
  .some(x) -> this#x,
  .empty -> {}
  }
````

````
Bool: Sealed, Stringable, ToImm[Bool]{
  .and(b: Bool): Bool,
  &&(b: mut MF[Bool]): Bool -> this.and(b#),
  &(b: Bool): Bool -> this.and(b),
  .or(b: Bool): Bool,
  |(b: Bool): Bool -> this.or(b),
  ||(b: mut MF[Bool]): Bool -> this.or(b#),
  .not: Bool,
  .if[R:**](f: mut ThenElse[R]): R,
  ?[R:**](f: mut ThenElse[R]): R -> this.if(f),
  }
True: Bool{ .and(b) -> b, .or(b) -> this, .not -> False, .if(f) -> f.then(), .str -> "True", .toImm -> True }
False: Bool{ .and(b) -> this, .or(b) -> b, .not -> True, .if(f) -> f.else(), .str -> "False", .toImm -> False }
ThenElse[R:**]: { mut .then: R, mut .else: R, }
````




OMIT_START
-------------------------*/@Test void anotherPackage() { run("""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}