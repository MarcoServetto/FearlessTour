package chapter1A;

import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;

class ZH_001Preface {
/*START
--CHAPTER-- Preface
--SECTION-- Fearless: Zero to Hero

# Fearless: Zero to Hero

### Preface: What is Fearless?

Fearless is a minimalist, nominally-typed, object-oriented programming language. Fearless gives the programmer fine control of side effects using Reference and Object Capabilities instead of an Effect System.

If you are new to programming, the sentence above was probably a little difficult to understand. Don't worry – we will unpack these ideas piece by piece and build understanding through examples. More importantly, understanding that definition isn't the first step. The first step is understanding why we're approaching learning the way we are.

### More Than Just Code: Our Philosophy

Often, programming is taught simply as a tool to reach a practical goal, like building a website or automating a task. It can feel like a necessary hurdle to "get over with" so you can achieve something else.

This guide sees things differently. While you will learn the Fearless programming language, our deeper aim is to use this journey as an opportunity to practice and strengthen valuable learning habits. We believe that learning how to learn effectively is as important as learning any single skill.

Why use programming for this? Because programming, especially the structured, concepts-first approach we will take with Fearless:

- Requires Core Learning Traits: It inherently demands abstract thinking, logical reasoning, patience when facing errors, focused concentration, and persistence in problem-solving. These are muscles that grow stronger with practice.

- Provides Objective Feedback: Unlike many subjects, code often gives clear, objective feedback. The compiler finds errors, or the program runs correctly (or incorrectly) based on the logic you implemented. This provides a concrete way to assess your understanding and track genuine progress.

Our goal, therefore, is not just to teach you Fearless, but to help cultivate comfort with abstraction, resilience in the face of challenges, patience for deep focus, and perhaps even finding intrinsic satisfaction in the process of careful, logical construction itself.

We aim to move beyond treating programming as an obstacle, and instead use it as a rewarding training ground for becoming a more effective learner overall.

### Our Approach: Foundations First

This philosophy shapes our teaching method. You might notice this guide takes a different path than many programming introductions:

- Foundations Over Quick Wins: Instead of focusing on writing short working programs, we are going to focus on the basics of programming as a way to organize concepts and thought.

- Building from Scratch: We'll build everything, even familiar ideas like numbers or decisions, from fundamental programming concepts.

- Conceptual Clarity: The emphasis is on understanding why things work the way they do and how concepts connect.

This "foundations-first" approach might feel slower or more demanding initially. However, we believe it leads to a deeper, more robust understanding of programming as a way to organize thought, and simultaneously provides the focused practice needed to build those positive learning traits.

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
