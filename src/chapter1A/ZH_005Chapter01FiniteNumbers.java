package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_005Chapter01FiniteNumbers {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Finite numbers

### Finite Numbers and Modulo Arithmetic.
The example of the four cardinal directions is intriguing but limited, given there are only four options. Many things, including numbers, come in much larger, even seemingly unlimited quantities. Before exploring sets with infinite elements, let's discuss arithmetic within a finite set of numbers.

Consider a set of numbers that functions like the hours on a clock. Typically, a clock displays 12 hours. After 12 o'clock, the next hour doesn't advance to 13; instead, it cycles back to 1. In this system, each number has a predefined position, and upon reaching the highest number, the sequence loops back to the start.

While clocks numerically range from 1 to 12, starting from zero offers better mathematical properties. For instance, with a set of 12 numbers ranging from 0 to 11, after 11 comes 0, and the cycle continues.
This system is formally known as **modulo arithmetic**. The term 'modulo' refers to the number of elements in our set, akin to the 12 in a 12-hour clock. Modulo arithmetic allows us to add, subtract, and multiply numbers within this finite set, always accounting for the cyclical nature of our number sequence.
Here we can see an example of encoding numbers from 0 to 11 in Fearless, where `.pred` and `.succ` represent predecessor and successor: the number before and after the current number.
```
Number:{ .pred:Number, .succ:Number,}
 0: Number{.pred-> 11, .succ->  1, } //before 0 wraps to 11, after 0 is 1
 1: Number{.pred->  0, .succ->  2, }
 2: Number{.pred->  1, .succ->  3, }
 3: Number{.pred->  2, .succ->  4, }
 4: Number{.pred->  3, .succ->  5, }
 5: Number{.pred->  4, .succ->  6, }
 6: Number{.pred->  5, .succ->  7, }
 7: Number{.pred->  6, .succ->  8, }
 8: Number{.pred->  7, .succ->  9, }
 9: Number{.pred->  8, .succ-> 10, }
10: Number{.pred->  9, .succ-> 11, }
11: Number{.pred-> 10, .succ->  0, } //before 11 is 10, after 11  wraps to 0
```
The code above is clear and very similar to the code of `Direction`.
Method `.succ` works like the method `.turn` and method `.pred` is just turning in the other way. Thus, the predecessor of `0` is `11` and the successor of `11` is `0`.
That is, `10.succ` is `11` and `10.succ.succ.succ` is `1`.
Note how we are using numbers as type names.
Those numbers just have `.succ` and `.pred`. Can we define an operation `+` doing addition?

One way might be to define `+` for each number individually:

```
// Overly verbose approach
Number:{ 
  .pred:Number, .succ:Number,
  +(other: Number): Number
  }
 0: Number{
   .pred-> 11, .succ->  1,
   +(other)-> other,
   }
 1: Number{
   .pred-> 11, .succ->  1,
   +(other)-> other.succ,
   }
 2: Number{
   .pred-> 11, .succ->  1,
   +(other)-> other.succ.succ,
   }
 ...//all other numbers as before
```
Where we simply call `.succ` a large amount of times.

This works, and for just 12 numbers, it's barely feasible. But imagine doing this for thousands or millions of numbers.
Imagine writing a chain of `.succ` thousands or millions of calls long! It would be incredibly repetitive and impractical. We need a more general approach.



#### Inductive definitions: Building from simplicity


Instead of defining `+` for every single number, we can define it using just two rules that build upon each other. This powerful technique is called an inductive definition.

**Base Case:** The simplest case is adding zero. Adding zero to any number a doesn't change it: `0 + a => a`.

**Inductive Step:** How do we add a non-zero number (let's call it `this`) to another number `a`?
- We can think of this as being "one more than its predecessor": `this` = `this.pred + 1`.
- Thus we can use this mathematical property 
`(x + 1) + y = x + (y + 1)` to simplify the problem!
- In order to calculate `this + a`, we can calculate 
`(this.pred) + (a.succ)`. 
- We have made the left side smaller: `this.pred` is a smaller number than `this`
- In this way, we get closer and closer to the base case zero.
- We repeat this step until the left side becomes zero, and then we use the base case rule.



#### Implementing addition inductively in Fearless:

Let's translate these rules into Fearless code. We define a general method `Number+` for the inductive step and a specific 
`0+` method for the base case, overriding the general one.

```
Number:{ 
  .pred:Number, .succ:Number,
  +(other: Number): Number -> this.pred + (other.succ)
  }
0: Number{
 .pred-> 11, .succ->  1,
 +(other)-> other,
 }
1: Number{.pred->  0, .succ->  2, }
...
10: Number{.pred->  9, .succ-> 11, }
11: Number{.pred-> 10, .succ->  0, }
```
We have two implementations of `+`: one in `Number`, that is **inherited** by all the numbers except `0`. Number `0` **overrides** the method `+` with a different implementation.
That is, types `1` through `11` inherit the `+` method from `Number`.

The Method `+` above directly encodes the intended inductive logic
```
0 + a = a            //base case
(a+1) + b = a + (b+1)//inductive case
```
with `a` and `b` being any two numbers.
- The base case works on zero: adding any number to zero just returns that number.
That is, if the left number is zero, we know the result of addition without the need of computing it.
- The inductive case is used if the left number is not zero. If the number is not zero we can see the left number as if it is composed of a number `a` plus `1`.
In our code this is represented by calling `this.pred`; that is, the number `this` is equivalent to `this.pred + 1`.
We then move this `+1` to the right of the `+` operator by using succ.
That is, `other.succ` is equivalent to `other+1`
Putting those ideas together we get the code of `Number+`:
```
+(other: Number): Number -> this.pred + (other.succ)
```
This reasoning is based on the idea that it is very easy to sum a number with zero, and that we can slowly reduce any addition to a sum with zero by moving units around.


We now show the reduction for `3 + 2`.
Note how in the first step `this` is `3` (not `0`) and 'other' is 2. Thus we apply method`Number+`.

01. `3 + 2`  
02. `3.pred + 2.succ`
03. `2 + 2.succ`
04. `2 + 3`
05. `2.pred + 3.succ`
06. `1 + 3.succ`
07. `1 + 4`
08. `1.pred + 4.succ`
09. `1.pred + 4.succ`
10. `0 + 4.succ`
11. `0 + 5`
12. `5`
In step 11, `this` is `0`, and thus we apply method `0+`.

This inductive approach, defining a base case and a rule to reduce other cases towards the base case, is fundamental in programming and especially elegant in object-oriented languages like Fearless.

> **Inductive thinking:** When we define methods like Number.+  partly in terms of themselves, it might look like circular reasoning. It's not! This technique is simply about defining behavior based on the structure we've already established.
>For numbers, we define addition based on the previous number. We're breaking the problem down into a simpler version of itself plus one step. This way of thinking is crucial for any kind of programming, but it may take a while to get used to it.
>With enough exercise, most humans become very proficent with inductive thinking. It is not hard, it is alien.

> **Inductive thinking:** When we define methods like `Number.+` partly in terms of themselves, it might look like circular reasoning. It's not! This technique is simply about defining behavior based on the structure we've already established. For numbers, we define addition based on the previous number.
We are breaking the problem down into a simpler version of itself plus one step.
>This way of thinking is crucial for any kind of programming, but it may take a while to get used to it. It requires a shift in perspective. With practice, however, this inductive approach becomes a powerful and logical tool, not because it's inherently hard, but because it's initially unfamiliar.
>Experience shows that with dedicated practice, people become eventually become proficient in inductive thinking.
It's a common hurdle, but definitely a conquerable one.

#### Implementing multiplication inductively in Fearless:

Building on those ideas, we can encode the other operations of numbers.
We now show with multiplication:
```
Number:{ 
  .pred:Number, .succ:Number,
  +(other: Number): Number -> this.pred + (other.succ)
  *(other: Number): Number -> (this.pred * other) + other
  }
0: Number{
  .pred-> 11, .succ->  1,
  +(other)-> other,
  *(other)-> 0,
  }
1: Number{.pred->  0, .succ->  2, }
...
10: Number{.pred->  9, .succ-> 11, }
11: Number{.pred-> 10, .succ->  0, }
```
Here the inductive logic is as follows:
```
0 * a = 0                //base case
(a+1) * b = (a*b) + b //inductive case
```

Logically, how do we multiply a non-zero number `this` by another number `other`? Again, think of `this` as `this.pred + 1`. We can apply the mathematical property 
`(x + 1) * y = (x * y) + y`.
Multiplying `this` by `other` is the same as first multiplying `this.pred` by `other`, and then adding `other` to that result. This reduces the multiplication problem `this * other` to a smaller multiplication `this.pred * other` plus an addition (`+ other`), eventually reaching the base case where the left side is `0`.

#### Reduction examples and operator precedence

As an exercise, we can look to the reduction of some arithmetic operations:
01. `2 * 3 + 1`
02. `2.pred * 3 + 3 + 1`
03. `1 * 3 + 3 + 1`
04. `1.pred * 3 + 3 + 3 + 1`
05. `0 * 3 + 3 + 3 + 1`
06. `0 + 3 + 3 + 1`
07. `3 + 3 + 1`
08. ...
09. `6 + 1`
10. ...
11. `7`

Note how here we do get `7` as we would expect with the normal precedence of multiplication and addition.

However, look what happens when we reduce 
`1 + 2 * 3`, equivalent to `1+(2)*(3)` and thus to `(1+2)*3`:

01. `1 + 2 * 3`
02. `1.pred + (2.succ) * 3`
03. `(0 + (2.succ)) * 3`
04. `0 + 3 * 3`
05. `3 * 3`
06. `(3.pred * 3) + 3`
07. `(2 * 3) + 3`
08. `(2.pred * 3) + 3 + 3`
09. `(1 * 3) + 3 + 3`
10. `(1.pred * 3) + 3 + 3 + 3`
11. `(0 * 3) + 3 + 3 + 3`
12. `0 * 3 + 3 + 3 + 3`
13. `0 + 3 + 3 + 3`
14. `3 + 3 + 3`
15. ....
16. `6 + 3`
17. ....
18. `9`

Eventually, we get `9`.
That is, if we want to get `7` we need to use 
`1 + (2 * 3)`
01. `1 + (2 * 3)`
02. `1 + (2.pred * 3 + 3)`
03. `1 + (1 * 3 + 3)`
04. `1 + (1.pred * 3 + 3 + 3)`
05. `1 + (0 * 3 + 3 + 3)`
06. `1 + (0 + 3 + 3)`
07. `1 + (3 + 3)`
08. ...
09. `1 + 6`
10. `1.pred + 6.succ`
11. `0 + 6.succ`
12. `0 + 7`
13. `7`

In order to obtain `7`, those parentheses are needed. Fearless do not have operator precedence: operators are just methods, and when parentheses are omitted, the method will eagerly capture the first piece of code that looks like a parameter. This behaviour is called **left associativity**.
Thus, when coding in Fearless we need to ignore the usual operator precedence that they tried to hammer in our head at school, and we just follow this simpler rule of eager application.

>In other words: Fearless method calls (including operators like `+`,`*`, etc.) happen from left to right unless we use parentheses `(..)` to group them.
>There is no built-in "multiplication before addition" rule like in math class.
>In Fearless `a + b * c` means `(a + b) * c`.


Note how this applies also for named methods.
For example we used parenthesis in `this.pred + (other.succ)`.
This is needed. without those parenthesis, the code would be interpreted as `(this.pred + other).succ`


#### The challenge of subtraction

Subtraction `-` presents a slight twist. The easy base case is `a - 0 = a`, where zero is on the right. However, our inductive approach works by simplifying the left operand `this`. How can we handle subtraction?

We can use a helper method. The main - method can delegate to an auxiliary method (let's call it `._rightSub`) that effectively swaps the operands so the induction can work correctly based on the original right-hand operand.

```
Number:{ 
  .pred:Number, .succ:Number,
  +(other: Number): Number -> this.pred + (other.succ),
  *(other: Number): Number -> (this.pred * other) + other,
  -(other: Number): Number -> other._rightSub(this),
  ._rightSub(other: Number): Number-> this.pred._rightSub(other.pred),
  }
0: Number{
  .pred-> 11, .succ->  1,
  +(other)-> other,
  *(other)-> 0,
  ._rightSub(other: Number): Number-> other,
  }
1: Number{.pred->  0, .succ->  2, }
...
10: Number{.pred->  9, .succ-> 11, }
11: Number{.pred-> 10, .succ->  0, }
```

Here the inductive logic is as follows:

```
a - 0 = a                //base case
(a+1) - (b+1) = a - b    //inductive case
```

We had to introduce a method `._rightSub` since we can only reason inductively on the receiver. We used `_` at the beginning of the method to express that we do not expect to use that method directly, and that it is just a tool to implement `-`.

Later we will show ways to actually hide the existence of those auxiliary methods.


This section introduced modulo arithmetic and showed how fundamental operations like addition, multiplication, and subtraction can be implemented from scratch using inductive definitions (base cases and recursive steps) purely with types and methods.

### Numbers as a Common Resource

We've meticulously built our own `Number` type, representing the numbers `0` through `11`, complete with `.succ`, `.pred`, and arithmetic operations. We saw how operations like `11.succ` wrapped around back to `0`, and `0.pred` wrapped to `11` – this is modulo arithmetic, just like a clock face.

While building `Number` was insightful, doing this for very large range of numbers would be impractical.

Numbers are a very useful abstraction to have, and it would be absurd to have to reimplement them in every Fearless program.
Programming languages have the concept of **libraries**: useful code that has been written by someone some time in the past and that we can reuse without the need of cut pasting it into our project. In general, cut pasting code around is considered a bad practice.

There are two kinds of libraries:

- Third party libraries, that can be written by any Fearless programmer and have to be manually included into our projects
- The Fearless standard library, which is integrated with Fearless and is always implicitly available.

A few kinds of numbers are part of the Fearless standard library:

`Nat` (Natural Numbers): These represent non-negative whole numbers (`0`, `1`, `2`, `3`, and so on). You write them just like you'd expect: `1`, `0`, `34`, `45235`.
They have familiar methods like `+`, `-`, `*`, `.succ`, `.pred`, similar to our `Number` example. For example: `5 + 3` results in `8`.

`Int` (Integers): These represent positive and negative whole numbers (..., `-2`, `-1`, `+0`, `+1`, `+2`, ...). To distinguish them, you must include the sign before the number. Note that `+0` is the only way to write zero as an `Int`.
Some `Int`s: `+10`, `-25`, `+0`, `+12345`, `-987`.

Crucially, `+10` or `-25` are treated as single tokens (building blocks) by the Fearless compiler. `Int` also provides methods like `+`, `-`, `*`, etc. For example: `+10 + -3` results in `+7`.

#### How they work? Like our clock, just... BIGGER!

Crucially, `Nat` and `Int` work exactly like our `Number` example, using **modulo arithmetic**. The key difference is the size of the "clock face". Instead of wrapping around after `11`, they wrap around after reaching an enormously large value, that we will discuss later.

`Nat` behaves like a massive clock counting from `0` up to this  huge maximum. Adding `1` to this maximum `Nat` wraps around back to `0`. Subtracting `1` from `0` wraps around to the maximum `Nat`. That is, in the standard library there are many, many types defined following roughly the schema below:


```
//Nat: Familiar Logic, Ludicrous Scale
Nat:{ 
  .pred:Nat, .succ:Nat,
  +(other: Nat): Nat->....,/*more method as in Number*|/
  }
0: Nat{
   .pred-> 18446744073709551615, .succ-> 1,
   +(other)-> other,/*more method as in Number*|/
   }
1: Nat{.pred->  0, .succ->  2, }
...
18446744073709551615: Nat{.pred-> 18446744073709551614, .succ->  0, }
```

Here’s the key: Nat operate using the exact same modulo arithmetic logic as our `0-11` `Number`. They have a maximum value, and `.succ` wraps around to the minimum; `.pred` on the minimum wraps around to the maximum.
The schemas look just like our clock's.
The only difference? The scale is mind-boggling. The max value isn't `11`; 
It is ( 2<sup>64</sup> ) - 1, that is 18,446,744,073,709,551,615.

>Eighteen quintillion, four hundred forty-six quadrillion,
>seven hundred forty-four trillion, seventy-three billion,
>seven hundred nine million, five hundred fifty-one thousand,
>six hundred fifteen.
>More than 18 followed by 18 zeros!

Wow, what a number!
We will discuss shortly why this oddly specific number.

How does the standard library provide all eighteen quintillion Nat types? Did someone actually type them all out?
Of course, there isn't really a file containing billions of billions of lines like this:
```
...
18446744070000000004: Nat{.pred-> 18446744070000000003, .succ->  18446744070000000005, }
18446744070000000005: Nat{.pred-> 18446744070000000004, .succ->  18446744070000000006, }
18446744070000000006: Nat{.pred-> 18446744070000000005, .succ->  18446744070000000007, }
...
```
But let's imagine, just for fun. Picture "The Infinite Typist," a mythical programmer fueled by pure determination and questionable amounts of coffee, who decided one day to manually define numbers, one after another. Day after day, century after century, they typed...
If we printed their monumental work, using tiny font and three columns per page, just the definitions for Nat would fill a stack of books so high... it would stretch roughly from the Earth to Pluto. Seriously. We did the math (it involves quintillions of lines and very large bookshelves).



#### The Reality: Clever Optimization
Okay, back to reality. The standard library doesn't rely on mythical typists or planet-sized bookshelves. It uses highly optimized internal techniques, leveraging how computer hardware works, to represent these numbers efficiently in a small, fixed amount of memory. This allows mathematical operations on `Nat` to be incredibly fast.
The Fearless standard library is internally optimised in ways that a library written by a regular programmer could not. In particular, the standard library can define an amount of types that is out of the reach of what can realistically be coded by hand, or even stored on your hard drive.
However, those types do exist and we can code in Fearless using them.
This also means that the type names `0`, `1`, `2` and so on are already taken, and thus we can not actually define our numbers called `0`-`11` as we did before.

Crucially, even though the implementation is optimized, the behavior perfectly matches the conceptual model of that massive, wrap-around clock face. It's not magic, just very clever engineering built upon the same principles we saw with our `0`-`11` `Number` type.

In addition to `Nat` we have `Int`.
`Int` implements another kind of modulo arithmetic, where we can have negative values, and instead of rolling back to zero when we overflow, we roll back to the smallest possible negative value.
That is, `Int` follows the schema below:
```
Int:{ 
  .pred:Int, .succ:Int,
  +(other: Int): Int->...,/*more method as in Number*|/
  }
+0: Int{
   .pred-> -1, .succ-> +1,
   +(other)-> other,/*more method as in Number*|/
   }
+1: Int{.pred->  +0, .succ->  +2, }
...
+9223372036854775807: Int{.pred-> +9223372036854775806, .succ->  -9223372036854775808, }
-1: Int{.pred->  -2, .succ->  +0, }
...
-9223372036854775808: Int{.pred-> +9223372036854775806, .succ->  -9223372036854775807, }
```
As you can see, the predecessor of `+0` is `-1` and the successor and predecessor of the biggest numbers are linked together.

Finally, `Float` and `Num` are numeric types useful to represent fractions. We will discuss them later.


### Staring into the Abyss: Overflow and Murphy's Law

Because `Nat` and `Int` use this fixed-size, wrap-around (modulo) arithmetic, they are subject to overflow (going past the max) and underflow (going below the min). Just like `11.succ` became `0` on our small clock, adding `1` to the maximum `Nat` silently produces `0`. Adding two large positive `Int`s might silently result in a negative `Int`.

There is no warning bell, no error message. It just happens.

And Murphy's Law ("Anything that can go wrong, will go wrong") practically guarantees that if your program runs long enough or handles large enough inputs, someone, somewhere, will eventually trigger an unexpected overflow if you're solely relying on `Nat` or `Int` without careful checks.
This isn't a Fearless-specific issue; it's a fundamental trade-off for the speed gained by optimized integers in most programming languages. Ignoring it is building on shaky ground. Accepting this reality is step one to writing robust code.

//OMIT_START
-------------------------*/@Test void fullNumber() { run("""
Number:{
  .pred:Number, .succ:Number,
  +(other: Number): Number -> this.pred + (other.succ),
  *(other: Number): Number -> (this.pred * other) + other,
  -(other: Number): Number -> other._rightSub(this),
  ._rightSub(other: Number): Number-> this.pred._rightSub(other.pred),
  }
N0: Number{
  .pred-> N11, .succ->  N1,
  +(other)-> other,
  *(other)-> N0,
  ._rightSub(other: Number): Number-> other
  }
N1: Number{.pred->  N0, .succ->  N2, }
N2: Number{.pred->  N1, .succ->  N3, }
N3: Number{.pred->  N2, .succ->  N4, }
N4: Number{.pred->  N3, .succ->  N5, }
N5: Number{.pred->  N4, .succ->  N6, }
N6: Number{.pred->  N5, .succ->  N7, }
N7: Number{.pred->  N6, .succ->  N8, }
N8: Number{.pred->  N7, .succ->  N9, }
N9: Number{.pred->  N8, .succ->  N10, }
N10: Number{.pred->  N9, .succ-> N11, }
N11: Number{.pred-> N10, .succ->  N0, }
"""); }/*--------------------------------------------
//OMIT_END
END*/
}
