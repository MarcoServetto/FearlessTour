package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_016Chapter02ThanksGame {
  public static final String fullPreface= """
package test
alias base.Void as Void,
alias base.Int as Int,
alias base.F as F,
alias base.Bool as Bool,
alias base.True as True,
alias base.False as False,
Points:{ #(x: Int, y: Int): Point -> Point:{'self
  .x: Int -> x,
  .y: Int -> y,
  +(other: Point): Point -> Points#(other.x + x, other.y + y),
  .move(d: Direction): Point -> self + ( d.point ),
  ==(other: Point): Bool -> self.x == (other.x)  .and (self.y == (other.y) ),
  }}
Direction: { .turn: Direction, .point:Point, }
North: Direction {.turn -> East,  .point -> Points#(-1, +0), }
East : Direction {.turn -> South, .point -> Points#(+0, +1), }
South: Direction {.turn -> West,  .point -> Points#(+1, +0), }
West : Direction {.turn -> North, .point -> Points#(+0, -1), }
Tank: {
  .heading: Direction,
  .aiming: Direction,
  .position:Point,
  .move:Tank -> Tanks#(
    this.heading,
    this.aiming,
    this.position.move(this.heading)
    ),
  }
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank->
  { .heading -> heading, .aiming -> aiming, .position -> position } }        
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
Continuation[T,C,R]: { #(x: T, self: C): R }
Let[R]: {
  .let[T](x: F[T], c: Continuation[T,Let[R],R]): R-> c#(x#,this),
  .return(f: F[R]): R -> f#,
  }
Let: { #[R]: Let[R] -> {} }        
""";
/*START
--CHAPTER-- Chapter 2
--SECTION-- Tanks Game

### Tanks game

Now we have enough understanding about Fearless that we can implement a simple game in fearless.

We start by repeating some of the code we discussed above:

-------------------------*/@Test void oldCode1 () { run("""
Points:{ #(x: Int, y: Int): Point -> Point:{'self
  .x: Int -> x,
  .y: Int -> y,
  +(other: Point): Point -> Points#(other.x + x, other.y + y),
  .move(d: Direction): Point -> self + ( d.point ),
  ==(other: Point): Bool -> self.x == (other.x)  .and (self.y == (other.y) ),
  }}
Direction: { .turn: Direction, .point:Point, }
North: Direction {.turn -> East,  .point -> Points#(-1, +0), }
East : Direction {.turn -> South, .point -> Points#(+0, +1), }
South: Direction {.turn -> West,  .point -> Points#(+1, +0), }
West : Direction {.turn -> North, .point -> Points#(+0, -1), }
Tank: {
  .heading: Direction,
  .aiming: Direction,
  .position:Point,
  .move:Tank -> Tanks#(
    this.heading,
    this.aiming,
    this.position.move(this.heading)
    ),
  }
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank->
  { .heading -> heading, .aiming -> aiming, .position -> position } }
"""); }/*--------------------------------------------
We have points, directions and tanks with positions.
Tanks can move in directions.
Note how we added an `==` method on Point.

The state of the game will be represented by a `Stack[Tank]`.
The game is implemented by a `NextState` function.
`NextState#` is implemented with a `Let`, and uses some sub methods for readability.

The idea is that first all the tanks shoot, and all of the tanks in the direction of fire are eliminated.
Then, all the surviving tanks move in their heading position, but only if that position is free.
A position is free if it is neither the current position or the destination of another tank.

-------------------------*/@Test void newCode1 () { run("""
//|OMIT_START
"""+fullPreface+"""
//OMIT_END
NextState: F[Stack[Tank],Stack[Tank]]{
  #(tanks) -> Let#
    .let danger    = { tanks.map{ t -> t.position.move(t.aiming) } }
    .let survivors = { tanks.filter{t -> this.notIn(t,danger)} }
    .let occupied  = { (survivors.map{::position}) ++ (survivors.map{::move.position}) }
    .return { survivors.map{t -> this.moveIfFree(t,occupied)} },
 
 .notIn(t: Tank, ps: Stack[Point]): Bool -> 
    ps.fold(True,{acc, p -> acc .and  ( t.position == p .not) }),

  .moveIfFree(t: Tank, occuied: Stack[Point]): Tank -> this.notIn(t.move, occupied).if{
    .then -> t.move,
    .else -> t,
    },
  }
"""); }/*--------------------------------------------

In the code above we implement `NextState#` with a let. We define all the danger positions.
We filter only the tanks not in a dangerous location using method `.notIn`.
We collect the space occupied by the survivor tanks: this is the union of the space occupied by the survivors in their current position and the space occupied by the survivors after they move in their heading direction.
Finally we move our tanks if the space they want to go into is free using the method `.moveIfFree`.
We conclude returning the new Stack of moved tanks.

Method `.notIn` uses a `.fold`:
Starting with `True`, we accumulate with `.and`, checking that our tank is not in any of the positions `p` inside `ps`.
This method only exists because we are using our little minimal implementation of a `Stack`.
Sequences in the standard library do offer much more natural ways to check if specific elements are present inside.

Method `.moveIfFree` uses a `.notIn` and `.if`:
if the moved tank would not be in an occupied position, we return the moved tank; otherwise we return the original tank.

In the code above, there is a subtle logical bug. Can you find it?
- Hint 1: This bug makes so that no tank will ever move
- Hint 2: We collect the occupied positions for all Tanks.

**Solution coming soon**

**Solution coming soon**

**Solution:** By checking if our specific `Tank` wants to move in an occupied position, we also check against the position this very tank wants to move into.
With the code as written, every `Tank` will want to move in an occupied position, since we count the position they want to move in as an occupied position.
If some other tank wants to also go in the same position, then there would be two different points in the occupied Stack that are in conflict with the point our current tank wants to go in.
Thus, we can fix the bug by counting the number of points present in our desidered next location.

Note how we used the word "our" there. By doing so, we are imagining us to be the tank that is moving. This is a useful psychological technique we can use as programmers to better visualise code execution.

To fix this bug we can simply edit the `.moveIfFree` method as follows:
-------------------------*/@Test void newCode2 () { run("""
//|OMIT_START
"""+fullPreface+"""
NextState: F[Stack[Tank],Stack[Tank]]{
  #(tanks) -> Let#
    .let danger    = { tanks.map{ t -> t.position.move(t.aiming) } }
    .let survivors = { tanks.filter{t -> this.notIn(t,danger)} }
    .let occupied  = { (survivors.map{::position}) ++ (survivors.map{::move.position}) }
    .return { survivors.map{t -> this.moveIfFree(t,occupied)} },
 
 .notIn(t: Tank, ps: Stack[Point]): Bool -> 
    ps.fold(True,{acc, p -> acc .and  ( t.position == p .not) }),

//OMIT_END
  .moveIfFree(t: Tank, occupied: Stack[Point]): Tank -> occupied
    .fold(0, {acc, p -> t.move.position == p .if{ .then 1, .else 0,} + acc })
    == 1 .if { .then-> t.move, .else-> t, },
//OMIT_START
  }
//OMIT_END
"""); }/*--------------------------------------------

Or, with two methods:

-------------------------*/@Test void newCode3 () { run("""
//|OMIT_START
"""+fullPreface+"""
NextState: F[Stack[Tank],Stack[Tank]]{
  #(tanks) -> Let#
    .let danger    = { tanks.map{ t -> t.position.move(t.aiming) } }
    .let survivors = { tanks.filter{t -> this.notIn(t,danger)} }
    .let occupied  = { (survivors.map{::position}) ++ (survivors.map{::move.position}) }
    .return { survivors.map{t -> this.moveIfFree(t,occupied)} },
 
 .notIn(t: Tank, ps: Stack[Point]): Bool -> 
    ps.fold(True,{acc, p -> acc .and  ( t.position == p .not) }),

//OMIT_END
  .moveIfFree(t: Tank, occupied: Stack[Point]): Tank -> 
    this.countHits(t,occupied) == 1
      .match { .true-> t.move, .false-> t, }, //arguably more readable than if/then/else?
	  
  .countHits(t: Tank, occupied: Stack[Point]): Tank -> occupied.fold(0, {acc, p ->
    t.move.position == p .if{ .then 1, .else 0,} + acc 
    }),
//OMIT_START
  }
//OMIT_END
"""); }/*--------------------------------------------

There are many other ways to check this, and if the stack had a 'size' method, we could just do
-------------------------*/@Test void newCode4 () { run("""
//|OMIT_START
"""+fullPreface+"""
NextState: F[Stack[Tank],Stack[Tank]]{
  #(tanks) -> Let#
    .let danger    = { tanks.map{ t -> t.position.move(t.aiming) } }
    .let survivors = { tanks.filter{t -> this.notIn(t,danger)} }
    .let occupied  = { (survivors.map{::position}) ++ (survivors.map{::move.position}) }
    .return { survivors.map{t -> this.moveIfFree(t,occupied)} },
 
 .notIn(t: Tank, ps: Stack[Point]): Bool -> 
    ps.fold(True,{acc, p -> acc .and  ( t.position == p .not) }),

//OMIT_END
  .moveIfFree(t: Tank, occupied: Stack[Point]): Tank -> occupied
    .filter{ ::== t.move.position}
    .size
    == 1 
    .if{ .then-> t.move, .else-> t, },
//OMIT_START
  }
//OMIT_END
"""); }/*--------------------------------------------

This second way is more common in practical Fearless, but exercising on using .fold is very educational.
Alternatively, we can look at those two ways to implement `.moveIfFree` and realize that `.size` is actually a very good abstraction, since
- it has a clear name with an obvious behaviour
- it is useful independently
- it allows us to simplify the method `.moveIfFree` quite a lot.
- probably, there are a lot of other methods like `.moveIfFree`, that are easier to define if we have `.size`.
Those are iconic features of good abstractions!

#### Reflecting on our progress

Pause for a moment and appreciate the depth of our journey so far.

We started from just the capacity of declaring types and methods, and we have constructed meaningful code from first principles.
We defined our own representations of booleans, numbers, stacks, and optionals, patiently assembling them from minimal concepts.

The little tank game we have just built is not trivial. It demonstrates how complexity emerges naturally and cleanly from minimal building blocks.
By mastering this foundational thinking, you've already gained the ability to envision and construct software in ways many programmers never deeply experience.

This chapter marks a milestone: You have begun to see the beauty in the minimalistic design of Fearless. As we continue, you will find these insights becoming not just a guide to coding, but a  lens through which all programming becomes clearer.

Let's keep going.

END*/
}