package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_019Chapter03Packages {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Packages


### Packages and libraries

Up to now, we've shown Fearless code as individual text examples. However, a realistic Fearless program will be organised into multiple files to manage complexity effectively. A Fearless file is a text file with the `.fear` extension, but fearless files do not live in a vacuum; the sit in a directly/folder called the project folder or just **the project**.
The project can contain any kind of files, but they must have portable lowercase file names that would be accepted by Windows, Linux and MacOs.
These files can be freely organised into subfolders within the project.

Additionally, a Fearless project may contain pre-compiled libraries. Such libraries are simply zipped folders containing Fearless source files.

Fearless source files are divided into **packages**.
A package is a folder whose name starts with `_`.
For example, all the files inside `_geometry` are in the `geometry` package.

With packages, types are identified by their package name combined with the type name. For example, the type `Block` defined in the package `base` has the full name `base.Block`. 
Using those long winded names everywhere would make the code repetitive and harder to read.
To mitigate this, Fearless supports **use directives**.
Use directives need to be in the rank file.
The rank file is a file in the package with name `_rank_app.fear`; other specific standard names are possible, but we will not discuss them here.

For example we can have the following:
```
//inside file _test/_rank_app.fear
use base.Num as Num;
use base.Block as B;
``` 
In the example above, the use lines allow all code in package `test` to use the shorter names `Num` and `B` instead of their longer forms `base.Num` and `base.Block`.

Note that there can be many files inside of package `test`, and the aliases will work across all of those files.

Moreover, within a package, all types declared in that package can always be referred to by their simple names.

This means that if your project is small enough to fit into a single package, you will get simplified naming:

- All the types you defined can be referenced directly by their simple names.

- You can define what you `use` once and for all in one single file of your package, and all your other files will consistently see them.

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
-------------------------*/@Test void anotherPackage() { run("""
MyTrue: base.True {}
//ERROR|In file: [###]/_test/_rank_app111.fear
//ERROR|
//ERROR|001| MyTrue: base.True {}
//ERROR|   | ^^^^^^^^^^^^^^^^^^^^
//ERROR|
//ERROR|While inspecting type declaration "MyTrue"
//ERROR|Type declaration "MyTrue" implements sealed type "base.True".
//ERROR|Sealed types can only be implemented in their own package.
//ERROR|Type declaration "MyTrue" is defined in package "test".
//ERROR|Type "True" is defined in package "base".
//ERROR|Error 9 WellFormedness
"""); }/*--------------------------------------------
OMIT_END
END*/
}