package chapter1A;

import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_002Chapter01Directions {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Directions

# Chapter 1

### The three Main Fearless Concepts
In Fearless we have three main concepts: 
- Types
- Methods
- Expressions

To demonstrate these concepts, let’s consider an example expressing
the four cardinal directions:

-------------------------*/@Test void compile4() { run("""
North:{}
East: {}
South:{}
West: {}
"""); }/*--------------------------------------------


We used four names of real world directions to name **types** in Fearless.
Those four lines are **type declarations**, and the four names are **type names**.
The `:` token means "declaration".

Those types are not very useful because they are unconnected.
We can create connections between those types by adding **methods**
to our directions:

`````
North: {.turn-> East, } 
East : {.turn-> South,}
South: {.turn-> West, }
West : {.turn-> North,}
`````

In the example above,
- `North: { .turn -> East, }` is a type declaration.
Within this declaration
- `North` is the typeName
- `.turn` is a **method name**. It is a single token including the `.`
- `.turn -> East,` is a **method declaration** and in this case
  the method body is just `East`.
- The token `->` gives a body to a method. In the case of `.turn` the body
 is the new direction after turning 90&deg; clockwise.

We will see that method bodies can get much more involved than that.
We introduced `East` as a type name; but when `East` is used inside
the method body it is an **expression**,
specifically, an object literal expression.

Coding is all about defining kinds of objects and the relations between them.
Other terms for 'object' could be 'value', 'entity', 'instance' or 'element'.
We will use the term 'object' consistently to talk about those.

To understand this better, consider the English sentence "Stop."
This sentence contains a single word; and yet, it is still a sentence.
An English sentence is a collection of words and punctuation with at least one word. In the same way, some expressions in Fearless are simply
a single type name. However, just as sophisticated sentences in English are fundamental to effective communication, more elaborate expressions are crucial in programming.

### Method calls: expressions composed by multiple parts.

Method calls are expressions composed of other expressions.
Method calls allow us to write the equivalent of sentences in natural language.
Single words have relatively simple meanings but they can be chained together with other words to communicate a more complex idea.
Using the code above, a simple example of a method call is `North.turn`.
- `North` is an expression and
- `North.turn` is an expression built on top of `North`.
- `North.turn.turn` is also a valid expression, built on top of `North.turn`.

We can write `North` to refer to the object `North`,
which we are interpreting as representing the cardinal direction north.
We can also write `North.turn` to refer to the object `East`, which we are interpreting as representing the cardinal direction east.
In other words, there are exactly two ways to refer to an object:
1. by directly mentioning it, or
2. by mentioning some expression that returns it.

We say that the `.turn` method of `North` returns the object `East`.
In other words, `North.turn` is an expression which is equivalent to the object `East`.
In the same way, `South.turn.turn` is an expression which is equivalent to the object `North`. This process of discovering the meaning of an expression is called evaluation.

The example we are discussing is still incomplete, and it
would cause an error if we try to compile it.
`````
North: {.turn-> East, } 
East : {.turn-> South,}
South: {.turn-> West, }
West : {.turn-> North,}

Error: missing type for method .turn in North
`````
While the exact error message may vary between different versions
of Fearless, the core of this error is that the method `.turn`
does not know what type it returns.

>**Errors are good things:**
>errors help us be precise and to avoid more mistakes later.

Types help reasoning and they are used to ensure that
our program is safe to use. 
Many programmers rely on types to understand code.

So, what does the `North.turn` method return? `East`. So we can write:
`````
North: {.turn: East  -> East,  }
East : {.turn: South -> South, }
South: {.turn: West  -> West,  }
West : {.turn: North -> North, }
`````
This is correct, and would make our code compile. However,
this is stating that the four methods `.turn` are different
kinds of methods, since they return different types.
This does not capture our intention.
We intended for each of our four directions to be examples
of a general idea, the idea of `Direction`;
and for every direction to have a `.turn` method, giving us the next
clockwise direction.

We can capture this intention with the following code:

-------------------------*/@Test void dir1() { run("""
Direction: { .turn: Direction, }
North: Direction { .turn -> East,  }
East : Direction { .turn -> South, }
South: Direction { .turn -> West,  }
West : Direction { .turn -> North, }
"""); }/*--------------------------------------------


Since `North`,`East`,`South` and `West` are all directions,
we can introduce a `Direction` type to group `North`,`East`,
`South` and `West` as kinds of `Direction`.
We say that `North`,`East`,`South` and `West` implement `Direction`.
We also say that `Direction` is a supertype of 
`North`,`East`,`South` and `West`.

There is now a type `Direction` that has an abstract method `.turn`
returning a `Direction`. The method `Direction.turn` is **abstract** since there is no body. In other words, there is no `->` (arrow) symbol followed by an expression.
We can think that there are five different `.turn` methods, one abstract and four concrete/implemented.
Alternatively, we can see `.turn` as a single method, whose implementation is scattered across all the kinds of directions.
Those two viewpoints are equivalently correct.

Note how we do not need to repeat the type `Direction` in `North.turn`.
Fearless can infer or predict that type since `North` implements `Direction`
and `Direction.turn` returns a `Direction`.
This process is called type inference and is an important concept in Fearless.

### Reverse

We can now imagine a method `.reverse` that turns 180 degrees.
A naive approach would hard code the method result in all the cases, as shown below.

`````
Direction: { .turn: Direction, .reverse: Direction,  }
North: Direction { .turn -> East,  .reverse -> South, }
East : Direction { .turn -> South, .reverse -> West,  }
South: Direction { .turn -> West,  .reverse -> North, }
West : Direction { .turn -> North, .reverse -> East,  }
`````

This solution works but is quite verbose.
A better solution for any given direction would be to call `.turn`
twice and get the desired result:
rotating 180 degrees clockwise is the same as rotating 90 degrees clockwise, twice.

However, if we simply follow this intuition and we add `.turn.turn` in all the directions, we get even more verbose code.
`````
Direction: { .turn: Direction, .reverse: Direction, }
North: Direction { .turn -> East,  .reverse -> North.turn.turn, }
East : Direction { .turn -> South, .reverse -> East.turn.turn,  }
South: Direction { .turn -> West,  .reverse -> South.turn.turn, }
West : Direction { .turn -> North, .reverse -> West.turn.turn,  }
`````
However, the code is now more regular, the four bodies of `.reverse` are all almost identical.
They are all of the form `.reverse->???.turn.turn,` where `???` is the current direction.

We are able to reuse the  `.reverse->???.turn.turn,` part by putting it into our definition of `Direction`.

`````
Direction: {
  .turn: Direction,
  .reverse: Direction -> ???.turn.turn,
  }
North: Direction { .turn -> East, }
East : Direction { .turn -> South,}
South: Direction { .turn -> West, }
West : Direction { .turn -> North,}
`````

Now the code of `.reverse` appears only one time in our program.
Note how we are using some new lines and spaces to show visually the
content of `Direction`. This is called indentation. 

Of course, `???` is not valid Fearless code.
Originally, we wrote the name of the current direction:
`North.turn.turn`, `East.turn.turn`, ...
Methods allows us to reuse some code, but here instead of repeating the same exact behaviour over and over again, we want our behaviour to be contextual over the current direction.
That is, `???` should be `North` inside of `North.reverse` and `South` inside of `South.reverse`.

We need a way to refer to the current direction within the `.reverse` method. If we're calculating `North.reverse`, we need `North.turn.turn`. If we're calculating `East.reverse`, we need `East.turn.turn`.

We can indeed access the current direction by using `this`, as shown below.
-------------------------*/@Test void directionReverse() { run("""
Direction: {
  .turn: Direction,
  .reverse: Direction -> this.turn.turn,
  }
North: Direction {.turn -> East, }
East : Direction {.turn -> South,}
South: Direction {.turn -> West, }
West : Direction {.turn -> North,}
"""); }/*--------------------------------------------
That is, when the `.reverse` method is called,
the **parameter** `this` will refer to the current object.
For example, even though the method `North.reverse` is `this.turn.turn`, in that context `this` is `North`, so when the method is called we would replace `this` with `North` and get  `North.turn.turn`.

Here is an example showing this execution / replacement process where
the first step correctly jumps from `North.reverse` into `North.turn.turn`:

- Method `North.reverse` comes from `Direction.reverse`.
- The body of method `Direction.reverse` is `this.turn.turn`.
- The parameter `this` is replaced with `North`, since we are calling `North.reverse`.
- Thus we get `North.turn.turn`.


 | ------------------ | ----------------------- |
 | Original form      |  `North.reverse`        |
 | Reduces to         |  `North.turn.turn`      |
 | Reduces to         |  `East.turn`            |
 | Reduces to         |  `South`                |

`this` is an example of the third and last kind of expression: parameters.

### The three kinds of Fearless expressions

1. parameters: `this`.
  Later, we will see how to declare and use other parameters.
2. method calls: `North.turn` and `North.reverse`.
  Later we will see more kinds of method calls.
3. object literals: `North`, `East`, `South` and `West`. 
  The object literals we have seen are simply the names of existing types.
  Later on in this tutorial we will see more kinds of objects,
  capable of summoning into existence novel or unique values;
  in addition to the ones already existing.

Note: `North`, `East`, `South`, and `West` can be used directly as
objects because they have no abstract methods. 
Since `Direction` has at least one abstract method (`.turn`),
it  can not be used directly as a valid object literal.
We will call types with abstract methods **abstract types**.


END*/
}
