package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_022Chapter03ShowTanks {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Show Tanks

### Visualising the Tank game

Now that we know how to write a full Fearless program, we can write a program reading tanks from a file and running the Tank game on the console.
Ashii art is a well known way to visualise simple games. We will use this to visualise the state of a Tank.
Below the representation of a tank heading `East` and aiming `North`, and another one heading `North` and aiming `West` 

```
/ | \  / - \
| < |  | A -
\ _ /  \ _ /
```

The idea is that we can use `<`,`>`,`V` and `A` to represent the heading direction, and `|` or `-` to represent the aiming direction.
Of course, many different options exist.
The general idea is that we will create a grid of tanks to represent all of the tanks in any given moment, and we will repeat that grid over and over to show the passage of time.
Since a tank is represented on 3 lines, we will write 3 methods, `.repr1`, `.repr2` and `.repr3`.
Then, we could just write a `.str` method simply calling those 3 methods. However, having to convert objects into strings is a common task in programming, and we are now trying to rely on the standard library instead of implementing everything from scratch.

Thus, we will use the standard library type `base.ToStr` shown before.
```
ToStr:{ read .str: Str }
```
Note how `.`str` is 'read': In this way it can be called on both mutable and immutable objects.

# The new code for the tanks game

The reworked code would look like this, nicely divided into files.

The following file defines all our aliases.
```
//File _tank_game/_rank_app.fear
use base.Main as Main;
use base.Int as Int;
use base.Bool as Bool;
use base.F as F;
use base.Str as Str;
use base.ToStr as ToStr;
use base.List as List;
````

The following file defines `Point`. We will implement `ToStr` also for `Point`.
The code below uses the novel syntax `'self`, to name the current `Point` object. Since `Point` is defined inside of `Points`, the `this` in scope would be an instance of `Points`, not `Point`. Using `'someName` at the beginning of an object literal, allows to name the current object.
That is, all the top level object literals implicitly use `'this`.
```
//File _tank_game/point.fear
Points:{#(x: Int, y: Int): Point -> Point: ToStr{ 'self
  .x: Int -> x;
  .y: Int -> y;
  +(other: Point): Point -> Points#(other.x + x, other.y + y);
  .move(d: Direction): Point -> self + ( d.point );
  ==(other:Point): Bool -> self.x == (other.x)  .and (self.y == (other.y) );
  .str -> `[x=` + x + `, y=` + y + `]`;
  }}
```

We now define `Direction`. 
Instead of dispersing the implementation of `.point` and `.turn` inside all the directions, we define a `.match` and use it as a way to define generic extensible operations.
This is the standard way to define those kinds of data types in fearless.
We call **enumerations** any type whose subtypes are all constants.
```
//File _tank_game/direction.fear
North: Direction {::.north}
East : Direction {::.east}
South: Direction {::.south}
West : Direction {::.west}
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Direction: ToStr {
  .match[R: **](mut DirectionMatch[R]): R;
  .turn: Direction -> this.match{
    .north -> East;
    .east  -> South;
    .south -> West;
    .west  -> North;
    };
  .point: Point -> this.match{
    .north -> Points#(-1, +0);
    .east  -> Points#(+0, +1);
    .south -> Points#(+1, +0);
    .west  -> Points#(+0, -1);
    };
  .str -> this.match{
    .north -> `North`;
    .east  -> `East`;
    .south -> `South`;
    .west  -> `West`;
    };
  }
````

We define `DirectionMatch[R]` to be mutable. This is because we want to allow the execution of `.match` to mutate the state of objects captured by the running operation. In some cases we will want our matchers to be more restrictive and to only support operations that do not perform mutations, but in most cases we will use the shown signature.
Note how assuming the intention of defining an enumeration, the three lines
```
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Direction: {
  .match[R: **](mut DirectionMatch[R]): R;
```
are completely determined given the declaration for the four directions. Experienced Fearless programmers find writing those 3 lines trivial but boring. However, writing those three lines over and over again has a great educational value for new programmers, since they require using the match pattern, reference capabilities, generic types and generic methods. 


Now we define our tanks:
```
//File _tank_game/tank.fear
Tank: ToStr {
  .heading: Direction;
  .aiming: Direction;
  .position: Point;
  .move:Tank -> Tanks#(this.heading, this.aiming, this.position.move(this.heading));
  .repr1: Str -> ` / | \ `;
  .repr2: Str -> ` | < | `;
  .repr3: Str -> ` \ _ / `;
  .str -> `` | this.repr1 |this.repr2 | this.repr3 |;
  }
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank ->
  { .heading -> heading; .aiming -> aiming; .position -> position; }
  }
```
The code above is wrong in two different ways:
- We do not check our aiming/heading direction and simply show a predefined representation. This is intentional at this point, so that we could show the structure of the code before going into the details. Writing a skeleton of the code is often a good technique to start our coding tasks.
- The code as written does not compile. The method .str reports 3 errors:
   We can not call `this.repr1`, `this.repr2` and `this.repr3` from `.str`.

This is because the `.str` method is `read` and we declared those repr methods as `imm`.
This is a common inconvenience in Fearless: we expect that all kinds of tanks will be immutable, but this may not be the case.
From the point of view of the type system, future programmers may write new types extending `Tank`; and those may mutate state, so we can not call an immutable method from a `read` receiver.

However, inside of `Tanks`, we explicitly create an immutable tank. Thus, we can simply move the implementation of `.str` inside of `Tanks`!
We remove the line implementing `.str` in `Tank` and we write `Tanks` as follows:
```
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank -> {'self
  .heading -> heading; .aiming -> aiming; .position -> position;
  .str -> ``| self.repr1 | self.repr2 | self.repr3 |;
  }}
```
Here `self` is always immutable since it is created as an `imm Tank`.

Now we need to implement the repr methods.
The challenge is that we need to synthesise the right character `<`,`>`,`V`,`A`,`-`,`|` for the various cases.
We can do it using `DirectionMatch`.

```
Tank: ToStr {
  .heading: Direction;
  .aiming: Direction;
  .position:Point;
  .move:Tank -> Tanks#(this.heading, this.aiming, this.position.move(this.heading));
  .repr1:Str-> this.aiming .match AimingRepr1;
  .repr2:Str-> this.aiming .match AimingRepr2{this.heading .match HeadingChar};
  .repr3:Str-> this.aiming .match AimingRepr3;
  }
HeadingChar: DirectionMatch[Str]{
  .north -> `A`;
  .east  -> `<`; 
  .south -> `V`;
  .west  -> `>`;
  }
AimingRepr1: DirectionMatch[Str]{
  .north -> ` / | \ `;
  .east  -> ` / - \ `; 
  .south -> ` / - \ `;
  .west  -> ` / - \ `;
  }
AimingRepr2: DirectionMatch[Str]{
  .centre:Str;
  .north -> ` | ` + this.centre + ` |  `;
  .east  -> ` - ` + this.centre + ` |  `;
  .south -> ` | ` + this.centre + ` |  `;
  .west  -> ` | ` + this.centre + ` -  `;
  }
AimingRepr3: DirectionMatch[Str]{
  .north -> ` \ _ / `;
  .east  -> ` \ _ / `; 
  .south -> ` \ | / `;
  .west  -> ` \ _ / `;
  }
```

We think this code is very clear, declarative and self explanatory.
It does have quite a few lines, but most lines are very short and do specific very well defined tasks.
We could get this code to be shorter by inlining `AimingRepr1-3` in the code of `Tank`, but we think this would make the resulting code much harder to read.
Note how `AimingRepr2` requires knowing the  central character, and we can pass it to the operation by implementing the abstract method `.centre` in the call site.
We could have alternatively made a factory capturing the missing information in the lambda:

```
... 
  .repr2: Str -> this.aiming .match (AimingRepr2#(this.heading .match HeadingChar));
...
AimingRepr2: Function[Str, DirectionMatch[Str]]:{ centre->{
  .north -> ` | ` + centre + ` |  `;
  .east  -> ` - ` + centre + ` |  `;
  .south -> ` | ` + centre + ` |  `;
  .west  -> ` | ` + centre + ` -  `;
  }}

```
Both styles are perfectly valid and good Fearless code. We just like the first one a little more.

Now we can represent tanks as strings.
We can test our code as follows:
```
Test:Main {sys -> sys.out.println(  Tanks#(North, West, Points#(1, 2))  )}
```
This code will print

```
 / - \ 
 - A |
 \ _ /   
```

We can now rewrite state change using features from the standard library instead of our poor man stack.

```
//File _tank_game/next_state.fear
NextState:F[List[Tank],List[Tank]]{
  #(tanks)->Block#
    .let danger= { tanks.flow.map{ t -> t.position.move(t.aiming) }.list }
    .let survivors= { tanks.flow.filter{t -> danger.flow.filter{::==(t.position)}.isEmpty } .list }
    .let occupied= { (survivors.flow.map{::.position}) ++ (survivors.flow.map{::.move.position}) .list }
    .return { survivors.flow.map{t -> this.moveIfFree(t,occupied)} };
 
  .moveIfFree(t: Tank, occupied: List[Point]): Tank-> occupied.flow
    .filter{::==(t.position)}
    .size == 1 .if{
      .then -> t.move;
      .else -> t;
    };
  }
```
The main difference is that we are now using `List` instead of `Stack`.
A `List` is not very different from a `Stack`. It has many more useful methods and the implementation is more efficient.
Note how we are not using `.map` directly on the list but we call the `.flow` method before.

The idea is that the standard library does not define those useful `.map`/`.filter`/`.fold` methods independently on all sequences.
Instead, there is a unified concept of `Flow`. Many different data types can be converted into flows, the elements can be manipulated using a very expressive set of `Flow` methods, then the result can be converted back into some supported data type.

In the code above, the `List[E].flow` method returns a `Flow[E]` and the methods `Flow[E].map`/`.filter` return another `Flow[E]`.
`Flow[E].isEmpty` is true if the flow is empty, `Flow[E].size` returns the size of the flow, and `Flow[E].list` returns a `List[E]` with the same elements of the flow. 
We will see many operations on flow by examples in the next few pages.

Here you can see all the code of this section packed together.

-------------------------*/@Test void allCode() { run("""
//File _tank_game/_rank_app.fear
use base.Main as Main;
use base.Str as Str;
use base.Int as Int;
use base.Bool as Bool;
use base.F as F;
use base.ToStr as ToStr;
use base.List as List;
use base.Block as Block;
use base.WidenTo as WidenTo;
//----------------------------------
//File _tank_game/point.fear
Points:{#(x: Int, y: Int): Point -> Point: ToStr{ 'self
  .x: Int -> x;
  .y: Int -> y;
  +(other: Point): Point -> Points#(other.x + x, other.y + y);
  .move(d: Direction): Point -> self + ( d.point );
  ==(other:Point): Bool -> self.x == (other.x)  .and (self.y == (other.y) );
  .str -> `[x=` + x + `, y=` + y + `]`;
  }}
//----------------------------------
//File _tank_game/direction.fear
North: Direction {::.north}
East : Direction {::.east}
South: Direction {::.south}
West : Direction {::.west}
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Direction: ToStr, WidenTo[Direction] {
  read .match[R: **](mut DirectionMatch[R]): R;
  .turn: Direction -> this.match{
    .north -> East;
    .east  -> South;
    .south -> West;
    .west  -> North;
    };
  .point: Point -> this.match{
    .north -> Points#(-1, +0);
    .east  -> Points#(+0, +1);
    .south -> Points#(+1, +0);
    .west  -> Points#(+0, -1);
    };
  .str -> this.match{
    .north -> `North`;
    .east  -> `East`;
    .south -> `South`;
    .west  -> `West`;
    };
  }
//----------------------------------
//File _tank_game/tank.fear
Tanks: { #(heading: Direction, aiming: Direction, position: Point): Tank -> {'self
  .heading -> heading; .aiming -> aiming; .position -> position;
  .str -> ``| (self.repr1) | (self.repr2) | (self.repr3) |;
  }}
Tank: ToStr {
  .heading:  Direction;
  .aiming:   Direction;
  .position: Point;
  .move:     Tank -> Tanks#(this.heading, this.aiming, this.position.move(this.heading));
  .repr1:    Str  -> this.aiming .match AimingRepr1;
  .repr2:    Str  -> this.aiming .match mut AimingRepr2{this.heading .match HeadingChar};
  .repr3:    Str  -> this.aiming .match AimingRepr3;
  }
HeadingChar: DirectionMatch[Str]{
  .north -> `A`;
  .east  -> `<`; 
  .south -> `V`;
  .west  -> `>`;
  }
AimingRepr1: DirectionMatch[Str]{
  .north -> ` / | \u005c\u005c `;
  .east  -> ` / - \u005c\u005c `; 
  .south -> ` / - \u005c\u005c `;
  .west  -> ` / - \u005c\u005c `;
  }
AimingRepr2: DirectionMatch[Str]{
  mut .centre: Str;
  .north -> ` | ` + (this.centre) + ` |  `;
  .east  -> ` - ` + (this.centre) + ` |  `;
  .south -> ` | ` + (this.centre) + ` |  `;
  .west  -> ` | ` + (this.centre) + ` -  `;
  }
AimingRepr3: DirectionMatch[Str]{
  .north -> ` \u005c\u005c _ / `;
  .east  -> ` \u005c\u005c _ / `; 
  .south -> ` \u005c\u005c | / `;
  .west  -> ` \u005c\u005c _ / `;
  }
//----------------------------------
//File _tank_game/next_state.fear
NextState:F[List[Tank],List[Tank]]{
  #(tanks)->Block#
    .let danger= { tanks.flow.map{ t -> t.position.move(t.aiming) }.list }
    .let survivors= { tanks.flow.filter{t -> danger.flow.filter{::==(t.position)}.isEmpty } .list }
    .let occupied= { (survivors.flow.map{::.position}) ++ (survivors.flow.map{::.move.position}) .list }
    .return { survivors.flow.map{t -> this.moveIfFree(t,occupied)} .list };
 
  read .moveIfFree(t: Tank, occupied: List[Point]): Tank-> occupied.flow
    .filter{::==(t.position)}
    .size == 1 .if{
      .then -> t.move;
      .else -> t;
    };
  }
"""); }/*--------------------------------------------


END*/
}