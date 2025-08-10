package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_004Chapter01Rotations {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Rotations

### Directions and rotations.
In the example before, the turret could only rotate one step at a time.
How to rotate the turret a variable amount of times?
Conceptually there are a few more options: we can turn zero, one, two or three times.
A simple way to encode this idea would be to simply add those methods:
```
  Direction: {
    .turn0: Direction-> this,
    .turn1: Direction,                   //called .turn before
    .turn2: Direction -> this.turn1.turn1,//called .reverse before
    .turn3: Direction -> this.turn2.turn1,//turn twice, then turn again
    }
  North: Direction {East}
  East : Direction {South}
  South: Direction {West}
  West : Direction {North}
```
This code allows us to rotate any direction any number of times.
Note how `.turn1` is the only abstract method in `Direction`.
The syntactic sugar can help us make this code more compact: since it is clear that we want to implement the method `.turn1`, because it is the only abstract method. Thus as discussed before we can write just `East` in `North` instead of the more verbose (but equivalent) `.turn1->East`.

Having names like `.turn0`, `.turn1`, `.turn2`, etc., often signals that we're missing some abstraction.
These aren't fundamentally different actions, they are different degrees of the same action: `Rotation`.
In the same way `North`, `East`, `South` and `West` are all `Direction`, those methods are all kinds of `Rotation`.

We can define Rotation as a type that represents **something that knows how to rotate a `Direction`**.

We can define the concept of rotations as follows:

```
  Direction: { .turn: Direction }
  North: Direction {East}
  East : Direction {South}
  South: Direction {West}
  West : Direction {North}

  Rotation: { #(d: Direction): Direction, }
  ...//various kinds of rotation will be shown later
```
Now `Direction` is back to the minimal form, with only a single `.turn` method,
and we have a new concept, called `Rotation`, with an `#` method.
A `Rotation` is an object with a method `#` that can take a `Direction` called `d` and rotate it in a certain way.
Now we can have a single `.turnTurret` method and pass a parameter of type `Rotation` describing how much to turn.
The syntax `d: Direction` defines `d` as a parameter of method `#`.
We now show how to define the various kinds of `Rotation`. We will call them `Turn0`, `Turn90`, `Turn180`, and `Turn270` to indicate the amount of degrees of rotation.
Since the syntax of Fearless is quite flexible, there are a few ways to declare those.
We show 4 ways to declare `Turn90`, from the most verbose to the most compact:
```
  Turn90: Rotation{#(dir: Direction): Direction -> dir.turn }
  Turn90: Rotation{#(dir)-> dir.turn }
  Turn90: Rotation{dir-> dir.turn }
  Turn90: Rotation{::turn }
```
- The first way repeats the type declaration of `#`. As we discussed, this is not needed.
Note how the code is naming the parameter `dir` instead of `d`.
This is ok, when implementing a method the name of the parameters is irrelevant and can be chosen anew every time the method is implemented.
- The second explicitly implements `#`.
- The third relies on the fact that the `#` method is the only abstract method of `Rotation`, thus we can avoid mentioning the method name. We still need to mention the parameter name and the `->` symbol.
- The last uses a new form of syntactic sugar, designed to simplify writing literals overriding a single method with a single parameter just to immediately call methods on that parameter. At your stage of learning, you may be surprised that syntactic sugar supports this specific case in particular, but with more experience you will see that this apparently oddly specific case is actually very common in Fearless code.
Using this compact syntax, here it is how we would define all the Rotations:
-------------------------*/@Test void colonColon1() { run("""
  //OMIT_START
  Direction:{ /*..as before..*/ .turn: Direction}
  //OMIT_END
  Rotation: { #(d: Direction): Direction }
  Turn0: Rotation{::}
  Turn90: Rotation{::turn }
  Turn180: Rotation{::turn.turn }
  Turn270: Rotation{::turn.turn.turn }
"""); }/*--------------------------------------------
As you can see, we just call `.turn` the appropriate number of times.
The case of `Turn0` looks quite mysterious at first: that compact syntax is desugared into
`Turn0: Rotation{ #(d)-> d }`.
**desugaring** is the act of removing the sugar, to show the code in its more primitive form.

With those type declarations, we can write code like the following:

`Turn180#(North)` and we get the following reduction:
 1. `Turn180#(North)`
 2. `North.turn.turn`
 3. `East.turn`
 4. `South`

We can also write more complex expressions, like `Turn180#(Turn180#(South))`. The reduction of this expression is left as an exercise for the reader.

Now that we have both `Direction` and `Rotation` we can rewrite `Tank` to rotate the turret parametrically!

-------------------------*/@Test void tankParameter1() { run("""
//OMIT_START
Direction:{ /*..as before..*/ .turn: Direction}
Rotation: { #(d: Direction): Direction }
Tanks: { #(heading: Direction, aiming: Direction): Tank->
  {.heading ->heading, .aiming ->aiming,}
}
//OMIT_END
Tank: {
  .heading: Direction,
  .aiming: Direction,
  .turnTurret(r: Rotation): Tank-> Tanks#(this.heading, r#(this.aiming))
}
"""); }/*--------------------------------------------

The parametric `Tank.turnTurret` method is great progress! Instead of needing multiple turret-turning methods, we now have one method that takes a `Rotation` object as input. We have separated the "what" (turn the turret) from the "how much" (the specific Rotation object).


This code example shows the core ideas of programming: 
 1. We define names to denote concepts. Those names can be type names, method names and parameter names.
 2. Using those names we model a world where our code will be able to run.
 3. We encode behaviour by passing values around from method to method.
 4. Parameters are used to hold those values while we wire them from one place to another.

Under this lens, we can describe programming as **Naming Parametric Abstractions**.
We have now seen two kinds of abstractions: 
- Methods allow us to abstract away the specific implementation of a method body: we can simply call the method again instead of typing again the full body.
- Subtyping allows us to abstract types into categories: when mentioning `Direction` as a type we mean any of the values implementing `Direction`.
We will see other forms of abstraction later on.

Note how `Tanks` and `Rotation` are kind of similar: they are both top level types with an `#` method. We call types like those **functions**.
In the common mathematical notation, a function can be directly applied to the arguments doing `f(x,y)`. In Fearless we need to add the extra `#` symbol, and we get `f#(x,y)`.

### Composing rotations

A natural feature to add to Rotations is to make them composable. Can we add two rotations together to get a rotation that rotates as much as the sum of the two individual rotations?
Consider the code below:
```
RotateTwice: { #(r1: Rotation, r2: Rotation, d: Direction): Direction->
  r1#(r2#(d))
}
```
The type `RotateTwice` has a single method `#` taking three parameters:
two `Rotation`s and a `Direction`. It then returns a `Direction`.
In the method body, we see `r1 # ( r2 # ( d ) )`, where we added some spaces for clarity.
This code can be read from the inside out: we first call `r2#` on the input direction `d`. This is going to produce some other direction, that is passed in input to method `r1#`.
The resulting effect is that we rotate the input by the sum of the rotations `r1` and `r2`.

Function `RotateTwice` takes three parameters.
While taking three parameters is perfectly fine, this gives us the opportunity to explain the concept of partial application:

>Can we make it so that `RotateTwice` takes the first two parameters and gives us a function that will rotate the direction?

This could be convenient if we needed to rotate many directions by the same two rotations.

The crucial consideration is that we already have a name for the concept of a function that takes a direction and rotates it: `Rotation`.
Can we define an "addition" operation taking two `Rotation`s and producing a new `Rotation`?

We could do this by adding a method to `Rotation`, that takes another `Rotation` and produces a `Rotation`.
Something like the following:
```
Rotation: {
  #(d: Direction):Direction,
  +(r: Rotation): Rotation-> ...
}
```
As you can see, we can define `+` as a method. As we have seen with `#`, we can use symbols like `+` and `-` as method names.
Method `Rotation+` has two parameters: `this` and `r`; the two `Rotation`s we want to compose.
For example `this` could be `Turn90` and `r` could be `Turn180`.

Using syntax 
`(Turn90+(Turn180))#(North)`
method `Rotation+` will combine those two parameters to produce a new `Rotation` object, equivalent to `Turn270`.
Then it is going to rotate `North` 270 degrees producing `West`.
Here is the full code:
-------------------------*/@Test void rotationPlus() { run("""
//OMIT_START
Direction:{ /*..as before..*/ .turn: Direction}
//OMIT_END
Rotation: {
  #(d: Direction):Direction,
  +(r: Rotation): Rotation-> { d -> this#( r#(d) ) } 
}
Turn0: Rotation{::}
Turn90: Rotation{::turn}
Turn180: Rotation{::turn.turn}
Turn270: Rotation{::turn.turn.turn}
"""); }/*--------------------------------------------


Note how `this#( r#(d) )` uses `this` and `r` similarly to how `RotateTwice` used `r1` and `r2`.
`Rotation` has the single abstract method `Rotation#`.
Thus `this` and `r` will be some concrete instances of `Rotation`:
they will have some implementation for method `Rotation#`.
This implementation may simply come from `Turn0`, `Turn90`, `Turn180` or `Turn270`.

But, there is another possibility: the `Rotation` object returned by `Rotation+` has yet another version of `Rotation#`.
This last implementation is flexible: its behaviour depends from the captured rotations!

The method `Rotation+` is considered very elegant code.
Inside it, `this` refers to the first `Rotation` (`Turn90` in `Turn90 +(Turn180)`).
`r` refers to the second rotation (`Turn180`).
The object literal `{ d -> this#( r#(d) ) }` creates a new `Rotation` object. When this new object's `#` method is called later, it will use the `this` and `r` that were captured when it was created.

Thanks to our syntactic sugar and inference, the body  of method `Rotation+` is very compact.
The expression `{ d-> this#(r#(d) }` is equivalent to 
`SomeName156:Rotation{#(d: Direction): Direction-> this#(r#(d)) }`.
Before we discussed how `North` is a literal.
`North` is just sugar for `SomeName147:North{}`. Exactly in the same way and via the same process `SomeName156:Rotation{#(d: Direction): Direction-> this#(r#(d)) }` can be shortened by the sugar to `{ d-> this#(r#(d) }`.

At first look, you may think that the body `this#(r#(d))`
would go in an infinite reduction since we call method `Rotation#`  on `this`
during the execution of `Rotation#`.
However, that `this` is the outer rotation object (the receiver of the call `Rotation+`)
that must be some `Rotation` object defined before `Rotation+`
was called, thus the behaviour of method `this#` was fully determined before `Rotation+` was called
and the return value of `Rotation+` created.
We are sure that the method `Rotation#` of `this` is implemented because all literals have no abstract methods, and parameters (like `this`) are replaced with literals when methods are called.

A common source of confusion when looking to code like 
```
Rotation: {
  #(d: Direction):Direction,
  +(r: Rotation): Rotation-> { d -> this#( r#(d) ) } 
}
```
is to assume that the method `Rotation#` will have the behaviour that we can see in `Rotation`.
Here `Rotation#` is abstract. Thus, there is no way that the calls  `this#` or `r#` would ever resolve into the non-existent code of `Rotation#`; they will always resolve to some concrete implementation of it.

While this is self evident in `Rotation#`, since there is no body, this holds also when a body is present; since methods can be overridden in other literals.

The code of `Rotation+` is similar to the code of `Tanks#`: it is creating a new kind of object by capturing the method parameters inside of the returned literal.
With the `+` method we are able to create all kinds of `Rotation`s by using only `Turn90`:
For example, `Turn90+(Turn90)` behaves exactly like `Turn180` but is conceptually a different object.
To obtain `Turn0` we could just write `Turn90+(Turn90+(Turn90+(Turn90)))`
While writing parenthesis after the `#` method feels natural, we are all used to writing mathematical operations without parentheses. This is possible also in Fearless: every method with zero or one argument can be called without parentheses.
Thus, we can write `Turn90 + Turn90 + Turn90 + Turn90` to obtain the same result as before.
We will discuss Fearless operator precedence, or the Fearless lack thereof, when we introduce numbers in the next section.

END*/
}
