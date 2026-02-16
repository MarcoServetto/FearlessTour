package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_027Chapter04Action {
/*START
--CHAPTER-- Chapter 4
--SECTION-- Action

### Fearless errors and actions.

In Fearless, some methods throw errors.
As discussed, errors are not part of the basic semantics of Fearless, and they can be thrown using the magic method `Error!(Info)`.
That is, any code anywhere can write `Error!(someInfo)` and throw that `Info` object as a structured error message.


Before we discussed the convenience method `Error.msg` that takes a simple string, converts it into an `Info` object mapping that
string to the `` `msg` `` key.
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
ActionMatch[R:**,RR:**]: {//NOTE: should be R:* or R:**? actually unobvious
  mut .ok(R): RR;
  mut .info(Info): RR;
  }
Action[R:**]: {
  mut .run[RR:**](mut ActionMatch[R,RR]): RR;
  }
```
The `ActionMatch[R,RR]` type is unsurprising. Very similar to `OptMatch[T,R]` or `StackMatch[T,R]`.
As we discussed before, remember that `R:*` stays for `R:imm,mut,read` and `R:**` includes all of the reference capabilities.
At its core, Action has a very simple implementation.
We call the generic parameters `R` and `RR` to suggest that `R` is the result of the action, while `RR` is the result of processing either the action result or the `Info`.

`Action[R]` has a single abstract `.run` method that takes an `ActionMatch[R,RR]`.
Note how everything is either `mut` or `**`. This is because actions are often used together with side effects and mutations.
The actual implementation of `Action[R]` also offers some convenience methods (`.map`,`.mapInfo`, `.andThen`, `!` and `.context`).
Methods `!` and `.context` are widely used and beginner friendly, while method `.map`, and `.andThen` are used more rarely.
Here we exam those methods one by one:

>Note: Action.mapInfo is also present, but we do not discuss it in the guide

#### Method `Action[R]!`
```
Action[R:**]: {
  mut .run[RR:**](mut ActionMatch[R,RR]): RR;
  ..
  mut !: R -> this.run{.ok x -> x; .info i -> Error!i};
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

That is, ``Try#{ myAction! }`` **does not** execute the action, it just creates an action that, when run, would execute the action `myAction`.
To force the execution of an action and still get the action back, we should use the following code:
````
myAction.run{ .ok v->Actions.ok(v); .info i-> Actions.info(i); }
````
Where `Actions.ok` and `Actions.info` create an always succeeding or always failing action.
However, actions that do no actions when called are misleading, so the code above is better avoided.

#### Method `Action[R].context`

```
Action[R:**]: {
  mut .run[RR:**](mut ActionMatch[R,RR]): RR;
  ..
  mut .context(msg: read F[Str]): mut Action[R] -> {m -> this.run{
    .ok x   -> m.ok(x);
    .info i -> m.info(Infos.msg(msg#) + i);
    }};
  }
```
Method `.context` is a convenience method to add contextual information to actions.
Internally, it uses method `Info+`, that we have not seen yet:
The method `Info+` makes it easy to compose information together.
Two `Info` messages are concatenated, two `Info` lists are concatenated and two `Info` maps are merged:
- If a key is present in only one of the two sources, the key -> element mapping will be present in the resulting information.
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
MF[R:**]: { mut #: R } //mutable function, seen before
Action[R:**]: {
  mut .run[RR:**](mut ActionMatch[R,RR]): RR,
  mut .map[RR:**](f: mut MF[R,RR]): mut Action[RR] -> {m -> this.run{
    .ok x   -> m.ok(f#x);
    .info i -> m.info(i);
    }};
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

Note that if the map operation would fail by calling `Error.msg`; 
that error would not turn the action into a failed action with the `.info` containing the error; it would just leak out when we attempt to `.run` our action.
>TODO: should we design this alternative behaviour too and see how it looks?

#### Method `Action[R].andThen`
```
MF[R:**]: { mut #: R } //mutable function, seen before
Action[R:**]: {
  mut .run[RR:**](mut ActionMatch[R,RR]): RR;
  mut .andThen[RR:**](f: mut MF[R,mut Action[RR]]): mut Action[RR] -> {m -> this.run{
    .ok x   -> f#x.run(m);
    .info i -> m.info(i);
    }};
..
  }
```

We can see `.andThen` as a version of `.map` allowing more control over how the resulting `Action[RR]` is created.

The `.map` method takes a function that transforms a value of type `R` into a new value of type `RR`. This transformation is straightforward and direct. If `Action[R]` contains a value, that value is transformed using the provided function to produce an `Action[RR]`.
If the original `Action[R]` is successful (i.e., produces a value), `.map` applies the function to this value and wraps the result in a new `Action[RR]`. If the original `Action[R]` is a failure (i.e., produces an `Info` object), `.map` does nothing to the failure information—it simply carries it forward unchanged.

`.andThen` extends the functionality of `.map` by allowing the transformation function itself to produce the new `Action[RR]`. This is useful when the transformation’s outcome isn’t just a value, but a new computation or action that might itself succeed or fail.
The function used in `.andThen` takes a value of type `R` and returns a new `Action[RR]`.
Method `.andThen` is designed to handle nested actions. If the original `Action[R]` is successful, `.andThen` uses the value to generate a new `Action[RR]` using the provided function. This new Action is then executed as part of the overall computation. If the original `Action[R]` is a failure, both `.map` and `.andThen` simply propagate the failure information.
Method `.andThen` is particularly useful in scenarios where subsequent actions depend on the results of previous ones.
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
> should we have a general Bool.orMsg(`...`) ?
> if we want disableable assertions, we could have .assert taking a void like do.
> then we get to write .assert{ x.checkInRange(0,10) } taking an read F[Void]
>read F[Void] is a very unusual type, but this seems to be the good use case: it guarantees no side effects visible AND no value created

```
Points: F[Nat,Nat,Point], FromInfo[Point] {

  .fromInfo(i) -> Points#(i.map.get(`x`).msg.nat, i.map.get(`y`).msg.nat);

  # x, y ->Block#
    .do { x.checkInRange(0,10) }
    .do { y.checkInRange(0,10) }
    .return{ Point: DataType[Point,Point]{'self
      .x: Nat -> x,
      .y: Nat -> y,
      +(other: Point): Point -> Points#(other.x + x, other.y + y),
      .move(d: Direction): Point -> self + ( d.point ),
      .cmp {.x,.y}1, {.x,.y}2, m -> x1 <=> (x1,m && { y1 <=>(y2,m) };
      .hash -> x.hash.hashWith(y.hash);
      .info -> Infos.map(`x`,x,  `y`,y);
      .str -> `[` + x + `, ` + y + `]`;
      }}}
```

Here Points has a `.fromInfo` method creating a point using the `.x` and `.y` coordinate stored in an `Info` object.
Then, method `Points#` takes the `x` and `y` `Nat` coordinates and creates the resulting `Point`.
However, before the point is created, we use `Nat.checkInRange` to check that `x` and `y` are in the desired range.
`Nat.checkInRange` will use `Error!` if the condition is false.

> TODO: we need to decide if those are deterministic or non deterministic errors.
> Same for most errors in the std library right now.

This means that if we try to create a `Point` object, either manually or from an `Info`, we either get a valid `Point` or an error.
The idea is that most of the code can create points assuming that the creation would succeed.
From the perspective of that code, a failure to create a `Point` object is an observed bug.

Then, if the program as a whole does not want to consider failing to create points an observed bug, it can wrap a function making a lot of computations about points into a `Try#{..}`, thus producing an action that can be safely handled.

In a complex program we can have a few layers of responsibility like this, where some code works as a supervisor of some other code that is allowed to fail.

### Try, CapTry and exact shape of error messages.

We have seen that `Try#{...}` creates an `Action[R]` and that `myAction!` can do
`Error#(info)`.
But.. what happen next? 
Another `Try#{...}` can turn the leaked error into another `Action[R]`, or the whole program could fail. How does this failure looks?
It will look something like this:
````
Error info: {.msg:`....`}
stack trace:
...//TODO: complete example with correct code.
...
...
````
The first chunk prints the info.
it could be just `.msg` or could be richer if the info contained more data.
But we also get this **stack trace** details.
What is that about?
It is about the set of active calls when the error leaked.

For example
````
Stuff:{
  .foo (myList)->myList.get(5);
  .bar -> try{this.foo};
  .beer1 -> this.bar!
  .beer2 -> this.bar.context `InBeer2`!
  }
````

`this.beer1` would have
`list too short`
stack trace `Stuff.beer1, Stuff.foo, List.get`.

`this.beer2` would have
```
InBeer2
list too short
```
stack trace `Stuff.beer2`
As you can see, the call to context added custom text but removed information from the stack trace.
>Is this what we want? big design decision. The other implementation where we keep the original stack trace must also be possible. (mutate the exception obj and re-throw it)

We can capture this information programmatically with the capability `System.try`

````
example.
````

Code `capTry#{...}` creates an `Action[R]` whose `Info` is going to be the sum of the
normal `Info` and the info containing the stack trace.
> How? list of string or structured?

Moreover, `System.try` allows to capture some sneaky errors, known as **non deterministic errors**.
Non deterministic errors are errors that may change depending on the specific system limitations and set up.
They include:
- Memory overflow: discuss
- Stack overflow: discuss
- Assertion errors (since they can be disabled? Discuss)
- Arithmetic errors (since they can be disabled? Discuss)
- Other? Discuss

>TODO: Right now _Throw does
>  .msg[R:**](msg: Str): R -> _Throw.deterministic(Infos.msg msg + (_Throw.stackTrace));
>  .nonDeterministic[R:**](msg: Str): R -> _Throw.nonDeterministic(Infos.msg msg + (_Throw.stackTrace));
>Instead, it should be in the catch:
> catch(Deterministic d){ return m.mut$info$1(d.i); }
> catch(NonDeterministic d){ return m.mut$info$1(d.i); }


As you can imagine, it is quite rare to have to capture non deterministic errors; but there are many reasonable examples:
- Testing framework: this is code that runs and supervision tests.
  Any kind of failure should be captured and handled/recorded/logged in some way.
- Web server: It must run many different code serving web pages, and a page error, no matter how severe, should not terminate the whole web server.
- Try different strategies from a database of options to solve a problem. Then record statistical data about what option solved the problem better and faster to produce self improving programs.
- Airplane or nuclear plant supervision system: you probably do not want it to terminate, no matter the error.
- Videogame where users can somehow forge code either directly or by combining magic objects with standard effects.

> need a Cap that give you current line number and file name!

# Offensive programming
> should be a section of the guide 
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


The whole idea of offensive programming is that code should fail immediately when an unexpected situation is detected.
This does have a cost: the programmer must take an active role and write checks to detect unexpected situations.

The standard library helps: methods like ...... all fail early.
But, in a real program 90% of the code is new abstractions written by the programmer; those abstractions will have their own unexpected situations to check for.

When programmers learn about offensive programming, they are usually scared about two issues:
Performance and false positives.
In Fearless both issues are solved at the same time by tunable assertion levels.

#### Tunable assertion levels:
There are many different layers of assertions:
 - assert:  observed bug: the code of this package has a bug.
 - assertPre: precondition violation: the code of this package has been called with parameters that violate it's requirements.
 - assertSys: The program is fine but the system running this program does not support the needed requirements. For example, a file must be present for the application to work; memory or stack are insufficient; OS resources can not be obtained.
 - assertNetwork: The program and the system are fine, but some needed remote service is unavailable.
 - given that there are already 4 levels, we also have `.assert5`... `.assert16` to cover other user defined situations. `assert1`..`assert4` are aliases for those shown named assertion kinds.

Each package can disable/enable each of those levels.
That is,
````
//in file _foo/bar.fear
Block#
  .assertPre{ x.checkInRange(0,10) }
  .do{...}
  ..
````
would simply discard the literal `{ x.checkInRange(0,10) }` if the code is compiled with 
```
disable foo.assertPre;
```
> exact syntax to discuss

Another form of assertions is useful when we want to provide alternative behaviour upon disabling:

````
//in file _foo/bar.fear
Block# //many options for the actual API
  .let content= {AssertSys.or({..readFile `foo.txt` ..},{..ask user for existing file with file choser..} }
  .let foo= AssertPre#(foo.bar(),{::.inRange(3,25)},{_->10})
  .do{...}
  ..
````
Here if the file `foo.txt` is not present, we would give an error, or ask the user for another location if
```
disable foo.assertSys;
```
is present.

This also works for numeric checks:
```
disable base.intOverflowChecks;
```
would make `+` behave exactly as the faster `.aluxxx` variant.
```
disable base.intOverflowChecks and caps;
```
this instead would suppress the overflow/underflow behaviour and cap the result to max/min `Int`.

> I'm just throwing ideas to the wall here.

So if the condition is false/throws an assert can
- fail
- be ignored
- use a recovery expression (only available if recovery provided)
- write the failure on a log and continue
- use a recovery expression and write the failure on a log.(only available if recovery provided)

This encompass the ignore/evaluate the check and ignore/evaluate the recovery.
An assert without a recovery is equivalent to an assert where the recovery is the identity function, so we pass the same value, and an assert with no value is equivalent to having/returning `Void`.

Under this, disabled is kind of ambiguous and we may want more names.

The idea is that we want to have a Software produce line:
Many different kinds of software are produced depending on the assertion profile, and some of those are better for testing instead of deployment.

>TODO: API: we need a matcher form of the int to float etc methods, where if the number can not be represented cleanly in the other format we get some info (delta?) to a dedicated method to provide alternative behaviour


> Discuss the various forms of match: match with datatype, match without data type (.cmp), match over data that exists (stack etc), match over computations (action); note how composing those two kinds of match is different; Finally matchers can carry extra parameters and pass / wire them around; consider CapTry with iso.

OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}