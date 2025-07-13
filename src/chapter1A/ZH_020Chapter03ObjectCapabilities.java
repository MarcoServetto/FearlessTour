package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_020Chapter03ObjectCapabilities {
/*START
--CHAPTER-- Chapter 3
--SECTION-- ObjectCapabilities

### Object capabilities and Main

Finally, we have discussed all of the knowledge needed to make our first Fearless program.

-------------------------*/@Test void finallyMain() { run("""
package test
alias base.Main as Main,
alias base.caps.UnrestrictedIO as UnrestrictedIO,

Test:Main {sys -> UnrestrictedIO#sys.println(`Hello, World!`)}
//prints Hello, World!
"""); }/*--------------------------------------------

This program will print `Hello, World!`.

That is, if we run `Fearless test.Test` over a folder containing just that file, we will see `Hello, World!` on the console.

Any type implementing `base.Main` can be the starting point for the execution.
`Main` is declared in `base` as follows:
```
Main:{ .main(sys: mut System): Void }
```

The parameter `sys` refers to an **object capability**: an object able to do external side effects.
`base.UnrestrictedIO` is a function creating a restricted object capability from the system capability.
The result of `UnrestrictedIO#sys` is an object of type `mut base.IO`: an object that can do input output side effects, like writing on the console or reading/writing files.

That is, Object capabilities and Reference capabilities are two different concepts.
- **Reference capabilities** are a type system feature, while
- **Object capabilities** are just a programming style that is embraced by the standard library.


OMIT_START
-------------------------*/@Test void anotherPackage() { run("""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}