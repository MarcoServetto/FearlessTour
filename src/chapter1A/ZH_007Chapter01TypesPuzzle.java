package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_007Chapter01TypesPuzzle {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Types and Meaning

### Types as Fitting Puzzle Pieces

In the world of programming, types can be envisioned as the puzzle pieces of software design, each equipped with connectors—commonly referred to as ears in the context of puzzle pieces. Each type acts like a connector with unique shapes that guide how it can combine with others. This selective compatibility is essential: it aims to reduce errors and streamline the construction process, mirroring how a puzzle guides you to the correct final image through the design of its connectors and their corresponding ear holes.

Imagine constructing a puzzle where almost any ear can attach to nearly any ear hole. While this might offer an illusion of flexibility and creativity, it actually complicates the assembly process significantly. Most pieces, while connectable, do not necessarily belong together, leading to a plethora of possible configurations that deviate from the intended outcome. Such a scenario mirrors a weakly typed system, where the abundance of permissible type interactions can flood a project with potential mismatches, making the correct assembly (that is, the correct implementation of program logic) far more challenging and error-prone.

Conversely, a puzzle where each piece has a unique ear that fits with only one specific ear hole might prevent exploring possible combinations that could be useful to understand how the puzzle works overall. This analogy applies to overly rigid type systems, which might slow down the process of exploring different solutions.

Crucially, unlike puzzle assembly, software development is an inherently exploratory process. In puzzle making, the designer knows the final image and can create pieces that fit together perfectly. In software development, programmers often design the connectors (the types) while still exploring what the final program will look like. They have to predict and shape how parts of the program will interact, often revising their understanding as the software evolves.
Note how the types are the connectors, and the methods are the pieces:
A method taking an `Int` and a `Str` and producing a `Nat` would be a puzzle piece with two holes, one `Int` shaped and one `Str` shaped. This puzzle piece also has an ear that is `Nat` shaped.

As a programmer, you can choose how much to rely on types. By defining types with specific names and properties, you can control which data types can interact, akin to a puzzle where each type of ear matches only certain ear holes.

We will now show some concrete examples of how these principles apply in programming.

Some of the types we have seen are `Tank`, `Direction`, `Rotation`, `Int` and `Str`.
Method `Tanks#` takes two `Direction` parameters.
Attempting to call it with integers would result in a type error:
```
  Tanks#(+3,-6) //type error: `Int` is not a valid `Direction`
```

Several mechanisms can block our program even before the code starts reducing.
The first line of defence is syntax.
A syntax error, like unbalanced parentheses, signals that Fearless does not understand our instructions.

A type error, however, indicates that our instructions, though understood, do not make logical sense within the defined system. 
We need two directions to create a `Tank`, and the code is trying to smuggle two `Int` values as `Directions`.
Creating a `Tank` requires two `Direction` values, not two `Int` values. The type system detects and blocks such discrepancies.

Formally, the code using `Tanks` must respect the constraints that the type declaration for `Tanks` requires.
Talking about "the code using `Tanks`" and "that the type declaration for `Tanks` requires" is kind of verbose.

When discussing code, it's common to personify it, merging the code with the coding process. In this context, code can be a user of other code.

For instance, the code `Tanks#(+3,-6)` is considered a user of `Tanks`, `Int`, and method `Tanks#`. We can simplify the earlier sentence to:
"Users of Tanks must respect the Tanks constraints."
Here, "users" refers not to humans, but to pieces of code. Metaphorically the programmer is identified with the code they write.

In the same way, the programmer that coded the type declaration for `Tanks` is now identified with the code of `Tanks`.

This perspective might seem unconventional, but it underscores the fluid roles and responsibilities in coding. It also enables discussion about programming, regardless of who writes the code, acknowledging that many programs are developed by large teams of programmers.

We now explore a new example and the related type system choices.
We want to encode a `Rectangle`, defined by the coordinates of the top-left and bottom-right points.
We could just write
-------------------------*/@Test void rect1 () { run("""
Rectangles:{#(x1: Int, y1: Int, x2: Int, y2: Int): Rectangle -> Rectangle:{
  .x1: Int-> x1,
  .y1: Int-> y1,
  .x2: Int-> x2,
  .y2: Int-> y2,
  }}
"""); }/*--------------------------------------------

But this is quite error prone: what if we accidentally wrote
`.y2: Int-> y1,` instead?
This is also error prone for the user: they would have to write something like `Rectangles#(+1,+3,+10,+25)`
Can they remember the role of each of the four numbers?
What if they accidentally swap two numbers? the type system will not be able to discover this, since this version of `Rectangle` is simply made of four `Int`s.

Consider the following alternative solution:
-------------------------*/@Test void rect2 () { run("""
Points:{#(x: Int, y: Int):Point ->Point:{
  .x: Int -> x,
  .y: Int -> y,
  }}
Rectangles:{#(topLeft: Point, bottomRight: Point): Rectangle -> Rectangle:{
  .topLeft: Point-> topLeft,
  .bottomRight: Point-> bottomRight,
  }}
"""); }/*--------------------------------------------

Now both `Point` and `Rectangle` have two methods each, instead of `Rectangle` having four methods. This helps us to structure our code.
Consider now the user code:
`Rectangles#(Points#(+1,+3),Points#(+10,+25))`
The code is slightly longer, but it is more structured and more understandable.
Moreover, it is likely that in many cases the constructor will be able to take points directly, as in `Rectangles#(p1,p2)`, where `p1` and `p2` are parameters in scope.

There are still ways the user can get it wrong: they can confuse `x` and `y` coordinates, and they can confuse the meaning of the two points.
We now address the first issue.
We can declare types `X` and `Y`, so that the type system can guide us.

-------------------------*/@Test void rect3 () { run("""
Xs:{#(val: Int): X -> X:{.val: Int -> val}}
Ys:{#(val: Int): Y -> Y:{.val: Int -> val}}
Points:{#(x: X, y: Y):Point -> Point:{
  .x: X -> x,
  .y: Y -> y,
  }}
"""); }/*--------------------------------------------
Now to create a point the use has to either do
`Points#(Xs#+2,Ys#+5)` or otherwise use values of types `X` and `Y`.
We can imagine long chains of method calls passing arguments with various names, sometimes `x`, `y` sometimes `row`, `col` and sometimes just `a`, `b`, `c`.
It could be hard to track the role of those numbers without relying on the type system.
On the other hand, the type system can trivially check those chains for us.
As we just showed, by introducing more types we enable the type system to make more checks for us.

However, this is a delicate balance. Consider what happens if we want to have an `.area` method giving us the area of the rectangle.
With the first style, the code would be the following:

-------------------------*/@Test void rect4 () { run("""
Rectangles:{#(x1: Int, y1: Int, x2: Int, y2: Int): Rectangle -> Rectangle:{
  .x1: Int -> x1,
  .y1: Int -> y1,
  .x2: Int -> x2,
  .y2: Int -> y2,
  .area: Int -> (x2 - x1) * (y2 - y1),
  }}
"""); }/*--------------------------------------------

But with the new style with more types the code becomes

-------------------------*/@Test void rect5 () { run("""
Xs:{#(val: Int): X -> X:{.val: Int -> val}}
Ys:{#(val: Int): Y -> Y:{.val: Int -> val}}
Areas:{#(val: Int): Area -> Area:{.val: Int-> val}}
Points:{#(x: X, y: Y): Point ->Point:{
  .x: X-> x,
  .y: Y-> y,
  }}
Rectangles:{#(topLeft: Point, bottomRight: Point): Rectangle -> Rectangle:{
  .topLeft: Point -> topLeft,
  .bottomRight: Point -> bottomRight,
  .area: Area -> Areas#(
    bottomRight.x.val - (topLeft.x.val)
    * (bottomRight.y.val - (topLeft.y.val))
    ),
  }}
"""); }/*--------------------------------------------

As you can see, there is overall more code, and we need to explicitly wrap and unwrap those extra types.
We overall think this extra verbosity is worth it, but of course it depends on the specific situation we are in. In particular, for short programs and simple code examples relying on less types is appropriate. Thus, most examples of this guide will use strings and numbers directly instead of wrapping them into types encoding their role.

Now, for the other issue: the user can confuse the meaning of the two points.
How to fix that? the two points are not top-left and right-bottom in an absolute sense, but just in relation to each other.
Fearless offers good ways to handle this other case, but we will see them later in the guide. The main idea is that we can check that the property we want holds just before creating the point.

### Types and the source of meaning

Consider again the code for Point and the code for Direction:

-------------------------*/@Test void pointA () { run("""
Points:{#(x: Int, y: Int): Point -> Point:{
  .x: Int -> x,
  .y: Int -> y,
  }}
Direction: { .turn: Direction }
North: Direction {East }
East : Direction {South}
South: Direction {West }
West : Direction {North}
"""); }/*--------------------------------------------

Is there 'meaning' in this code?
Sure, as humans we have a lot of knowledge in our head, so we know what `North` and `East` mean, we understand what a `Direction` is supposed to be, especially if in the context of `North` and `East` being directions.
Same for Point: we know what a Point is and we understand what `x` and `y` may mean in this context.
But, the program does not know anything about this background knowledge.
The program does not give any external meaning to names. We could have written the code using abstract names and nothing would change for the program. That is, the code below is equivalent to the code above:
-------------------------*/@Test void pointB () { run("""
A:{#(b: Int, c: Int): D -> D:{
  .e: Int -> b,
  .f: Int -> c,
  }}
G: { .h: G}
I: G {L}
L: G {M}
M: G {N}
N: G {I}
"""); }/*--------------------------------------------
What does the code above mean? For us humans, it now looks incomprehensible, but for a machine it looks exactly the same as the code before.

That is, there is nothing in the code that makes `G` more "norty" than L, or `.e` more "vertical" than `.f`.

We can use more precise terminology:
- Intrinsic Meaning/Intrinsic Semantics: the meaning derived purely from the structure and connections within the code itself, independent of human interpretation.
- Extrinsic Meaning/Contextual Semantics: the meaning that arises when the code is interpreted with the background knowledge and context that humans bring to it.

However, by adding more and  more connections between our units of code, the meaning starts to emerge.

For example, we can make it so that points can be moved according to directions:
-------------------------*/@Test void pointC () { run("""
Points:{#(x: Int, y: Int): Point -> Point:{'self
  .x: Int -> x,
  .y: Int -> y,
  +(other: Point): Point -> Points#(other.x + x, other.y + y),
  .move(d: Direction): Point -> self + ( d.point ),
  }}
Direction: { .turn: Direction, .point: Point, }
North: Direction {.turn -> East,  .point -> Points#(-1, +0), }
East : Direction {.turn -> South, .point -> Points#(+0, +1), }
South: Direction {.turn -> West,  .point -> Points#(+1, +0), }
West : Direction {.turn -> North, .point -> Points#(+0, -1), }
"""); }/*--------------------------------------------

By adding a connection between points and directions, both types become more meaningful.
This is how meaning emerges. Meaning is obtained by the network of connections between our data types.

We can now add a position to our Tanks, and make them move in the direction indicated by their '.heading'.

-------------------------*/@Test void pointD () { run("""
//OMIT_START
Points:{#(x: Int, y: Int): Point -> Point:{'self
  .x: Int -> x,
  .y: Int -> y,
  +(other: Point): Point -> Points#(other.x + x, other.y + y),
  .move(d: Direction): Point -> self + ( d.point ),
  }}
Direction: { .turn: Direction, .point: Point, }
North: Direction {.turn -> East,  .point -> Points#(-1, +0), }
East : Direction {.turn -> South, .point -> Points#(+0, +1), }
South: Direction {.turn -> West,  .point -> Points#(+1, +0), }
West : Direction {.turn -> North, .point -> Points#(+0, -1), }
//OMIT_END
Tank: {
  .heading: Direction,
  .aiming: Direction,
  .position:Point,
  .move:Tank -> Tanks#(this.heading,this.aiming,this.position.move(this.heading))
  }
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank->
  { .heading -> heading, .aiming -> aiming, .position -> position }
  }
"""); }/*--------------------------------------------

Again, by connecting `Direction`, `Point` and `Tank`, the whole group of interconnected types gains more meaning.

The Extrinsic Meaning is always supposed to be a super set of the intrinsic meaning.
The program is capturing a part of the full semantic of the names.
A Bug is a situation where the intrinsic meaning expresses behaviour outside of the extrinsic behaviour.

For example, if `North.turn` was returning `North`, this would be a situation where the intrinsic semantic is different from the expected behaviour.

END*/
}
