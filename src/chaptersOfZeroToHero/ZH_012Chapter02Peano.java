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
only a small amount of memory (with 34 bytes being the minimum size; still much more than the 8 bytes needed for a `Nat`).

On the other extreme, storing a single element of a string near to the maximum representable size would take about 2 GB (two gigabytes).
2 GB is a large amount of memory, but nowadays we have computers with thousands of times more memory than that.
Such a string would be very, very long. If we were to print it on conventional A4 paper with the standard 10 points font size and make a book out of it, that book would be more than 30 meters tall; taller than a 10-storey building.
Big, but still not infinite. I mean, actually quite small,... we have many buildings taller than that!

Can we represent an actual infinite set of numbers?
Of course we would not be able to actually store in memory numbers of any size; but we can represent numbers as big as our memory allows.
Below, you can see an implementation for Peano numbers.
Peano is a number representation where numbers are represented as a **Zero** or a **Successor** of another number.
We can encode Peano numbers in Fearless as follows:

-------------------------*/@Test void peano1 () { run("""
Number:{
  .pred: Number;
  .succ: Number -> {this}; // equivalent to .pred->this
  }
Zero:Number { this.pred } // equivalent to .pred->this.pred
"""); }/*--------------------------------------------

As you can see, it is confusingly simple and minimal.
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
As you can see, this is very similar to the way we encoded those operations for finite number sets, like `Nat`.
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

Basically, if we use the fraction symbol `/` on natural numbers or integers we get those arbitrarily large fractional numbers.
It is very common to write numbers followed by `/1` as a way to specify that we mean arbitrary size numbers.
For example `(18446744073709551615/1) * (18446744073709551615/1)` is a very large instance of `Num`; much bigger than what can be represented with `Nat` or `Int`.
`Num` has no literal syntax of its own: a `Num` whose numerator or denominator is too large to be written as a `Nat` or `Int` literal is obtained by parsing a string with `.getNum`.
For example `"123456789012345678901234567890/7".getNum` is a `Num` whose numerator is far beyond the largest `Nat`.
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

END*/
}
