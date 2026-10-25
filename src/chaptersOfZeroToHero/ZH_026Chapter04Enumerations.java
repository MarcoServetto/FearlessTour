package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
final class ZH_026Chapter04Enumerations{
/*START
--CHAPTER-- Chapter 4
--SECTION-- Enumerations

#### `Direction`: an Enumeration

The `Direction` type we have seen before is an example of a general programming concept: an enumeration.

Enumerations usually offer many common features, like ordering, `.str`, `.info` and `.fromInfo`.
The standard library offers a simple way to add those common features to our enumerations: just implement the types `Enum` and `Enums` as shown below:

```
Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .close->this; .close->::;
  }
North: Direction{}
East:  Direction{}
South: Direction{}
West:  Direction{}
```

With this alone, `Directions.list` is the four directions in declaration order, `North.index` is `0`
and `West.index` is `3`, `North < East` is `True`, `Directions.map.get("North")` is `North`, and
`Directions.fromInfo(North.info)` round-trips back to `North`:

OMIT_START
-------------------------*/@Test void withoutMatch (){ run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.Debug as Debug;
use base.Enums as Enums;
use base.Enum as Enum;
Test: Main{sys-> Block#(
  Block#(
    Debug#(Directions.list.size),
    Debug#(North.index),
    Debug#(West.index),
    Debug#(North < East),
    Debug#(Directions.map.get("North"))
    ),
  Debug#(Directions.fromInfo(North.info)),
  Void
  )}

Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .close->this; .close->::;
  }
North: Direction{}
East:  Direction{}
South: Direction{}
West:  Direction{}
//PRINT|4
//PRINT|0
//PRINT|3
//PRINT|True
//PRINT|North
//PRINT|North
"""); }/*--------------------------------------------
OMIT_END

Or, if we want to also support our `.match` method:

```
DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Direction: Enum[Direction]{
  .enums->Directions;
  read .match[R:**](m: mut DirectionMatch[R]): R;
  read .close->this; .close->::;
  }
North: Direction{::.north}
East:  Direction{::.east}
South: Direction{::.south}
West:  Direction{::.west}
```

Now `North.match(DirectionMatch[Str]{ .north->"n"; .east->"e"; .south->"s"; .west->"w"; })` is `"n"`;
every value still gets `.list`/`.map`/`.index`/`.info`/`.fromInfo` for free from `Enums`/`Enum`, on top
of its own `.match`:

OMIT_START
-------------------------*/@Test void withMatch (){ run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.Str as Str;
use base.Debug as Debug;
use base.Enums as Enums;
use base.Enum as Enum;
Test: Main{sys-> Block#(
  Debug#(North.match(DirectionMatch[Str]{ .north->"n"; .east->"e"; .south->"s"; .west->"w"; })),
  Void
  )}

DirectionMatch[R:**]: { mut .north: R; mut .east: R; mut .south: R; mut .west: R; }
Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .match[R:**](m: mut DirectionMatch[R]): R;
  read .close->this; .close->::;
  }
North: Direction{::.north}
East:  Direction{::.east}
South: Direction{::.south}
West:  Direction{::.west}
//PRINT|n
"""); }/*--------------------------------------------
OMIT_END

As you can see, we list our directions and we define the matcher listing the directions in method form.
Finally, we connect the direction-type with the direction-match-method.
The string representation of each direction is the simple name of its type: `North.str` is `"North"`.

Admittedly, many other languages require significantly less code for enumerations.
However, in Fearless you can see how the whole mechanism works, and how simple method calls encode all the needed logic. In turn, this makes it more natural to extend enumerations with other useful methods and features.

The code of `Enum[E]` and `Enums[E]` is actually quite simple and educational:

```
//Code from the Fearless standard library
FromInfo[T]: { .fromInfo(i: Info): T }

Enums[E]: FromInfo[E]{
  .list: List[E];
  .strBy: ToStrBy[E];
  .map: Map[Str,E] -> this.list.flow.mapping({::},{.key e->(this.strBy#e).str; .elem e->e;}).as{::};
  .indexMap: Map[Str,Nat] -> this.list.flow.fold(
    {Maps#[Str,Str,Nat]{::}.as{::}},
    {acc,e->acc.with((this.strBy#e).str,acc.size).as{::}}
    );
  .fromInfo i -> this.map.get(i.getMsg);
  }
Enum[E]: DataType[E,E], Named, CaptureFree{
  read .enums: Enums[E];
  .str -> this.name.split "." .get 1;
  .imm -> this.close;
  .info -> Infos.msg(this.str);
  read .index: Nat -> this.enums.indexMap.get(this.str);
  read .close(t: read E): read Enum[E];
  .cmp t0,t1 -> this.close(t0).index <=> (this.close(t1).index);
  .hash -> this.index;
  }
```
A type implementing `Named` gets `.name`: the qualified name of the type of the object, like `"pkg.North"`.
`Enum[E]` takes `.str` from `.name`, keeping the part after the dot: `"North"`.
`Enum[E]` is `CaptureFree`, so inside `Enum[E]` the receiver `this` is `imm`, and `.imm -> this.close` returns the value itself.
For the same reason, the `.close` of `Direction` is `read .close->this`: an `imm` `this` cannot be the result of `mut .close: mut Direction`, offered by `Pipe[Direction]`, so that method stays abstract, and a `Direction` value is never `mut`.

Thanks to `Enums[Direction]` we get a `.map` method mapping names to directions.
- Method `.map` returns a `Map[Str,Direction]` linking the string names of directions
 (`North`, `East`, etc.) to their corresponding `Direction` objects. This allows us to look up a `Direction` by its name. This is computed by using the method `.mapping`; taking a literal with a `.key` method and an `.elem` method, converting the flow elements into the key and element values for the newly created map.
`E` is a bare generic parameter, so it carries no methods of its own - not even `.str` - which is why
`Enums[E]` also needs a `.strBy: ToStrBy[E]` witness: a function turning any value of type `E` into
something with a `.str`. `Directions.strBy -> {::};` says that witness is just the identity, which is
valid precisely because every concrete `Direction` value already has its own `.str`.
That is, the result of `.map` will be equivalent to the result of `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West)`.

Many enumeration types will have similar utility methods.
Note how methods `.list` and `.map` are fully deterministic. They take no arguments: the receiver contains no information (since it is a singleton) and there are no other parameters. This means that every time that code is executed, it produces the same result.
Fearless will cache the result of such methods, so that the computation runs only one time.
This means that user code can call `Directions.map` many times without worrying about the performance cost of creating the map over and over again. The map is created only one time and then the system remembers it.

Finally, the `.fromInfo` method uses this map to convert an `Info` string back into a `Direction` object. It should find the direction's name in the map and return the associated `Direction`.

A map only connects some specific keys to an element.
Most possible key values will not have any associated element.
In the case of `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West)` only four specific strings have an associated `Direction`.
So, how can we extract an element from a map if we have a string that may or may not be a valid key?

The `Map[K,E]` type from the standard library offers two different methods:
`.get` and `.opt`.

Calling `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).opt("North")`
will result in `Opts#(North)`: an `Opt[Direction]` containing the `North` direction.
Calling `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).opt("Nope")`
will result in `Opt[Direction]`: an empty optional.

This is a general approach: when a method may not be able to return a result, we can return an optional value.
This is a minimum effort approach that works on a small scale, but does not provide the user of our code with much useful information.
Either the data is there, or it is not. If the data is not there, we are not giving any hint of why it is not there.

Calling `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).get("North")` will result in the `North` direction, but calling `Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).get("Nope")` will fail with an error.
That is, the code will stop running and the program will report that it failed in a certain code location because the key `"Nope"` is not in the set `"North"`, `"East"`, `"South"`, `"West"`.

Next we will see how to handle those errors.

OMIT_START
-------------------------*/@Test void lookups(){ run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.Debug as Debug;
use base.Enums as Enums;
use base.Enum as Enum;
use base.Maps as Maps;
use base.Infos as Infos;
use base.Str as Str;
Test: Main{sys-> Block#
  .do{Debug#(Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).opt("North").str{::})}
  .do{Debug#(Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).opt("Nope").str{::})}
  .do{Debug#(Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).get("North"))}
  .do{Debug#(Directions.map.size)}
  .do{Debug#(Directions.map.opt("Nope").orValue(North))}
  .do{Debug#(Directions.map.flow{k,e->k}.list.str{::})}
  .do{Debug#(Directions.indexMap.get("South"))}
  .do{Debug#(South.index)}
  .do{Debug#(South.info.str)}
  .do{Debug#(Directions.fromInfo(Infos.msg("West")))}
  .do{Debug#(West > South)}
  .do{Debug#(Lists#(West,North,South).flow.sort{::}.list.get(0))}
  .return{Void}}

Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .close->this; .close->::;
  }
North: Direction{}
East:  Direction{}
South: Direction{}
West:  Direction{}
//PRINT|Opt[North]
//PRINT|Opt[]
//PRINT|North
//PRINT|4
//PRINT|North
//PRINT|[North, East, South, West]
//PRINT|2
//PRINT|2
//PRINT|"South"
//PRINT|West
//PRINT|True
//PRINT|North
"""); }/*--------------------------------------------
-------------------------*/@Test void mapGetMessage(){ run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.Debug as Debug;
use base.Enums as Enums;
use base.Enum as Enum;
use base.Maps as Maps;
use base.Try as Try;
Test: Main{sys-> Block#
  .do{Debug#(Try#{Maps#({::},"North",North,  "East",East,  "South",South,  "West",West).get("Nope")}.info!.getMsg)}
  .return{Void}}

Directions: Enums[Direction]{
  .list -> Lists#(North,East,South,West);
  .strBy -> {::};
  }
Direction: Enum[Direction]{
  .enums->Directions;
  read .close->this; .close->::;
  }
North: Direction{}
East:  Direction{}
South: Direction{}
West:  Direction{}
//PRINT|Map.get: Tried to get key Nope that is not contained in this map.
//PRINT| Consider using `Map.opt` to properly handle the failure case.
"""); }/*--------------------------------------------
OMIT_END
END*/
}
