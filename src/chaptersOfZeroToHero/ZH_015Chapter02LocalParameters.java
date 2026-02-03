package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_015Chapter02LocalParameters {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Locals

### Local parameters (Locals)

Up to now, all the parameters we have seen are method parameters; either the receiver (parameter zero) or one of the others.
This can encourage us to write repetitive and hard to read code.

For example, we show below a difficult to read method computing the distance between two points; using the square root function (sqrt) present on `Nat`:
-------------------------*/@Test void distance1 () { run("""
//OMIT_START
use base.Nat as Nat;
Point:{ .x: Nat; .y: Nat }
A:{
//OMIT_END
.distance(p1: Point, p2: Point): Nat->
  p1.x - (p2.x) * (p1.x - (p2.x)) + (p1.y - (p2.y) * (p1.y - (p2.y)))  .sqrt .nat  
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

Note that those are the needed parenthesis:
     `p1.x - p2.x` would be interpreted as `(p1.x - p2).x`
This method uses the Pythagorean theorem, but it is not ideal:
  - we duplicate code for `p1.x - (p2.x)` and `p1.y - (p2.y)`
  - all the code is in a single hard to read line.

What if we want to introduce more names?
We can define a function on the fly and call it, as shown below:
-------------------------*/@Test void distance2 () { run("""
//OMIT_START
use base.Nat as Nat;
use base.F as F;
Point:{ .x: Nat; .y: Nat }
A:{
//OMIT_END
.distance(p1: Point, p2: Point): Nat->
  F[Nat,Nat,Nat]{diffX, diffY ->
     (diffX * diffX) + (diffY * diffY).sqrt.nat }
  #( p1.x - (p2.x), p1.y - (p2.y) )
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

Theoretically, this new code achieves our goals, but most humans find this new version even harder to read.
We think this is mostly because 
1. The values for `diffX` and `diffY` are very far in the code from the declaration point of `diffX` and `diffX`.
2. This new version is just much longer: we have to add the types for `diffX`, `diffY`, and the return type.
3. This version is only working for two new parameters defined at the same time. What if we wanted to give a name to the result before `.sqrt`?

We first show how to solve those 3 issues in the core language, then we show a new form of syntactic sugar making this approach more readable.
We can define a standard `Let` type allowing to define local parameters by generalising the idea of the code above:
```
Let:{ #[T,R](x: T, f: F[T,R]): R -> f#x }
```
With this Let type we can define our .distance method as follows:

-------------------------*/@Test void distance3 () { run("""
//|OMIT_START
use base.Nat as Nat;
use base.Void as Void;
use base.F as F;
Point:{ .x: Nat; .y: Nat }
Let:{ #[T,R](x: T, f: F[T,R]): R -> f#x }
A:{
//OMIT_END
.distance(p1: Point, p2: Point): Nat->
  Let#(p1.x - (p2.x), {diffX ->
  Let#(p1.y - (p2.y), {diffY ->
  Let#((diffX * diffX) + (diffY * diffY), {res ->
  res.sqrt.nat
  })})})
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

now we can use any amount of lets to declare local bindings and use them in the rest of the code.
However, the code is now kind of noisy: every `Let` introduces a new layer of object literals, and thus we end up with a lot of closed parenthesis at the end.

Also, this code does not follow the 'fluent' pattern we discussed before.
In particular, while most lines start by telling you what that line is doing (`Let#`) the last one just computes an expression.
We will now refactor our Let to follow the fluent pattern:

-------------------------*/@Test void letFull () { run("""
//|OMIT_START
use base.Nat as Nat;
use base.Void as Void;
use base.F as F;
Point:{ .x: Nat; .y: Nat }
//OMIT_END
Continuation[T,C,R]: { #(x: T, self: C): R }
Let[R]: {
  .let[T](x: F[T], c: Continuation[T,Let[R],R]): R-> c#(x#,this);
  .return(f: F[R]): R -> f#;
  }
Let: { #[R]: Let[R] -> {} }
//OMIT_START
A:{
//OMIT_END
//...
.distance(p1: Point, p2: Point): Nat->Let#
  .let({p1.x - (p2.x)}, {diffX, self0 -> self0
  .let({p1.y - (p2.y)}, {diffY, self1 -> self1
  .let({(diffX * diffX) + (diffY * diffY)}, {res, self2 -> self2
  .return {res.sqrt.nat}
  })})})
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

I guess you are now very confused about the code above, and you may be thinking this is not much of an improvement.
You may be thinking this is actually much worse!
However, this new form is very regular. Every line is kind of self similar, and everything is expressed via method calls.
Regular code expressed via method calls is fertile ground for both code reuse and syntactic sugar.

### The = sugar.

We are now going to show one crucial form of syntactic sugar in Fearless. 
Any method with two parameters (three counting also the receiver) can be called using this sugar. In particular this includes the '.let' method defined above.

Consider the call
`Let#.let({p1.x - (p2.x)}, {diffX, self0 -> self0 ...})`
Here the receiver is `Let#`, the first parameter is `{p1.x - (p2.x)}`
and the second parameter is `{diffX, self0 -> self0 ...}`

With the `=` sugar we can rewrite that call as follows:
`Let#.let diffX= {p1.x - (p2.x)} ...`

The distance method can use this sugar three times and becomes as follows:

-------------------------*/@Test void letFullSugar () { run("""
//|OMIT_START
use base.Nat as Nat;
use base.Void as Void;
use base.F as F;
Point:{ .x: Nat; .y: Nat }
Continuation[T,C,R]: { #(x: T, self: C): R }
Let[R]: {
  .let[T](x: F[T], c: Continuation[T,Let[R],R]): R -> c#(x#, this);
  .return(f: F[R]): R -> f#;
  }
Let: { #[R]: Let[R] -> {} }
A:{
//OMIT_END
.distance(p1: Point, p2: Point): Nat -> Let#
  .let diffX = {p1.x - (p2.x)}
  .let diffY = {p1.y - (p2.y)}
  .let res   = {(diffX * diffX) + (diffY * diffY)}
  .return {res.sqrt.nat}
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

That is, the `=` takes the binding name on its left and uses it to forge an object literal implementing a single method with two arguments.
The body of such a method is whatever method chain follows. 
By using a two argument method, the `Let` library can specify the receiver for the continuation of the call chain.
In the case of the `Let[R]`, it is just the same `Let[R]` object; we will see later cases where it is useful to change the receiver to a different value or type.

Let see again this code, comparing line by line to see what changes thanks to this sugar

```
.distance(p1:Point, p2:Point):Nat->Let#
  .let diffX= {p1.x - (p2.x)}           | .let({p1.x - (p2.x)}, {diffX, self0 -> self0
  .let diffY= {p1.y - (p2.y)}           | .let({p1.y - (p2.y)}, {diffY, self1 -> self1
  .let res={(diffX*diffX)+(diffY*diffY)}| .let({(diffX*diffX)+(diffY*diffY)},{res,self2->self2
  .return {res.sqrt}                    | .return {res.sqrt}
                                        | })})}) 
```

In the code above, we call `diffX`, `diffY` and `res` local parameters, or **locals** for short.
Local parameters are a staple of most programming language, but in Fearless they are represented 
via syntactic sugar instead of being a core language feature.
In other languages they are often known by one (or more) of the following names:
local bindings, bindings, let-bindings, lets, constants, local variables, (final) variables.

END*/
}