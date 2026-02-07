package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
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
`read F[X]` can capture `mut` references as `read`. `MF` (mutable function) sees the world as `mut`. This
means that it can capture `mut` references as `mut`.
Those two types are present in the Fearless standard library.
A method requiring a function as a parameter can leverage RCs to enforce different guarantees
on it.

1. The behaviour of `imm F[X]` can not observe or perform any mutation.
2. The behaviour of
`read F[X]` can not cause mutation, but could observe mutation in the outer scope.
3. The behaviour
of `mut MF[X]` can actively mutate captured references.

### DataType, later!

Boolean, numbers, strings and many other widely used types from the standard library implement `DataType`.
`DataType` contains a lot of useful functionalities, and we will see them in the details later. For now you just need to know that this interface exists and that we need to implement a few method from it.

### Bool, as actually declared

Here you can see the full code of Bool, as it is in the actual Fearless standard library.
First, two matcher types:
````
ThenElse[R:**]: { mut .then: R; mut .else: R; }
BoolMatch[R:**]:{ mut .true: R; mut .false: R; }
````
We have seen `ThenElse[R]` before; `BoolMatch[R]` is the same but with names for the two cases. They work in the same way, but sometime one of the two is more readable then the other.
Note how we take any kind of `R` by using `R:**` and the methods require a `mut` receiver.
We are not requiring the boolean to be `mut`. This is about the `ThenElse` object that is usually created in order to call the `.if` (or `?`) method.
With `mut .then` and `mut .else`, the operation inside the `.match` is able to mutate external state if need be.
We also add a more standard `BoolMatch` allowing to see `.true` and `.false` as the two cases of `Bool`. It is just like the `.if`, but uses different terminology.

Then we can see the Bool type declaration itself.
It is implementing `Sealed` and `DataType[Bool,Bool]`.
We discussed `Sealed` before: it just means that user code can not define new kinds of booleans like `MayBe` `NotToday` etc.
````
Bool:Sealed,DataType[Bool,Bool]{
  .and(b: Bool): Bool;
  &(b: Bool): Bool -> this.and(b);
  &&(b: mut MF[Bool]): Bool;

  .or(b: Bool): Bool;
  |(b: Bool): Bool -> this.or(b);
  ||(b: mut MF[Bool]): Bool;

  .if[R:**](f: mut ThenElse[R]): R;
  ?[R:**](f: mut ThenElse[R]): R -> this.if(f);
  .match[R:**](m: mut BoolMatch[R]): R -> this?{.then->m.true; .else->m.false};

  .not: Bool;
  ==> (b: mut MF[Bool]): Bool -> this.not || b;
````
We then proceed with the methods we have seen before, implemented exactly as before, or trivial extensions:
- `.and`, `&` (alias for `.and`), `&&` (computing other only when needed)
- `.or`, `|` (alias for `.pr`), `||` (computing other only when needed)
- `.if`, `?` (alias for `.if`), `.match` (taking the other matcher for more regular naming)
- `.not`, returning the other boolean
- `==>`, logical implication; defined with `.not` and `||`.

Overall, as you can see, we are choosing to support many different ways to do the same conceptual thing: see methods `.or` and `|`, `.if`, `?` and `.match`.
We do this to support different programming styles instead to impose our preferences.

Next we are going to see some new methods. They all either come from `DataType` or use features coming from `DataType`
````
  .str -> this.imm?{ .then->`True`; .else->`False` };//this as Str, from DataType

  .assertTrue: Void -> this.assertEq True;
  .assertFalse: Void -> this.assertEq False;

  .info -> Infos.msg(this.str); //methods from DataType here and below
  .close -> this; .close -> ::;
  .hash -> this.imm?{ .then->1; .else->0 };
  .cmp a, b, m -> a.imm?{
    .then->b.imm?{ .then->m.eq; .else->m.gt };
    .else->b.imm?{ .then->m.lt; .else->m.eq };
    };
}
````

- `.str` produces a string representation of our `Bool`.
`.str` is declared as a `read` method in `DataType`; `Bool` values are always immutable, but the type system does not know it; so we need to call `.imm` to turn a potentially `read Bool` into an `imm Bool` before matching.

- `.assertTrue`, `.assertFalse`
Assert methods are very useful to add additional checks and to trigger additional errors.
Many times in the code we have conditions that we expect to hold, and by using those methods we can verify them.
For example
`customer.age >= 18 .assertTrue`
would stop the execution with a readable message if the age is under 18.  
  `drink.hasAlcohol ==> {customer.age >= 18} .assertTrue`
would stop the execution if a customer under 18 tries to buy an alcoholic drink.
How does this work?
  - If `drink.hasAlcohol` is `False`, the whole `drink.hasAlcohol ==> {...}` returns `True` and the assertion passes.
  - If `drink.hasAlcohol` is `True`, then we check `{...}` and return it.

  Methods `.assertTrue`/`.assertFalse` are implemented on top of `DataType.assertEq`.

- `.info`, `.close`, `.hash` and `.cmp` are methods from DataType to represent the boolean in a serialisable format and to allow booleans to be compared and organized into data structures.

````
True:Bool{
  .and b -> b;
  &&   b -> b#;
  .or  b -> this;
  ||   b -> this;
  .not   -> False;
  .if m  -> m.then;
  .imm   -> True;
}
False:Bool{
  .and b -> this;
  &&   b -> this;
  .or  b -> b;
  ||   b -> b#;
  .not   -> True;
  .if  m -> m.else;
  .imm   -> False;
}
````
Finally, the declaration for `True` and `False` is exactly what we have seen before.
Note how we can implement the `read .imm: imm Bool` method by just returning `True` or `False`. An object literal summoned by name can be of any RC, of course including `imm`.

### Full code for `Opt[T]`

Below we show the full standard library code for optionals.

`Opts` is the factory for `Opt[T]`. This is exactly what we have seen before. Note how `Opts#` returns a `mut Opt[T]`.
This is what gives the most flexibility to the user.
If the user needs an `imm Opt[T]`, promotion can be transparently used.

````
Opts:{
  #[E:*](x: E): mut Opt[E] -> { .match m -> m.some(x) };
}
OptMatch[E:*, R:**]:{
  mut .some(E): R;
  mut .empty: R;
}
````
Also `OptMatch` is pretty much what we would expect.
Note how the two methods `.some` and `.empty` takes a `mut` receiver.
We are not requiring the optional to be `mut`. This is about the `OptMatch` object that is usually
created in order to call the `.match` method.
With `mut .some` and `mut .empty`, the operation inside the `.match` is able to mutate external state if need be.


Via `E:*`, `Opt` works for `imm,read,mut` references.
`_Opt[E]` is used to separate the type signatures from the method implementations. It is a common pattern in Fearless code; it helps to focus on the behaviours and the types separately.
As we will show while discussing `_Opt[E]`, it also avoids a lot of repeated code.
In turn, `_Opt[E]` implements `DataType`, thus `Opt` also implements `DataType`. However, there are two kinds of `DataType`: `Bool` implements `DataType`, `Opt` implements the generic version of `DataType`, since `Opt` is a generic type.

In addition to `.match`, the full `Opt[E]` supports other useful methods.
````
Opt[E:*]: _Opt[E]{
  .match m    -> m.empty;
  .isEmpty    -> this.match{.some _ -> False; .empty -> True};
  .isSome     -> this.match{.some _ -> True; .empty -> False};  
  !           -> this.match{.some x -> x; .empty -> Error.msg `Opt was empty`};  

  .or default -> this.match{.some x -> x; .empty -> default};
  
  |   default -> this.or(default);
  ||  default -> this.match{.some x -> x; .empty -> default#};
  
  .flow       -> this.match{.empty -> Flows#; .some x -> Flows#(x)};

  .as[R] f    -> this.match{.some x->Opts#(f#x); .empty->{}};
  
  .ifSome  f  -> this.match{.some x -> f#x; .empty -> {}};
  .ifEmpty f  -> this.match{.some _ -> {}; .empty -> f#};
````

Methods `.isEmpty` and `.isSome` simply return a boolean stating if the optional was empty or not.

Method `!` is a convenience method that returns the optional content or produces and error.
Calling this method is equivalent to claim

> I, the programmer, know that in this case the optional will definitively have a value inside.
> If not, this is an observed bug.

Note how this is conceptually similar to the `.assertTrue` method we have seen before. Indeed, the internal call ``Error.msg `..` `` is pretty much what the body of `.assertEq` from `DataType` does.

The two methods `Opt[E].or` and `Opt[E]||` are similar to `Bool.or` and `Bool||`:

- `Opt[E].or` returns the value stored in the optional,
or the parameter value as a default result if the optional is empty.
- `Opt[E]||` returns the value stored in the optional,
or it executes the lazy parameter value to get a default result if the optional is empty.

Exactly as for `Bool`, method `|` is just an alias for `.or`.

Method `.flow` returns a `Flow[E]`. Flows are a very important data type in the fearless standard libraries and we will discuss them later.

The method `.as` is used to change the type of the optional, taking a function to map the content to a new type.

Finally, methods  `.ifSome` and `.ifEmpty` execute some `Void` returning computation in case the optional has a value or not.



The methods below are just to implement `DataType`.
We have a `.str` method, allowing to easily turn optionals into a string.
````
  .str  by  -> this.match{
    .empty -> `Opt[]`;
    .some x -> `Opt[`+(by#x)+`]`;
  };
````
The code above shows `.str` taking a parameter `by`.
Then we match on the `this` optional; on `.empty` we return a constant string, but on `.some x` we need to turn `x` into a string and then join some other strings before and after to forge the expected output.
Crucially, `x` is of type `E`; thus we do not know anything about `x` and we can not call any method on it to turn it into a string.
This is where `by` comes into place. `by` takes the `x` and turn it, not into a `Str` but in a `ToStr`.

String concatenation operations like `+` take a `ToStr` and call `.str` internally.
`DataType` implements `ToStr`, so we can `.str` numbers, booleans, etc.

For example
`myOptNat.str{::}` will convert the optional `Nat` into a `Str`.
Similarly, 
`myOptPerson.str{::}` will convert the optional person into a string,
assuming the person implements `ToStr`.
Indeed, resolving sugar and inference we get the following code:
```
myOptPerson.str(ToStrBy[Person]{#(p: read Person): read ToStr -> p})
```
As you can see, the `{::}` sugar is very useful in those cases.
But, what if we have a `data: Opt[Opt[Person]]`?
No problem, we can just do `data.str{::str{::}}` and get our string.
This pattern of using nested `{::}` is quite common as we will see more and more.
Here are the three types used by this mechanism:
```
ToStr:{ read .str: Str }
ToStr[E:*]:{ read .str(ToStrBy[imm E]): Str }
ToStrBy[T]:{ #(read T): read ToStr }
```
This structure is really important and we have many occurrences of this same pattern in Fearless:
- A basic type offering a functionality (`ToStr`)
- A generic version of that type, with a generic parameter (`ToStr[E]`)
- A **by** type able to turn any `read T` into the basic type (`read ToStr`)

Here below you can see the other `Opt[E]` methods. Note how `.info`, `.imm` and `.hash` are using the same exact construction of `.str`, with `.cmp` also taking a `by` argument for the same reason and using it in the same way.
````
  .info by  -> this.match{ .some x -> Infos.list(by#x); .empty -> {} };
  .imm by     -> this.match{.some x -> Opts#(by#x.imm); .empty->{} };
  .hash by -> this.match{
    .empty  -> 0;
    .some e -> 31.aluAddWrap(by#e.hash);
    };
  .cmp by, a, b, m -> a.match{
    .empty -> b.match{ .empty -> m.eq; .some _ -> m.lt; };
    .some ea -> b.match{
      .empty   -> m.gt;
      .some eb -> by#ea<=>(by#eb, m);
      }
    };
  .close->this; .close->::;
  }
````

Finally, here we can see in the type `_Opt[E:*]` all the gory type signatures. You might find them quite surprising.

````
_Opt[E:*]:DataType[Opt[E],Opt[imm E],E,imm E],_AssertEmpty{
  mut  .match[R:**](mut OptMatch[E, R]): R;
  read .match[R:**](mut OptMatch[read/imm E, R]): R;
  imm  .match[R:**](mut OptMatch[imm E, R]): R;

  mut  .or(E): E;
  read .or(read/imm E): read/imm E;

  mut  |(E): E;
  read |(read/imm E): read/imm E;

  mut  ||(mut MF[E]): E;
  read ||(mut MF[read/imm E]): read/imm E;
  imm  ||(mut MF[imm E]): imm E;

  mut  !: E;
  read !: read/imm E;

  mut  .flow: mut Flow[E];
  read .flow: mut Flow[read/imm E];
  imm  .flow: mut Flow[imm E];

  mut  .ifSome(mut MF[E, Void]): Void;
  read .ifSome(mut MF[read/imm E, Void]): Void;
  imm  .ifSome(mut MF[imm E, Void]): Void;

  read .ifEmpty(mut MF[Void]): Void;

  read .isEmpty: Bool;
  read .isSome: Bool;
  read .as[R:imm](mut MF[read/imm E, R]): Opt[R];  
}
````

As you can see, this is where the major difference lies with respect to the optional seen in Chapter 2: Here most methods come in two or three variants, one for `E`, one for `read/imm E` and one for `imm E`.
The core idea is that when we implement `.match` in `Opt[E]` we are using the same implementation to satisfy all 3 type signatures. 


`Opt[E]` is an example of a generic container type: a type whose main goal is to contain any kind of `E`, where `E` can be `read,imm,mut`.
As you can see, designing generic container types supporting a range of reference capabilities is not a beginner friendly task. However, this pattern is quite consistent, and many generic containers follow this same structure, with many methods offering exactly those type variants.


### The Reality of Production Code

You may have noticed a shift in tone. The code for `_Opt[E]` looks significantly more intimidating than the conceptual `Opt[T]` we wrote in Chapter 2.

We are crossing the bridge from **conceptual logic** to **production engineering**.
The logic remains identical: an Optional is still just "something or nothing." However, a production-grade library seamlessly handle `mut`,`imm` and `read` data.

Up to now we pushed to make sure to explain every single details when first used. We will eventually provide all the details and teach you the ins and outs of every corner; but there is no more a clear linear path to follow.
Here we are showing you the real implementation of those very useful types, and by their nature of being used in all contexts of the language, they are interconnected with every aspect of the language.

We could have hidden this complexity from you. Alternatively, we could have kept showing you more and more layers of simplified toy versions of the standard library.

**Do not panic if the type signatures above look dense.**
You do not need to master writing generic containers with reference capabilities right now.
For now, take away two key realisations:
1.  **It is reachable:** The standard library is just code. It uses the same methods and matches you already understand, just decorated with more precise type constraints.
2.  **It is robust:** The verbosity exists to guarantee that when you use `Opt` in your own simple programs, it behaves exactly as you expect, regardless of whether your data is mutable or immutable.


OMIT_START
-------------------------*/@Test void allCodeTogether(){ run("""
use base.DataType as DataType;
use base.Sealed as Sealed;
use base.Void as Void;
use base.Info as Info;
use base.Infos as Infos;
use base.Error as Error;
use base.Flow as Flow;
use base.Flows as Flows;
//use base._AssertEmpty as _AssertEmpty;

F[R:**]: {read #: R}
F[A:**,R:**]: {read #(a: A): R}
F[A:**,B:**,R:**]: {read #(a: A, b: B): R} //and a few more overloads

MF[R:**]: {mut #: R}
MF[A:**,R:**]: {mut #(a: A): R}
MF[A:**,B:**,R:**]: {mut #(a: A, b: B): R} //and a few more overloads
ThenElse[R:**]: { mut .then: R; mut .else: R; }
BoolMatch[R:**]:{ mut .true: R; mut .false: R; }
Bool:Sealed,DataType[Bool,Bool]{
  .and(b: Bool): Bool;
  &(b: Bool): Bool -> this.and(b);
  &&(b: mut MF[Bool]): Bool;

  .or(b: Bool): Bool;
  |(b: Bool): Bool -> this.or(b);
  ||(b: mut MF[Bool]): Bool;

  .if[R:**](f: mut ThenElse[R]): R;
  ?[R:**](f: mut ThenElse[R]): R -> this.if(f);
  .match[R:**](m: mut BoolMatch[R]): R -> this?{.then->m.true; .else->m.false};

  .not: Bool;
  ==> (b: mut MF[Bool]): Bool -> this.not || b;
  .str -> this.imm?{ .then->`True`; .else->`False` };//this as Str, from DataType

  .assertTrue: Void -> this.assertEq True;
  .assertFalse: Void -> this.assertEq False;

  .info -> Infos.msg(this.str); //other methods from DataType here and below
  .close -> this; .close -> ::; //methods from DataType
  .hash -> this.imm?{ .then->1; .else->0 };
  .cmp a, b, m -> a.imm?{
    .then->b.imm?{ .then->m.eq; .else->m.gt };
    .else->b.imm?{ .then->m.lt; .else->m.eq };
    };
}
True:Bool{
  .and(b) -> b;
  &&(b) -> b#;
  .or(b) -> this;
  ||(b) -> this;
  .not -> False;
  .if(f) -> f.then;
  .imm -> True;
}
False:Bool{
  .and(b) -> this;
  &&(b) -> this;
  .or(b) -> b;
  ||(b) -> b#;
  .not -> True;
  .if(f) -> f.else;
  .imm -> False;
}
Opts:{
  #[E:*](x: E): mut Opt[E] -> { .match m -> m.some(x) };
}
OptMatch[E:*, R:**]:{
  mut .some(E): R;
  mut .empty: R;
}
Opt[E:*]: _Opt[E]{
  .match m    -> m.empty;
  .isEmpty    -> this.match{.some _ -> False; .empty -> True};
  .isSome     -> this.match{.some _ -> True; .empty -> False};  
  !           -> this.match{.some x -> x; .empty -> Error.msg `Opt was empty`};  

  .or default -> this.match{.some x -> x; .empty -> default};
  
  |   default -> this.or(default);
  ||  default -> this.match{.some x -> x; .empty -> default#};
  
  .flow       -> this.match{.empty -> Flows#; .some x -> Flows#(x)};

  .as[R] f    -> this.match{.some x->Opts#(f#x); .empty->{}};
  
  .ifSome  f  -> this.match{.some x -> f#x; .empty -> {}};
  .ifEmpty f  -> this.match{.some _ -> {}; .empty -> f#};
  .str  by  -> this.match{
    .empty -> `Opt[]`;
    .some x -> `Opt[`+(by#x)+`]`;
  };
  .info by  -> this.match{ .some x -> Infos.list(by#x); .empty -> {} };
  .imm by     -> this.match{.some x -> Opts#(by#x.imm); .empty->{} };
  .hash by -> this.match{
    .empty  -> 0;
    .some e -> 31.aluAddWrap(by#e.hash);
    };
  .cmp by, a, b, m -> a.match{
    .empty -> b.match{ .empty -> m.eq; .some _ -> m.lt; };
    .some ea -> b.match{
      .empty   -> m.gt;
      .some eb -> by#ea<=>(by#eb, m);
      }
    };
  .close->this; .close->::;
  }
_Opt[E:*]:DataType[Opt[E],Opt[imm E],E,imm E]{
  mut  .match[R:**](mut OptMatch[E, R]): R;
  read .match[R:**](mut OptMatch[read/imm E, R]): R;
  imm  .match[R:**](mut OptMatch[imm E, R]): R;

  mut  .or(E): E;
  read .or(read/imm E): read/imm E;

  mut  |(E): E;
  read |(read/imm E): read/imm E;

  mut  ||(mut MF[E]): E;
  read ||(mut MF[read/imm E]): read/imm E;
  imm  ||(mut MF[imm E]): imm E;

  mut  !: E;
  read !: read/imm E;

  mut  .flow: mut Flow[E];
  read .flow: mut Flow[read/imm E];
  imm  .flow: mut Flow[imm E];

  mut  .ifSome(mut MF[E, Void]): Void;
  read .ifSome(mut MF[read/imm E, Void]): Void;
  imm  .ifSome(mut MF[imm E, Void]): Void;

  read .ifEmpty(mut MF[Void]): Void;

  read .isEmpty: Bool;
  read .isSome: Bool;
  read .as[R:imm](mut MF[read/imm E, R]): Opt[R];  
}
"""); }/*--------------------------------------------


We are going to explain those in the details later, but we shall summarise them here.
Do not worry, we are going to discuss all those types in details later!

````
ToStr:{ read .str: Str }
ToStr[E:*]:{ read .str(ToStrBy[imm E]): Str }
ToStrBy[T]:{ #(read T): read ToStr }

ToInfo:{ read .info: Info }
ToInfoBy[E]:{ #(read E):read ToInfo }
ToInfo[E:*]:{ read .info(ToInfoBy[imm E]): Info }
Info:{ /*explained later; exposes information for communicating across programs*|/ }

ToImm[T0]:{ read .imm: T0 }
ToImmBy[E,E0]:{#(t: read E):read ToImm[E0]}
ToImm[E:*,E0,T0]:{ read .imm(by: ToImmBy[imm E,E0]): T0 }

Order[T]:{ /*explained later; provides methods ==, !=, <=, >= etc*|/ }
Order[T,E:*]:{ /*explained later; like order but for generics *|/ }
OrderHash[T]:Order[T],ToStr{ /*explained later; Order plus mappings*|/ }
OrderHash[T,E:*]:Order[T,E],ToStr[E]{ /*explained later; like OrderHash but for generics *|/ }

DataType[T,T0]:ToInfo,ToImm[T0],WidenTo[T],OrderHash[T],Extensible[T]{ read .close(t: read T): read DataType[T,T0]; }
DataType[T,T0,E:*,E0]:ToInfo[E],ToImm[E,E0,T0],WidenTo[T],OrderHash[T,E],Extensible[T]{ read .close(t: read T): read DataType[T,T0,E,E0]; /*...*|/ }
DataTypeBy[E,K,K0]:ToInfoBy[E],ToImmBy[E,K0],OrderHashBy[E,K]{ #(e: read E): read DataType[K,K0]; /*...*|/ }
````

Method `ToStr.str` represents an object as a string.
Method `ToInfo.info` represents an object in a structured data format (similar to JSON) useful for communication across programs.
Type `OrderHash[T]` provides hashing and comparisons methods to a type `T` extending it. Objects extending `OrderHash[T]` can easily be organised in efficient data structures.
Method `ToImm[T].imm` converts an object of any reference capabilities into an immutable version of the same object. For objects that can only ever be immutable, this method simply returns the object itself.

Note how many of those types have a generic variant, like `ToStr` and `ToStr[T]`. As we will see later, this is because for generic containers we need a way to convert the contained objects to be able to convert the container itself.



OMIT_END
END*/
}