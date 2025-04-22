package chapter1A;

import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;

class ZH_001Preface {
/*START
--CHAPTER-- Preface
--SECTION-- Fearless: Zero to Hero

# Fearless: Zero to Hero

### Preface: What is Fearless?
Fearless is a minimalist, nominally-typed, object-oriented programming language.
Fearless gives the programmer fine control of side effects using Reference and Object Capabilities instead of an Effect System.

If you are new to programming, the sentence above was probably a little difficult to understand.
We will unpack these ideas piece by piece, and build an understanding through examples.

In this guide we are going to take a step by step approach to Fearless programming.
Instead of focusing on writing short working programs,
we are going to focus on the basics of programming as a way to organize concepts and thought.

### Fearless: code, compilation, and execution

A Fearless program consists of a specific form of text, called (source) code.
Fearless code is text designed to be read by both humans and a program called the Fearless compiler.

When you read text, your brain separates words from punctuation. Reading this sentence below

`Pizza, again?`

you see two words: `Pizza` and `again`,
and two punctuation marks: `,` and `?`.

Code works similarly, but everything is just a "token". The compiler would just see four tokens: `Pizza`, `,`, `again` and `?`.
From the point of view of the fearless compiler newlines and spaces are irrelevant; any amount of spacing is equally effective at separating pieces of code, and sometimes no spacing is needed, as for the comma after `Pizza`.
This means that we can freely use more or less spacing to make the code more understandable for humans.
Additionally, we use parenthesis `(..)`, `[..]` and `{..}` to group concepts together.

### Code Readability and Comments
In Fearless we can use newlines and spaces freely to make our code more readable.
Comments are another tool to make our code more readable or clear.
 A line starting with `//` is a single line comment. The whole line is ignored by the Fearless compiler. Any text enclosed by the delimiters `/*...*|/` is called a multi-line comment and it is also ignored by the Fearless compiler.
 
Additional spaces and newlines can be added mostly anywhere in the code and are also ignored; that is, comments and extra spaces do not play any role in the optimised representation produced by the Fearless compiler, and thus will have no impact on the execution.

That is, comments have no impact on the execution; but can be precious to insert human-readable explanations in the code; facilitating the understanding (and re understanding) of code.

Next we will see our first example of code.


> **A Note on Our Approach:** You might notice this guide takes a different path than many programming introductions.
> Instead of starting with quick commands to make things happen, we're focusing first on the core ideas of Object-Oriented Programming (OO) – modeling concepts as Types and defining their interactions via Methods.
> We'll build everything, even familiar ideas like numbers or decisions, from these fundamental OO building blocks.
> This might feel slower initially, but the goal is to build a deep, foundational understanding of how software can be structured logically, focusing on organizing thought and learning the crucial skill of abstract symbolic manipulation.
>We believe this "foundations-first" approach, while demanding, ultimately leads to clearer, more robust programming skills.

OMIT_START
-------------------------*/@Test void helloWorldPreface() { run("""
      package test
      alias base.Main as Main,
      Test:Main {sys -> base.Debug.println("Hello, World!")}//OK
      //prints Hello, World!
      """); }/*--------------------------------------------
OMIT_END

END*/
}
