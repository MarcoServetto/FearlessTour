package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_017Chapter03ImplementingFromScratch {
/*START
--CHAPTER-- Chapter 3
--SECTION-- ImplementingFromScratch

# Chapter 3

### Implementing all from scratch versus just using standard library code

Up to now we tried to show you nearly all the implementation of all the concepts we introduced.
We think this a very good way to learn to code, and hopefully you have learned a lot.

However, this was possible since all of those concepts could be expressed with just a few lines of code.
We will keep showing you the full code of concepts that can be expressed concisely, but as we discussed before, many useful programs and libraries are composed of millions of lines.
For those, we will only show you how to use them.

Moreover, certain operations simply can not be done in the language directly.
All that we discussed lives in the world of the language itself. If we want our code to have any impact at all on the world outside of the program itself, we need something more. 

For example, no matter how many generics, types and methods we write from scratch, we will not be able to draw an image on the screen, or to save a file, or to read information from the internet.
Those are examples of external side effects.

Similarly, there are a few cases where we want to modify the behaviour of our code itself. This is possible using internal side effects.

We call **Magic methods** the methods from the standard library giving direct access to operations that would not be possible in plain Fearless.

### Var: the first bit of magic

The following code introduces updatable local variables, and uses a few new features.
-------------------------*/@Test void var1 () { run("""
//|OMIT_START
package test
alias base.Magic as Magic
//OMIT_END
Void:{}
Var[E: imm,mut,read]:{
  mut .set(v: E): Void -> Magic!,
  mut .get: E,
  read .get: read/imm E,
  }
Vars: {
  #[E: imm,mut,read](var: E): mut Var[E] -> { 
    .get -> var,
    }
  }
"""); }/*--------------------------------------------

The description below explains how the code above works, but will only mention `mut`, `imm`, `read` and `read/imm` without explaining how they work in the details yet.
First we define a type `Void:{}`. Nothing special here, just a type that does nothing. `Void` is often used to represent an operation that has no meaningful result, and simply performs side effects, and to do so it will have to use magic methods internally.
Then we define `Var[E]` as a generic type.
The type `E` has a constraint: it can only be `imm`,`mut` or `read`.
Those are keywords called reference capabilities: they describe how values can interact with magic.
An instance of `Var` will store an object of type `E`. The crucial bit is that such a value can change over time.
Method `.set` takes a new value, and magically changes the current object to store that new value instead of the old one.
Note how this method starts with the `mut` keyword.
In Fearless types and methods can have a keyword in front to track how they interact with magic.
Crucially, the type system tracks those types and provides extensive guarantees on how the flow of magic interacts with the program at large.
Then we define two variants of the method `.get`, one for `mut` and one for `read` receivers. You can think of those two variants as two different methods that just so happen to have the same name.
They will both return the current value, but with different types:
- If we have a `mut` receiver we produce the value with type `E`.
- If we have a `read` receiver, we produce the value with a  weakened type `read/imm E`.
  
Finally `Vars` is a factory type making new `Var[E]` originally containing the value provided by the user.
The syntax `.get -> var,` implements both .get methods with the same exact body.
In this case, both methods will simply return the value of `var` provided while calling `Vars#`.
However, the magic `.set` will impact those methods: when `.set` is called, the implementations of `.get` will change to return the new result instead.

With `Var[E]` we can now encode mutable objects, like in the following `Animal` example:

-------------------------*/@Test void mdfsImplicit () { run("""
Points:{ #(x: Nat, y: Nat): Point -> Point:{.x: Nat -> x, .y: Nat -> y} }

Animals: {
  #(start: Point): mut Animal -> Block#
   .var loc= {start}
   .return{ mut Animal: {
      read .location: Point -> loc.get,  
      mut .run(x:Nat): Void ->
        loc.set(Points#(loc.get.x + x, loc.get.y)),
    }}}
"""); }/*--------------------------------------------

As you can see, the code above defines `Point` and `Points` using features we understand well, and then defines `Animal` and `Animals`.
Method `Animals#` uses `Block`.
`Block` is a type in the standard library supporting fluent programming and the `=` sugar pretty much like our `Let`, but with many more features and utilities.
In particular, `Block` offers a `.let` method working exactly like `Let.let` and a `.var` method that instead wraps the value  into a `Var[E]`.
That is, in this case the local parameter `loc` is of type `mut Var[Point]`.
In this way, it is easy to create either local parameters or local variables. Local variables are just local parameters of type `mut Var[..]`.

An `Animal` is created `mut`: it can change state by updating `loc` using `loc.set` inside the method `.run`.
In this simple example, when an `Animal` runs, it moves across the `x` coordinate.
Note how to access the value inside of `loc` we need to use `.get`.

While location is a `mut Var[Point]`, the `Point` itself is immutable.
As a sugar, any typename without a modifier in front is implicitly `imm`.
To clarify this, here is the code from above with all the imm keywords explicitly added:

-------------------------*/@Test void mdfsExplicit () { run("""
Points:{ imm #(x: imm Nat, y: imm Nat): imm Point ->
  imm Point:{.x: imm Nat -> x, .y: imm Nat -> y} }

Animals: {
  imm #(start: imm Point): mut Animal -> imm Block#
   .var loc= {start}
   .return{ mut Animal: {
      read .location: imm Point -> loc.get,  
      mut .run(x: imm Nat): imm Void ->
        loc.set(imm Points#(loc.get.x + x, loc.get.y)),
    }}}
"""); }/*--------------------------------------------

### Aliasing

Now that we have mutation, we can have aliasing.
Aliasing is both the best feature of mutation, and the very reason mutation needs to be kept under control.
Aliasing is like a nuclear power plant:
- very powerful 
- very dangerous if misused
- some people are very vocal against it
- what happens inside feels like (dark) magic
- working with it requires a great level of competence

Using aliasing in the appropriate way can make our code much more efficient, and in some conditions even easier to read and understand.
However, when aliasing and mutation are misused or run outside of our control, the code will behave in ways that most humans find to be very unpredictable.

Consider the code below, showing a simple example of Aliasing.
-------------------------*/@Test void aliasing1 () { run("""
AliasingExample: {#: Num -> Block#
  .let bunny= { Animals#(Points#(10,20)) }
  .let mammal= {bunny}
  .do { bunny.run(15) }
  .return { mammal.location.x }
  }

"""); }/*--------------------------------------------
Here we use a `Block` to declare two local paramters for animals.
One is a `bunny` and the other one is the same animal (aliased) but called `mammal`.
The crucial point here is that those two local parameter are the same `mut Animal`.
Mutating `bunny` will affect `mammal` and vice versa.
Then we use `.do` to run code returning `Void`.
Finally, we return the `x` coordinate of `mammal`.
Here, calling `AliasingExample#` will return `25`.
That is, only one `Animal` has been created by that code, and by making the two bindings refer to the same animal object, we can observe mutation happened over `bunny` by looking at the state of `mammal`.

Note how we use `.let` and not `.var`.
`bunny` and `mammal` are not variables. They are local parameters referring to objects that (indirectly) contain a variable inside.

END*/
}