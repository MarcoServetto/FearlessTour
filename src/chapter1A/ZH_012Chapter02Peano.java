package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_012Chapter02Peano {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Peano numbers

### Peano and the infinite range of natural numbers

We have seen how in the standard library we have many finite, but gigantic, number types:
There are  2<sup>64</sup> - 1 instances of `Nat` and there are 96<sup>2,147,483,647</sup> − 1 instances of `Str`.
Each `Nat` can be stored in exactly 4 bytes, were a byte is eight bit.
Strings use an incremental space consumption; this means that storing small strings would use only a little amount of memory (with 34 bytes being the minimum size; still much more than the 4 bytes needed for a `Nat`.

On the other extreme, storing a single element of a string near to the maximum representable size would take about 2 GB (two giga bytes).
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
  .pred: Number,
  .succ: Number -> {this}, // equivalent to .pred->this
  }
Zero:Number { this.pred } // equivalent to .pred->this.pred
"""); }/*--------------------------------------------

As you can see, it is confusingly simple and minimal.
Here some examples of peano numbers:
```
Zero  //0
Zero.succ  //1 == {Zero}
Zero.succ.succ //2 ==  {{Zero}}
```
By continuing this sequence, we can represent any kind of number.
With `Nat`, there was a type representing zero, a type representing one, a type representing two and so on.
Note how this is not the case for Peano numbers. There is not a type representing the number one, two and so on.
Numbers are created as needed using the `Number.succ` method.

We can add operations to our Peano numbers as follows:

-------------------------*/@Test void peano2 () { run("""
Number:{
  .pred: Number,
  .succ:Number->{ this },
  +(other: Number): Number -> this.pred + (other.succ),
  *(other: Number): Number -> this + (this.pred * other),
  }
Zero:Number {
  .pred -> this.pred,
  +(other) -> other,
  *(other) -> this,
 }
"""); }/*--------------------------------------------
As you can see, this is very similar to the way we encoded those operations for finite number sets, like `Nat`.
The fearless standard library does not support Peano numbers. As we have shown you, it is very easy to implement them if you need.

However, the fearless standard library supports the `Num` type.
A value of type `Num` represents an arbitrary large fractional number.
Something of the form `a/b` where 
 - `a` can be an arbitrary large integer number; positive or negative.
 - `b` can be an arbitrary large positive natural number, (different from zero).

This is now a good time to summarise how to write numbers in fearless:
- `Nat` is the type of natural numbers, and we write them as `0`,`1`,`2`,.... 
- `Int` is the type of signed integers, and we write them as `-2`,`-1`,`+0`, `+1`, `+2`,... 
- `Num` is the type of arbitrarily large fractions, and we write them as `+12/1`, `-13.004/75.5`, ...

Basically, if we use the fraction symbol `/` we talk about those arbitrarily large fractional numbers.
It is very common to write large numbers followed by `/1` as a way to specify that we mean arbitrary size numbers.
For example `+249023892334949590290854892389343489723789478923/1` is a very large instance of `Num`; much bigger than what it can be represented with `Nat` or `Int`.

OMIT_START
-------------------------*/@Test void num1 () { run("""
package test
alias base.Void as Void,
alias base.Num as Num,
alias base.Main as Main,
+23/4:{}
Test:Main{s->base.Debug#(+23/4)}
"""); }/*--------------------------------------------
//OMIT_END

END*/
}
