package chapter1A;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_009Chapter02GenericTypes {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Generic Types

# Chapter 2

### Generic Types: a type abstraction

We have seen how methods abstract over values. We will now see how generic are a way to abstract over types.

When we declare a method we abstract over values; consider when we declared method `-` as follows:
```
  -(other: Number): Number -> other._rightSub(this),
```
We did this because writing `10 - 4` is much clearer than writing `4._rightSub(10)`.
The method call `4._rightSub(10)` works on those two **concrete** values. The method body of `-` is `other._rightSub(this)` and works on all kinds of values of the right type. The exact values are abstracted away.

We can read the method using the **forall** word as follows:
Forall `this` and `other`, subtraction returns `other._rightSub(this)`.

In real programming methods may end up quite long, and thus we can abstract quite a lot of code behind a small method call.
We want to do this kind of abstraction to avoid having to repeat the method body over and over again in our program.

We will now see **Generics**. While methods abstract over values,
generics abstract over types, and are specified in square parenthesis `[..]`.

Generics allows to encode decisions on arbitrary data. For example, consider the concept of `Fork`s in the road,where the road can choose to go either `Left` or `Right`.
We could have a method `.choose` that took two parameters and returned either the one on the left or the one on the right.

-------------------------*/@Test void fork1 () { run("""
Fork : { .choose[Val](leftVal: Val, rightVal: Val): Val, }
Left : Fork{ l,r -> l }
Right: Fork{ l,r -> r }
"""); }/*--------------------------------------------

Here we see the generic type `[Val]`.
We can read the above code as follows:

>`Fork` has a generic method `.choose` that forall types `Val` takes two arguments of type `Val` and returns a `Val`. Method `Fork.choose` is abstract.
`Left` is a kind of `Fork` where the `.choose` method returns the first parameter.
`Right` is a kind of `Fork` where the `.choose` method returns the second parameter.

Generic type arguments like `Val` are also uppercase starting identifiers exactly like type names.

With the code above, ``Left.choose(`Hello`,`Hi`)`` reduces to `` `Hello` `` and
``Right.choose(`Hello`,`Hi`)`` reduces to `` `Hi` ``.
Crucially, ``someFork.choose(`Hello`,`Hi`)``, where `someFork` is a parameter of type `Fork` will reduce one way or the other depending on the value of `someFork`.
The idea is that a method can return a decision by returning a `Fork`, and then the user of that method can use the decision to select a value between two.
Of course, once we have forks we can nest them to select a value between three, for example: 
```
firstChoice.choose(`Option1`,  secondChoice.choose(`Option2`, `Option3`)  )
```
where `firstChoice` and `secondChoice` are `Fork`s that we obtained somewhere.
Note how we need the generic type `Var` so that our `Fork` can work on any type:
We can write ``someFork.choose(`Hello`,`Hi`)`` but also ``someFork.choose(1,5)``.
However, ``someFork.choose(`Hello`,5)`` would be ill typed: there needs to be a type that can be used to instantiate `Var`.
The type inference is usually taking care of finding the types that instantiate a generic method call.
However, we can pass the parameter ourselves if we want, using syntax ``someFork.choose[Str](`Hello`,`Hi`)``.
As you can see, we add the `[..]` after the method name and before the list of parameters.
You are now ready to see the full syntax for a method call:
```
expression methodName [types](expressions)
```
Where types and expressions are lists of types and expressions separated by commas.
When we omit the `[..]` we are asking the type inference to infer that part.
On the other side, when we declare a generic method, as in
```
.choose[Val](leftVal: Val, rightVal: Val): Val,
```
we need to specify all the generic types that we are introducing.
If we omit the `[..]` in the method declaration, then the conventional sugar allowing to omit any empty parentheses triggers. Thus, when we declared
`.turn: Direction` at the start of our journey, we actually declared
`.turn[](): Direction`: a method called `.turn` that takes zero generic types and zero parameters.

#### Mental models of generic types
For some reason, human brains find generic types hard to understand.
We now try to give you two different ways to see them.

First: try to imagine a version of Fearless when the type system 'relaxes' and we can write `Ignore` instead of a type so that the type system ignores checks about those types.
In this cursed version of Fearless we could write 
```
Fork : { .choose(leftVal: Ignore, rightVal: Ignore): Ignore, }
Left : Fork{ l,r -> l }
Right: Fork{ l,r -> r }
```
Where basically anything goes.
In this set up, ``someFork.choose(`Hello`,`Hi`)`` would work as before, but
``someFork.choose(`Hello`,23)`` would also pass type checking.
What would happen at run time? consider for example:
```
someFork.choose(`Hello`,23) * 2
```
If `someFork` is `Right`, we would get `23 * 2` and then `46`.
However, if `someFork` is `Left` we would get `` `Hello` * 2`` and since `Str` does not have a `*` method, then the reduction would get stuck, unable to proceed.

Alternatively, we could imagine to declare multiple variants of the `.choose` method:

```
Fork : {
  .chooseInt(leftVal: Int, rightVal: Int): Int,
  .chooseStr(leftVal: Str, rightVal: Str): Str,
  .chooseDirection(leftVal: Direction, rightVal: Direction): Direction,
  .choosePoint(leftVal: Point, rightVal: Point): Point,
  }
```

As you can see this is quite repetitive and error prone. Moreover, it is never enough.
When programming we will add new types over and over again, and we can not realistically add a variant for each type we will ever declare.

However, if you squint looking at the code, you see that there is a clear pattern.
```
Fork : {
  .chooseInt(leftVal: Int, rightVal: Int): Int,
  .chooseStr(leftVal: Str, rightVal: Str): Str,
  .chooseDirection(leftVal: Direction, rightVal: Direction): Direction,
  .choosePoint(leftVal: Point, rightVal: Point): Point,
  ...
  .chose[Type](leftVal: Type, rightVal: Type): Type,
  }
```
And... that is exactly the syntax, and semantic, of generic methods: it is a way to declare an infinite amount of methods, all following a simple pattern, where the only thing that changes is  some types.


#### Generic methods and generic types
Even with all of this explanation, calls of method `Turn.choose` are not very readable:
``someFork.choose(`Hello`,`Hi`)`` is really cryptic.
We think this is for the same reason the original `Rectangles#` was cryptic: the role of the two parameters is not obvious by just reading the text.

We can make it more readable by introducing a literal forcing us to name the two branches.
In this way, a call to `Turn.choose` could look as follows:

-------------------------*/@Test void fork2 () { run("""
//OMIT_START
Fork : { .choose[Val](leftRight: LeftRight[Val]): Val, }
LeftRight[LR]: { .left: LR, .right: LR }
Left : Fork{::left}
Right: Fork{::right}
A:{#(someFork: Fork):Str->
//OMIT_END
someFork.choose{
  .left->`Hello`,
  .right->`Hi`,
}
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------
To this end we could declare `Fork` as follows:
```
Fork : { .choose[Val](leftRight: LeftRight[Val]): Val, }
LeftRight[LR]: { .left: LR, .right: LR }
Left : Fork{::left}
Right: Fork{::right}
```
The main difference is that instead of taking a `leftVal` and a `rightVal` parameter, we take a single parameter of type `LeftRight[Val]` that can compute the two original parameters when needed.

Type `LeftRight[Val]` is a generic type.

In the same way, `LeftRight[LR]:{ .left: LR, .right: LR }` is a generic type declaration.

Before we have seen generic methods, as methods taking both type parameters and actual parameters. Alternatively, we can see generic methods as a way to define an infinite amount of concrete methods; one for each possible type instantiation.

Generic type declarations are a different concept, and they denote families of types:
One for each possible type instantiation.

In this case there is `LeftRight[Str]`, `LeftRight[Int]` and so on.
Even `LeftRight[LeftRight[Str]]` is a valid member of the `LeftRight` family.
As you can see, a single generic type declaration actually declares an infinite amount of types!

While generic parameters are inferred for generic methods, they are always explicit for generic types.
When at the start we declared `Direction:{.turn:Direction,}`
thanks to the sugar we were actually declaring `Direction[]:{.turn[]():Direction[],}`.
Again, empty parentheses can be omitted.

We can now understand what is the meaning of 
```
someFork.choose{
  .left->`Hello`,
  .right->`Hi`,
}
```
Without the sugar and the type inference, the code would look as follows:
-------------------------*/@Test void fork3 () { run("""
//OMIT_START
Fork : { .choose[Val](leftRight: LeftRight[Val]): Val, }
LeftRight[LR]: { .left: LR, .right: LR }
Left : Fork{::left}
Right: Fork{::right}
A:{#(someFork:Fork):Str->
//OMIT_END
someFork.choose(SomeLeftRight[]:LeftRight[Str]{
  .left[]():Str -> Str1[]:`Hello`[]{},
  .right[]():Str-> Str2[]:`Hi`[]{},
})
//OMIT_START
}
//OMIT_END
"""); }/*--------------------------------------------

That is, the argument of `Fork.choose` is a literal of some anonymous type that implements `LeftRight[Str]`, since that is the expected type for the argument of `Fork.choose`.
In order to implement a `LeftRight[Str]`, we need to specify an implementation for the two abstract methods, `.left` and `.right`.
Another advantage of this new way, is that we can now write complex and time consuming computations inside the body of methods `.left` and `.right`, and only one of those computations is only going to be triggered.

A good way to understand how generic types work is to do the same reasoning we did for generic methods; the code below can be understood as the following:
```
//with generics
Fork : { .choose[Val](leftRight: LeftRight[Val]): Val, }
LeftRight[LR]: { .left: LR, .right: LR }

//idealised version with expanded generics
Fork : {
  .chooseInt(leftRight: LeftRightInt): Val,
  .choosePoint(leftRight: LeftRightPoint): Val,
  ...
  }
LeftRightInt: { .left: Int, .right: Int }
LeftRightPoint: { .left: Point, .right: Point }
...
```
As you can see, `LeftRightInt` and `LeftRightPoint` are different types, and thus there is no subtyping relation between them. In the same way, 
`LeftRight[Int]` and `LeftRight[Point]` are different types, and thus there is no subtyping relation between them either.

### Recap

- Generic methods and Generic types are ways to declare an infinite amount of methods and types.

- Dynamic dispatch is used to make decisions. Here `Turn` has an abstract method `.choose`.
The `Left.choose` implementation choses the `.left` option, while the
`Right.choose` implementation choses the `.right` option.

In Fearless, there are many types that look like `Fork`, we will see them next.
Overall, `Fork` itself is not really used in Fearless, but it is a really interesting type, and it should open your mind to the next big topic: Booleans.

END*/
}
