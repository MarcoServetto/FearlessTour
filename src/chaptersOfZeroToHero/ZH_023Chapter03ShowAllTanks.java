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
Tank:{.position:Point }
Point:{.x:Int;.y:Int}
NextState:F[List[Tank],List[Tank]]{::}
//OMIT_END
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
  .return { res.flow.map{::.flow.join(``)}.join(``|) };
  .newLine: mut List[mut Var[Str]] -> 0.rangeUntil(10).map{ _ -> Vars#(`      `)}.list;
  }
PrintGame: {
  mut .out: mut Output;
  mut .singleLine(ts: List[Tank]): Void -> this.out.println(TanksToS#(ts));
  mut .lines(rounds: Nat, ts: List[Tank]): Void -> Block#
    .var current= {ts}
    .return{ 0.rangeUntil(rounds).forEach{step -> Block#(
      this.fs.println(`Step `+step|),
      this.fs.println(`------------------------------------------------------------`|),
      this.singleLine(ts.get),
      this.fs.println(`------------------------------------------------------------`|),
      ts.set(NextState#(ts.get))
      )}}
  }
//ReadGame: { ... }
ReadGame: { .cap[T]:T; .read:List[Tank]->{};}
Test: Main {sys -> Block#
  .let out= {sys.out}
//  .let in= {sys.inputFile}
  .let game= ReadGame{20}.read
  .return{ PrintGame{out}.lines(50,game) }
  }
"""); }/*--------------------------------------------

As you can see, we omitted the code reading the initial game state.
This is because in order to read data from files there is still quite some content that we need to learn. We will handle that in Chapter 4.
Assuming a properly implemented `ReadGame`, this code could print something like the following:

```
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

```
Where tanks can be displayed on the screen, showing the various steps of the game

We now focus on this line:
```
.let res = {0.range(30).map{_->this.newLine}.list }
```

- Here we use `.range` to create a flow containing the numbers 0..29.
- Then we use `.map` to call `this.newLine` 30 times.
  Note how we use the `_` to show that we do not need the current element of the flow (a number in 0..29).
- Finally, we use `.list` to turn the flow into a list.

The next section will discuss `List`s and `Flow`s in the detail.

OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}