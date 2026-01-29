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
  .fromInfo(i) -> Tanks#(
    Directions.fromInfo(i.map.get(`heading`)),
    Directions.fromInfo(i.map.get(`aiming`)),
    Points.fromInfo(i.map.get(`point`)),
    ),
  .fromInfo{map} -> Tanks#(//with the new sugar
    Directions.fromInfo(map.get(`heading`)),
    Directions.fromInfo(map.get(`aiming`)),
    Points.fromInfo(map.get(`point`)),
    ),
  #(heading, aiming, point) -> Tank:ToInfo, ToStr,OrderHash[Tank]{'self
    .heading: Direction -> heading,
    .aiming: Direction -> aiming,
    .info -> Infos.map(`heading`, heading, `aiming`, aiming,   `point`, point),
    .str -> ...,//includes repr1...repr3
    <=>(o) -> o.heading<=>heading && {o.aiming <=> aiming} && {o.point <=> point},
    .hash(h)->h#(heading,aiming,point),
  }}
```
As you can see, `Tank` is quite similar to `Point`.

#### Reading a `List[Tank]` from a file

Now that we have serializable and deserializable tanks, we can implement the type reading from file.
In chapter 3 we showed code
```
Test: Main {sys -> Block#
  .let fs= {UnrestrictedIO#sys}
  .let game= ReadGame{fs}.read
  .return{ PrintGame{fs}.lines(50,game) }
```
Where we did not discuss how to write `ReadGame`.
Now we can show such code!
```
ReadGame: {//Why UStr paths? what reading files really do? what takes in input and why?
  mut .io: mut IO,
  mut .read: List[Tank] -> this.read(List.of("StartConfiguaration.txt"))! 
  mut .read(fileName: List[UStr]): mut Action[List[Tank]] -> this.io
    .accessR(fileName)             //mut ReadPath
    .readStr                       //mut Action[Str]
    .map{s -> Infos.fromStr(s)}    //mut Action[Info]
    .map{::list.flow.map{i->Tanks.fromInfo(i)}.list}
  }
```

As you can see, the no-args `.read` method delegates to a more reusable path -> action method.
This is a common way to handle reading external data: by defining a way to turn some kind of resource id into an action of data we can keep the abstraction levels separate and combine data sources when needed.
Consider the following alternative implementation:
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
> Also, some way to go from Flow[Action[T]] into Action[List[T]] ? or Action[R] with a transformation function on the flow?

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