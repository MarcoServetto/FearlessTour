package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_026Chapter04Enumerations {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Enumerations

#### `Direction`: an Enumeration

The `Direction` type we have seen before is an example of a general programming concept: An enumeration.

Enumerations usually offer many common features, like ordering, `.str`, `.info` and `.fromInfo`.
The standard library offers a simple way to add those common features to our enumerations: just implement the types `Enum` and `Enums` as shown below:

```
//Code we can write to declare our own enumerations
Directions: Enums[Direction]{ List#(North,East,South,West) }
Direction: Enum[Direction]{ Directions }
North: Direction{`North`}
East:  Direction{`East`}
South: Direction{`South`}
West:  Direction{`West`}
```
Or, if we want to also support our `.match` method:

```
Directions: Enums[Direction]{ List#(North,East,South,West) }
Direction: Enum[Direction]{
  Directions;
  .match[R:**](m: mut DirectionMatch[R]): R;
  }
DirectionMatch[R:**]{ mut .north: R; mut .east: R; mut .south: R; mut .west: R }
North: Direction{::.north; `North`}
East:  Direction{::.east;  `East` }
South: Direction{::.south; `South`}
West:  Direction{::.west;  `West` }
```

As you can see, we list our directions and we define the matcher listing the direction in method form.
Finally, we connect the direction-type with the direction-match-method and their string representation.

Admittedly, many other languages require significantly less code for enumerations.
However, in Fearless you can see how the whole mechanism works, and how simple method calls encode all the needed logic. In turn, this make it more natural to extend enumerations with other useful methods and features.

The code of `Enum[E]` and `Enums[E]` is actually quite simple and educational:

```
//Code from the Fearless standard library
FromInfo[E]: { .fromInfo(i: Info): E }

Enums[E]: FromInfo[E]{
  .list: List[E];
  .map: Map[Str,E] -> this.list.flow.mapping({::},{ ::; .key  e -> e.str; });
  .fromInfo i -> this.map.get(i.msg);
  }
Enum[E]: DataType[E,E]{
  .enums: Enums[E];
  .info -> Infos.msg(this.str);
   //enumerate gives pair index/element
  .index:Nat->this.enums.list.flow
    .enumerate.filter{{.e.str}->this.str == str}.get.i
  //alternative: .flatMapIndex give index,e
  .index:Nat->this.enums.list.flow
    .flatMapIndex{i,e->this.str == (e.str)? {Flows#i; .else-> Flows#}.get
  .cmp {.index}1, {.index}2, m -> index1 <=> (index2,m);
  .hash-> this.index;
  }
```
Thanks to `Enums[Direction]` we get a `.map` method mapping names to directions.
- Method `.map` returns a `Map[Str,Direction]` linking the string names of directions
 (`North`, `East`, etc.) to their corresponding `Direction` objects. This allows us to look up a Direction by its name. This is computed by using the method `.mapping`; taking a literal with a `.key` method and an `.elem` method, converting the flow elements into the key and element values for the newly created map. We also need to provide the identity function to *confirm* that strings implement `OrderHash`.
That is, the result of `.map` will be equivalent to the result of ``Maps#({::},`North`,North,  `East`,East,  `South`,South,  `West`,West)``.

>Note: .enumerate and .flatMapIndex do not exist yet.

Many enumeration types will have similar utility methods.
Note how methods `.list` and `.map` are fully deterministic. They take no arguments: the receiver contains no information (since it is a singleton) and there are no other parameters. This means that every time that code is executed, it would produce the same result.
Fearless will cache the result of such methods, so that the computation runs only one time.
This means that user code can call `Directions.map` many times without worrying about the performance cost of creating the map over and over again. The map is created only one time and then the system remembers it.

Finally, the `.fromInfo` method uses this map to convert an `Info` string back into a `Direction` object. It should find the direction's name in the map and return the associated `Direction`.

A map only connects some specific keys to an element.
Most possible key values will not have any associated element.
In the case of ``Map#(`North`,North,  `East`,East,  `South`,South,  `West`,West)`` only four specific strings have an associated `Direction`.
So, how to extract an element from a map if we have a string that may or may not be a valid key?

The `Map[K,E]` type from the standard library offers three different methods:
`.get`, `.opt`, and `.tryGet`.

> Note: .tryGet should be added to both Map/List/Set

Calling ``Map#(`North`,North,  `East`,East,  `South`,South,  `West`,West).opt(`North`)``
will result in `Opts#(North)`: an `Opt[Direction]` containing the `North` direction.
Calling ``Map#(`North`,North,  `East`,East,  `South`,South,  `West`,West).opt(`Nope`)``
will result in `Opt[Direction]`: an empty optional.

This is a general approach: when a method may not be able to return a result, we can return an optional value.
This is a minimum effort approach that works on a small scale, but does not provide the user of our code with much useful information.
Either the data is there, or is not. If the data is not there, we are not giving any hint of why it is not there.

Calling ``Map#(`North`,North,  `East`,East,  `South`,South,  `West`,West).get(`North`)`` will result in the `North` direction, but calling ``Map#(`North`,North,  `East`,East,  `South`,South,  `West`,West).get(`Nope`)`` will fail with an error.
That is, the code will stop running and the program will report that it failed in a certain code location because the key `Nope` is not in the set `North`, `East`, `South`, `West`.

Next we will see how to handle those errors.

OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}