package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_028Chapter04SerializingTanks {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Serialising Tanks

## Serialising Tanks

With our understanding from before, we can reimplement `Tanks` as follows:
```
Tanks: F[Direction,Direction,Point,Tank], FromInfo[Tank] {
  heading, aiming, position -> Tank:DataType[Tank,Tank]{'self
    .heading: Direction -> heading;
    .aiming: Direction -> aiming;
    .position: Point -> position;
    .info -> Infos.map("heading", heading, "aiming", aiming,   "position", position);
    .str  -> ...;//includes repr1...repr3
    .cmp t1,t2 -> t1 <=> (t2, {::.imm.heading}.then{::.imm.aiming}.then{::.imm.position});
    .hash -> heading.hash.hashWith(aiming.hash).hashWith(position.hash);
    .close->self; .close->::; .imm->self;
    };
  .fromInfo(i) -> Tanks#(
    Directions.fromInfo(i.getMap.get("heading")),
    Directions.fromInfo(i.getMap.get("aiming")),
    Points.fromInfo(i.getMap.get("position"))
    );
  }
```
As you can see, `Tank` is quite similar to `Point`.
Its `.cmp` uses `<=>` with an `OrderBy`, as we have seen while comparing objects in Chapter 3: two tanks are compared by heading, then by aiming, then by position.
The methods `.heading`, `.aiming` and `.position` are `imm`, while the parameters of `.cmp` are `read`, so each key goes through `.imm`.

#### Reading a `List[Tank]` from a file

Now that we have serialisable and deserialisable tanks, we can implement the type reading tanks from a file.
In Chapter 3 we showed this code:
````
//File _tank_game/_rank_app.fear
Test: Main {sys -> Block#
  .let out= {sys.out}
  .let in= {sys.inputCursor# !}
  .let game= {mut ReadGame{in}.read}
  .return{ mut PrintGame{out}.lines(50,game) }
  }
````
Where we did not discuss how to write `ReadGame`.
Now we can show such code!
```
ReadGame: {
  mut .in: mut InputCursorNode;
  mut .read : List[Tank] -> Block#
    .let[Str] text= {this.in.text!}
    .let[Info] info= {Infos.fromStr(text)}
    .return {info.getList.flow.map{i->Tanks.fromInfo(i)}.list};
  }
```

As you can see, the no-args `.read` method takes the text of the file, parses it as an `Info` and then parses that `Info` as a list of `Tank`s.

You may be scratching your head about where this file comes from.
We are not specifying an actual operation like read the file called `"input.txt"`. Where is this file coming from?
It turns out that every single OS has ways to capture files intended as input, and 
this is exactly what `sys.inputCursor# !` is doing.
The idea is that there can be many files intended as input, and more may be added at any time.
This is the code of `InputCursor`.
````
InputCursor: ToIso[InputCursor], WidenTo[InputCursor]{
  mut #: mut Opt[mut InputCursorNode];
  mut .next: mut InputCursor;
  }
InputCursorNode: ToIso[InputCursorNode], WidenTo[InputCursorNode]{
  mut .label: Str; /// best effort portable name to help debugging
  mut .text: Opt[Str]; /// fresh read+decode each call; throws on I/O failure
  }
````
- `sys.inputCursor` is the capability to observe the files intended as input.
- `sys.inputCursor#` gets the first input node.
- `sys.inputCursor# !` extracts the actual `InputCursorNode`, throwing an error if no input has been provided yet.
- `this.in.text` then calls the `.text` method. If the file has text, the optional will not be empty.
- With `this.in.text!` we extract the content from the optional with `!`.

Note how this code simply leaks any error, wherever it is raised.
There are three main kinds of error here:
- 1 Reading the string from file
- 2 Deserialising the string into an `Info`
- 3 Deserialising the info into a `List[Tank]`

We may want to provide alternative behaviour ...
> not sure this would make for a good example.

> should the input node have a method .info directly?
> If so, what should happen if the file is valid text but not valid info? Opt empty or error? why? similar questions may pop up for malformed images.

> this text below was for a former API, that used action and we could discuss how to nest `.andThen`+`Try`
```
ReadGame: {..
  mut .read(fileName: List[UStr]): mut Action[List[Tank]] -> this.io
    .accessR(fileName)                      //mut ReadPath
    .readStr                                //mut Action[Str]
    .andThen{s -> Try#{Infos.fromStr(s)}}   //mut Action[Info]
    .andThen{i -> Try#{i.getList.flow.map{i->Tanks.fromInfo(i)}.list}}
  }
```
> Note: could we have `Flow.tryMap`, and make it work also for `Action[mut T]`?  
> Also, should we have a variant of .andThen/.map that uses Try# on the argument, since it seems common?  

Instead of `.map` we now use `.andThen` + `Try#`.
The type is the same, but the error management is now very different.
There are three main points of error:
- 1 Reading the string from file
- 2 Deserialising the string into an `Info`
- 3 Deserialising the info into a `List[Tank]`

Those three kinds of errors are handled differently when calling `.read(fileName).run{..}`.
What errors are captured inside the `Info` of the `Action[List[Tank]]` returned by the `.read(fileName)` method?
What errors just leak out?
The errors from (1) are always handled by the action. This means that they always end up captured by the action `Info`.
The errors in 2 and 3 leak out when using `.map` and are captured when using `.andThen` + `Try#`.


> An interesting corner of design would be to offer some way to go from `Flow[Action[T]]` into `Action[List[T]]` ? or `Action[R]` with a transformation function on the flow?


Note that `ReadGame.read` body is `this.read(Lists#("StartConfiguration.txt"))!`,
thus the errors that we carefully separated in the second implementation end up together again when we call the method `!` on the result of `.read(fileName)`.
This causes all the errors to become observed bugs and to stop our application.

This is not always the desired behaviour. When writing larger applications it becomes important to distinguish which errors are recoverable situations (so that we can capture them into the action `Info`) and which errors are observed bugs.
Using `.map` or `.andThen` + `Try#` we can choose how to classify such details.

We can also add information to the error messages using code as below
```
    .andThen{s -> Try#{Infos.fromStr(s)}
      .context{"While deserialising Info from string"}}
    .andThen{i -> Try#{i.getList.flow.map{i->Tanks.fromInfo(i)}.list}
      .context{"While deserialising tanks from Info"}}
```
Note how the indentation helps us see the context text becoming part of the action.

#### Graduation
This is the end of Chapter 4.
In these four chapters we used the tank game as an example of how to build simple behaviour.

We are now going to move forward, toward other interesting examples.



OMIT_START
-------------------------*/@Test void serialiseTanks() { run("""
use base.Main as Main;
use base.Str as Str;
use base.Nat as Nat;
use base.Bool as Bool;
use base.F as F;
use base.ToStr as ToStr;
use base.ToInfo as ToInfo;
use base.List as List;
use base.Lists as Lists;
use base.Block as Block;
use base.Sealed as Sealed;
use base.WidenTo as WidenTo;
use base.DataType as DataType;
use base.FromInfo as FromInfo;
use base.Infos as Infos;
use base.Info as Info;
use base.Enums as Enums;
use base.Enum as Enum;
use base.OrderHash as OrderHash;
use base.InputCursorNode as InputCursorNode;
use base.Void as Void;
use base.Debug as Debug;
use base.Try as Try;
use base.Action as Action;
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .match[R:**](m: mut DirectionMatch[R]): R;
  .close->this; .close->::;
  }
North: Direction{::.north; .imm->North; "North"}
East:  Direction{::.east;  .imm->East;  "East" }
South: Direction{::.south; .imm->South; "South"}
West:  Direction{::.west;  .imm->West;  "West" }
Points: F[Nat,Nat,Point], FromInfo[Point] {
  .fromInfo(i) -> Points#(i.getMap.get("x").getMsg.getNat, i.getMap.get("y").getMsg.getNat);
  # x, y ->Block#
    .do { x.assertInRange(0=~~10) }
    .do { y.assertInRange(0=~~10) }
    .return{ Point: DataType[Point,Point]{'self
      read .x: Nat -> x;
      read .y: Nat -> y;
      .cmp t0, t1 -> t0.x <=> (t1.x) && {t0.y <=> (t1.y)};
      .hash -> x.hash.hashWith(y.hash);
      .info -> Infos.map("x",x,  "y",y);
      .str -> "[" + x + ", " + y + "]";
      .close->self; .close->::; .imm->self;
      }}}
Tanks: F[Direction,Direction,Point,Tank], FromInfo[Tank] {
  heading, aiming, position -> Tank:ToInfo, ToStr,OrderHash[Tank]{'self
    read .heading: Direction -> heading;
    read .aiming: Direction -> aiming;
    read .position: Point -> position;
    .info -> Infos.map("heading", heading, "aiming", aiming,   "position", position);
    .str  -> "tank";
    .close->self; .close->::;
    .cmp t1,t2 -> t1.heading <=> (t2.heading)
      && {t1.aiming <=> (t2.aiming)} && {t1.position <=> (t2.position)};
    .hash -> heading.hash.hashWith(aiming.hash).hashWith(position.hash);
    };
  .fromInfo(i) -> Tanks#(
    Directions.fromInfo(i.getMap.get("heading")),
    Directions.fromInfo(i.getMap.get("aiming")),
    Points.fromInfo(i.getMap.get("position"))
    );
  }
ReadGame: {
  mut .in: mut InputCursorNode;
  mut .read : List[Tank] -> Block#
    .let[Str] text= {this.in.text!}
    .let[Info] info= {Infos.fromStr(text)}
    .return {info.getList.flow.map{i->Tanks.fromInfo(i)}.list};
  }

Test: Main{s-> Block#
  .do{Debug#(Tanks#(North,East,Points#(1,2)).info.str)}
  .do{Debug#(Tanks.fromInfo(Infos.fromStr(Tanks#(North,East,Points#(1,2)).info.str)) == (Tanks#(North,East,Points#(1,2))))}
  .do{Debug#(Tanks.fromInfo(Infos.fromStr(Tanks#(North,East,Points#(1,2)).info.str)) == (Tanks#(North,East,Points#(1,3))))}
  .do{Debug#(Infos.fromStr(`[{"heading":"North","aiming":"East","position":{"x":"1","y":"2"}},{"heading":"West","aiming":"South","position":{"x":"7","y":"3"}}]`).getList.flow.map{i->Tanks.fromInfo(i)}.list.size)}
  .do{Debug#(Infos.fromStr(`[{"heading":"North","aiming":"East","position":{"x":"1","y":"2"}},{"heading":"West","aiming":"South","position":{"x":"7","y":"3"}}]`).getList.flow.map{i->Tanks.fromInfo(i)}.list.get(1).position.x)}
  .do{Debug#(Infos.fromStr(`[{"heading":"North","aiming":"East","position":{"x":"1","y":"2"}},{"heading":"West","aiming":"South","position":{"x":"7","y":"3"}}]`).getList.flow.map{i->Tanks.fromInfo(i)}.list.get(1).heading)}
  .do{Debug#(Try#{Infos.fromStr("[")}.context{"While deserialising Info from string"}.info!.getMsg.startsWith("While deserialising Info from string"))}
  .do{Debug#(Try#{Infos.fromStr("[")}.context{"While deserialising Info from string"}.info!.getMsg.contains("unterminated list"))}
  .do{Debug#(Try#{Tanks.fromInfo(Infos.fromStr(`{"heading":"North"}`))}.context{"While deserialising tanks from Info"}.info!.getMsg.startsWith("While deserialising tanks from Info"))}
  .do{Debug#(Try#{Infos.fromStr("[")}.map{i->i.str}.info.isEmpty)}
  .return{Void}}

//PRINT|{"heading":"North","aiming":"East","position":{"x":"1","y":"2"}}
//PRINT|True
//PRINT|False
//PRINT|2
//PRINT|7
//PRINT|West
//PRINT|True
//PRINT|True
//PRINT|True
//PRINT|False

"""); }/*--------------------------------------------
OMIT_END
END*/
}