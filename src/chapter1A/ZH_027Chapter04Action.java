package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_027Chapter04Action {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Errors are good!

### Fearless errors and actions.

In Fearless, some methods throw errors.
As discussed, errors are not part of the basic semantics of Fearless, and they can be thrown using the magic method `Error!(Info)`.
That is, any code anywhere can write `Error!(someInfo)` and throw that `Info` object as a structured error message.

>TODO: Error.msg returns {msg:text, stackTrace:[...]}

Before we discussed the convenience method `Error.msg` that takes a simple string, converts it into an `Info` object mapping that
string to the `` `msg` `` key and adds more debugging information.
The method `Map.get` either returns the mapped value or uses `Error.msg(..)` to report the lookup failure.

When a method throws an error the computation stops and the error is reported outside of the program.
This behaviour can be overridden using `Try#{ ../*code that can fail*|/.. }`.
The method `Try#` takes a function returning a result of type `R` and (without executing the function in input) produces an `Action[R]` object.
Fearless actions are a way to handle behaviour that can fail while recovering potential errors.

Let's make this more concrete.

- Code 1: ``Directions.map.get(`Nope`)`` raises an error with a readable error message.
- Code 2: ``Try#{Directions.map.get(`Nope`)}`` returns an object of type `Action[Direction]`.
- Code 3: ``Directions.map.tryGet(`Nope`)`` behaves identically to Code 2, but could be faster.

We can use code 1 when we trust that the error will not raise, or because if the error condition happens, then what we want is for the program to terminate with a good error message.
When we want to consciously extract an element that may or may not be there, with the intention that the element being missing does not represent an error, we can use the method `.opt`, as in `Directions.map.opt(`Nope`)`, returning an `Opt[Direction]`.

Using `Try#` or `.tryGet` means that we suspect that the value may not be there because of some buggy logic or input.

## Actions
`Action[R]` is conceptually similar to `Opt[E]`, but
- the empty case has an associated `Info` object.
- the contained value has not been computed yet. While an `Opt[E]` stores an element of type `E`, an `Action[R]` stores a computation that can create such an `R` result.

The code of `Action[R]` is not very long and it uses Fearless in interesting ways, thus we will read and explain the implementation of `Action[R]` as present in the standard library:
```
ActionMatch[T:*,R:**]: {//NOTE: should be T:* or T:**? actually unobvious
  mut .ok(x: T): R,
  mut .info(info: Info): R,
  }
```
The `ActionMatch[T,R]` type is unsurprising. Very similar to `OptMatch[T,R]` or `StackMatch[T,R]`.
As we discussed before, remember that `T:*` stays for `T:imm,mut,read` and `T:**` includes all of the reference capabilities.

At its core, Action has a very simple implementation: 
```
MF[R:**]: { mut #: R }
Action[R:**]: {
  mut .run[RR:**](m: mut ActionMatch[R,RR]): RR,
  }
```
We call the generic parameters `R` and `RR` to suggest that `R` is the result of the action, while `RR` is the result of processing either the action result or the error `Info`.

 If you remember from before, `MF` is similar to `F`, but it is mutable. That is, it is a function that can make mutations while it runs.
`Action[R]` has a single abstract `.run` method that takes an `ActionMatch[R,RR]`.
Note how everything is either `mut` or `**`. This is because actions are often used together with side effects and mutations.
The actual implementation of `Action[R]` also offers some convenience methods (`.map`,`.mapInfo`, `.andThen`, `!` and `.context`).
Methods `!` and `.context` are widely used and beginner friendly, while method `.map`, and `.andThen` are used more rarely.
Here we exam those methods one by one:
>Note: Action.mapInfo is also present, but we do not discuss it in the guide

#### Method `Action[R]!`
```
Action[R:**]: {
  mut .run[RR:**](m: mut ActionMatch[R,RR]): RR,
  ..
  mut !: R -> this.run{.ok(x) -> x, .info(i) -> Error!i},
  }
```
Method `Action!` returns the `R` value, or throws an error with the provided info.
That is, `!` is actually the opposite of `Try#`.
For example, 
- ``Try#{ Direction.map.get(`Nope`) }!``

is equivalent to
- ``Direction.map.get(`Nope`)``

In the same way, 
- ``Try#{ myAction! }``

is equivalent to
- ``myAction``

Note how in the first example the `!` is outside of the `Try` and in the second example the `!` is inside of the `Try`.

#### Method `Action[R].context`

```
Action[R:**]: {
  mut .run[RR:**](m: mut ActionMatch[R,RR]): RR,
  ..
  mut .context(msg: read F[Str]): mut Action[R] -> {m -> this.run{
    .ok(x) -> m.ok(x),
    .info(i) -> m.info(Infos.msg(msg#) + i),
    }},
  }
```
Method `.context` is a convenience method to add contextual information to actions.
Internally, it uses method `Info+`, that we have not seen yet:
The method `Info+` makes it easy to compose information together.
Two `Info` messages are concatenated, two `Info` lists are concatenated and two `Info` maps are merged:
- If a key is present in only one of the two sources, the key->element mapping will be present in the resulting information.
- If the key is present in both sources, the resulting information will map that key to the sum of the two elements using `Info+` recursively.

Finally, if the two infos are not of the same kind, they are lifted to maps with a single mapping containing the original information:
  - A message info is lifted to a map with a single mapping `` `msg` ``.
  - A list info is lifted to a map with a single mapping `` `list` ``.

Often we use `.context` to add context to our actions.
Consider the code below where `persons` is a `List[Person]`:
``` 
persons.tryGet(5).context{`The list persons was too small when\n`}
```
This code would add the information that we were looking into the list of persons, and the error would look like
  
>  The list persons was too small when  
>  attempted access in position 5 over a list of size 3

Note that the computation required to format this extra information will only be executed if the `Action[R]` actually fails. This is often a big win in terms of performance, since nicely formatted error messages may be quite slow to compute.

#### Method `Action[R].map`
```
Action[R:**]: {
  mut .run[RR:**](m: mut ActionMatch[R,RR]): RR,
  mut .map[RR:**](f: mut MF[R,RR]): mut Action[RR] -> {m -> this.run{
    .ok(x) -> m.ok(f#x),
    .info(i) -> m.info(i),
    }},
  ...
  }
```
Method `.map` takes another mutating function `f` and returns a `mut Action[RR]` object that when a matcher 'm' is provided, calls the `.run` method on the outer `Action[R]` and delegates the behaviour of `.ok` and `.info` to `m.ok` and `m.info`.
- `m.ok` will take in input not the original value `x`, but the result of applying `f` to `x`.
Method `.map` is useful to transform the type of actions without actually executing any code at that moment.
For example 
```
persons.tryGet(3).map{::name}
```
would return an `Action[Str]` containing the name of the person in position 3.


#### Method `Action[R].andThen`
```
Action[R:**]: {
  mut .run[RR:**](m: mut ActionMatch[R,RR]): RR,
  mut .andThen[RR:**](f: mut MF[R,mut Action[RR]]): mut Action[RR] -> {m -> this.run{
    .ok(x) -> f#x.run(m),
    .info(i) -> m.info(i),
    }},
..
  }
```

We can see `.andThen` as a version of `.map` allowing more control over how the resulting `Action[RR]` is created.

The `.map` method takes a function that transforms a value of type `R` into a new value of type `RR`. This transformation is straightforward and direct. If `Action[R]` contains a value, that value is transformed using the provided function to produce an `Action[RR]`.
If the original `Action[R]` is successful (i.e., produces a value), `.map` applies the function to this value and wraps the result in a new `Action[RR]`. If the original `Action[R]` is a failure (i.e., produces an `Info` object), `.map` does nothing to the failure information—it simply carries it forward unchanged.

`.andThen` extends the functionality of `.map` by allowing the transformation function itself to produce the new `Action[RR]`. This is useful when the transformation’s outcome isn’t just a value, but a new computation or action that might itself succeed or fail.
The function used in `.andThen` takes a value of type `R` and returns a new `Action[RR]`.
Method `.andThen` is designed to handle nested actions. If the original `Action[R]` is successful, `.andThen` uses the value to generate a new `Action[RR]` using the provided function. This new Action is then executed as part of the overall computation. If the original `Action[R]` is a failure, both `.map` and `.andThen` simply propagate the failure information.
Method andThen is particularly useful in scenarios where subsequent actions depend on the results of previous ones.
For example, the code below
```
persons.tryGet(3).andThen{p->
  jobs.tryGet(p.name).map{ j->`Person `+p+` works as a `+j } }
```

does two different actions: extracts person `3` and connects their name with their job using `jobs: Map[Str,Job]`.
Finally it uses the job `j` and the person `p` to produce an `Action[Str]`. Note how this is only needed if we want to get a detailed error message in case of failure. We can encode the same idea with optionals with the more conventional flow code below:
```
persons.opt(3).flow.flatMap{p->
  jobs.opt(p.name).flow.map{ j->`Person ` + p + ` works as a ` + j } }.opt
```
Here, if the person or the job is not present, we would simply get an empty optional.
Note: if we expect the person and the job to be there, and it should be an observed bug if this is not the case, then we should write the simpler code
```
  Block#
    .let p= {persons.get(3)}
    .return { `Person ` + p + ` works as a `+ jobs.get(p.name) }
```
In this way our code will correctly fail as soon as an error is detected.

### Errors or Actions?
In Fearless, Actions and errors can be used together to handle computation that can potentially fail.
The idea is that the code will have layers of responsibility:
Code with less responsibility can simply throw errors, while code with higher responsibility will use actions.

For example, consider `Point`: before we chose to visualise only tanks in locations from 0-10; but the `Point` objects can have any kind of coordinates.
We could use errors to enforce that whenever a `Point` is observed, the `.x` and `.y` coordinates are in the 0-10 range.
> Note: we do not show     .assert { x.inRange(0,10) } since it would make a bad error message.
> should we have a general Bool.orMsg(`...`)
> if we want disableable assertions, we could have .assert taking a void like do.
> then we get to write .asser{ x.checkInRange(0,10) } taking an read F[Void]
>read F[Void] is a very unusual type, but this seems to be the good use case: it guarantees no side effects visible AND no value created
```
Points: F[Nat,Nat,Point], FromInfo[Point] {

  .fromInfo(i) -> Points#(i.map.get(`x`).msg.nat, i.map.get(`y`).msg.nat),

  #(x, y) ->Block#
    .do { x.checkInRange(0,10) }
    .do { y.checkInRange(0,10) }
    .return{ Point: ToInfo,ToStr,OrderHash[Point]{'self
      .x: Nat -> x,
      .y: Nat -> y,
      +(other: Point): Point -> Points#(other.x + x, other.y + y),
      .move(d: Direction): Point -> self + ( d.point ),
      <=>(other) -> other.x <=> x  && { other.y <=> y },
      <=>{x,y}1 -> x <=> x1  && { y <=> y1 },//other application of the sugar? also on + and .move
      .hash(h)-> h#(x, y),
      .info -> Infos.map(`x`,x,  `y`,y),
      .str -> `[` + x + `, ` + y + `]`,
      }}}
```

Here Points has a `.fromInfo` method creating a point using the `.x` and `.y` coordinate stored in an `Info` object.
Then, method `Points#` takes the `x` and `y` `Nat` coordinates and creates the resulting `Point`.
However, before the point is created, we use `Nat.checkInRange` to check that `x` and `y` are in the desired range.
`Nat.checkInRange` will use `Error!` if the condition is false.
>//TODO: what is the current state in Fearless for those kinds of assertions?

This means that if we try to create a `Point` object, either manually or from an `Info`, we either get a valid `Point` or an error.
The idea is that most of the code can create points assuming that the creation would succeed.
From the perspective of that code, a failure to create a `Point` object is an observed bug.

Then, if the program as a whole does not want to consider failing to create points an observed bug, it can wrap a function making a lot of computations about points into a `Try#{..}`, thus producing an action that can be safely handled.

In a complex program we can have a few layers of responsibility like this, where some code works as a supervisor of some other code that is allowed to fail.


## Offensive programming

We must ensure that code fails fast and visibly when undesired situations occur. The goal is to have the code fail as soon as a fault is introduced. This approach helps in quickly pinpointing the source of an error, making it easier to debug and fix.
The reality is that all code, at any moment, may fail at run time. Perfection will likely always elude us.
While striving for perfection in coding is admirable, it is nearly impossible to achieve flawlessness. We acknowledge this reality and instead focus on managing imperfections effectively.
When failures occur, it is crucial that they produce clear and understandable error messages. This will help us to understand issues quickly and accurately.

The primary adversary here is the silent failure: code that continues to run despite broken assumptions or invalid data. Such failures can lead to unpredictable behaviour and can corrupt the system insidiously, affecting increasingly larger portions of the application.

By inserting run time checks as shown above, we can minimise the lifetime of faulty executions, and ensure that the code only receives correct data. We need to be proactive rather than reactive, preventing issues from going unnoticed and causing further complications down the line.
With immutable objects like `Point`, it is usually sufficient to insert appropriate checks during object creation.
We will later see other techniques supporting offensive programming when working with mutable data.

Some programmers think the code should simply never fail by construction; basically it should encode the proof of it's own correctness inside it's structure.
While amazing in theory, this mindset causes issues in the real world, where programmers are negotiating their code quality with time and skill constraints.
Bob, the stressed programmer would probably chose to somehow twist the intended behaviour in subtle ways so that the code compiles and he does not get fired.  
The offensive programming philosophy instead gives plenty of escape hatches to Bob:
he can produce code working correctly in most real situations, while failing with observed bugs when needed. This explicitly and sincerely communicate what is going on.


OMIT_START
-------------------------*/@Test void anotherPackage() { run("""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}