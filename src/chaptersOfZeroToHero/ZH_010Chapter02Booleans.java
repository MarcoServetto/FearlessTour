package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_010Chapter02Booleans {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Booleans

### Booleans: True, False and internalised choices.

If you have done some logic in your school, you will be familiar with the concepts of `True`, `False`, `and`, `or` and `not`.

Anyway, we are going to recall them now:
In the same way we have only the four cardinal `Direction`s, we only have two fundamental truth values: `True` and `False`. They are called the `Bool`s or the booleans, in memory of George Boole, who laid the groundwork for what is now known as Boolean algebra.
We can easily encode booleans in Fearless as follows:
-------------------------*/@Test void bool1 () { run("""
//|OMIT_START
package test
alias base.Str as Str,
alias base.Int as Int,
alias base.Nat as Nat,
alias base.Void as Void,
//OMIT_END
Bool: {
  .and(other: Bool): Bool,
  .or(other: Bool): Bool,
  .not: Bool,
  }
True: Bool{
  .and(other) -> other,
  .or(other) -> this,
  .not -> False,
  }
False:Bool{
  .and(other) -> this,
  .or(other) -> other,
  .not -> True,
  }
"""); }/*--------------------------------------------

That is, `Bool` has three methods, and they all return another `Bool`.
Method `.and` and `.or` taking an `other: Bool` parameter.
Method `.not` takes zero parameters.
In this sense, `Bool` is very similar to numbers, where the operations on numbers (`+`,`-`,`*`, etc) takes numbers and returns numbers. In the same way, `Bool` operations take `Bool`s and return `Bool`s.

Then, `True` and `False` are kinds of `Bool`, where
`True.and` returns the parameter and `False.and` returns `False`
(since `this` is `False` inside of `False`).

It may look surprising that this is all we need to encode `.and`.
The desired behaviour of `.and` is represented by the following table:
```
True  .and True    -->  True
True  .and False   -->  False
False .and True    -->  False
False .and False   -->  False
```
That is, we only return `True` if both values are `True`.
This means that if `this` is `False` we just have to return `False` and that if `this` is `True` we need to return the value of the parameter.

The implementation of `.or` is exactly the opposite:
`True.or` returns `True` (since `this` is `True` inside of `True`)
and `False.or` returns the parameter.

The desired behaviour of `.or` is represented by the following table:
```
True  .or True    -->  True
True  .or False   -->  True
False .or True    -->  True
False .or False   -->  False
```
That is, we only return `False` if both values are `False`.
Finally, `.not` inverts the value: `True.not` reduces to `False` and `False.not` reduces to `True`.

Booleans are present in the Fearless standard library, and the implementation follows the same ideas presented here.
`Str`, `Nat`, `Int` and many other types of the standard library offer a method `==`, that returns the `True` `Bool` if and only if the two strings or numbers are conceptually the same, and a method `!=`, returning a `True` `Bool` if and only if the two strings or numbers are conceptually different.

#### Examples of reductions

All of those lines reduce in a single step:
- `True.not`        --> False` because `True.not` returns `False`
- `False.not`       --> True`  because `True.not` returns `True`
- `True.and(False)  --> False` because `True.and` returns `other`
- `False.or(True)   --> True` because `False.or` returns `other`
- `True.or(False)   --> True` because `True.or` returns `this`
- `False.and(True)  --> False` because `False.and` returns `this`

The code above shows that we can combine booleans to get more booleans. This is similar to what we have seen with `Nat` and `Rotation`. But the real power comes when we use them to make decisions: to execute different pieces of code depending on whether something is `True` or `False`. Crucially, Booleans are a better form of `Fork`.
- There are two kinds of `Bool` in the same way there are two kinds of `Fork`,
- We can compose `Bool`s with `.and`, `.or` and `.not`.
- We can obtain `Bool`s from many other data types using `==` and `!=`.

Can we add a concept of choice on our booleans, as we did for `Fork`?

This is where the Generics we saw earlier becomes essential. We need a way to represent the two possible code paths (what to do if `True`, what to do if `False`) and the result they produce. Remember the `Fork` example where `.choose[Val]` worked with any type `Val`? We need something similar here. Let's define a method `Bool.if`, that can produce a result of any type, let's call that type `R`, for Result.
To provide the two code paths we need a container object. We can define a generic type called `ThenElse[R]`. The `[R]` is a type parameter, just like `[Val]` was in `Fork`. It stands for the Result type that both code paths must ultimately produce.

-------------------------*/@Test void bool2 () { run("""
//|OMIT_START
package test
alias base.Str as Str,
alias base.Int as Int,
alias base.Nat as Nat,
alias base.Void as Void,
//OMIT_END
Bool: {
  .and(other: Bool): Bool,
  .or(other: Bool): Bool,
  .not: Bool,
  .if[R](m: ThenElse[R]): R,
  }
ThenElse[R]:{ .then: R, .else: R, }

True: Bool{
  .and(other) -> other,
  .or(other) -> this,
  .not -> False,
  .if(m) -> m.then, //If True, execute the .then branch
  }
False:Bool{
  .and(other) -> this,
  .or(other) -> other,
  .not -> True,
  .if(m) -> m.else, //If False, execute the .else branch
  }
//OMIT_START
A:{#:Bool ->
True .and False .if{
  .then -> True,
  .else -> False,
  }
}
//OMIT_END
"""); }/*--------------------------------------------
```
//usage example
True .and False .if{
  .then -> /*code for the then case*|/,
  .else -> /*code for the else case*|/,
  }
```
As you can see, now we can encode binary choices as expressions inside of method bodies.
Here we use the generic type variable `R` to represent the type returned by the methods of the `ThenElse[R]` literal. That is, the code `True.if[Str]{..}` returns a string.
This is a crucial abstraction step. We can now write a lot of example code.


As an example, we will define a simple `Bot` type that can respond to a few specific messages. It will have one method, `.message`, which takes an input `Str` and returns a response `Str`.

-------------------------*/@Test void example1 () { run("""
Bot: {
  .message(s: Str): Str ->
    // Outer Check: Is the message `hello`?
    (s == `hello`).if { //here R = Str
      .then -> `Hi, I'm Bot; how can I help you?`, 
      .else -> // Logic for when s is NOT "hello"
        // Inner Check: Is the message "bye"?
        (s == `bye`).if { //writing .if[Str] would be the same
          .then -> `goodbye!`, //Response if inner condition is True
          .else -> `I don't understand` // Response if inner condition is False

        } //End of inner ThenElse
    } //End of outer ThenElse
}
"""); }/*--------------------------------------------

The `.message` method uses an `.if` checking whether the input `s` is equal to `hello`.
The call is conceptually
``(s ==(`hello`)).if[Str]({..})``
but we can just write ``(s == `hello`).if {..}``
by removing parenthesis for single argument methods and relying on of generic type inference.
The [Str] indicates that both the `.then` and `.else` branches must produce a `Str` result.
The first `.then` branch is simple: it just returns the greeting string.
The first `.else` branch contains another `.if` call, nested inside. This inner check sees if `s` is equal to `bye`.
This nesting allows us to create more complex decision trees.

#### Visualizing reductions
---
1. ````
   Bot.message(`hello`)
   ````

2. ````
   (`hello` == `hello`).if {
     .then -> `hi...`,
     .else -> ...
     }
   ````

3. ````
   True.if {
    .then -> `hi...`,
    .else -> ...
    }
   ````

4. ````
  {
    .then -> `hi...`,
    .else -> ...
    }.then
   ````

5. ````
   `hi, I'm Bot; how can I help you?`
   ````
---

1. ````
   Bot.message(`bye`)
   ````

2. ````
   (`bye` == `hello`).if{
     .then -> `hi...`,
     .else -> (`bye` == `bye`).if{
       .then -> `goodbye!`,
       .else -> `I don't understand`
       }
     }
   ````

3. ````
   False.if{
     .then -> `hi...`,
     .else -> (`bye` == `bye`).if{
       .then -> `goodbye!`,
       .else -> `I don't understand`
       }
     }
   ````

4. ````
   {
     .then -> `hi...`,
     .else -> (`bye` == `bye`).if{
       .then -> `goodbye!`,
       .else -> `I don't understand`
       }
     }.else
   ````

5. ````
   (`bye` == `bye`).if{
     .then -> `goodbye!`,
     .else -> `I don't understand`
     }
   ````

6. ````
   True.if{
     .then -> `goodbye!`,
     .else -> `I don't understand`
     }
   ````

7. ````
   {
     .then -> `goodbye!`,
     .else -> `I don't understand`
     }.then
   ````


8. ````
   `goodbye!`
   ````
---
1. ````
   Bot.message(`test`)
   ````

2. ````
   (`test` == `hello`).if{
     .then -> `hi...`,
     .else -> (`test` == `bye`).if{
       .then -> `goodbye!`,
       .else -> `I don't understand`
       }
     }
   ````

3. ````
   False.if{
     .then -> `hi...`,
     .else -> (`test` == `bye`).if{
       .then -> `goodbye!`,
       .else -> `I don't understand`
       }
     }
   ````

4. ````
   (`test` == `bye`).if{
     .then -> `goodbye!`,
     .else -> `I don't understand`
     }
   ````
   
5. ````
   False.if{
     .then -> `goodbye!`,
     .else -> `I don't understand`
     }
   ````
6. ````
   `I don't understand`
   ````
---
> You may have notice that in the last reduction we omitted the execution step with the body of the `.if`: the explicit call to `.then` or `.else`.
We will do this more and more, skipping steps to make reductions more compact.
Indeed, when showing the method `Str==`, used over and over in the examples before, we just reduced it to `True` or `False` in a single step, but the actual execution of `Str==` does involve many, many steps.

As you can see from the example, the `.if` method directs the flow of execution.
Generics ensure that the outcomes of different branches are type-compatible.
Note how the generics are explicitly needed when **defining** the `.if` method but they are all inferred when **using** the `.if` method.

This is where our journey of learning Fearless programming starts to intersecting with concepts common to most other programming languages.
I still vividly remember the moment it struck me: every possible computation can be represented as just an enormous pile of ifs invoking each other. Mind blowing!

But just because something can be done, doesn't mean it's the best approach. Solving problems by throwing a massive heap of binary decisions at them (like firing wildly with a machine gun) rarely leads to elegant, maintainable code. A program built this way quickly becomes brittle and hard to evolve. Soon, we'll explore specialized decision-making constructs, each tailored to different scenarios, and we'll learn to select the right tool for each job. 

But for now, let's pause to appreciate what we've accomplished. Understanding the `.if` is a big achievement.


### Functions and delayed computation

Generics are very common in Fearless.

Possibly the most important generic types in the Fearless standard library are the function types, that looks similar to the following:
(there are some minor differences that we will discuss later)
-------------------------*/@Test void fun1 () { run("""
//|OMIT_START
package test
alias base.Str as Str,
alias base.Int as Int,
alias base.Nat as Nat,
alias base.Void as Void,
//OMIT_END
F[R]: { #: R }
F[A,R]: { #(a: A): R }
F[A,B,R]: { #(a: A, b: B): R }
F[A,B,C,R]: { #(a: A, b: B, c: C): R }
"""); }/*--------------------------------------------
Those types represent functions with zero, one, two ,three arguments.
Of course we can define more if more arguments are needed.
As you can see, thanks to the way generic types works, we can call them all `F` because
the presence of different numbers of generic arguments disambiguate their names.

With `F`, we can rewrite much of the code we showed in the beginning.
For example:
-------------------------*/@Test void f2 () { run("""
//OMIT_START
Direction:{}
//OMIT_END
Tank: { .heading: Direction, .aiming: Direction }
Tanks: { #(heading: Direction, aiming: Direction): Tank->
  { .heading ->heading, .aiming ->aiming }
  }
"""); }/*--------------------------------------------
could be rewritten as 
-------------------------*/@Test void f3 () { run("""
//OMIT_START
Direction:{}
//OMIT_END
Tank: { .heading: Direction, .aiming: Direction }
Tanks: F[Direction,Direction,Tank] { h,a -> { .heading -> h, .aiming -> a } }
"""); }/*--------------------------------------------
This code is not just slightly shorter, but now `Tanks` is a valid element that can be passed to any method taking a generic `F[A,B,R]`.

This is what is usually called the abstract factory pattern: 
A factory object is an object whose main goal is to create other objects.
`Tanks` is a factory object.
We can have various ways to create objects and we can pass those factory objects to code that needs to create objects internally.


The factory pattern is just a sub pattern of the more general idea of lifting behaviour into objects.
In particular `F[R]` is often used to represent delayed computation. By turning the behaviour producing an `R` into an object of type `F[R]` we can now pass this object around.

### Delayed computations for Booleans

As an example: 
We have seen how the `.and` and `.or` methods compute the overall result from two boolean expressions and then produce a cumulative result.
However, in the case of `False .and ...` we do not need to compute the second expression. The result will be False anyway.
Same for `True .or ...`. The result will be True anyway.
If the computation for the second part of the `.and` / `.or` was very intricate this could save a lot of time.
We can define **short-circuited** versions for `.and` and `.or`, called `&&` and `||` as shown below:


-------------------------*/@Test void bool3 () { run("""
//|OMIT_START
package test
alias base.Str as Str,
alias base.Int as Int,
alias base.Nat as Nat,
alias base.Void as Void,
alias base.F as F,
//OMIT_END
Bool: {
  .and(other: Bool): Bool,
  .or(other: Bool): Bool,
  .not: Bool,
  .if[R](m: ThenElse[R]): R,
  &&(other: F[Bool]): Bool,
  ||(other: F[Bool]): Bool,
  }
ThenElse[R]:{ .then: R, .else: R }

True: Bool{
  .and(other) -> other,
  .or(other) -> this,
  .not -> False,
  .if(m) -> m.then,
  &&(other) -> other#,
  ||(other) -> this,
  }  
False:Bool{
  .and(other) -> this,
  .or(other) -> other,
  .not -> True,
  .if(m) -> m.else,
  &&(other) -> this,
  ||(other) -> other#,
  }
"""); }/*--------------------------------------------
We can then use those operators as follows:
```
Much:   {.code: Bool -> ..., }
Slow:   {.code: Bool -> ..., }
ATonOf: {.code: Bool -> ..., }
```
-------------------------*/@Test void bool4 () { run("""
//|OMIT_START
package test
alias base.Str as Str,
alias base.Int as Int,
alias base.Nat as Nat,
alias base.Void as Void,
alias base.F as F,
Bool: {
  .and(other: Bool): Bool,
  .or(other: Bool): Bool,
  .not: Bool,
  .if[R](m: ThenElse[R]): R,
  &&(other: F[Bool]): Bool,
  ||(other: F[Bool]): Bool,
  }
ThenElse[R]:{ .then: R, .else: R }

True: Bool{
  .and(other) -> other,
  .or(other) -> this,
  .not -> False,
  .if(m) -> m.then,
  &&(other) -> other#,
  ||(other) -> this,
  }  
False:Bool{
  .and(other) -> this,
  .or(other) -> other,
  .not -> True,
  .if(m) -> m.else,
  &&(other) -> this,
  ||(other) -> other#,
  }
Much:   {.code: Bool -> True, }
Slow:   {.code: Bool -> False, }
ATonOf: {.code: Bool -> False, }
AA:{#:Bool ->
//OMIT_END
Much.code && { Slow.code } && {ATonOf.code} // version 1
//OMIT_START
} BB:{#:Bool ->
//OMIT_END
Much.code && { Slow.code  && {ATonOf.code}} // version 2
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------
In this version, if `Much.code` reduces to `False`, we will not execute `Slow.code` and `ATonOf.code`.
If `Much.code` reduces to `True`, and `Slow.code` reduces to `False`, we will not execute `ATonOf.code`.
Both versions (note the different parenthesis) are equivalent, and reduce in pretty much the same amount of time.

On the other side, if we used the `Bool.and` method
```
Much.code .and (Slow.code) .and (ATonOf.code)  // version 1
Much.code .and (Slow.code .and (ATonOf.code) ) // version 2
```
both versions would always run all the three computations.

Note how `{Slow.code}` is an object Literal of type `F[Bool]`.
The full version would be  :
```
Anon1[]:F[Bool] { #[](): Bool[] -> Anon2[]:Slow[]{}.code[](), }
```
That is, since the method `F[Bool]#` has exactly zero arguments, we can omit both the method name # and the arrow -> when implementing it.

We can now compare and contrast the above with the syntax
```
Tanks: F[Direction,Direction,Tank]{ h,a -> { .heading -> h, .aiming -> a } }
```
to implement the method `F[Direction,Direction,Tank]#`.
Note the presence of `->` after `h,a ->`.
Since method `F[Bool]#` takes zero arguments, we implement it with just `{Slow.code}` instead of having to awkwardly write `{-> Slow.code}`

Finally, consider again
```
Much.code && { Slow.code } && {ATonOf.code} // lazy
Much.code .and (Slow.code) .and (ATonOf.code)  // eager
```  
Are those two lines of code equivalent, except for speed?
Not really.
In Fearless, as in most programming languages, it is possible to
encode non terminating computations.
For example, what if `Slow` were defined as follows:
```
Slow:{.code: Bool -> this.code, }
```
The method call `Slow.code` reduces in one step to `Slow.code`, that reduces in itself again, and again, and again. This reduction never stops!
Executing `Slow.code` would either never terminate or produce some kind of error.
In that case, if `Much.code` reduces to `False`, the first line simply reduces to `False`, while the second line would either never terminate or produce an error.
That is, while `Slow.code` never terminates, `{Slow.code}` is a value of type `F[Bool]`. Non termination only happens when and if method `#` is called on that value.

In the rest of the guide we will see other situations where lazy and eager operations can have radically different behaviours. Overall, thinking that they are equivalent can be useful in first approximation, but can hurt us down the line.

When programming we often need to **keep in mind multiple levels of abstractions and multiple levels of precision**.
At a more coarse level of precision, `.and` and `&&` are equivalent; at a more fine level of precision, we can see differences. Occasionally, fine behavioural details can bubble up the ladder of abstractions and become relevant.

END*/
}
