package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_028Chapter04SerializingTanks {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Serialising Tanks

## Serialising Tanks

With our understanding from before, we can reimplement `Tanks` as follow:
```
Tanks: F[Direction,Direction,Point,Tank], FromInfo[Tank] {
  heading, aiming, point -> Tank:ToInfo, ToStr,OrderHash[Tank]{'self
    .heading: Direction -> heading;
    .aiming: Direction -> aiming;
    .info -> Infos.map(`heading`, heading, `aiming`, aiming,   `point`, point);
    .str  -> ...;//includes repr1...repr3
    .cmp t1,t2,m -> t1.heading <=> (t2.heading,//This looks atrocious
      m&&{t1.aiming <=> (t2.aiming, m&&{t1.point <=> (t2.point,m)})});
    .cmp t1,t2,m -> m.cmp({::.heading}.then{::.aiming}.then{::.point},t1,t2);
    //can we get the above instead?
    .hash -> heading.hash.hashWith(aiming.hash).hashWith(point.hash);
    };
  .fromInfo {map} -> Tanks#(
    Directions.fromInfo(map.get(`heading`)),
    Directions.fromInfo(map.get(`aiming`)),
    Points.fromInfo(map.get(`point`)),
    );
  }
```
As you can see, `Tank` is quite similar to `Point`.

#### Reading a `List[Tank]` from a file

Now that we have serialisable and deserialisable tanks, we can implement the type reading from file.
In chapter 3 we showed code
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
  mut .in: mut InputCursorNode,
  mut .read : List[Tank] -> Block#
    .let[Str] text= {this.in.text!}
    .let[Info] info= {Infos.fromStr(text)}
    .return {info.list.flow.map{i->Tanks.fromInfo(i)}.list};
  }
```

As you can see, the no-args `.read` method takes the text of the file, parse it as an `Info` and then parse that `Info` as a list of `Tank`s

You may be scratching your head about where this file comes from.
We are not specifying a actual operation like read the file called `` `input.txt` ``. Where is this file coming from?
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
  mut .image: Opt[Image];
  ...//many more methods for other kinds of files.
  }
```
- `sys.inputCursor` is the capability to observe the files intended as input.
- `sys.inputCursor#` gets the first input node.
- `sys.inputCursor# !` extracts the actual `InputCursorNode`, throwing error if no input has been provided yet.
- `this.in.text` then calls the text method. If the file has text, the optional will not be empty.
- `this.in.text!` we extract the content from the optional with `!`.

Note how this code simply leaks any kind of error anywhere it may raise.
There are three many kinds of error here:
- 1 Reading the string from file
- 2 Deserializing the string into an `Info`
- 3 Deserializing the info into a `List[Tank]`

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
    .andThen{i -> Try#{i.list.flow.map{i->Tanks.fromInfo(i)}.list}}
  }
```
> Note: could we have `Flow.tryMap`, and make it work also for `Action[mut T]`?  
> Also, should we have a variant of .andThen/.map that uses Try# on the argument, since it seems common?  

Instead of `.map` we now use `.andThen` + `Try#`.
The type is the same, but the error management is now very different.
There are three many points of error:
- 1 Reading the string from file
- 2 Deserializing the string into an `Info`
- 3 Deserializing the info into a `List[Tank]`

Those three kinds of errors are handled differently when calling `.read(fileName).run{..}`.
What errors are captured inside the `Info` of the `Action[List[Tank]]` returned by the `.read(fileName)` method?
What errors just leak out?
The errors from (1) are always handled by the action. This means that they always end up captured by the action `Info`.
The errors in 2 and 3 leak out when using `.map` and are captured when using `.andThen` + `Try#`.


> An interesting corner of design would be to offer some way to go from `Flow[Action[T]]` into `Action[List[T]]` ? or `Action[R]` with a transformation function on the flow?


Note that `ReadGame.read` body is `this.read(List.of("StartConfiguaration.txt"))!`,
thus the errors that we carefully separated in the second implementation ends up together again when we call the method `!` on the result of  `.read(fileName)`.
This causes all the errors to become observed bugs and to stop our application.

This is not always the desired behaviour. When writing larger applications it becomes important to distinguish which errors are recoverable situations (so that we can capture them into the action `Info`) and which  errors are observed bugs.
Using  `.map` or `.andThen` + `Try#` we can choose how to classify such details.

We can also add information to the error messages using code as below
```
    .andThen{s -> Try#{Infos.fromStr(s)}
      .context{`While deserializing Info from string`}}
    .andThen{i -> Try#{i.list.flow.map{i->Tanks.fromInfo(i)}.list}
      .context{`While deserializing tanks from Info`}}
```
Note how the indentation helps seeing the context text becoming part of the action.

#### Graduation
This is the end of chapter 4.
In those 4 chapters we used the tank game as an example on how to build simple behaviour.

We are now going to move forward, toward other interesting examples.



OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}