package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
final class ZH_020Chapter03ObjectCapabilities{
/*START
--CHAPTER-- Chapter 3
--SECTION-- Object Capabilities

### Object capabilities and Main

Finally, we have discussed all of the knowledge needed to make our first Fearless program.

-------------------------*/@Test void finallyMain(){ run("""
//in file _demo/_rank_app.fear
use base.Main as Main;
use base.Output as Output;

Test:Main {sys -> sys.out.println("Hello, World!")}
//PRINT|Hello, World!
"""); }/*--------------------------------------------

If we run this program, it will print `Hello, World!`.
You may be having two radically different reactions to the sentence above.
- You understand perfectly and you are relieved that we finally reached the traditional `Hello World`. If this is you, probably you are one of the few humans still knowing what a terminal/shell/console is.
If you are in this group, you should be easily able to figure out how to run that Fearless program. Yes, you can use a shell as for any other programming language.
 
- You have no idea what *running a program* means, let alone how to do it.
If you are in this second group, you are welcome! This guide is for you. Thank you for engaging in the more theoretical part of our material; what you have learned will be crucial going forward in this more practical part.

Please, follow along with those steps:
Go to <a href="https://github.com/FearlessLang/StandardLibrary/tree/main/fearlessStandalone">Fearless Compiler</a>

Download the file appropriate to your operating system,
put it in some folder and unzip it.
That is the Fearless compiler. Congrats, now you have it on your machine.

Then, click on the executable whose name starts with `fearless` and ends with `w`, like `fearlessBin0_001w` or `fearlessBin0_001w.exe` (the exact name depends on the version).

You will see a window asking to create a demo project in a location of your choice. Choose anywhere you like, for example a new folder on your Desktop.
This new folder will contain the following files and folders:
- `start.fearless`
- `_demo`
- `_demo/_rank_app.fear`

You should right click on `start.fearless` and associate files with extension `*.fearless` with 
that same executable.
That is it, you are all set to use Fearless on your machine.

Double click on `start.fearless` and the program will start.
(The first run may take a while since the project is warming up).

Then, open `_demo/_rank_app.fear` with any kind of text editor (not a word processor; use something like notepad, gedit, kate, or notepad++).
You can now see and edit the Fearless code.
Try to write a different message instead of `Hello World!` and rerun the project, by double clicking again on `start.fearless`.

Any type implementing `base.Main` can be the starting point for the execution.
`Main` is declared in `base` as follows:
```
Main:{ .main(sys: mut System): Void }
```

The parameter of type `mut System` refers to an **object capability**: an object giving access to the world outside of the program.
`sys.out` is a method creating a restricted object capability from the system capability.
The result of `sys.out` is an object of type `mut base.Output`: an object that can print text out.

That is, object capabilities and reference capabilities are two different concepts.
- **Reference capabilities** are a type system feature, while
- **Object capabilities** are just a programming style that is embraced by the standard library.

### What are object capabilities for?

You may think that capabilities are there to stop our code from **doing** things: no `Output`, no printing.
I find the real story much more interesting: capabilities decide what our code can **observe**.

Look at this method:
-------------------------*/@Test void squares(){ run("""
//OMIT_START
use base.Nat as Nat;
//OMIT_END
Squares: { .of(n: Nat): Nat -> n * n }
"""); }/*--------------------------------------------

`Squares.of` has no `mut` parameters (the receiver counts too).
The methods of an object capability that look at the world are all `mut` methods, so `Squares.of` has no way to use any of them.
What can `Squares.of(5)` possibly return? It has the `5` and nothing else to look at: no file, no web page, nothing that changes behind its back.
The result is `25` today, tomorrow, on my computer and on yours.
This predictability is a superpower:
- a result can be remembered instead of being computed again;
- many calls can run at the same time, on different cores, in any order;
- a check on an object, like the coordinates of a `Point` being in range, gives the same answer wherever and whenever it runs.

All of those tricks are safe as long as nobody can tell the difference.
Reading a file or downloading a web page would break the promise: the same question can get a different answer every time.
That is why **an object capability is the permission to observe**: without one, our code can only compute with what it has been given.

Acting on the world is different.
Once a line is printed, nothing in our program can read it back: no code can tell if the line was printed once, twice or never.
Printing cannot make a result unpredictable, so it needs no permission: `base.Debug#(x)` prints `x` from any method, with no `sys` in sight.
Even a call that never ends needs no permission: nobody can observe that it does not end, since the code that would notice is the code that never runs.

Logs follow the same rule: anybody can write to a log, but reading it back is observing.
In the following example `Squares.of` leaves a note in an in-memory log, and only the `Test`, holding `sys`, can read the notes:
-------------------------*/@Test void logsCanBeWrittenAnywhere(){ run("""
//OMIT_START
use base.Main as Main;
use base.Block as Block;
use base.InMemoryLog as InMemoryLog;
use base.Nat as Nat;
use base.Str as Str;
//OMIT_END
Events: InMemoryLog[Str]{"Events"}
Squares: {
  .of(n: Nat): Nat -> Block#
    .do {Events.log("squaring " + n)}
    .return {n * n}
  }
Test: Main {sys -> Block#
  .let[Nat] result= {Squares.of(5)}
  .do {sys.out.println(result)}
  .return {sys.out.println(sys.readLog(Events).consume.get(0))}
  }
//PRINT|25
//PRINT|squaring 5
"""); }/*--------------------------------------------

The note changed nothing for `Squares.of`: it still returns `25`, always.
Whoever reads the notes, instead, can tell how many times and in which order `Squares.of` was called, so for that code the tricks above are no longer invisible.
Observing has a price, and `sys` is where we pay it.

So why does `sys.out.println("Hello, World!")` ask for a capability, if printing cannot be observed?
Because in an object oriented language, to do something we ask an object able to do it; and a method receiving an `Output` announces that it prints.
An `Output` gives our code no new way to observe anything, so it is transparent: the results of a method do not depend on whether the text is printed, ignored or sent somewhere else.

Mutation inside the program is a different story.
After `set` the program can read the new value of a `Var` with `get`, so the change can be observed.
That is why reference capabilities restrict both who can change an object and who can look at an object that changes, while object capabilities restrict who can look outside of the program.


OMIT_START
-------------------------*/@Test void outputCapability(){ run("""
use base.Main as Main;
use base.Output as Output;
use base.Block as Block;
use base.System as System;
Test:Main {sys -> Block#
  .let[mut Output] out= {sys.out}
  .do {out.println("one")}
  .return {out.println("two")}
  }
//PRINT|one
//PRINT|two
"""); }/*--------------------------------------------
OMIT_END
END*/
}
