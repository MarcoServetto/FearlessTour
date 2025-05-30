package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_006Chapter01Strings {
/*START
--CHAPTER-- Chapter 1
--SECTION-- Strings

### From numbers to text

This short section takes a detour into number bases and very large numbers, exploring how text is encoded into Fearless from the ground up. This perspective might help some learners connect text back to earlier concepts, but it might also feel confusing, and that's okay.

**If you find it dense, relax.**

Understanding the details of alternative number representations is absolutely not required to use text or to understand the rest of this guide. Focus instead on the practical examples that follow.
For all practical coding purposes, what matters is that we have a way to represent text. **You do not need to calculate or remember anything about number bases to use text in Fearless.** The following is about how text can be derived from first principles.

### Numbers with base bigger than 10

We have seen numbers represented with digits 0-9.
In this representation, 23 is the number  resulting from 'two times ten plus three'.
The number 345 is the number resulting from '(three times ten plus four) times ten plus five'.
That is, the first 10 numbers (starting from zero) are assigned a single symbol:
`0`, `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`
and numbers bigger than nine are represented using two or more symbols.
There is nothing special about the number nine, and we could imagine having more symbols for higher numbers.
For example, consider the English words for numbers, nine, ten, eleven, twelve, thirteen, fourteen,fifteen...
As you can see, thirteen, fourteen,fifteen follow a pattern, suggesting they are conceptually composed by two symbols:
thir-teen, four-teen,fif-teen, but this does not happen for nine, ten, eleven, twelve, or any number before that.
So, what if we select some more symbols to use for ten, eleven and twelve?
We could select `0`, `1`, `2`, `3`, `4`, `5`, `6`, `7`, `8`, `9`, `A`, `B`, `C`.
Now we have 13 symbols, so we can use those symbols to represent numbers in base 13:
  `C` is twelve,
  `AC` is `ten times thirteen plus twelve`; equivalent to 140 in base 10.
As you can see, we used only two symbols `AB` instead of three `140`.
Using higher bases we can represent higher numbers with less symbols.
Moreover, `CAB` is both a number in base 13, but also an English word. Basically, when we grow the base, we start being able to express not just numbers, but text.

So... what if we include all of the following symbols?
```
0123456789
abcdefghijklmnopqrstuvwxyz
ABCDEFGHIJKLMNOPQRSTUVWXYZ
+-*|/=<>,.;:()[]{}`'"!?@#$%^&_|~\
space and new line
```
It is now 96 symbols. Those are all the symbols we can easily type on most keyboards.
With this, we could express any text!
We call numbers expressed in this form (simple) strings.
It is very compact to represent very large numbers in this notation. For example number 5,000 would be just `` `Q8` ``
1,000,030 in base-96 has a representation of `` `1cM ` ``. Note the space after the character `` `M` ``.
If we did not use the backtick character (`` ` ``) it would be very hard to spot trailing spaces in our base-96 numbers.
The number of humans currently alive is 8 Billions, or `` `inRW` ``.
A more precise estimate is 8,122,862,820; corresponding to 
... hmm... 
`` `<newLine>Zb2A` ``? maybe?

As you can see, when the new line character ends up in the number representation it becomes unobvious how to write it down when embedded in other text.
The same problem would emerge if the backtick character (`` ` ``) was present; since we used backticks to delimit the border of our 
base 96 number. And we really need to select some characters to be used to show the start and end of our base-96 numbers to avoid confusion.
How can we handle this issue?
This is an instance of a more general problem: how to embed text inside text.
Most programming languages use the following convention: the backslash character `\` works as an escape character.
Thus, backticks can be written as ``` `\`` ```, new lines can be written as `` `\n` ``, and backslash can be written as `` `\\` ``.
That is, the three strings composed by a single character that is just single quote, new line and backslash will be represented as
``` `\`` ```, `` `\n` ``, `` `\\` ``.
This can be surprising for beginners since it means that strings of length 1 (containing just one character) will be composed of two characters;  or four if we count also the delimiters.

Thus `` `Hi, \`John\`, are you really John?` `` is containing `John` in backticks.
It is also a number, a massively large number. So large that it would not make sense for us to show it. Well, here it is:
1,218,548,022,657,255,659,383,869,870,726,090,934,496,236,095,273,804,605,882,264,818

As you can see, strings are useful to represent text.
However, there are logically just very large numbers, and we have learned before how to represent numbers from zero to eleven.
From that, it is quite obvious how to encode numbers up to any amount.
In the same way numbers 0,1,2,... are defined in the Fearless standard library, all possible strings are also defined. And can be used out of the box.

Note again how the standard library can define an amount of types that is out of the reach of what can realistically be coded by hand.
However, those types do exist and we can code in Fearless using them.
Note that strings in Fearless are still a finite number.

While there are  ( 2<sup>64</sup> ) - 1 instances of `Nat`, there are ( 96<sup>2,147,483,647</sup> ) − 1 instances of Str.
The number 96<sup>2,147,483,647</sup> is incomprehensibly large, far exceeding the number of atoms in the observable universe 10<sup>80</sup> and a googol 10<sup>100</sup>. However, it is still smaller than a googolplex 10<sup>googol</sup>.

### Strings

So much about computers and programs is expressed with text. `Str` is the type of the simple strings discussed before.
The `Str` type has many methods that are unique to strings and does not have methods allowing to treat them as numbers.
That is, when working with text, there is absolutelly no reason to think about the corresponding giant numbers in base 96.

For example we can write `` `Hello`.size`` to get `5`.
We will discuss those methods when they become relevant in the rest of the guide.

Strings and comments can contain any character, thus they can contain unbalanced parenthesis. For example, the following is a valid string: `` `A(B` ``.
If we ignore parentheses in strings and comments, a Fearless program always has balanced parentheses.
For example: ``A:{ .foo:Str->`B}`}``
is a valid type declaration, with balanced parenthesis. The `}` inside of the string literal does not matter.
Strings have method `+` supporting string concatenation, so
`` `Hello ` + `world` `` will reduce to `` `Hello world` ``.
Note the space after the o in `` `Hello ` ``.

That is, the `+` method does not sum the two strings as numbers but just concatenate them.

//OMIT_START
-------------------------*/@Test void exampleStrings () { run("""
Test:Main {sys -> base.Debug.println(`Hello ` + `world`)}//OK
//prints Hello world
"""); }/*--------------------------------------------
-------------------------*/@Test void exampleStringsEscape () { run("""
Test:Main {sys -> base.Debug.println(`He\\`ll\\`o ` + `wor\\\\ld`)}//OK
//prints He`ll`o wor\\ld
"""); }/*--------------------------------------------

//OMIT_END
END*/
}
