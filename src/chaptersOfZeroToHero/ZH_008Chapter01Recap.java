package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
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

#### Syntactical elements

- Type name:
  - Relevance: giving type names to concepts allows us to talk about concepts and to classify them into cases
  - Syntax: Uppercase starting identifier, including letters, numbers underscores but not spaces
  - Examples: `North`, `Direction`
  
- Type:
  - Relevance: A value is accessed via a parameter with a certain type. Types help control what we can do on values.
  - Syntax: Simple types are just type names. We will soon see more syntax for more expressive types.
  - Examples: `Direction`, `Rotation`, `Tank`
  
- Method name:
  - Relevance: giving names to methods (aka operations) allows to mnemonically connect behaviour with names.
  - Syntax: dot `.` followed by lowercase identifier, including letters, numbers underscore but no spaces; or operator symbols.
  - Examples: `.foo`, `._bar`, `._12`, `.baz`, `+`, `++`, `+>`, `<=`, `*|/-`
  
- Parameter name:
  - Relevance: parameter names describe the role the parameter value will exercise inside the method execution. A parameter that is intended to be unused can be called `_`.
  - Syntax: lowercase identifier; can also have `'` at the end
  - Examples: `foo` in `.baz(foo:Bar)`, `.baz(foo)` or `{foo->..}`
  
- Argument:
  - Relevance: passing expressions to method calls allows to provide a value to the parameter. This value is then used during the method execution in order to compute the method result.
  - Syntax: any expression
  - Examples: `1+2` or `foo` in `this.baz(1+2)`, or in  `this.baz(foo)`
  
- Method:
  - Relevance: methods contain expressions (instructions/behaviour) and allow us to run/execute/start those instructions.
  They can be abstract or concrete.
  - Examples:<BR/>
  `mut .foo(a: A): B;` (abstract method),<BR/>
  `mut .foo(a: A): B -> a.toB;` (concrete method full form),<BR/>
  `mut .foo(a: A): B -> a.toB;` (concrete method inferred types),<BR/>
  `a->a.foo` or `a.foo` inside `{a->a.foo}` and `{a.foo}` 
  (concrete method inferred name and types),

- Expression:
  There are three kinds of expression: parameters, method calls, and object literals.
  - Relevance: Expressions are the way we instruct the system to perform actions or calculate results.

- Parameter
  - Examples: `this`, `foo`, `bob`, `alice`, `x`, `x'`,   `_foo`,  `_12`
- Method call
  - Examples: `this.bar(foo.baz)`, `foo+`, `foo+(bar)`, `foo!`

- Object literal: We use many different names for object literals, to focus the attention of some aspects. But they are all just aliases for the same concept. Common aliases: Object, Value, Constant, Lambda, Instance.

- Object: used to focus on the idea that the literal has behaviour and captures state
  - Example:  `Point{ .x: Int-> x; .y: Int-> y; }`, `{ .foo->myFoo; }`

- Value: used to focus on the idea that the literal is produced by a computation
  - Example: `Points#(4,5)` returns a value.

- Constant: used when type names are used directly to make instances. Constants can not capture state, so all the instances of `North` are conceptually identical; they are conceptually all the same `North`.
  - Examples: `North`, `12`, `45`, `+34`, `-13`, `` `Foo` ``, `` `foo dd` ``, `{}`
(the last one will have the type name inferred by the type system)

- Lambda: this is the common name given to an object literal with exactly one method is implemented. Syntactic sugar is present to make this case easier.
  - Examples: `{a,b->a+b}`,  `{ a -> a.foo }`, `{ 1 + 2 }`, `{::foo}`, `{::foo.bar }`, `{::+3 }`

- Instance: used to focus on types and subtypes relationships
  - Example1: `Tank: { .heading-> North; .aiming-> North;}`
   is an instance of `Tank`.
  - Example2: `North`
   is an instance of `North` and also an instance of `Direction`, via subtyping.

#### `North` as an instance / `North` as a type

Note how in the sentence before, the first `North` is the expression `North`, equivalent via desugaring to `Anon[]:North[]{}`, while the 
  second `North` is the type `North`.
To get the `North` value/constant as an expression, we can just mention the `North` type.

That is, 
- when we write a type after a `:`, we are using it as a type
- when we write a type inside an expression, we are implicitly using it to summon a value of that type.
  
OMIT_START
-------------------------*/@Test void emptySquareOk () { run("""
Direction:{.point: Point->Points#(+0,+1)}
Points:{#(x: Int, y: Int): Point -> Point[]:{'self
  .x: Int -> x;
  .y: Int -> y;
  +(other: Point): Point -> Points[]#(other.x + x, other.y + y);
  .move(d: Direction): Point -> self + ( d.point[] );
  }}
"""); }/*--------------------------------------------
//OMIT_END

END*/
}
