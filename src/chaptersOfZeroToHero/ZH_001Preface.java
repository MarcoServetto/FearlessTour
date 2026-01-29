package chaptersOfZeroToHero;

import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;

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
That is, programming is often dressed as a necessary hurdle to "get over with" so you can achieve something else. We aim to move beyond treating programming as an obstacle.

We see programming an artistic expression demanding abstract thinking, focused concentration, and persistence in problem-solving. These are muscles that grow stronger with practice. Programmers find intrinsic satisfaction in the process of careful logical construction itself.


### Expect confusion: everything is circularly dependent concepts

If you ever learned a foreign language, you will have started by trying to connect each word in the foreign language to a definition in your native language. We do not have this luck when it comes to programming. Programming is a universe on its own.

Learning programming is a little like learning to speak again from scratch: a child learning to speak has to create a network of concepts in their head; and those concepts circularly depend on each other.

```
Car <--> Wheels
```

- Cars have wheels, wheels are the thing cars have.

- Note how we used language in a structured way when we made up the symbol `<-->`.
What does that symbol mean? It mean anything we want it to mean, we just made it up. By using it over and over we may solidify its meaning.

Both natural language and programming languages are web of relationships.
The meaning of the terms emerges from how the terms connect with each other.

### Fearless programs and Fearless code

A Fearless program consists of a specific form of text, called (source) code.
Fearless code is text designed to be read by both humans and a program called Fearless.

When you read text, your brain separates words from punctuation. Reading this sentence below

```
Pizza, again?
```

you see two words: `Pizza` and `again`,
and two punctuation marks: `,` and `?`.

Code works similarly, but everything is just a "token". Fearless would just see four tokens: `Pizza`, `,`, `again` and `?`.
From the point of view of Fearless newlines and spaces are irrelevant; any amount of spacing is equally effective at separating pieces of code, and sometimes no spacing is needed, as for the comma after `Pizza`.
This means that we can freely use more or less spacing to make the code more understandable for humans.

Additionally, parenthesis are kinds of tokens that work in pairs: we use parenthesis `(..)`, `[..]` and `{..}` to group concepts together.

### Comments

Comments are another tool to make our code more readable or clear.
A `//` starts a single-line comment. 
Fearless ignores everything from `//` to the end of the line.
Any text enclosed by the delimiters `/*...*|/` is called a multi-line comment.
Fearless ignores everything between these two delimiters, even if it spans multiple lines.
Comments spaces and newlines have no impact on the execution; but can be precious to insert human-readable explanations in the code; facilitating the understanding of code.

Comments are the first crucial abstraction step we are seeing. Comments are not unique to Fearless and you can use them in any kind of text.
Consider the following text, where someone may be planning to apply for their dream Job:
```
Dear Hiring Manager //actual name?

I am writing to apply for the mattress tester position at Sleep well Inc.
//What about: I am excited to apply for ..

I am a professional /*rester?*|/ with 25 years of experience in effective sleeping.
//TODO: connect more with their top requirement
In my current role, I've been able to test
 many different sleeping surfaces.
I can describe a mattress the way sommeliers describe wine: texture, support, finish.
With my extensive experience I'm confident I can provide effective suggestions
to improve the comfort of any kind of sleeping
set up while sticking to a very, very tight budget.
//is sticking the right word?

I have attached my CV and would welcome the chance to discuss how I could contribute to /*...*|/
Thank you for your time and consideration.
Kind regards, // or Sincerely?
Bob Snoozeman
```

As you can see, even comments can have their little language inside: `TODO:` is a short 5 letter annotation meaning that there is an open task.

In the next page we will see our first example of code.


OMIT_START
-------------------------*/@Test void helloWorldPreface() { run("""
      use base.Main as Main;
      Test:Main {sys -> base.Debug#(`Hello, World!`)}//OK
      //PRINT|Hello, World!
      """); }/*--------------------------------------------
OMIT_END

END*/
}
