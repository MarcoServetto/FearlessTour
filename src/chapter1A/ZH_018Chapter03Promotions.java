package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
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
An `imm` parameter will always refer to an immutable object. A `mut` parameter will always refer to a mutable object.
A `read` parameter may refer to either a mutable or an immutable object.
That is, `read` parameters are useful to write code able to work on all kinds of objects.
In addition to `imm`, `mut` and `read`, there are more kinds of reference capabilities, but we will see them later.


### Promotions

As we discussed before, a mutable object can become immutable, and the type system can recognize it.
This happens through a process called promotion.
There are many kinds of promotion; we will now see the simplest and most useful form of promotion:
The result of any method returning a mut but taking in input no `mut` or `read` parameters can be promoted to `imm`.
For example
-------------------------*/@Test void promotion () { run("""
//OMIT_START
Points:{ #(x: Nat, y: Nat): Point -> Point:{.x: Nat -> x, .y: Nat -> y} }

Animals: {
  #(start: Point): mut Animal -> Block#
   .var loc= {start}
   .return{ mut Animal: {
      read .location: Point -> loc.get,  
      mut .run(x:Nat): Void ->
        loc.set(Points#(loc.get.x + x, loc.get.y)),
    }}}
//OMIT_END
PromotionExample: {
  #: imm Animal -> Animals#(Points#(10,20))
  }
"""); }/*--------------------------------------------

The code above compiles and produces an immutable Animal.
The method `Animals#` is declared to return a `mut Animal`.
However, this call can be promoted to `imm` because the method `Animals#` is called using an immutable point.
Code `PromotionExample#.run(10)` would not compile because method `PromotionExample#` return an immutable `Animal` and
`Animal.run` is a `mut` method.

END*/
}