package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_020Chapter03ObjectCapabilities {
/*START
--CHAPTER-- Chapter 3
--SECTION-- ObjectCapabilities

### Object capabilities and Main

Finally, we have discussed all of the knowledge needed to make our first Fearless program.

-------------------------*/@Test void finallyMain() { run("""
//in file _test/_rank/app.fear
use base.Main as Main;
use base.Output as Output;

Test:Main {sys -> sys.out.println(`Hello, World!`)}
//PRINT|Hello, World!
"""); }/*--------------------------------------------

If we run this program, it will print `Hello, World!`.
You may be having two radically different reactions to the sentence above.
- You understand perfectly and you are relieved that we finally reached the traditional `Hello World`. If this is you, probably you are one of the few humans still knowing what a terminal/shell/console is.
If you are in this group, you should be easily able to figure how to run that Fearless program. Yes, you can use a shell as for any other programming language.
 
- You have no idea what *running a program* means, let alone how to do it.
If you are in this second group, you are welcome! This guide is for you. Thank you for engaging in the more theoretical part of our material; what you have learned will be crucial going forward in this more practical part.

Please, follow along with those steps:
Go to <a href="https://github.com/FearlessLang/StandardLibrary/tree/main/fearlessStandalone">Fearless Compiler</a>

Download the file appropriate to your Operative System,
put it in some folder and unzip it.
That is the fearless compiler. Congrats, now you have it on your machine.

Then, click on the executable called `fearlessw` or `fearlessw.exe`.

You will see a windows asking to create an empty project in a location of your choice. Chose anywhere you like, for example a new folder on your Desktop.
This new folder will contain the following files:
- `start.fearless`
- `_demo`
- `_demo/_rank_app.fear`
You should right click on `start.fearless` and associate files with extension `*.fearless` with 
the executable `fearlessw`.
That is it, you are sorted to use Fearless on your machine.

Double click on `start.fearless` and the program will start.
(The first run may take a while since the project is warming up).

Then, open `_test/_rank_app.fear` with any kind of text editor (not a word processor; use something like notepad, gedit, kate, or notepad++)
You can now see and edit the fearless code.
Try to write a different message instead of `Hello World!` and re run the project, by double clicking again on `start.fearless`.

Any type implementing `base.Main` can be the starting point for the execution.
`Main` is declared in `base` as follows:
```
Main:{ .main(sys: mut System): Void }
```

The parameter `sys` refers to an **object capability**: an object able to do external side effects.
`base.Output` is a function creating a restricted object capability from the system capability.
The result of `sys.out` is an object of type `mut base.Output`: an object that can print text out.

That is, Object capabilities and Reference capabilities are two different concepts.
- **Reference capabilities** are a type system feature, while
- **Object capabilities** are just a programming style that is embraced by the standard library.


OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}