package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_019Chapter03Packages {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Packages

### Packages and libraries

Up to now we just showed Fearless code as text, but of course a large Fearless program will be divided in multiple files
To better manage the complexity of a large Fearless program divided in many files,
Fearless offers **packages**.
A Fearless file is a text file with extension .fear and with the first line of form
`package somePackageName`.

We say that such a file is inside the package `somePackageName`.

A Fearless project is a directory/folder containing many Fearless files.
Those Fearless files can be in any subfolder of the project folder.

A Fearless project can also contain many pre-compiled Fearless libraries.
They are just zipped folders containing Fearless files and some cached information to speed up the compilation process.
A Fearless File can be in any package, except packages defined by the standard library or by any pre-compiled library present in the project folder.

With packages, types names are composed by the name of the package followed by the type name as declared.
For example the full name of the `Block` type defined in the `base` package is `base.Block`.
Of course using those long winded names would make the code harder to read, thus Fearless supports type aliases.
For example a Fearless file can start with the following:

```
package test

alias base.Num as Num,
alias base.SimpleString as Str,
``` 

Here we define that inside the package test, base.Num can be referred simply as `Num` and that `base.SimpleString` can be referred simply as `Str`.
That is, `base.SimpleString` is the full name for the string type we discussed in this guide.
Note that there can be many files inside of package test, and the aliases will work across all of those files.
This means that if your project is small enough to fit in a single package, you can have a file defining your aliases, and all your other files will consistently see them.

All the code in a specific package will see the typenames declared in that package using just the simple type name and not the full name.

### Sealed

Packages allow for some additional form of control on what the user can do with existing code.
If a type extends the special type base.Sealed, then such a type is 'Sealed'.
A sealed type can only be extended by types declared in its own package.
Many of the types we have seen in this guide are sealed.
All kinds of numbers and strings are sealed, optionals and booleans are sealed. The type `Void` is also sealed.
Sealed types can still be extended if the type name is directly used as a literal, so
`True` 
is valid, but
`True{.not->True,}`
is not valid.

### Package private types

Type names can start with the `_` character, but a full name of form `somePackageName._SomeTypeName` is ill formed.
This allows for a form of package private types, where we can declare types whose name starts with `_` to indicate that the users of our code should not refer to those types, they are only intended as a way to internally encode the behaviour of the library.
Of course all the fresh type names that the inference adds to our code start with `_`, and thus are package private.
OMIT_START
-------------------------*/@Test void anotherPackage() { run("""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}