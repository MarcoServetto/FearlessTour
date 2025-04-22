package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_008Chapter01Recap {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Terminology Recap

### Terminology Recap

#### Language Concepts
- Evaluation: An expression evaluates to a result by slowly progressing into a more and more reduced form
  - Example: `North.turn.turn --> East.turn --> South`
- Supertype: Types are connected by supertypes relations allowing to generalise concepts.
This is also used for code reuse.
  - Example: `North` and `South` are types of `Direction` so `Direction` is a supertype of `North` and `South`.
- Implement(s): By implementing a supertype a type reuses the code of the supertype and is required to satisfy the logical contracts of the super type. Satisfaction of those (informal) contracts is the responsibility of the programmer, but the type system can catch some common violations. Supertypes and Implements help code reuse and enable  dynamic dispatch.
  - Example: `North` implements `Direction`

- Inherited: a method that comes from an implemented supertype.

- Indentation and code formatting: The deliberate act of adding spacing and new lines in order to make the code more readable for humans. Those added symbols are ignored by the compiler.

- Syntactic sugar: Many syntactical forms can be expressed in an extended or a more compact form. Those more compact forms are called syntactic sugar and are to be understood as their expansion.

- Scope: A parameter is only visible in their scope. A method parameter is visible inside the method body. 


#### Syntactical elements

- TypeName:
  - Relevance: giving types to concepts allows us to talk about concepts and to classify them into cases
  - Syntax: Uppercase starting identifier, including letters, numbers underscore but no spaces
  - Examples: `North`, `Direction`
  
- Type:
  - Relevance: types are instantiated typenames. A value is accessed via a binding with a certain type. Types help control what we can do on values.
  - Syntax: Simple types are just typenames. We will soon see more syntax for more expressive types.
  - Examples: `Direction`, `Rotation`, `Tank`
  
- MethodName:
  - Relevance: giving names to methods (aka operations) allows to mnemonically connect behaviour with names.
  - Syntax: dot followed by lowercase identifier, including letters, numbers underscore but no spaces; or operators
  - Examples: `.foo`, `._bar`, `._12`, `.baz`, `+`, `++`, `+>`, `<=`, `*|/-`
  
- Formal parameter/argument:
  - Relevance: giving names to parameters allows to describe the role the parameter value will exercise inside the method execution. A parameter that is intended to be unused can be called '_'.
  - Syntax: lowercase identifier, can have `'` at the end
  - Examples: `foo` in `.baz(foo:Bar)`, `.baz(foo)` or `{foo->..}`
  
- Actual parameter/argument:
  - Relevance: passing expressions to method calls allows to provide an actual value to the formal parameter. This actual value is then used during the method execution in order to compute the method result.
  - Syntax: any expression
  - Examples: `1+2` or `foo` in `this.baz(1+2)`, or in  `this.baz(foo)`
  
- ParameterName:
  - Relevance: general word used to capture both a formal parameter and a binding focusing the attention on how this binding comes from a formal argument.
  - Syntax: lowercase identifier, can have `'` at the end
  - Examples: any binding can be referred to as a parameter.
  
- Method:
  - Relevance: methods contain expressions (instructions/behaviour) and allow us to run/execute/start those instructions.
  They can be abstract or concrete.
  - Examples:<BR/>
  `mut .foo(a: A): B,` (abstract method),<BR/>
  `mut .foo(a: A): B -> a.toB,` (concrete method full form),<BR/>
  `mut .foo(a: A): B -> a.toB,` (concrete method inferred types),<BR/>
  `a->a.foo` or `a.foo` inside `{a->a.foo}` and `{a.foo}` 
	(concrete method inferred name and types),

- Expression:
  There are three kinds of expression: literals, method calls, and parameters.
  - Relevance: Expressions are the way we instruct the system to perform any actions or calculate results.

- Literal: We use many different names for literals, to focus the attention of some aspects. But they are all just aliases for the same concept. Common aliases: Object, Value, Instance, Constant/Singleton, Lambda.

- Object: used to focus on the idea that the literal has behaviour and captures state
  - Example:  `Point{ .x: Int-> x, .y: Int-> y, }`, `{ .foo->myFoo, }`

- Value: used to focus on the idea that the literal is produced by a computation
  - Example: `Points#(4,5)` returns a value.

- Instance: used to focus on types and subtypes relationships
  - Example: `North` is an instance of `Direction`. This instance knows how to move points up.
  
- Constant/Singleton: used when type names are used directly to make instances. Constants can not capture state, so all the instances of `North` are conceptually identical; they are conceptually all the same `North`.
  - Examples: `North`, `12`, `45`, `+34`, `-13`, `` `FOO` ``, `` `foo\ndd` ``, `{}`
(the last one will have the type name inferred by the type system)
- Lambda: used when exactly one method is implemented. Much syntactic sugar is present to make this case easier.
  - Examples: `{a,b->a+b}`,  `{ a -> a.foo }`, `{ 1 + 2 }`, `{::foo}`, `{::foo.bar }`, `{::+3 }`
- Parameter
  - Examples: `this`, `foo`, `bob`, `alice`, `x`, `x'`,   `_foo`,  `_12`
- Method call
  - Examples: `this.bar(foo.baz)`, `foo+`, `foo+(bar)`, `foo!`

OMIT_START
-------------------------*/@Test void emptySquareOk () { run("""
Direction:{.point: Point->Points#(+0,+1)}
Points:{#(x: Int, y: Int): Point -> Point[]:{'self
  .x: Int -> x,
  .y: Int -> y,
  +(other: Point): Point -> Points[]#(other.x + x, other.y + y),
  .move(d: Direction): Point -> self + ( d.point[] ),
  }}
"""); }/*--------------------------------------------
//OMIT_END

END*/
}
