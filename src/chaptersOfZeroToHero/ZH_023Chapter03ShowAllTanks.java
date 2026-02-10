package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_023Chapter03ShowAllTanks {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Show All Tanks

### Visualising a full tank game.

How to visualise a full tank game? Those tanks can be in any position; it would be impractical to display a grid able to contain any x/y number coordinates. For simplicity, we will just display the grid with x,y ranging from zero to nine inclusive.
To do this, we will use mutation:

- We will first create a list of lists of variable strings, initially representing an empty grid.
- We will then fill it by looking at all the tanks.
- Finally, we will concatenate all the results into a big string.

Since each tanks is represented as 3 lines, we will have a grid 30 * 10.
Initially, every cell will contain `      `: six spaces, representing the absence of a tank. This is because each tank line is 6 characters.
-------------------------*/@Test void showAllTanks1(){ run("""
//OMIT_START
use base.Main as Main;
use base.Str as Str;
use base.Var as Var;
use base.Vars as Vars;
use base.Void as Void;
use base.Int as Int;
use base.Nat as Nat;
use base.Bool as Bool;
use base.F as F;
use base.ToStr as ToStr;
use base.List as List;
use base.Block as Block;
use base.Sealed as Sealed;
use base.WidenTo as WidenTo;
use base.Output as Output;
use base.InputCursorNode as InputCursorNode;
//File _tank_game/point.fear
Points:{#(x: Nat, y: Nat): Point -> Point: ToStr{ 'self
  .x: Nat -> x;
  .y: Nat -> y;
  +(other: Point): Point -> Points#(other.x + x, other.y + y);
  .move(d: Direction): Point -> d.match{
    .north -> Points#(x - 1, y    );
    .east  -> Points#(x,     y + 1);
    .south -> Points#(x + 1, y    );
    .west  -> Points#(x,     y - 1);
    };
  ==(other:Point): Bool -> other.x == x  .and (other.y == y );
  .str -> `[x=` + x + `, y=` + y + `]`;
  }}
//----------------------------------
//File _tank_game/direction.fear
North: Direction {::.north}
East : Direction {::.east}
South: Direction {::.south}
West : Direction {::.west}
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Direction: ToStr, Sealed, WidenTo[Direction] {
  read .match[R: **](mut DirectionMatch[R]): R;
  .turn: Direction -> this.match{
    .north -> East;
    .east  -> South;
    .south -> West;
    .west  -> North;
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
  .north -> ` / | \\ `;
  .east  -> ` / - \\ `; 
  .south -> ` / - \\ `;
  .west  -> ` / - \\ `;
  }
AimingRepr2: DirectionMatch[Str]{
  mut .centre: Str;
  .north -> ` | ` + (this.centre) + ` | `;
  .east  -> ` - ` + (this.centre) + ` | `;
  .south -> ` | ` + (this.centre) + ` | `;
  .west  -> ` | ` + (this.centre) + ` - `;
  }
AimingRepr3: DirectionMatch[Str]{
  .north -> ` \\ _ / `;
  .east  -> ` \\ _ / `; 
  .south -> ` \\ | / `;
  .west  -> ` \\ _ / `;
  }
//----------------------------------
//File _tank_game/next_state.fear
NextState:F[List[Tank],List[Tank]]{
  #(tanks)->Block#
    .let danger= { tanks.flow.map{ t -> t.position.move(t.aiming) }.list }
    .let survivors= { tanks.flow.filter{t -> danger.flow.filter{::==(t.position)}.isEmpty } .list }
    .let occupied= { 
      (survivors.flow.map{::.position}) ++ (survivors.flow.map{::.move.position}) .list }
    .return { survivors.flow.map{t -> this.moveIfFree(t,occupied)} .list };
 
  read .moveIfFree(t: Tank, occupied: List[Point]): Tank-> occupied.flow
    .filter{::==(t.position)}
    .size == 1 .if{
      .then -> t.move;
      .else -> t;
    };
  }
//----------------------------------
//File _tank_game/read_game.fear
ReadGame: { mut .in: mut InputCursorNode; mut .read:List[Tank]->{};}
//OMIT_END
//----------------------------------
//File _tank_game/print_game.fear
TanksToS: F[List[Tank],Str]{ ts -> Block#
  .let res = {0.rangeUntil(30).map{_->this.newLine}.list }
  .do{ ts.flow.forEach{ t -> Block#
    .let x= { t.position.x }
    .let y= { t.position.y }
    .if {x.inRange(0,10).not} .done
    .if {y.inRange(0,10).not} .done
    .do{ res.get(y * 3)    .get(x).set(t.repr1) }
    .do{ res.get(y * 3 + 1).get(x).set(t.repr2) }
    .do{ res.get(y * 3 + 2).get(x).set(t.repr3) }
    .done
    }}
  .return { res.flow.map{::.flow.map{::.get}.join(``)}.join(``|) };
  read .newLine: mut List[mut Var[Str]] -> 0.rangeUntil(10).map{ _ -> Vars#(`      `)}.list;
  }
PrintGame: {
  mut .out: mut Output;
  mut .singleLine(ts: List[Tank]): Void -> this.out.println(TanksToS#(ts));
  mut .lines(rounds: Nat, ts: List[Tank]): Void -> Block#
    .var current= {ts}
    .return{ 0.rangeUntil(rounds).forEach{step -> Block#(
      this.out.println(`Step `+step|),
      this.out.println(`------------------------------------------------------------`|),
      this.singleLine(current.get),
      this.out.println(`------------------------------------------------------------`|),
      current.set(NextState#(current.get))
      )}}
  }
//----------------------------------
//File _tank_game/_rank_app.fear
Test: Main {sys -> Block#
  .let out= {sys.out}
  .let in= {sys.inputCursor# !}
  .let game= {mut ReadGame{in}.read}//ReadGame omitted for now
  .return{ mut PrintGame{out}.lines(50,game) }
  }
//OMIT_START
//PRINT|[###]
//OMIT_END
"""); }/*--------------------------------------------

As you can see, we omitted the code reading the initial game state.
This is because in order to read data from files there is still quite some content that we need to learn. We will handle that in Chapter 4.
Assuming a properly implemented `ReadGame`, this code could print something like the following:
<pre class="code-50"><code>
Step 5
------------------------------------------------------------
 / - \       / - \
 | > |       - V |
 \ | /       \ _ /



       / - \ 
       | V | 
       \ | / 
                                     / - \
                                     | < |
                                     \ | /
             / | \ 
             | > | 
             \ _ / 



 / - \       / - \
 | > |       - V |
 \ | /       \ _ /



                                     / - \
                                     | < |
                                     \ | /
------------------------------------------------------------

Step 6
------------------------------------------------------------
       / - \     
       | > |     
       \ | /     
             / - \
             - V |
             \ _ /
 


       / - \                   / - \
       | V |                   | < |
       \ | /                   \ | /
                   / | \  
                   | > | 
                   \ _ / 



       / - \
       | > |
       \ | /
             / - \
             - V |
             \ _ /
                               / - \
                               | < |
                               \ | /
------------------------------------------------------------
</code></pre>
Where tanks can be displayed on the screen, showing the various steps of the game

We now focus on those two lines:
```
  .let res = {0.rangeUntil(30).map{_->this.newLine}.list }
  ...
  read .newLine: mut List[mut Var[Str]] -> 0.rangeUntil(10).map{ _ -> Vars#(`      `)}.list;
```

- Here we use `.rangeUntil` to create a flow containing the numbers 0..29.
- Then we use `.map` to call `this.newLine` 30 times.
  Note how we use the `_` to show that we do not need the current element of the flow (a number in 0..29).
- Finally, we use `.list` to turn the flow into a list.
- `.newLine` internally does a very similar work:
  We range from 0..9 and for each of those we create a `Var` initialised with six spaces.
That is, the type of `res` is `mut List[mut List[mut Var[Str]]]`.
This is a large and intricate type, representing a list of list of variable strings.
The next section will discuss `List`s and `Flow`s in the detail.

The code shown below uses `res` to represent a grid of information, that can be updated as needed.

````
    .let x= { t.position.x }
    .let y= { t.position.y }
    .if {x.inRange(0,10).not} .done
    .if {y.inRange(0,10).not} .done
    .do{ res.get(y * 3)    .get(x).set(t.repr1) }
    .do{ res.get(y * 3 + 1).get(x).set(t.repr2) }
    .do{ res.get(y * 3 + 2).get(x).set(t.repr3) }
````
This code runs `.forEach` of the tanks `t` in `ts`
`x/y` are just short names for the coordinates of `t`.
If `x` or `y` are `.not` in the visualized range, we do not represent tank `t` on our board `res`.
Otherwise, we write the three lines representing `t` on the appropriate position on `res`.
Note how we call `.get(..).get(..).set(..)`
to access two layers of `List` and then set a new value in our variable.

What we are creating now is basically a 'text art' based game.
Those were popular in the (far) past. Of course Fearless supports proper graphics, and we will see how to render nice looking images of tanks later on; but this way of printing the 'current screen' line by line is how those more fancy graphic systems work too under the hood.
Here we use characters as graphical symbols, they use (much smaller) coloured pixels as graphical symbols.

But the idea of doing graphics by using a grid of graphical symbols is the same,
and the struggle to decide what symbol to place in each location is very similar too.

That is, the techniques and mindset shown here do scale to full modern 2-D, or even 3-D graphics.
The computer screen is conceptually accessed as a large `mut List[mut List[mut Var[Color]]]`
and the computer is simply insanely fast at switching those colours around creating the illusion of movement.

When wanting to display shapes on the screen, the logic will look a lot like what we had for our tanks: forall shapes to display, display the shape.
To display an individual shape: for all the parts of the shape: display the individual part (the three lines of the tank in our example).
The act of displaying a shape part is the act of setting new colours in specific places in the large `mut List[mut List[mut Var[Color]]]` screen.

Hopefully this removes another layer of mystery on how computers works, and the realisation that those little pixels are indeed explicit entities that operations in the computer are able to update fast enough to create the illusion of movement clarifies in a visceral way how fast those computers are.

END*/
}