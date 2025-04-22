package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_011Chapter02Optional {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Optionals

### Optional
There are just two kinds of Booleans: `True` and `False`.
In programming, there are many cases where there are two kinds of an abstract idea.
Another very popular case is Optionals:
An optional represents the presence or absence of a certain unit of data.
We can think of optionals as a kind of boolean where `True` holds some extra information.

You should now be able to read the code below and understand it.
-------------------------*/@Test void opt1 () { run("""
//|OMIT_START
package test
alias base.Void as Void,
//OMIT_END
Opt[T]: {
  .match[R](m: OptMatch[T,R]): R -> m.empty
  }
OptMatch[T,R]: {
  .empty: R,
  .some(t: T): R,
  }
Opts: {
  #[T](t: T): Opt[T] -> { .match(m) -> m.some(t) }
  }
"""); }/*--------------------------------------------

The code below can be used as follows:
-------------------------*/@Test void opt2 () { run("""
//|OMIT_START
package test
alias base.Void as Void,
alias base.Nat as Nat,
Opt[T]: {
  .match[R](m: OptMatch[T,R]): R -> m.empty
  }
OptMatch[T,R]: {
  .empty: R,
  .some(t: T): R,
  }
Opts: {
  #[T](t: T): Opt[T] -> { .match(m) -> m.some(t) }
  }
Person:{ .age:Nat}
A:{#(bob:Person):Opt[Person]->
//OMIT_END
Opts#bob //(1) Bob is here

//OMIT_START
}B:{#(bob:Person):Opt[Person]->
//OMIT_END
Opt[Person] //(2)no one is here

//OMIT_START
}C:{
//OMIT_END
.age(p: Opt[Person]): Nat-> p.match{  //(3) age or zero
  .empty    -> 0,
  .some(p') -> p'.age, //Yes, p' is a valid parameter name
  }
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

Here we show (1) how to make an optional containing a `Person`,
(2) an empty optional and (3) how to make a method using an optional person.

Optionals and booleans are used a lot in Fearless programming.
Optionals and booleans in the Fearless standard library are conceptually identical to the ones we have shown here.
There are a few minor differences because of some language features that we have not seen yet; and a few extra utility methods.
- Later, we will show you the exact implementation as in the Fearless standard library.
- The implementation we have just seen is a very good mental model to conceptualise those two types.

END*/
}
