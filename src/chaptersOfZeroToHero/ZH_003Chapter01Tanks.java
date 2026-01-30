package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_003Chapter01Tanks {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Basic code reuse

### How to Avoid Repetitive Code in Fearless
You may already have noticed  that even relatively simple programs
have the potential to be very long.
Programs which  you may use daily (such as Steam, Zoom, TikTok, and Instagram)
are composed of millions of lines of code. Some programming languages
force the user to write very repetitive code; either by literally
repeating the same text over and over again, or by repeating similar but
slightly different code. Repetitive code is bad and Fearless has ways to
avoid repeated code and promote **code reuse**.

We have already seen three ways fearless avoids repetitive and redundant code.

- One way is simply defining and calling methods: by calling methods we can
avoid repeating their body over and over again. Since methods can call other
methods, this can produce a massive reduction in code.
- Another way is to implement existing types and to inherit their methods.
This imports/reuses all of the methods from the implemented types without
mentioning them one by one.
In the `Direction` example, `.reverse` is implicitly and automatically
inherited by all of the directions.
- We have also seen inference; where we can omit type informations that
are clear from the context. For example, when implementing method `Direction.turn` in 
`North` we could write `.turn -> East` instead of `.turn: Direction -> East`.
The return type `Direction` is clear from the context.

We now introduce the concept of 
**syntactic sugar**.
As for method calls, method inheritance and
type inference, also syntactic sugar is designed to avoid redundant code.

Syntactic sugar allows representing specific well known coding patterns using more concise and more readable syntax.

Syntactic Sugar does not change the meaning, it just provides a shorter way to write the exact same thing. Think of it like a contraction in English: "don't" instead of "do not". It is shorter, but the underlying meaning is identical.

We will now see how a combination of syntactic sugar and inference can make the
code for `Direction` even more compact.
The only abstract method in `Direction` is `.turn`,
so when implementing direction it is obvious that we want to implement `.turn`.
In this way, the syntactic sugar allows us to write the following, shorter version of the code we have seen before.
That is, to implement `Direction` we must implement `.turn`.
Fearless knows this, thus in this case we can omit `.turn->`.
Of course this is a more general concept, and there
are many other situations where we can omit the method name (and sometimes also the arrow)
when we implement a method.

-------------------------*/@Test void dirCompact() { run("""
Direction: {
  .turn: Direction;
  .reverse: Direction -> this.turn.turn;
  }
North: Direction { East  }
East : Direction { South }
South: Direction { West  }
West : Direction { North }
"""); }/*--------------------------------------------


### Understanding the code above

 1. `North: Direction { .turn -> East; }` is a type declaration.
 2. `North: Direction { East }` is a more compact form of the
same type declaration.
 3. `.turn: Direction` is a method declaration.
 4. `.turn -> East;` is a method implementation.
 5. `East` is an example of an object literal expression.
 6. `North.turn` is an example of a method call expression.
 7. `this` is an example of a parameter.
 8.  `North` and `East` are valid object literals because they have
no abstract methods.

We will later see that both method calls and object literals
can be much more involved than the ones shown in our examples so far.

### Object literal: inheritance plus syntactic sugar
Object literals work as shortcuts to avoid repeating the full code they represent.
Using literals as names referring to a type declaration is another way to reuse code. When writing `North` we are making heavy use of syntactic sugar.
`North` actually stands for `SomeName147: North {}`, where `SomeName147` is
some name chosen by the syntactic sugar to be different from any other name present anywhere else in the code.

Thus, when writing `North` we are declaring a version of `North`, called for example `SomeName147` that inherits all of the methods of `North` and does not add any new methods (the empty curly brackets `{}`).
The type declaration `SomeName147: North {}` produces an object of type `SomeName147` when evaluated.
Since `SomeName147` implements `North`, any object of type `SomeName147` is also of type `North`.

In this way, the object literal `North` evaluates into a standard object of type `North`, with all its defined methods.

### Example: Tank with turret
Now that we have the abstract type `Direction` we can make a simple
`Tank` object. This tank will have two Directions;
- `.heading`: the direction the tank is moving, and
- `.aiming`: the direction the tank gun is aiming.


-------------------------*/@Test void tankBase() { run("""
//OMIT_START
Direction: {
  .turn: Direction;
  .reverse: Direction -> this.turn.turn;
  }
North: Direction { East  }
East : Direction { South }
South: Direction { West  }
West : Direction { North }
//OMIT_END
Tank: {
  .heading: Direction;
  .aiming: Direction;
  }
TankNN: Tank{ .heading-> North; .aiming-> North;}
TankNE: Tank{ .heading-> North; .aiming-> East; }
TankNS: Tank{ .heading-> North; .aiming-> South;}
TankNW: Tank{ .heading-> North; .aiming-> West; }
TankEN: Tank{ .heading-> East;  .aiming-> North;}
/* ... 16 cases in total! */
TankWW: Tank{ .heading-> West;  .aiming-> West; }
"""); }/*--------------------------------------------


As you can see, while it is possible to manually list all sixteen cases as valid Fearless code,
the development process is boring, repetitive and error prone:
it's incredibly easy to make a typo. What if we accidentally include `.aiming-> North`
five times instead of four?

Would not it be better if we could just ask for a Tank with a specific heading and aiming direction when we need one?
We can create a `Tank` maker by defining a type `Tanks` whose job is to create `Tank` objects for us.
We give it the details (heading and aiming), and it gives us back the specific `Tank` object we need.

> This pattern of having one type create instances of another is common.
> Naming the maker type by pluralising the product type (`Tanks` for `Tank`) is a concise Fearless convention.

-------------------------*/@Test void tanks1() { run("""
//OMIT_START
Direction:{ /*..as before..*/ .turn: Direction}
//OMIT_END
Tanks: { .of(heading: Direction, aiming: Direction): Tank ->
  Tank: { .heading: Direction -> heading; .aiming: Direction -> aiming; }
  }
"""); }/*--------------------------------------------

As you can see, we have moved the declaration for `Tank` inside
of a method body. Let's break down this piece of code carefully.

#### Method parameters: Providing input
Look at the part inside the parentheses: `(heading: Direction, aiming: Direction)`.
This declares parameters for the `.of` method. Parameters are how we pass information into a method when we call it.
`heading: Direction` declares a parameter named `heading`. The 
`: Direction` part specifies that whoever calls `.of` must provide a `Direction` for this parameter.
`aiming: Direction` similarly declares an `aiming` parameter, which also must be a `Direction`.
You've already seen the implicit `this` parameter. Parameters listed in parentheses like this are explicit parameters - we give them names and types directly.

The `.of` method expects two inputs: a `Direction` for the `heading` and a `Direction` for the `aiming`.
Directly after the parenthesis and before the arrow `->` we can see `: Tank`.
This is the **return type** and it tells us the result must be a `Tank` object.

#### The method body: Creating the tank

Now look at the part after the arrow `->`:
```
Tank: { .heading: Direction -> heading; .aiming: Direction -> aiming; }
```

**This is the method body:** the code that executes when `Tanks.of` is called. It defines what the method does.
What does `Tank: { ... }` mean here, inside a method body? It looks like our earlier `Tank` type definition,
but it is now also an object literal.
In addition of defining the `Tank` type, it also serves as a template for `Tank` objects.
The execution of `Tanks.of` will evaluate this object literal and thus return
a `Tank` object customized with `heading` and `aiming`.

**Using the parameters:** Notice how `heading` and `aiming` (the parameter names) appear inside this object definition:
- `.heading: Direction -> heading` means:
This new `Tank`'s `.heading` method will return the value
that was passed in as the `heading` parameter.

- `.aiming: Direction -> aiming` means: 
This new `Tank`'s `.aiming` method will return the value
that was passed in as the `aiming` parameter.

**Capturing values:** The Tank object returned by `.of` **captures** (or remembers) the specific `heading` and `aiming` values
*that were provided when `.of` was called.
*If we call `Tanks.of(North, East)`, the object created will be a `Tank` heading `North` and aiming
*`East`. If we call `Tanks.of(South, West)`, it will be a `Tank` heading `South` and aiming `West`.
This allows us to create `Tank` objects with custom, specific states based on the inputs we give
to the `.of` method. We have moved from having only a few fixed `Direction` objects to being able to create many
different `Tank` objects, each remembering its own specific heading and aiming.

To summarize, `Tank: { ... -> heading, ... }` is a form of object literal expression;
it's a way to define the structure of a type and forge an object of that type in one step,
often using captured parameter values to customize it.

#### Method Declaration Syntax 
Methods can have as many parameters as we want.
Syntactically, explicit method parameters are defined inside
of round brackets.
When there are no explicit parameters, 
these brackets can be optionally omitted / left out.
For example, the methods `.turn` and `.reverse` take no explicit parameters, so before we omitted the parenthesis.
These same methods could equivalently be declared as `.turn()` and `.reverse()`.
To call the method `.turn` twice we showed the syntax `North.turn.turn`
but we could have equivalently called it with syntax 
`North.turn().turn()`.
The syntactic sugar of Fearless allows us to include or omit most
empty brackets.

This newly shown method can be called with syntax:
`Tanks.of(North, East)`
Here `Tanks` is the first implicit parameter and it is called **the receiver**.
The other are provided after the method name in parenthesis.

The syntax `.of(heading: Direction, aiming: Direction): Tank`
defines a method called `.of` with parameters `heading` and `aiming`.
The parameters must be kinds of `Direction` and the method must return a `Tank` object.
Parameters allow methods to take input.
In this case, our `Tanks.of` method requires us (the developer) to specify the initial `heading` and `aiming` of the tank we want to create.

**Example questions:**
- What is the receiver of the method call `North.turn`? It is `North`.
- What is the receiver of the method call `North.turn.turn`? It is `North.turn`.

That is, a receiver can be any expression, not just an object literal.

- What is the first parameter of the method call `Tanks.of(North.reverse,East)`? It is `North.reverse`.

That is, also method parameters can be any expressions.

#### English to Fearless Conversion
Fearless code can be understood by aligning over it some natural language.
Consider the following example where we will describe some desired features of our `Tanks` code in English and then show how they can be
implemented in Fearless code.
- **English version**
> *We would like the `Tanks.of` method to create a `Tank` object.*  
> *This `Tank` will have two methods: `.heading` and `.aiming`.*  
> *`Tank.heading` will return the direction the `Tank` is heading;*  
> *and `Tank.aiming` will return the direction the `Tank` is aiming.*  

- **Fearless version**
`````
  Tanks: { .of(heading: Direction, aiming: Direction): Tank ->
    Tank: { .heading: Direction -> heading; .aiming: Direction -> aiming }
    }
`````
`Tanks.of` takes two directions (`heading` and `aiming`) and returns a
newly-created Tank object. The second line of our Fearless code above
does this by using a type declaration for a type `Tank`
inside the method body. 

You may think that declaring a new type inside of the method body
contradicts the general idea that method bodies must be expressions.
However, an object literal expression is just a special kind of type declaration.
Before we have directly used type names, like `North`, as a object literals.
As we discussed, the object literal `North` is desugared into`SomeName147: North {}`.
All object literals are type declarations. Some object literals do not look like
type declarations because of the sugar allowing to omit `SomeName147:` and 
because of the general rule that empty parenthesis like `{}` can be omitted.

This use of type declarations as objects is interesting because we
can create a new kind of object, an object able to see / capture the method parameters.
In this way, objects can have a custom, useful state.
Up to now we have worked with just a finite set of objects:
the four directions.
Using capturing, we can create more objects, and we can compose objects
to obtain new objects.
For example, we could define a `Platoon` object containing many individual `Tank` objects.

In the code above we defined `Tank` directly in the `Tanks.of` method.
In this way, the only way to create `Tank` objects is to call `Tanks.of`.
Alternatively, we can keep the `Tank` declaration outside and use
inheritance as shown below:
-------------------------*/@Test void tanks2() { run("""
//OMIT_START
Direction:{ /*..as before..*/ .turn: Direction}
North:Direction{North}
//OMIT_END

Tank: { .heading: Direction; .aiming: Direction;}
Tanks: { .of(heading: Direction, aiming: Direction): Tank->
  MadeTank: Tank { .heading -> heading; .aiming -> aiming;}
  }
TankNN: Tank {.heading -> North; .aiming -> North;}
"""); }/*--------------------------------------------
In this way we have more freedom to create `Tank`s:
via the `Tanks.of` method or via a top level declaration, like `TankNN`.

To do so, we introduced the name `MadeTank`,
to indicate tanks originating from that point in the code.
The name `MadeTank` is not very useful, we will probably never want to
talk only about tanks made with the `Tanks.of` method,
so we can rely on the sugar and type inference to chose a name for us and to infer that the literal we are creating is extending `Tank`.
In this case, the name for our literal is going to be some fresh name
that never appears anywhere in the code.
That is, the code below is an equivalent but shorter version of the code above.
-------------------------*/@Test void tanks3() { run("""
//OMIT_START
Direction:{ /*..as before..*/ .turn: Direction}
North:Direction{North}
//OMIT_END
Tank: { .heading: Direction; .aiming: Direction;}
Tanks: { .of(heading: Direction, aiming: Direction): Tank->
  {.heading -> heading; .aiming -> aiming}
  }
TankNN: Tank {.heading -> North; .aiming -> North;}
"""); }/*--------------------------------------------

In practice, while coding in Fearless, most literals will rely on
inference and will be
written just as `{..}`.

That is, there are three different kinds of object literals:
- **Typed object literal**
  `North` is an example of a typed object literal. The type `North` is used to (directly) build the literal.
- **Bare object literal**
  `{.heading -> heading; .aiming -> aiming}` as we just seen is a bare object literal.
  It is bare because it requires the inference to infer its type from the context.
- **Named object literal**
  `MadeTank: Tank { .heading -> heading; .aiming -> aiming;}`<br/> 
  and `Tank: { .heading: Direction -> heading; .aiming: Direction -> aiming }`, as we have seen before
  are named object literals: we do explicitly chose the name for this new type we are declaring.
  It is the most complete form of literal; it is a top level declaration that doubles as an object creation. 
 
#### The three kinds of expressions, revisited.

We have now seen more examples for the three kinds of expressions:

- Parameters: `this`, `heading`, `aiming`
- Method calls: `North.turn`, `Tanks.of(North,East)`, `Tanks.of(North,East.reverse)`
- Object literals `North`, `East`, `Tank: { ... -> heading, ... }`, `{..}`

Method bodies are expressions, so any method body will be exactly one expression. Expressions can have sub expressions:
`Tanks.of(North,East.reverse)` has sub expressions
`Tanks`, `North` and `East.reverse`.  In turn `East.reverse` has sub-expression `East`.

Object literal expressions are also type declarations, and
they can internally contain method declarations.

### Declarations and expressions

In Fearless there are two contexts: declarations and expressions.
- When writing a type name into the declaration context, we are using it as a type, as in `.turn: Direction`.
- When writing a type name into an expression, we are using it as an object literal, as in `.turn -> East`.
This is because the extended form is `SomeName198:East{}`. 
That is, we are implicitly using the type `East` as a part of an anonymous type declaration.
- Methods are declared, and have (sub-)expressions inside their bodies
- Expressions can be object literals
- Object literals are type declarations and can have method declarations.

As you can see, the two contexts are interleaved inside each other.

#### Method names
At this point you must have noticed that all the method names we have show
start with `.`; and you may be wondering why the odd choice.
The `.` allows the computer to separate method names from other kinds of names;
for example in `North.turn` or `Tanks.of` it is clear where the type
name finishes and the method name starts.
Fearless has two kinds of method names:

- names starting with exactly one `.` symbol and continuing with a lowercase
letter and any number of letters and numbers, and
- names composed exclusively of a non empty sequence of operator symbols.

The full list of operator symbols is:
```
  ! ~ # & ^ + - * / < > =
```

That is, the following is a list of valid and invalid method names:

```
.foo  #  ==  ++  <=  .bar23  <#--  <+  //valid
.foo+  +bar  a=b  zoo  <hello>  .+>  : = //invalid
```
> Yes, symbol `=` is a little special and it is invalid as an operator on it's own.

In turn, this means that the code below is syntactically valid
```
Bar#(Add|-)
```
Adding spaces around all tokens this would look as follows:
```
Bar # ( Add |- )
```
This is a call of the method called `#` on the receiver `Bar`, and the single parameter is a call of the method called `:-` on the receiver `Add`.
Method `|-` takes zero parameters.

On the other side, parameter names start with a lower-case letter, and
type names mostly start with an upper-case letter.
The rules for valid type names are a little more involved,
and we will discuss them in detail later.

While the code above works fine, we think that using `.of` in this way is
verbose and distracting: is `.of` the right method name?
Conceptually we just want to do `Tanks`, go!!! do your thing! be!

It is very common for Fearless types to have a method that is the 
**most crucial method** of that type.
As a convention, we use the method called `#` for that role.
The operator `#` is much more compact to call than the method `.of`.

Rewriting our last code example using `#` we get the following:

-------------------------*/@Test void tanksHash() { run("""
Direction: {
  .turn: Direction;
  .reverse: Direction -> this.turn.turn;
  }
North: Direction { East  }
East : Direction { South }
South: Direction { West  }
West : Direction { North }

Tank: {.heading: Direction; .aiming: Direction; }
Tanks: { #(heading: Direction, aiming: Direction): Tank ->
  { .heading -> heading; .aiming -> aiming; }
  }
"""); }/*--------------------------------------------

Note how pretty much nothing has changed.
We just declared the method to be called `#` instead of `.of`.

We could have renamed into `#` also the method `Direction.turn`,
but we chose not to: 
in our mental model we do not think that turning is the main thing we do with directions.
When coding it is important to distinguish the intended/ideal state of the code from the current incomplete version that we are working with, and to name concepts in function of such ideal state and not the current sorry version of it.

We can now consider adding some methods to `Tank`:
for example the capacity of turning the turret!
To do so, we only need to update the code of the type declaration for `Tank`:
-------------------------*/@Test void tanksHashUse() { run("""
//OMIT_START
Direction: {
  .turn: Direction;
  .reverse: Direction -> this.turn.turn;
  }
Tanks: { #(heading: Direction, aiming: Direction): Tank -> { .heading -> heading; .aiming -> aiming; } }

//OMIT_END
Tank: {
  .heading: Direction;
  .aiming: Direction;
  .turnTurret: Tank-> Tanks#(this.heading, this.aiming.turn);
}
"""); }/*--------------------------------------------
As you can see we declare a new method `.turnTurret`
that returns a `Tank` with the turned turret.
We do this by calling `Tanks#` with the current heading
direction and the result of turning the current aiming direction.
We can call this method as follows:

`Tanks#(North,East).turnTurret`

This will reduce as follows:
 
<table>
  <tr>
    <td>
      <pre><code>Tanks#(North, East).turnTurret</code></pre>
    </td>
    <td>Call <code>Tanks#</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>Tank{.heading ->North; .aiming ->East }.turnTurret</code></pre>
    </td>
    <td>Call <code>Tank.turnTurret</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>
Tanks#(
  Tank{.heading ->North; .aiming ->East }.heading,
  Tank{.heading ->North; .aiming ->East }.aiming.turn
  )
      </code></pre>
    </td>
    <td>Call <code>.heading</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>
Tanks#(
  North,
  Tank{.heading ->North; .aiming ->East }.aiming.turn
  )
      </code></pre>
    </td>
    <td>Call <code>.aiming</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>
Tanks#(
  North,
  East.turn
  )
      </code></pre>
    </td>
    <td>Call <code>East.turn</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>Tanks#(North, South)</code></pre>
    </td>
    <td>Call <code>Tanks#</code></td>
  </tr>
  <tr>
    <td>
      <pre><code>Tank{.heading ->North; .aiming ->South }</code></pre>
    </td>
    <td>Final result</td>
  </tr>
</table>

It is important to learn to visualise how the code reduces in your mind, so that you can predict code behaviour.
Note how we wrote `Tank{.heading -> North; .aiming -> South }`.
- Should we just write `{.heading ->North; .aiming ->South }` and rely more on the inference?
- Should we write `Anon27: Tank{.heading -> North; .aiming -> South }` and put all the object literal explicitly?

Inference works on source code: the code we write.
Code under reduction is not source code, but just a tool for us to understand the code behaviour. Since it is just a tool,
we can afford to relax and to rely on inference as much, or as little, as we want.
We can even make up our own notation to represent code in a more compact way.

END*/
}
