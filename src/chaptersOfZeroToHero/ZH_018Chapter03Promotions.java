package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_018Chapter03Promotions {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Promotions

### Introduction to Reference Capabilities, ROG and MROG.

In Fearless, objects can form complex networks of dependencies by referring to each other. The **Reachable Object Graph** (ROG) of a given object is the graph of all objects reachable from it, including itself. An object is mutated whenever its ROG changes. That is,
mutation is grounded in the definition of ROG. This definition of mutation captures that objects can represent their state deep inside other objects in their ROG. There are three ways objects are mutated: 

1. An object can be born with a specific ROG that will never mutate.
2. An object can be mutated across all their life time.
3. An object can be mutated for an initial phase of its life, and then never again.

Clearly this last way subsumes the other two, simply by varying the length of the initial phase. We call an object inside this initial phase mutable, and one outside immutable.
By definition an immutable object will never be mutated and will never become mutable again.

The **Mutable Reachable Object Graph** (MROG) of a given object is the graph of all mutable objects reachable from it, including itself. Thus, the MROG of an immutable object is empty. Since we define mutability as a change in the whole ROG, the kind of immutability we discuss here is deep: if an object is immutable, the whole ROG of that object is immutable.

**Reference Capabilities** (RC) are a type systems technique to track mutable and immutable objects.
As for most type systems, RCs are a conservative approximation, where some objects that are already in the immutable state are still seen as potentially mutable. That is, there can be a large time gap from the moment the last mutation happens and the moment the object is recognised as immutable. It may also happen that an immutable object is never recognised as such by the type system. RC are purely an additional type system layer attempting to recognise immutable objects. They do not impact the semantics, they simply restrict the set of allowed programs.

Reference capabilities do not directly track mutable and immutable objects, but track the parameters/references to such objects.
We will call a parameter with an `imm` type an `imm` parameter. Same for the other reference capabilities.
An `imm` parameter refers to an immutable object. A `mut` parameter refer to a mutable object.
A `read` parameter may refer to either a mutable or an immutable object.
That is, `read` parameters are useful to write code able to work on all kinds of objects.
In addition to `imm`, `mut` and `read`, there are more kinds of reference capabilities, but we will see them later.


### Promotions

As we discussed before, a mutable object can become immutable, and the type system can recognise it.
This happens through a process called promotion.
There are many kinds of promotion; we will now see the simplest and most useful form of promotion:
The result of any method returning a mut but taking in input no `mut` or `read` parameters can be promoted to `imm`.

One easy detail to miss the first time around: "taking in input" includes the receiver, `this`, exactly as if it were one more parameter.
A method with no explicit parameters at all can still fail this rule, if its own receiver is `mut` or `read`.

For example
-------------------------*/@Test void promotion () { run("""
//OMIT_START
use base.Void as Void;
use base.Nat as Nat;
use base.Block as Block;
Points:{ #(x: Nat, y: Nat): Point -> Point:{.x: Nat -> x; .y: Nat -> y} }

Animals: {
  #(start: Point): mut Animal -> Block#
   .var[Point] loc= {start}
   .return{ mut Animal: {
      read .location: Point -> loc.get;
      mut .run(x: Nat): Void ->
        loc.set(Points#(loc.get.x + x, loc.get.y));
    }}}
//OMIT_END
//Animals: { #(start: Point): mut Animal -> ... }
PromotionExample: {
  #: imm Animal -> Animals#(Points#(10,20))
  }
"""); }/*--------------------------------------------

The code above compiles and produces an immutable Animal.
The method `Animals#` is declared to return a `mut Animal`.
However, this call can be promoted to `imm` because the method `Animals#` is called using an immutable point, and (implicitly) `Animals#` itself is called on an immutable receiver: `Animals` has no fields, so an `Animals` literal is trivially immutable, and the unannotated `#` defaults to an `imm` receiver.

Code `PromotionExample#.run(10)` would not compile because method `PromotionExample#` return an immutable `Animal` and
`Animal.run` is a `mut` method.

### The `iso` capability: freshly built, not yet committed

How does the type system let the very same call to `Animals#` be treated as `mut` in one place and `imm` in another, without us writing two different methods?
The answer is a fourth reference capability, `iso` ("isolated"), that we have not mentioned yet.
A value with type `iso` is freshly built and not yet reachable from anywhere else: nothing, anywhere in the program, holds another reference to it.
Because nothing else can see it, it does not yet matter whether we call it mutable or immutable: both are still on the table, and the type system defers the decision to whoever actually uses the value.
`Animals#`'s real, most precise return type is `iso Animal`, not `mut Animal`: writing `mut Animal` (as we did above) is a convenient rounding down, always allowed, because `iso` is more precise than (a subtype of) `mut`.

The choice between `mut` and `imm` is then made at the *usage* site, not at the *construction* site:
-------------------------*/@Test void isoBothWays () { run("""
//OMIT_START
use base.Void as Void;
use base.Nat as Nat;
use base.Block as Block;
Points:{ #(x: Nat, y: Nat): Point -> Point:{.x: Nat -> x; .y: Nat -> y} }
//OMIT_END
Animals: {
  #(start: Point): iso Animal -> Block#
   .var[Point] loc= {start}
   .return{ mut Animal: {
      read .location: Point -> loc.get;
      mut .run(x: Nat): Void ->
        loc.set(Points#(loc.get.x + x, loc.get.y));
    }}}

UseAsMutable: {
  #: Void -> Block#
    .let[mut Animal] pet= { Animals#(Points#(10,20)) }
    .return { pet.run(5) }
  }
UseAsImmutable: {
  #: imm Animal -> Animals#(Points#(10,20))
  }
"""); }/*--------------------------------------------

The exact same call, `Animals#(Points#(10,20))`, is used twice: once bound to a `mut Animal` and immediately `.run`, once returned directly as `imm Animal`.
Neither usage forces the other: `Animals#` does not commit to either capability, `iso` does.
This is why `iso` is the return type most factory-style methods should really have, and why `mut` in a return type is often really "at least `iso`, rounded down to `mut` because that is the more familiar word".

### When promotion breaks: capturing something not-yet-immutable

`iso` is fragile. The moment the value being built captures *anything* that is not itself already proven `imm`, uniqueness is gone: that captured thing might be reachable, and mutable, from somewhere else too, so the value being built can no longer be trusted to be free-standing.
At that point the type system has no choice but to commit early, to plain `mut`; and plain `mut` cannot later be promoted to `imm` no matter what.

This is easy to run into without noticing, exactly because "capturing `this`" so often does not look like capturing anything at all:
-------------------------*/@Test void brokenPromotion () { run("""
use base.Void as Void;
use base.Nat as Nat;
use base.List as List;
use base.Lists as Lists;
//OMIT_START
Points:{ #(x: Nat, y: Nat): Point -> Point:{.x: Nat -> x; .y: Nat -> y} }
//OMIT_END
Zoo: {
  read .home: Point;
  read .residents: List[Point] -> Lists#(Points#(1,1)).flow.map{p -> this.home}.list;
  }
//OMIT_START
VisitZoo: {
  #: List[Point] -> mut Zoo{ .home -> Points#(10,20); }.residents
  }
//ERROR|In file: [###]

//ERROR|010|   read .residents: List[Point] -> Lists#(Points#(1,1)).flow.map{p -> this.home}.list;
//ERROR|   |   --------------------------------^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
//ERROR|
//ERROR|While inspecting method call ".list" > ".residents" line 10
//ERROR|The body of method ".residents" of type declaration "Zoo" is an expression returning "mut List[Point]".
//ERROR|Method call "mut base.Flow[_].list" has type "mut List[Point]" instead of a subtype of "List[Point]".
//ERROR|
//ERROR|See inferred typing context below for how type "List[Point]" was introduced: (compression indicated by `-`)
//ERROR|[###]
//ERROR|Error 8 TypeError
//OMIT_END
"""); }/*--------------------------------------------

`.residents` has a `read` receiver, so inside it `this` is only `read`, not `imm`.
The lambda `{p -> this.home}` passed to `.map` captures that `read this`, so the `Flow`/`List` being built through `.map`/`.list` is no longer capture-free: it can no longer be trusted as fresh and unique, so it cannot stay `iso`, and it is fixed as plain `mut` instead.
`.residents` is declared to return a plain (immutable) `List[Point]`, so this is a compile error: a `mut List[Point]` can not be silently treated as one.

A few ways out, and picking between them is a real design decision, not a formality:
- **Keep the receiver `imm`.** `this` only poisons the capture because it is `read` here; a method left unannotated (the default, `imm` receiver) can capture `this` freely, since `this` is then already immutable. Whenever a type has no real need to be `read`/`mut`-receiver, leaving it `imm` keeps every computation inside it eligible for promotion, `this` included.
- **Do not capture it at all.** If `.residents` did not need `this` at all (for example, if the point to repeat came in as a parameter instead of through `this.home`), the whole computation would again be capture-free, and would promote to `imm` on its own, exactly like `Animals#` did.
- **Prove it by hand.** Sometimes neither of the above is an option, because the type genuinely needs a `read`/`mut` receiver and genuinely needs to capture `this`. We will later meet `List[E].as{::}`, which lets us assert "this is already immutable" for a list we can vouch for. The same idea exists for arbitrary types: implementing `ToImm[T]`'s `read .imm: T` method lets a type assert "an object built this way, right here, is always safe to treat as `imm`" for a value the type system itself could not have derived that fact for.

We will meet this exact situation again in the tank game later in this chapter: `NextState` is written so that its own receiver stays `imm`, precisely so that capturing `this` inside its `List`/`Flow` computation does not stop the result from promoting to the plain (immutable) `List` it declares.

END*/
}