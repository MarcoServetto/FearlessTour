package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_021Chapter03NewOptBool {
/*START
--CHAPTER-- Chapter 3
--SECTION-- NewOptBool

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
  F[A,B,R]:{ #(a: A,b:B): R } //and a few more overloads
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

### Common interfaces

Boolean, numbers, strings and many other widely used types from the standard library implement
a bunch of common types to make them usable in common situations.
We are going to explain those in the details later, but we shall summarize them here.
Do not worry, we are going to discuss all those types in details later!

````
ToStr:{ read .str: Str }
ToStr[A]:{ read .str(f: F[A,ToStr]): Str }
ToInfo:{ read .info: Info }
ToInfo[A]:{ read .info(f: F[A,ToInfo]): Info }
Info:{ /*explained later; exposes information for communicating across programs*|/ }
Order[T]:{ .. /*explained later; provides methods ==, !=, <=, >= etc*|/ }
OrderHash[T]:Order[T]{ .hash(h:Hasher): Nat }
OrderHash[T,E]:{ .order(f: F[E,OrderHash[E]]): OrderHash[T] }
Hasher:{ .. /*explained later*|/}
ToImm[T]:{ read .imm: T }
````
Method `ToStr.str` represents an object as a string.
Method `ToInfo.info` represents an object in a structured data format (similar to JSON) useful for communication across programs.
Type `OrderHash[T]` provides hashing and comparisons methods to a type `T` extending it. Objects extending `OrderHash[T]` can easily be organised in efficient data structures.
Method `ToImm[T].imm` converts an object of any reference capabilities into an immutable version of the same object. For objects that can only ever be immutable, this method simply returns the object itself.

Note how many of those types have a generic variant, like `ToStr` and `ToStr[T]`. As we will see later, this is because for generic containers we need a way to convert the contained objects to be able to convert the container itself.

### Full code for Bool
Here we can show the full code for `Bool`.
````
Bool: Sealed, ToStr, ToInfo, ToImm[Bool], OrderHash[Bool]{
  .and(b: Bool): Bool,
  &&(b: mut MF[Bool]): Bool,
  .or(b: Bool): Bool,
  ||(b: mut MF[Bool]): Bool,
  .not: Bool,
  .if[R:**](f: mut ThenElse[R]): R,
  ?[R:**](f: mut ThenElse[R]): R -> this.if(f),
  .match[R](m: mut BoolMatch[R]): R -> this?{.then->m.true, .else->m.false},
  .info-> Infos.msg(this.str),
  &(b: Bool): Bool -> this.and(b),
  |(b: Bool): Bool -> this.or(b),
  }
True: Bool{
  .and(b) -> b,
  .or(b) -> this,
  &&(b)  -> b#,
  ||(b)  -> this,
  .not -> False,
  .if(f) -> f.then,
  .str -> `True`,
  .imm -> True,
   <=>(other) -> other?{.then -> OrderEq, .else -> OrderGt},
   .hash(h)-> 1,
  }
False: Bool{
  .and(b) -> this,
  .or(b) -> b,
  &&(b)  -> this,
  ||(b)  -> b#,
  .not -> True,
  .if(f) -> f.else,
  .str -> `False`,
  .imm -> False,
   <=>(other) -> other?{.then -> OrderLt, .else -> OrderEq},
  .hash(h)-> 0,
  }
ThenElse[R:**]: {
  mut .then: R,
  mut .else: R,
  }
BoolMatch[R:**]:{
  mut .true: R,
  mut .false: R,
  }
````

As you can see, `Bool` is quite similar to what we have seen already.
There are a few new methods and changes that we will now explain.

The most crucial change is the `ThenElse` matcher type:
Note how the two methods `.then` and `.else` take a `mut` receiver.
We are not requiring the boolean to be `mut`. This is about the `ThenElse` object that is usually
created in order to call the `.if` (or `?`) method.

With `mut .then` and `mut .else`, the operation inside the `.match` is able to mutate external state if need be.
We also add a more standard `BoolMatch` allowing to see `True` and `False` as the two cases of `Bool`. It is just like the `.if`, but uses different terminology.

Overall, as you can see, we are chosing to support many different ways to do the same conceptual thing: see methods `.or` and `|`, `.if`, `?` and `.match`.
We do this to support different programming styles instead to impose our preferences.

For now, do not worry about the method `<=>`; it is needed to implement `Order[T]` as we will explain later.

### Full code for `Opt[T]`

Below we show the full standard library code for optionals.
You may not be able to understand all the details yet, but it is good to familiarize yourself with it.

````
Opts: {
  #[T:*](x: T): mut Opt[T] -> { .match(m) -> m.some(x) },
  }
OptMatch[T:*, R:**]: {
  mut .some(x: T): R,
  mut .empty: R
  }
````
`Opts` is the factory for `Opt[T]`. This is exactly what we have seen bebore. Note how `Opts#` returns a `mut Opt[T]`.
This is what gives the most flexibility to the user.
If the user needs an `imm Opt[T]`, promotion can be transparently used.

Also `OptMatch` is pretty much what we would expect.
Note how the two methods `.some` and `.empty` takes a `mut` receiver.
We are not requiring the optional to be `mut`. This is about the `OptMatch` object that is usually
created in order to call the `.match` method.
With `mut .some` and `mut .empty`, the operation inside the `.match` is able to mutate external state if need be.


````
Opt[T:*]: _Opt[T], Sealed, ToStr[T], ToInfo[T], OrderHash[Opt[T],T]{
  .match(m)   -> m.empty,

  .isEmpty    -> this.match{.some(_)  -> False, .empty -> True},
  .isSome     -> this.match{.some(_) -> True, .empty -> False},
  
  !           -> this.match{.some(x) -> x, .empty -> Error.msg "Opt was empty"},
  
  .or(default)-> this.match{.some(x) -> x, .empty -> default},
  |(default)-> this.or(default),
  ||(default) -> this.match{.some(x) -> x, .empty -> default#},
  
  .flow       -> this.match{.empty -> Flow#, .some(x) -> Flow#x},

  .as(f)      -> this.match{.some(x)->f#x .empty->{}},
  
  .ifSome(f)  -> this.match{.some(x) -> f#x, .empty -> {}},
  .ifEmpty(f) -> this.match{.some(_) -> {}, .empty -> f#},

  .str(f) -> this.match{ .some(x) -> `Opt[`+f#x+`]`, .empty -> `Opt[]` },
  .info(f) -> this.match{ .some(x) -> Infos.list(f#x), .empty -> Infos.empty },
  .order(f) -> { 
    .hash(h) -> this.match{ .some(x)-> f#(x).hash(h) + 1, .empty -> 0 },
    <=>(other) -> this.match{
      .some(x) -> other.match{.some(y)-> f#(x) <=> y, .empty -> OrderGt },
      .empty   -> other.match{ .some(y)-> OrderLt, .empty -> OrderEq },
      },
    },
  }
````
Via `T:*`, `Opt` works for `imm,read,mut` references.
`_Opt[T]` is used to separate the type signatures from the method implementations. It is a common pattern in Fearless code; it helps to focus on the behaviours and the types separately.
As we will show while discussing `_Opt[T]`, it also avoids a lot of repeated code.

In addition to `.match`, the full `Opt[T]` supports other useful methods.

Methods `.isEmpty` and `.isSome` simply return a boolean stating if the optional was empty or not.

Method `!` is a convenience method that returns the optional content or produces and error.
Calling this method is equivalent to claim

> I, the programmer, know that in this case the optional will definitivelly have a value inside.
> If not, this is an observed bug.

The two methods `Opt[T].or` and `Opt[T]||` are similar to `Bool.or` and `Bool||`:

- `Opt[T].or` returns the value stored in the optional,
or the parameter value as a default result if the optional is empty.
- `Opt[T]||` returns the value stored in the optional,
or it executes the lazy parameter value to get a default result if the optional is empty.

Exactly as for `Bool`, method `|` is just an alias for `.or`.

Method `.flow` returns a `Flow[T]`. Flows are a very important data type in the fearless standard libraries and we will discuss them later.
      
The method `.as` is used to change the type of the optional, taking a function to map the content to a new type.

Finally, methods  `.ifSome` and `.ifEmpty` execute some `Void` returning computation in case the optional has a value or not.

To make optionals integrate with other standard library conventions, we have a
`.str` method, allowing to easily turn optional of anything implementing `ToStr` into a string.
For example
`myOptPerson.str{::}` will convert the optional person into a string,
assuming the person implements `ToStr`.
Indeed, resolving sugar and inference we get the following code:

```
myOptPerson.str(F[Person,ToStr]{#(p: Person): ToStr -> p})
```
As you can see, the `{::}` sugar is very useful in those cases.
But, what if we have a `data: Opt[Opt[Person]]`?
No problem, we can just do `data.str{::str{::}}` and get our string.
This pattern of using nested `{::}` is quite common as we will see more and more.

`Opt[T].info` works exactly in the same way of `.str`, but to produce an `Info` object.

`Opt[T].order` is more complex, and it uses the `Order` type that we have not discussed yet, but overall follows the same idea: we need to take in input a function `f` to turn the `T` into the appropriate type.

We can now see the type signature for `Opt[T]`, as declared in `_Opt[T]`.

````  
_Opt[T:*]:{
  mut  .match[R:**](m: mut OptMatch[T, R]): R,
  read .match[R:*](m: mut OptMatch[read/imm T, R]): R,
  imm  .match[R:*](m: mut OptMatch[imm T, R]): R,

  mut  .map[R:*](f: mut OptMap[T, R]): mut Opt[R],
  read .map[R:*](f: mut OptMap[read/imm T, R]): mut Opt[R],
  imm  .map[R:*](f: mut OptMap[imm T, R]): mut Opt[R],

  mut  .flatMap[R:*](f: mut OptFlatMap[T, R]): mut Opt[R],
  read .flatMap[R:*](f: mut OptFlatMap[read/imm T, R]): mut Opt[R],
  imm  .flatMap[R:*](f: mut OptFlatMap[imm T, R]): mut Opt[R],

  mut  .or(default: T): T,
  read .or(default: read/imm T): read/imm T,
  imm  .or(default: imm T): imm T,

  mut  |(default: T): T,
  read |(default: read/imm T): read/imm T,
  imm  |(default: imm T): imm T,

  mut  ||(default: mut MF[T]): T,
  read ||(default: mut MF[read/imm T]): read/imm T,
  imm  ||(default: mut MF[imm T]): imm T,

  mut  !: T,
  read !: read/imm T,
  imm  !: imm T,

  mut  .flow: mut Flow[T],
  read .flow: mut Flow[read/imm T],
  imm  .flow: mut Flow[imm T],

  mut  .ifSome(f: mut MF[T, Void]): Void,
  read .ifSome(f: mut MF[read/imm T, Void]): Void,
  imm  .ifSome(f: mut MF[imm T, Void]): Void,

  read .isEmpty: Bool,
  read .isSome: Bool,
  read .ifEmpty(f: mut MF[Void]): Void,
  read .as[R:imm](f: mut MF[read/imm E, R]): Opt[R],  
  }
````

As you can see, this is where the major difference lies with respect to the optional seen in Chapter 2: Here most methods come in three variants, one for `T`, one for `read/imm T` and one for `imm T`.

`Opt[T]` is an example of a generic continer type: a type whose main goal is to contain any kind of `T`, where `T` can be `read,imm,mut`.
As you can see, designing generic container types supporting a range of reference capabilities is not a beginner friendly task. However, this pattern is quite consistent, and many generic containers follow this same structure, with many methods offering exactly those three type variants.



OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}