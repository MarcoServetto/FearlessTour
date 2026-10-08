package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_012Chapter02Peano {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Peano numbers

### Peano and the infinite range of natural numbers

We have seen how in the standard library we have many finite, but gigantic, number types:
There are 2<sup>64</sup> instances of `Nat` and there are 
just a little more than 10<sup>4,256,895,041</sup> instances of `Str`.
Each `Nat` can be stored in exactly eight bytes, where a byte is eight bits.
Strings use an incremental space consumption; this means that storing small strings would use
only a small amount of memory (still much more than the 8 bytes needed for a `Nat`, even for the empty string).

On the other extreme, storing a single string near the maximum representable size would take about 2 GB (two gigabytes).
2 GB is a large amount of memory, but nowadays we have computers with many times more memory than that.
Such a string would be very, very long. If we were to print it on conventional A4 paper with the standard 10 point font size and make a book out of it, that book would be tens of metres tall, about as tall as a 10-storey building.
Big, but still not infinite. I mean, actually quite small,... we have many buildings taller than that!

Can we represent an actual infinite set of numbers?
Of course we would not be able to actually store in memory numbers of any size; but we can represent numbers as big as our memory allows.
Below, you can see an implementation for Peano numbers.
Peano numbers are a number representation where numbers are represented as a **Zero** or a **Successor** of another number.
We can encode Peano numbers in Fearless as follows:

-------------------------*/@Test void peano1 () { run("""
Number:{
  .pred: Number;
  .succ: Number -> {this}; // equivalent to .pred->this
  }
Zero:Number { this.pred } // equivalent to .pred->this.pred
"""); }/*--------------------------------------------

In `.succ: Number -> {this}`, `this` is the number whose `.succ` is called: the new number is a literal answering `.pred` with that number.

As you can see, it is confusingly simple and minimal.
Note how `Zero.pred` just calls `Zero.pred` again: zero has no predecessor, and asking for it is a computation that never terminates (a real run ends with a stack overflow error).
Here are some examples of Peano numbers:
```
Zero  //0
Zero.succ  //1 == {Zero}
Zero.succ.succ //2 ==  {{Zero}}
```
By continuing this sequence, we can represent any natural number.
With `Nat`, there was a type representing zero, a type representing one, a type representing two and so on.
Note how this is not the case for Peano numbers. There is not a type representing the number one, two and so on.
Numbers are created as needed using the `Number.succ` method.

We can add operations to our Peano numbers as follows:

-------------------------*/@Test void peano2 () { run("""
Number:{
  .pred: Number;
  .succ:Number->{ this };
  +(other: Number): Number -> this.pred + (other.succ);
  *(other: Number): Number -> (this.pred * other) + other;
  }
Zero:Number {
  .pred   -> this.pred;
  + other -> other;
  * other -> this;
 }
"""); }/*--------------------------------------------
As you can see, this is very similar to the way we encoded those operations for finite number sets, like our clock numbers from `0` to `11`.
The Fearless standard library does not support Peano numbers. As we have shown you, it is very easy to implement them if you need to.

However, the Fearless standard library supports the `Num` type.
A value of type `Num` represents an arbitrarily large fractional number.
Something of the form `a/b` where 
 - `a` can be an arbitrarily large integer number; positive or negative.
 - `b` can be an arbitrarily large positive natural number (different from zero).

This is now a good time to summarise how to write numbers in Fearless:
- `Nat` is the type of natural numbers, and we write them as `0`,`1`,`2`,.... 
- `Int` is the type of signed integers, and we write them as `-2`,`-1`,`+0`, `+1`, `+2`,... 
- `Num` is the type of arbitrarily large fractions, and we write them by dividing a `Nat` or an `Int` by a `Nat`, as in `+12/1`, `-13/75`, `1/3`, ...
- `Float` is the type of approximate fractional numbers, written with a decimal point, as in `3.5` or `-4.75`.

Basically, if we use the fraction symbol `/` on natural numbers or integers we get those arbitrarily large fractional numbers.
It is very common to write numbers followed by `/1` as a way to specify that we mean arbitrary size numbers.
For example `(18446744073709551615/1) * (18446744073709551615/1)` is a very large instance of `Num`; much bigger than what can be represented with `Nat` or `Int`.
`Num` has no literal syntax of its own: a `Num` whose numerator or denominator is too large to be written as a `Nat` or `Int` literal is obtained by parsing a string with `.getNum`.
For example `"123456789012345678901234567891/7".getNum` is a `Num` whose numerator is far beyond the largest `Nat`.
OMIT_START
-------------------------*/@Test void num1 () { run("""
use base.Void as Void;
use base.Num as Num;
use base.Main as Main;
use base.Block as Block;
Test:Main{s->base.Debug#(+23/4)}
//PRINT|+23/4
"""); }/*--------------------------------------------
//OMIT_END

//OMIT_START
-------------------------*/@Test void peanoBehaviour() { run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Debug as Debug;
use base.Str as Str;
use base.Nat as Nat;
Test: Main{s-> Block#
  .do{Debug#(Zero.n)}
  .do{Debug#(Zero.succ.n)}
  .do{Debug#(Zero.succ.succ.n)}
  .do{Debug#((Zero.succ.succ + (Zero.succ.succ.succ)).n)}
  .do{Debug#((Zero.succ.succ * (Zero.succ.succ.succ)).n)}
  .do{Debug#((Zero * (Zero.succ.succ)).n)}
  .do{Debug#((Zero.succ.succ * Zero).n)}
  .return{Void}}
Number:{
  .pred: Number;
  .succ:Number->{ this };
  .n: Nat -> this.pred.n + 1;
  +(other: Number): Number -> this.pred + (other.succ);
  *(other: Number): Number -> (this.pred * other) + other;
  }
Zero:Number {
  .pred   -> this.pred;
  .n -> 0;
  + other -> other;
  * other -> this;
 }



//PRINT|0
//PRINT|1
//PRINT|2
//PRINT|5
//PRINT|6
//PRINT|0
//PRINT|0

"""); }/*--------------------------------------------
//OMIT_END
//OMIT_START
-------------------------*/@Test void numLiterals() { run("""
use base.Void as Void;
use base.Main as Main;
use base.Block as Block;
use base.Debug as Debug;
use base.Str as Str;
use base.Nat as Nat;
use base.Num as Num;
Test: Main{s-> Block#
  .do{Debug#(+12/1)}
  .do{Debug#(1/3)}
  .do{Debug#(-13/75)}
  .do{Debug#((18446744073709551615/1) * (18446744073709551615/1))}
  .do{Debug#("123456789012345678901234567891/7".getNum)}
  .do{Debug#((1/3) + (1/6))}
  .do{Debug#((2/4) == (1/2))}
  .return{Void}}


//PRINT|+12/1
//PRINT|+1/3
//PRINT|-13/75
//PRINT|+340282366920938463426481119284349108225/1
//PRINT|+123456789012345678901234567891/7
//PRINT|+1/2
//PRINT|True

"""); }/*--------------------------------------------
//OMIT_END
END*/
}
