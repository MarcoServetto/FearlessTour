package chaptersOfZeroToHero;

import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;

class ZH_001Preface {
/*START
--CHAPTER-- Preface
--SECTION-- Fearless: Zero to Hero

# Fearless: Zero to Hero

### Preface: What is Fearless?

Let's start with a cryptic definition:

>Fearless is a minimalist, nominally-typed, object-oriented programming language.
>Fearless gives the programmer fine control of side effects using Reference and Object Capabilities instead of an Effect System.

If you are new to programming, the sentence above was probably incomprehensible. Don't worry - we will unpack these ideas piece by piece and build understanding through examples.

Often programming is taught as a tool to reach a practical goal, like building a website, a video-game, or automating a task.
That is, programming is often dressed as a necessary hurdle to "get over with" so you can achieve something else.

We aim to move beyond treating programming as an obstacle, and instead use it as a rewarding training ground for becoming a more effective learner and thinker:

- Programming demands abstract thinking, logical reasoning, focused concentration, and persistence in problem-solving. These are muscles that grow stronger with practice.

- Programming often gives clear, objective feedback. Either the compiler finds errors, or the program runs correctly (or incorrectly) based on the logic you implemented.

In this guide, Instead of focusing on writing short working programs, we are going to focus on the basics of programming as a way to organise concepts and thought.
We will cultivate comfort with abstraction, resilience in the face of challenges, patience for deep focus, and to find intrinsic satisfaction in the process of careful, logical construction itself.


### Expect confusion: everything is circularly dependent concepts

Most of what we know circularly depend on other things we know:

**A picture is something you draw / drawing is making a picture**

- What’s a picture? What you draw.
- What’s drawing? Making a picture.

**Talking is when you say words" / words are what you say when you talk**

- What are words? Things you say.
- What is saying something? Using words.

Grammatical categories like verbs, nouns, and adjectives are not grounded in external definitions.
They are typically learned and understood by their roles in relation to each other.
This creates a network of mutually defining concepts:

- (Verb) Running is something you do.

- (Noun) A dog can run; dog is the noun because it does the running.

- (Adjective) A fast dog runs; fast is describing the dog.

- (Adverb) The dog runs quickly; quickly is describing how it runs.

Both natural language and programming languages are web of relationships.
The meaning of the terms emerges from how the terms connect with each other.

### Fearless: code, compilation, and execution

A Fearless program consists of a specific form of text, called (source) code.
Fearless code is text designed to be read by both humans and a program called the Fearless compiler.

When you read text, your brain separates words from punctuation. Reading this sentence below

```
Pizza, again?
```

you see two words: `Pizza` and `again`,
and two punctuation marks: `,` and `?`.

Code works similarly, but everything is just a "token". The compiler would just see four tokens: `Pizza`, `,`, `again` and `?`.
From the point of view of the fearless compiler newlines and spaces are irrelevant; any amount of spacing is equally effective at separating pieces of code, and sometimes no spacing is needed, as for the comma after `Pizza`.
This means that we can freely use more or less spacing to make the code more understandable for humans.

Additionally, parenthesis are kinds of tokens that work in pairs: we use parenthesis `(..)`, `[..]` and `{..}` to group concepts together.

### Comments

Comments are another tool to make our code more readable or clear.
A `//` starts a single-line comment. 
The compiler ignores everything from `//` to the end of the line.
Any text enclosed by the delimiters `/*...*|/` is called a multi-line comment.
The compiler ignores everything between these two delimiters, even if it spans multiple lines.
Comments spaces and newlines have no impact on the execution; but can be precious to insert human-readable explanations in the code; facilitating the understanding of code.

In the next page we will see our first example of code.


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
