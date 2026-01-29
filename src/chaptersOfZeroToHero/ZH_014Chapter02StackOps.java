package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_014Chapter02StackOps {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Operations on Sequences

###  General operations on sequences

Above, we have shown how to make an operation over the stack using match
```
.sum(ns: Stack[Nat]): Nat -> ns.match{
  .empty -> 0,
  .elem(top, tail) -> top + ( this.sum(tail) ),
  }
```
What if we want to multiply all the numbers together instead of summing them?
```
.times(ns: Stack[Nat]): Nat -> ns.match{
  .empty -> 1,
  .elem(top, tail) -> top * ( this.times(tail) ),
  }
```
As you can see, there could be many different operations done following the same pattern.
Repeating this pattern over and over would make the code quite long.
We can abstract over this pattern with the idea of folding values on top of each other starting from a beginning point:

-------------------------*/@Test void fold1 () { run("""
StackMatch[T,R]: { .empty: R, .elem(top:T, tail: Stack[T]): R, }
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty,
  .fold[R](start: R, f: F[T,R,R]): R -> start,
  +(e: T): Stack[T] -> { 
    .match(m) -> m.elem(e, this),
    .fold(start, f) -> f#(e, this.fold(start, f)),
    },
  }
ExampleSum: { #(ns: Stack[Nat]): Nat -> ns.fold(0, { n1,n2 -> n1 + n2 })  }
ExampleTimes: { #(ns: Stack[Nat]): Nat -> ns.fold(1, { n1,n2 -> n1 * n2 })  }//would work with .fold[Nat]
"""); }/*--------------------------------------------

Another icon operation over sequences is to map all the values to the result of an operation, for example adding 5 to all the numbers.
Of course this can be implemented with the match as shown below:
```
Example:{
  .add5(ns: Stack[Nat]): Stack[Nat] -> ns.match{
    .empty -> {},
    .elem(top, tail) -> Example.add5(tail) + (top + 5),
    }
}
```
But again, operations like this one are going to be very common, so we better define a generic support for it in the Stack type:
-------------------------*/@Test void map1 () { run("""
StackMatch[T,R]: { .empty: R, .elem(top:T, tail: Stack[T]): R, }
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty,
  .fold[R](start:R, f: F[R,T,R]): R -> start,
  .map[R](f: F[T, R]): Stack[R] -> {},
  +(e: T): Stack[T] -> { 
    .match(m) -> m.elem(e, this),
    .fold(start, f) -> f#(this.fold(start, f), e),
    .map(f) -> this.map(f) + ( f#(e) ),
    },
  }
ExampleSum5: {   #(ns: Stack[Nat]): Stack[Nat] -> ns.map { n -> n + 5 }  }
ExampleTimes2: { #(ns: Stack[Nat]): Stack[Nat] -> ns.map { n -> n * 2 }  }
"""); }/*--------------------------------------------

By adding fold and map to stacks, we have now unlocked a surprising amount of expressive power.
Do you want to add 10 to all the numbers, multiply the result for 3 and then get the sum of all of them?
Easy!
-------------------------*/@Test void fluent1 () { run("""
//OMIT_START
StackMatch[T,R]: { .empty: R, .elem(top:T, tail: Stack[T]): R, }
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty,
  .fold[R](start:R, f: F[R,T,R]): R -> start,
  .map[R](f: F[T, R]): Stack[R] -> {},
  +(e: T): Stack[T] -> { 
    .match(m) -> m.elem(e, this),
    .fold(start, f) -> f#(this.fold(start, f), e),
    .map(f) -> this.map(f) + ( f#(e) ),
    },
  }
//OMIT_END
ExampleFluent: { #(ns: Stack[Nat]): Nat -> ns
  .map { n -> n + 10 }
  .map { n -> n *  3 }
  .fold(0, { n1,n2 -> n1 + n2 })//would work with .fold[Nat]
  }
"""); }/*--------------------------------------------

This is the first example we see of fluent programming.
Fluent programming is a big deal in practical Fearless code.
The general idea is that some data types (Stack in this case) can have many useful/flexible methods, returning instances of types with many useful/flexible methods.
Usually, those methods are so useful and flexible because they take as a parameter instructions telling them what to do.

That is, a large amount of fearless code looks like the following
```
UsefulBox#(keep,all,my,data,in,the,box)
  .transformData{dataElement -> use.the(dataElement).andGetNewElement }
  .transformData{dataElement -> .. }
  .removeData{dataElement -> use.the(dataElement).toGetBoolean }
  .addData{dataElement -> use.the(dataElement).toGetManyElements }
  .extractResult{ .. }
```
What is this `UsefulBox`? What does `.transformData` do?
**Anything you want**, the above is just example code.
The idea is that in the Fearless standard library there are many different types that work like `UsefulBox` and programmers often define their own types working in a similar way.
While the idea of defining your own types supporting useful/flexible methods may feel overwhelming, remember that we just did it for `Stack`. 

#### Exercise: filter
Now as an exercise, we try to define a method `.filter` that removes elements from the stack

But again, operations like this one are going to be very common, so we better define a generic support for it in the Stack type:
```
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty,
  .fold[R](start:R, f: F[R,T,R]): R -> start,
  .map[R](f: F[T, R]): Stack[R] -> {}
  .filter ???, //Base case
  +(e: T): Stack[T] -> { 
    .match(m) -> m.elem(e, this),
    .fold(start, f) -> f#(this.fold(start, f), e),
    .map(f) -> this.map(f) + ( f#(e) ),
    .filter ???, //Inductive case
    },
  }
```
So, what do we write instead of ??? in the code above? 
```
  .filter ???, //Base case
  ...
    .filter ???, //Inductive case
```
Well, first we need to figure out the type. Method `Stack[T].filter` does not transform the elements, it just selects which one to keep. Thus the return type is going to be the same: `Stack[T]`.
As an argument, we need to take a function that tells `.filter` if the element should be kept or removed.
We can use a function returning a `Bool`: `True` will mean "keep the element" and `False` will mean "discard the element".
So, the first filter is going to be
```
  .filter(f: F[T,Bool]): Stack[T]-> {},
```
We return the empty stack because there are no elements to remove from the empty stack... it is already as empty as it can be.
What about the second `.filter`? there we have `this` and `e` in scope.
```
  .filter(f) -> f#(e).if{
    .then -> this.filter(f) + e,
    .else -> this.filter(f),
    },
```
Here we can use an `.if` on the result of `f#(e)`.
In the `.then` case, we propagate the operation on the stack tail and we sum the current element.
In the `.else` case, we just propagate the operation on the stack tail.

Here is the full code again.

-------------------------*/@Test void stackFull () { run("""
StackMatch[T,R]: {
  .empty: R,
  .elem(top:T, tail: Stack[T]): R,
  }
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty,
  .fold[R](start:R, f: F[R,T,R]): R -> start,
  .map[R](f: F[T, R]): Stack[R] -> {},
  .filter(f: F[T,Bool]): Stack[T]-> {},
  +(e: T): Stack[T] -> { 
    .match(m) -> m.elem(e, this),
    .fold(start, f) -> f#(this.fold(start, f), e),
    .map(f) -> this.map(f) + ( f#(e) ),
    .filter(f) -> f#(e).if{
      .then -> this.filter(f) + e,
      .else -> this.filter(f),
      },
    },
  }
"""); }/*--------------------------------------------

You should pause here, absorb every detail, and even commit this little snippet to memory.

This compact piece of code is not just another programming example. Within it lies the distilled essence of the core ideas we've been exploring.

What you're looking at is a mental blueprint. One that can become a key part of your intuitive programming toolkit. Once internalized, it fundamentally reshapes your thinking about problems. You'll begin seeing opportunities everywhere to elegantly apply these patterns, and clearly express yourself via code.

This snippet is fundational for a powerful mental model, that will guide you toward cleaner solutions, clearer abstractions, and more maintainable code.

END*/
}
