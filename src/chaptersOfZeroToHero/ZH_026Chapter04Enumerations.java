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
Direction: Enum[Direction]{ .enums->Directions, }
North: Direction{`North`}
East:  Direction{`East`}
South: Direction{`South`}
West:  Direction{`West`}
```
Or, if we want to also support our `.match` method:

```
Directions: Enums[Direction]{ List#(North,East,South,West) }
Direction: Enum[Direction]{ .match[R:**](m: mut DirectionMatch[R]): R , .enums->Directions,}
DirectionMatch[R:**]{ mut .north: R, mut .east: R, mut .south: R, mut .west: R }
North: Direction{.match(m)->m.north, .str->`North`}
East:  Direction{.match(m)->m.east,  .str->`East`}
South: Direction{.match(m)->m.south, .str->`South`}
West:  Direction{.match(m)->m.west,  .str->`West`}
//alternatives
West:  Direction{.match::west,  .str->`West`}
West:  Direction{::west,  `West`}//if our syntax allows to override the only abstract method with xx arguments
Even better, we could exclude any named method already, so the last unnamed abstract meth with n arguments is name-inferred. This would also allow to add new abstract methods while implementing old abstract ones
```


As you can see, we list our directions and we define the matcher listing the direction in method form.
Finally, we connect the direction-type with the direction-match-method and their string representation.

Admittedly, many other languages require significantly less code for enumerations.
However, in Fearless you can see how the whole mechanism works, and how simple method calls encode all the needed logic. In turn, this make it more natural to extend enumerations with other useful methods and features.

The code of `Enum[E]` and `Enums[E]` is actually quite simple and educational:

```
//Code from the Fearless standard library
FromInfo[T]: { .fromInfo(i: Info): T }

Enums[E]: FromInfo[E]{
  .list: List[E],
  .map: Map[Str,E] -> this.list.flow.mapping({::},{
    .key(e) -> e.str,
    .elem(e) -> e,
    })
  .fromInfo(i) -> this.map.get(i.msg),
  }
Enum[E]: ToStr, ToInfo, OrderHash[E]{
  .enums: Enums[E],
  .info -> Infos.msg(this.str),
  .index: Nat -> this.enums.list.enumerate.filter{ei->ei.e.str == (this.str)}.get.i
   //enumerate gives pair index/element
  .index:Nat->this.enums.list.enumerate.filter{{e,i}->e.str == (this.str)}.get.i//potential new syntax sugar
  <=>(other)->this.str<=>(other.str),
  <=>{str}->this.str<=>str,//LOL?
  <=>::str<=>(this.str),//LOL?
  <=>(other)->this.index<=>(other.index),//or this version with index
  .hash(h)->h#(this.str),//or on the index
  .hash::#(this.index),//like this?
  }
```
-------------
>variant with AutoMatch[E] and AutoToStr and generalized meth name inference
and allowing nested types to be instantiated via name if context shows no capture
```
Direction: Enum[Direction]{ Directions, .match[R:**](m: mut DirectionMatch[R]): R}
DirectionMatch[R:**]{ mut .north: R, mut .east: R, mut .south: R, mut .west: R }
Directions: Enums[Direction], AutoMatch{ List#(
  North: Direction{},
  East:  Direction{},
  South: Direction{},
  West:  Direction{},
) }
//for n names, 7+n Direction, 2*n names, n mut:R
FromInfo[T]: { .fromInfo(i: Info): T }

Enums[E]: FromInfo[E]{
  .list: List[E],
  .map: Map[Str,E] -> this.list.flow.mapping({::},{
    .key(e) -> e.str,
    .elem(e) -> e,
    })
  .fromInfo(i) -> this.map.get(i.msg),
  }
Enum[E]: AutoStr[], ToInfo, OrderHash[E]{
  .enums: Enums[E],
  .info -> Infos.msg(this.str),
  .index:Nat->this.enums.list.enumerate.filter{{e,i}->e.str == (this.str)}.get.i
  <=>(other)->this.index<=>(other.index),
  .hash(h)->h#(this.index),
  }
```
>AutoStr[`.a.b.c`] would try to generate a method .str printing classname[a=this.a, b=..]
for all and only the methods existing in 'this'.
Note how this would go in stack overflow if .str is in the list.
AutoMatch could try to implement a method .match with the lowecase version of the name of the type and simply propagate arguments, so
.match(m)->m.name
.match(m,a,b)->m.name(a,b)
Note: it will not implement .match if the method with the right name/arity is not present

----------------

Thanks to `Enums[Direction]` we get a `.map` method mapping names to directions.
- Method `.map` returns a `Map[Str,Direction]` linking the string names of directions
 (`North`, `East`, etc.) to their corresponding `Direction` objects. This allows us to look up a Direction by its name. This is computed by using the method `.mapping`; taking a literal with a `.key` method and an `.elem` method, converting the flow elements into the key and element values for the newly created map. We also need to provide the identity function to *confirm* that strings implement `OrderHash[Str]`.
That is, the result of `.map` will be equivalent to the result of ``Maps#({::},`North`,North,  `East`,East,  `South`,South,  `West`,West)``.

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