package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_025Chapter04Intro {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Errors are good!

# Chapter 4

In this chapter we will discuss errors in more detail.

A crucial environment where errors are important is data transmission.
The fundamental challenge of data transmission is binary data representation.
When data moves between representations, errors are possible and need to be managed.

While data transmission is the source of many errors and a great example to introduce error management, we will also see how errors are pervasive in many other aspects of programming.

Many activities, from sending an email to saving a document involves the transmission and storage of data. However, the underlying systems—whether networks or storage devices—do not handle Fearless objects and types directly. Instead, they operate using the most basic units of digital data: bits.

#### Understanding Bits and Bytes
A bit is the smallest unit of data in computing, with a possible value of either `0` or `1`.
Eight bits form a byte, which can represent values from `0` to `255`. This range is sufficient to encode single characters in various character sets, plus a few other special symbols/characters.
In this way, we can interpret a large sequence of bytes as a string of any length.
That is, it is easy to convert a string of text into a sequence of bits to store or transmit and vice versa, and the Fearless standard library offers easy ways to do this.

#### Serialisation and Deserialisation
- **Serialisation** is the process of transforming complex data structures into a linear sequence of bytes, enabling their storage or transmission.
- **Deserialisation** is the reverse process, where bytes are converted back into usable data structures.

#### Using Strings as Intermediaries
Since strings can be directly converted to bytes, they serve as an effective intermediary for serialisation. We can take complex objects and describe them using strings.
For example a `Tank` heading `North`, aiming `East`, located at coordinates `(10, 5)` could be serialised as the string `` `North, East, 10, 5` ``. This string captures the state of the tank in a format that can be easily converted into bits.

#### Drawbacks of (de)serialisation on strings
While using strings simplifies the process of converting data to bytes, it introduces two interconnected challenges:

- Ambiguity: The string `` `North, East, 10, 5` `` does not inherently explain what each part represents. Is `North` the direction the tank is heading or aiming?
- Deserialization complexity: While converting a tank into a string is easy, converting the string back into a tank object requires code to interpret each part correctly. 
This code would be specific for Tanks and will have to be rewritten for other kinds of data.

The solution is often to introduce an additional layer of abstraction that structures data before it is transformed into strings. This layer would act as a structured intermediary, organising data in a way that preserves the separation of each element while supporting a unified conversion process to and from strings.
First, complex objects are converted into this structured information format, that is then converted into strings, which in turn are converted into bits for storage or transmission.

Graphically, the general plan is as follows:

***Our data <--> unified representation format <--> string <--> bits***

When the whole infrastructure is completed, we would only need to worry about transforming our data to/from this unified representation. The other steps will simply reuse existing code.

#### Attempts toward an unified information format: Rigid vs Flexible representation of data.

What could an unified representation format be? We need a type that is flexible enough to represent any kind of information, but structured enough to avoid the drawbacks of strings.

- `Bool` is a very rigid type, it only contains two values; `True` and `False`.
- `Void` is even more rigid, it only contains the `Void` value.
- `Num` is more flexible, it contains a large amount of values.

However, those values are all numbers. Same for `Str`: it contains a mind bogglingly large amount of values, but they are all strings. As we discussed, interpreting a piece of a string as another kind of data is possible but error prone. Manually encoding and decoding data from strings is usually a bad idea.

- `Opt[E]` is the first truly flexible type we have seen: it can contain zero or one element of any fixed type `E`.
- `List[E]` is a little more flexible: it can contain any amount of elements of any fixed type `E`.
Using a `List[Str]`, a `Tank` could be represented as ``List#(`North`, `East`, `10`, `5`)``
This is much better than using strings! However, still unsatisfactory: The x and y coordinates come from the separate `Point` object, but are now flushed inside the `Tank` representation. That is, in this mindset every object needs to be represented as a flat set of attributes, while data is often composed of smaller units of existing data.
What if instead of using a `List[Str]` we used a list of something that contains itself?
For example
```
Info: {
  .msg: Str -> ``,
  .list: List[Info] -> {},
  }
```
Now an `Info` can contain both some message of type string and a list of more information.
We could then represent our tank as follow:
```
Info{ .list -> List#({.msg->`North`}, {.msg->`East`}, {.list->List#({.msg->`10`}, {.msg->`5`} )} )}
```

As usual, we can add factories to make objects easier to instantiate:
```
ToInfo: { .info: Info }//Strings and numbers implements ToInfo
Infos: {
  .msg(str: Str): Info -> {.msg -> str},
  .list(i1: ToInfo): Info -> {.list -> List#(i1.info)},
  .list(i1: ToInfo, i2: ToInfo): Info -> {.list -> List#(i1.info, i2.info)},
  ...//same for 3, 4 .. etc parameters
  }
```

Now we could represent our `Tank` as follows:

```
Infos.list(`North`,`East`,Infos.list(`10`,`5`))
```

However, we still do not know at a glance if the first element of the list represents the aiming direction.
We would like a list where every element is associated with a label.
That is a `Map[Str,Info]`.
If we add such a map component to `Info` we get the following.
```
Info:{
  .msg:Str->'',
  .list:List[Info]->{},
  .map:Map[Str,Info]->{}
  }
```
With that, we can represent our tank as follows:
```
Infos.map(`heading`,`North`,  `aiming`,`East`,  `point`,Infos.map(`x`,`10`,  `y`,`5`))
```
We could do ``Infos.map(`heading`,`North`,   `aiming`,`East`,   `x`,`10`,   `y`,`5`)``, but the corresponding mindset can cause issues.
The `x` and `y` coordinates come from the separate `Point` object, but are now flushed inside the `Tank` representation.

#### Serialisation and Deserialization: Simple with Info
In this section we discuss how to use `Info` in practice. While directly transforming text into complex objects like a list of tanks is possible, this approach can lead to unreadable and non-scalable code. This complexity arises because a tank object comprises various components like directions and points, and handling these in a single function would hinder code reusability for other data types.
That is, if we handle all those subcases internally in a single function, we would not be able to reuse that code if we have to read other kinds of data containing points, directions and tanks.

A much better solution is to decompose the serialisation process by creating dedicated functions for each component: one for `Direction`, one for `Point`, and another for `Tank`. Each of these functions handles its specific type and can be reused across different contexts where that type is involved. To do so, we will need some way so that the representation of an object (a `Point` for example) can be embedded inside the representation of another object (a `Tank`).

#### Using JSON and Info for Object Representation
JSON (JavaScript Object Notation) is a well-established format for encoding structured data into strings. It allows objects to be nested within each other, enabling complex data structures like a `Tank` containing `Point`s and `Direction`s to be seamlessly represented and manipulated.
An `Info` can be seen as a strict subset of JSON, tailored to be even more straightforward and focused on the essentials of data representation.
While full JSON is supported, in most cases using `Info` makes the code simpler and more reliable.

An `Info` can be one of the following:
- A simple string message.
- A list of Info values.
- A map from simple string keys to Info elements.
- An empty Info.
The operations `Info.str` and `Infos.fromStr` are provided to convert `Info` objects to JSON strings and to parse JSON strings as `Info` objects, respectively.

With an `Info` object named `myInfo`, you can access its content in several ways:
- `myInfo.msg` retrieves a string message if present.
- `myInfo.list` returns a list of Info objects.
- `myInfo.map` fetches a map of keys to Info values.

Notably, strings, lists and maps are three kinds of objects that can conceptually be empty.
Since `Info` can represent either a string, a list, or a map, only one of these properties can be not empty and hold data at any given time, reflecting the contained type.
There's also an empty `Info`, which returns the empty version of all three types, representing the absence of any data.
This structured approach to serialisation using `Info` not only simplifies data handling but also enhances the clarity and flexibility of your codebase, making it easier to manage and extend.

Similarly, we can use the type `Infos` to programmatically create `Info` objects using methods `Infos.msg(Str)`, `Infos.list(/*..elements..*|/)` and `Infos.map(/*..keys and values..*|/)` .
The empty `Info` object can be obtained using `Infos.empty` or just `{}`.

> NOTE: TODO: the current implementation of those methods is WRONG, and needs to return a singleton empty info object when an empty input is passed in. We probably want to also add an 'isEmpty' method to `Info`.



OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}