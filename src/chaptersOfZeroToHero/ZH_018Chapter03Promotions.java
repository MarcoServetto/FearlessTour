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

### How promotion is actually decided

The rule talks about the parameters' *declared* types, but what actually triggers promotion, for each one, is a comparison against the *actual* type of the thing you passed there.
`Animals#(start: Point)` merely declares `start` as `Point` (that is, `imm Point`); it does not, by itself, make the call promotable.
What makes it promotable is that the caller happened to pass an argument whose real, actual type is *also* `imm` (here, `Points#(10,20)`, a freshly built, uncapturing `Point`): the actual argument is at least as immutable as the parameter merely required.
The same reasoning applies to the receiver: an unannotated `#` merely requires (at least) `imm` for `this`; since `Animals` has no fields, any `Animals` literal used as a receiver is actually `imm`, so that requirement, too, is met with room to spare.
When every parameter (receiver included) is met this way, the whole call is allowed to round its declared `mut` return down to whatever the actual arguments can support, `imm` included.

### The `iso` capability: not committed to `mut` or `imm`

There is a fourth reference capability, `iso` ("isolated"), for exactly this situation: a value that is a subtype of `mut`, of `read`, and of `imm`, all at once, so that ordinary subtyping (no promotion bookkeeping needed) lets it satisfy whichever of them a given usage site actually asks for.
`Animals#`'s truly precise return type is `iso Animal`, not `mut Animal`: writing `mut Animal` (as we did above) is a convenient, always-allowed rounding down, since `iso` is a subtype of `mut` too.
Declaring a factory method's result as `iso`, when possible, is usually better style than declaring it `mut` and relying on promotion: it says directly "this is not committed to any single capability", rather than "this is `mut`, except when it secretly is not".

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

Note how the first usage pins its local down explicitly, `.let[mut Animal] pet= {...}`, rather than just `.let pet= {...}`.
This is not a stylistic choice: without it, there is no declared type for the type checker to compare the lambda's result against at all, so it does not attempt promotion, and `pet` simply gets the exact type the lambda's body computed to, whatever that happens to be.
We will rely on this same `.let[T]` pinning again later in this chapter, to let a multi-step computation promote to an immutable result.

### When promotion breaks: an argument that is only as good as declared, not better

Promotion needs *every* parameter along the way, receiver included, to actually be given something more immutable than that parameter merely requires.
The moment even one of them is given something that is only exactly as permissive as declared, with no room to spare, that call's result is fixed as plain `mut`; and plain `mut` cannot later be promoted to `imm`, no matter what surrounds it.

This is easy to run into without noticing, exactly because "the receiver is only as immutable as declared" so often does not look like a parameter at all, since the receiver is just `this`:
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

`.residents` has a `read` receiver, so inside it `this` is only exactly `read`: there is no room to spare on that parameter, since `Zoo` really might be `mut` somewhere else in the program.
The lambda `{p -> this.home}`, passed where `.map` merely requires (at least) a `read` function, is therefore also only exactly as good as required, with nothing better to offer: unlike `Animals#(Points#(10,20))` earlier, there is no surplus here for promotion to work with.
`.list`'s declared return is `mut List[Point]`, and with no surplus anywhere along the way, that is exactly what we get; `.residents` is declared to return a plain (immutable) `List[Point]`, so this is a compile error: a `mut List[Point]` can not be silently treated as one.

A few ways out, and picking between them is a real design decision, not a formality:
- **Keep the receiver `imm`.** `this` is only exactly `read` because `.residents` is declared `read` here; a method left unannotated (the default, `imm` receiver) gives `this` a genuine surplus over any `read` requirement, since it is then actually immutable. Whenever a type has no real need for a `read`/`mut`-receiver method, leaving it unannotated keeps every computation inside it eligible for promotion, `this` included.
- **Do not use `this` for it at all.** If `.residents` did not need `this` at all (for example, if the point to repeat came in as a parameter instead of through `this.home`), that parameter could again carry its own surplus, and the whole call would promote to `imm` on its own, exactly like `Animals#` did.
- **Prove it by hand.** Sometimes neither of the above is an option, because the type genuinely needs a `read`/`mut` receiver and genuinely needs to capture `this`. We will later meet `List[E].as{::}`, which lets us assert "this is already immutable" for a list we can vouch for. The same idea exists for arbitrary types: implementing `ToImm[T]`'s `read .imm: T` method lets a type assert "an object built this way, right here, is always safe to treat as `imm`" for a value the type system itself could not have derived that fact for.

We will meet this exact situation again in the tank game later in this chapter: `NextState` is written so that its own receiver stays `imm`, precisely so that capturing `this` inside its `List`/`Flow` computation does not stop the result from promoting to the plain (immutable) `List` it declares.

END*/
}