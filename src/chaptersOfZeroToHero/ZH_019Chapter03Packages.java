package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_019Chapter03Packages {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Packages


### Packages and libraries

Up to now, we've shown Fearless code as individual text examples. However, a realistic Fearless program will be organized into multiple files to manage complexity effectively. To support this, Fearless provides the concept of **packages**.

A Fearless file is a text file with the `.fear` extension. The first line of each Fearless file specifies its package using the following syntax:

```
package somePackageName
```

We say that such a file is inside the package `somePackageName`.

A Fearless project is a folder (or directory) containing multiple Fearless files.
These files can be freely organised into subfolders within the project folder.

Additionally, a Fearless project may contain pre-compiled libraries. Such libraries are simply zipped folders containing Fearless files along with cached data that speeds up compilation.

Note that your project's files cannot be in a package name already defined by the standard library or by any pre-compiled library included in the project.

With packages, types are identified by their package name combined with the type name. For example, the type `Block` defined in the package `base` has the full name `base.Block`. 
Using those long winded names everywhere would make the code repetitive and harder to read.
To mitigate this, Fearless supports **type aliases**.

For example a Fearless file can start with the following:

```
package test

alias base.Num as Num,
alias base.SimpleString as Str,
``` 

Type aliases apply across the entire package, not just code in the specific file containing the `alias`.
In the example above, the aliases allow all code in package `test` to use the shorter names `Num` and `Str` instead of their longer forms `base.Num` and `base.SimpleString`.

That is, `base.SimpleString` is the full name for the string type we discussed in this guide.
Note that there can be many files inside of package `test`, and the aliases will work across all of those files.

Moreover, within a package, all types declared in that package can always be referred to by their simple names.

This means that if your project is small enough to fit into a single package, you will get simplified naming:

- All the types you defined can be referenced directly by their simple names.

- You can define your aliases once and for all in one single file of your package, and all your other files will consistently see them.


Note how the alias keyword creates a type alias, not to be confused with aliasing between references that we discussed earlier.


### Sealed

Packages allow for some additional form of control on what the user can do with existing code.
If a type extends the special type `base.Sealed`, then such a type is **sealed**.
A sealed type can only be extended by types declared in its own package.
Many of the types we have seen in this guide are sealed.
All kinds of numbers and strings are sealed, optionals and booleans are sealed. The type `Void` is also sealed.
Sealed types can still be extended if the type name is directly used as a literal, so
`True` 
is valid, but
`True{.not->True,}`
is not valid.

### Package private types

Type names can start with the `_` character, but a full name of form `somePackageName._SomeTypeName` is not valid anywhere in Fearless.
For example, if a package `foo` declares a type `_Foo`:
- Any code inside package `foo` can use `_Foo` to refer to such type.
- Code outside of package `foo` can not write 
`foo._Foo`; not as a type, not as an alias declaration.

This allows for a form of package private types, where we can declare types whose name starts with `_` to indicate that the users of our code should not refer to those types, they are only intended as a way to internally encode the behaviour of the library.
Of course all the fresh type names that the inference adds to our code start with `_`, and thus are package private.
OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}