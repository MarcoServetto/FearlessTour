package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_024Chapter03Collections {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Collections

## Flows and related data types

As you can see, In Chapter 2 we showed how to build our own `Stack[E]` by hand and how to make the tank game using our own stack and `.map` methods. Here in Chapter 3 we have shown `List[E]` and how to make the tank game using `Flow[E]`.
We will conclude Chapter 3 showing many useful examples of flows and related data types, and many nice ways they can be used.

### Method `.map`
Flows can be used to transform lists in other lists.
When focusing on this aspect, it is important to keep in mind the size of the lists:
If the output list is supposed to be of the same size as the input list, we can use map.
For example:
```
Person: { .name: Str }
...
Names: {  #(ps: List[Person]): List[Str] -> ps.flow.map{::.name }.list   }
```
The code above uses `.map` to turn the list of persons `ps` into a list containing just the names of those persons.
The code above uses again the `::` sugar. `{::.name}` is equivalent to `{p->p.name}`.
Note the common pattern: we call `.flow` on our list, we call one or more operations (map in this case),
and finally call `.list` to collect the result into a new list.

### Method `.filter`
Sometimes we have elements that we do not like.
We can use `.filter` to take them away. For example:

```
Person: { .name: Str }
...
Doctors: { #(ps: List[Person]): List[Person] ->ps.flow.filter{::.name.startsWith `Dr.` }.list }
```

Here we only keep the persons whose name starts with `` `Dr.` ``.
That is, the list in output is always going to contain only elements from the original list; but not all elements may be present. Thus the list in output may be shorter than the list in input. Again, the size of the input/output guides our reasoning.

### Method `.flatMap`
Sometimes we want even more elements.
We can use `.flatMap`. For example:

```
Person: {  .name: Str; .cats: List[Cat]  }
Cat: { .name: Str }
...
AllCats: {  #(ps: List[Person]): List[Cat] -> ps.flow.flatMap{::.cats.flow }.list  }
```
Here we extract all the cats owned by the persons in the list.
Note how we use  `{::.cats.flow}` and not just `{::.cats}`. Method `.flatMap` requires a lambda returning a flow.
In this example we also see how we change the type of our list: we take a `List[Person]` in input and we produce a `List[Cat]` in output.
While `.flatMap` is often used to add elements, it may also remove them. For example, if all the persons in the input list have no cats, the output will be the empty list.

### Method `.join`
We have seen how flows can be used to transform lists into other lists.
There are also many ways to extract single values from flows.
Flows of strings can be joined into a single string by providing the separator.
For example:
```
Person: { .name: Str }
...
AllNames: { #(ps: List[Person]): Str -> ps.flow.map{::.name }.join `, ` }
```
The code above first uses `.map` to extra the names of all the persons, and then joins all the names together; separated by the "comma and space" characters.

We can also compute the sum of the size of all the names as follows:
```
Person:{ .name: Str }
...
SumSizes:{ #(ps: List[Person]): Str -> ps.flow.map{::.name.size }.sum 0 }
```

This sums all the sizes of all the names starting from zero.
That is, methods `.join` and `.sum` only work on flows of certain types.
Strings are joinable and number types are summable.

### Methods `.any`, `.all`, `.none`

We often want to extract a boolean value from a flow by searching if some elements respect a predicate.
```
Person:{ .name:Str; .cats: List[Cat]}
Cat:{ .name: Str }
...
Sad :{ #(ps: List[Person]): Bool -> ps.flow.any{::.cats.isEmpty  } }
```
The `.any` checks if any of the elements satisfy the predicate `{::.cats.isEmpty}`.
That is, the predicate is just a function returning a boolean.
Here we are searching if there is a person that sadly lacks cats.
This operation does not mindlessly search all the persons: as soon as the first catless person is found, we have our answer and we can return a result without any more searching.
Similarly, `.none` is true only if none of the elements respect the predicate.
Method `.all` instead checks that all of the elements respect the predicate, and it stops when it finds an element that does not.


### Method `.fold`
A typical operation is to extract the "best" element from a flow.
But, what makes something the best? We can use the `.fold` method and accumulate the best element using an `.if`, as shown in the code below.
```
Person:{ .name:Str; .cats: List[Cat] }
Cat:{ .name: Str }
...
Nicer :{ #(owner: Person, ps: List[Person]): Person->ps.flow
  .fold({owner}, {acc, p -> 
    p.cats.size >= (acc.cats.size) .if {.then->p, .else->acc,  } 
  }}
```
The `.fold` works pretty much in the same way of the `.fold` method we have seen on the stack.
The method takes two parameters: an initial value and a lambda accumulating elements on that value.
In the stack we passed the initial value directly, while here we are doing `{owner}`.
This is again to facilitate working with reference capabilities: this allows us to pass any function that does not capture mutable state, even if the result of the function is a mutable object.
It is very common for accumulator objects to be a freshly created mutable object that grows during the `.fold`.

Now that we have seen fold, we can see the implementation of `.sum`:
````
Flow[E:*]:{
  ...
  mut .sum(sn: SumNumber[E]): E -> sn.sumNumber this;
  }
SumNumber[E:*]:{ .sumNumber(mut Flow[E]): E }

Nat:Sealed,DataType[Nat,Nat],SumNumber[Nat]{
  ...
  .sumNumber xs -> xs.fold( {this}, {acc,e -> acc + e} );
  }
````
As you can see, to call `Flow[E].sum`, we need an argument implementing 
`SumNumber[E]`.
All the numeric types implement `SumNumber[_]`, so we can use any numeric value as as starting point for our sum.
This patterns allows to limit what parameter types I can pass to a generic entity.
The parameter type and the receiver generic type can cooperate to accept each other.
Method `.join` works in the same conceptual way.

## Lists
We have seen that we can call `.list` to produce a list out of our flows.
We can also use `Lists#(a,b,c,d...)` to create lists directly.
Lists are similar to the Stacks we discussed before, but they have different functionalities:


### Limited size and failures:
Lists contain any number of elements, from 0 up to the maximum positive number represented by an `Int`.
That is a big but not unlimited number, and flows could in principle contain many more elements than that. This means that some operations may fail when we can not make a list with a number of elements bigger than that limit. For example `myFlow.list` may fail to create a list big enough to store all the elements of the flow.

### Core List methods for Random Access: .size, .isEmpty and .get

With our home made `Stack[E]`, the only way to compute the amount of elements in the stack is to explore the whole stack and manually count the elements one by one.
Lists offers a method `List[E].size` that can return the number of elements in the list without the need of counting them. The size information is simply stored directly. For convenience, there is also a method `isEmpty` equivalent to calling `myList.size == 0`.

The method `List.get(i: Nat)` either returns the element in index `i` or uses `Error.msg(..)` to report the lookup failure.
This is particularly convenient for many applications if you can tolerate the continuous risk of failure.
Flow operations are especially effective when the exact positions of the elements in the list are not crucial.
However, when elements are arranged in a specific sequence, accessing them via index becomes essential. In a list, indexing starts from `0` and ends at `size - 1`. Thus, in a list of `5` elements, the indices are as follows:
- The first element is at index `0`,
- The second at index `1`,
- The third at index `2`,
- The fourth at index `3`,
- The fifth and last at index `4`.

This zero-based indexing system has beneficial mathematical properties. For example, it allows a single list to efficiently emulate a grid structure. Consider a 3x5 grid as depicted below, where `x` ranges from `0` to `4` (columns) and `y` from `0` to `2` (rows). Each cell `x, y` corresponds to the **single index** `y * 5 + x` in a flat list of length `15`:

```
 x,y                          -->  index mapping
 0,0 | 1,0 | 2,0 | 3,0 | 4,0  -->   0 |  1 |  2 |  3 |  4
 0,1 | 1,1 | 2,1 | 3,1 | 4,1  -->   5 |  6 |  7 |  8 |  9
 0,2 | 1,2 | 2,2 | 3,2 | 4,2  -->  10 | 11 | 12 | 13 | 14 
```
This grid shows how a two-dimensional coordinate system `x, y` can be flattened into a single list index. For example, `3, 1` maps to index `8`, since it’s the 4th element in the 2nd row of a 5-column grid.

That is, we can encode this grid in a single list.
The element in position `x, y` can be accessed using the formula
`index = y * 5 + x`, where 5 is the number of columns in the grid.

On the other side, if we have the `index` , we can recover the original `x, y` coordinates as:
`y = index divided by 5` and `x = reminder of the division between index and 5`
This bidirectional mapping makes it easy to simulate a 2D grid using a flat array.

A type representing the grid could look like this:

```
Grid:{
  .inner: List[Elem];
  .get(x: Nat, y: Nat): Elem -> this.inner.get(y * 5 + x);
  .y(index: Nat): Nat -> index .div 5;
  .x(index: Nat): Nat -> index .rem 5;
  }
```
Note: since `index` is a Nat, the division is rounded down (**integer division**).
For example `13 .div 5 = 2`; and the reminder/`.rem` operation returns the reminder of the
integer division: `13 .rem 5 = 3`

This kind of encoding was crucial in the past for extracting performance benefits from primitive machines with severe memory constraints. Today, while less common, it remains critical in some contexts.

### The `.as` method
`List[E].as` is a method creating a new list where the elements have gone through some kind of transformation.
That is, the following three expressions are equivalent:

```
  List#(`3`,`4`,`5`)
  List#(1,2,3).as{::+ 2 .str}
  List#(1,2,3).flow.map{::+ 2 .str}.list
```

While method `List[E].as` mostly behaves as `.flow.map.list`, the compiler can optimise it to run faster in a few crucial cases.

### Lists and mutability
Stacks as shown before are always immutable, while lists support both mutable and immutable elements.
Note how there are now two reference capabilities in play:
the capability of the list and the capability of the elements. This gives us a range of possible list types.

**Clear and useful options:**


- `List[Num]` is an immutable list of immutable numbers. It can also be written as `imm List[imm Num]` since `imm` is the default.
  You can think of it as a hard container of hard elements. For example a wooden plank with coins permanently glued into it.

- `mut List[mut Animal]` is a list of mutable animals. 
Since reference capabilities impact the whole reachable object graph, an object storing mutable objects needs to be mutable too.
Thus, we need to use `mut` twice.
Using `.get` we can obtain `mut` references to the contained animals and mutate them.
If we have a binding `animals` containing `[bunny,bunny]`; that is, the same bunny twice, we can call `animals.get(0).run` and the bunny will mutate; it will now be in a new position. Since the same bunny is contained in the list twice, `animals.get(1).location` will also result in the same updated location.
You can think of it as a hard container of soft elements, for example a table with soft and malleable clay sculptures permanently glued into it.

**Unclear and confusing options:**

In addition to the two options above, there are a few other permutations. Those should not be used in actual programs, since better more clear types are available.

- `mut List[Num]` is a list of immutable numbers. Here the fact that the list is mutable does not have any effect, since the contained numbers are immutable anyway. Types like this may emerge when using generics:
Consider the following code:
```
  .m[X:*](f: mut MF[X]):mut List[X]->Lists#(f#,f#,f#)
```
This code creates a list by repeatedly using the mutable function `f`.
When calling this code with `X=Num`, we would produce a `mut List[Num]`.
If we can not promote this result to `imm`, we can convert it to `imm` by doing `myList.as{::}`.
There `{::}` will be desugared as `F[Num,Num]{a->a}`. That is, a simple identity function, mapping the elements to themselves. Since method .as returns an `imm List`, we are effectively converting the `mut List[E]` into an `imm List[E]`.
This and a few other patterns are also optimised by the compiler to not create a new object but reuse the old one. Basically by calling this method we are guiding the type system to recognise that a `mut List[Num]` is really just an `imm List[Num]`.

- `List[mut Animal]`, also written as `imm List[mut Animal]` behaves exactly like a `List[Animal]`, that is, an immutable list of immutable elements, but the type system does not know about this. As for before, if we have an `animals: List[mut Animal]` and we need a `List[Animal]` we can just call `animals.as{::}`.
This last case often emerges from promotion: we may start with a `mut List[mut Animal]` and then turn it into immutable.

### Other core list methods: `+`, `++`, `.subList`

We can concatenate lists and elements using methods `++` and `+`.
The expression below are all equivalent:
```
Lists#(1,2,3,4)
Lists#(1,2) + 3 + 4
Lists#(1,2) ++ Lists#(3,4)
Lists#(1) ++ Lists#(2,3,4)
```
As you can see, we use `+` to concatenate list and element, and `++` to concatenate two lists.
Since `+` is a method of list, the following code does not work:
```
1 + Lists#(2,3,4)
```
We need to instead wrap the `1` in a singleton list, as shown above.

Finally, we can use `.subList(start,end)` to get a sub part of a list. Method `.subList` does not clone the list, but creates a minimal wrapper object referring to the transformed indexes; thus using `.subList` on a very, very long list is still a cheap operation.

### List and withers: `.with`, `.without`, `.withAlso`
Suppose we have a list of 10 tanks, and we want to insert a new tank in the middle.
We could do `tanks.sublist(0,5) + newTank ++ (tanks.sublist(5,10))`, but it is verbose and counter intuitive.
The `List[E]` type offers dedicated methods for those kinds of operation.
To obtain the desired result we can just write `tanks.withAlso(5,newTank)`.
If we wanted to replace the tank in position 5, we could write `tanks.with(5,newTank)`; and if we wanted to remove the tank in position 5 to get a list of only 9 tanks, we could write `tanks.without(5)`.

Note how those 3 methods do not mutate the current list but create a new List that looks like the old one but with one specific change.
This is the recommended way to handle lists in Fearless, but in the rare case where either performance or observing mutation via aliasing is crucial, you can use the type `EList[E]` (editable list) allowing to add, remove and update elements in place.
Sometimes `EList[E]` is used as a builder/accumulator to eventually create a `List[E]`. Since using `EList[E]` is quite rare we do not discuss it here in detail.

## Comparing objects

The standard library provides a simple way to define and combine ordering operations (`<`, `<=`, `==`, `!=`, `>=`, `>`) through the type `Order[T]`.

### Understanding `Order[T]`

The type Order represents the outcome of a comparison between two elements.
First we want to understand the match type for ordering:

````
OrderMatch[R:**]: { mut .lt: R; mut .eq: R; mut .gt: R; }
````
This looks like a standard matcher with three possible outcome: either the data is `lt` (less then), `eq` (equal), or `gt` (greater then).
But.. what is this data? Here we are talking about the result of a comparison operation. There is no need to materialise the data, we can just pass the matcher itself.
Consider this code:
````
Order[T]:{
  read .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  }
````
Here `.cmp` Does not return a thingy that can be `lt/eq/gt` so that we can then later match on it. It directly takes the matcher, so that we can immediately jump to the final result `R`.
We could implement it on our `Point` type as following:
````
Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> Block#
    .if {t0.x < (t1.x)}.return {m.lt}
    .if {t0.x > (t1.x)}.return {m.gt}
    .if {t0.y < (t1.y)}.return {m.lt}
    .if {t0.y > (t1.y)}.return {m.gt}
    .return {m.eq};
  read ==(other: read Point): Bool -> self.cmp(self,other,{.lt->False; .eq->True; .gt->False;});
  }}
````
As you can see, now we have a `.cmp` method in `Point`, and we can use it to implement `==`.
Crucially: `==` only uses `.cmp`. Great! we should move it into `Order[T]`

````
Order[T]:{
  read .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  read ==(other: read T): Bool -> this.cmp(this,other,{.lt->False; .eq->True; .gt->False;});
  }
Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> ...;
  }}
````
This code above does not compile.
Can you spot why?
**Solution coming soon**

**Solution coming soon**

**Solution coming soon**

**Solution coming soon**

**Solution coming soon**

**Solution:** 
In this code `this.cmp(this,other,{...});` we use `this` twice: the first time as `Order[T]` to call `.cmp`, but the second time we use it as a `T`.
And the type system do not see any connection between `T` and `Order[T]`.
The code used to work in `Point` because `Point` implements `Order[Point]`; thus in the context of `Point`, `self` was both a `Point` and an `Order[Point]`.

We can solve this type limitation by adding a `.close` method:

````
Order[T]:{
  read .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  read .close: read T; /// convert 'this' from type 'Order[T]' to type 'T'
  read ==(other: read T): Bool -> this.cmp(this.close,other,{.lt->False; .eq->True; .gt->False;});
  }
Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> ...;
  .close->self;
  }}
````
Now we can implement `==` on `Order[T]` and `Point` will automatically get an `==` method.
Right now it does not look like a great result, we implement one method `.cmp` to get one method `==`.
But.... there are many more convenience operators we can define on top of `.cmp`.
`==` equals, `!=` different, `<` less then, `<=` less or equal, `>` greater then, `>=` greater or equal.
There is more! we can check if our point is in a range between two other points.
Ranges can be open an closed on both ends, causing four methods: `.inRange(lo,hi):Bool`, `.inRangeOpen(lo,hi)`, `.inRangeLoOpen(lo,hi)`,`.inRangeHiOpen(lo,hi)`.
As you can see, this is already 10 methods.

Still, the implementation of `.cmp` is much longer then we would like.
Can we build some abstraction to make it more direct?
It is very verbose because we are repeating checks `<` and `>` for the components.
But.. those components implement `Order[T]` too, so we could call `.cmp` directly.


````
Order[T]:{
  read .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  read .close: read T; /// convert 'this' from type 'Order[T]' to type 'T'
  read ==(other: read T): Bool -> this.cmp(this.close,other,{.lt->False; .eq->True; .gt->False;});
  //and 9 more methods <,>,<=,>= etc implemented using .cmp
  }
Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> t0.x.cmp(t0.x,t1.x,{.lt->m.lt; .gt->m.gt; 
    .eq->t0.y.cmp(t0.y,t1.y,m));
  .close->self;
  }}
````
This is better, but having to pass `t0.x` and `t0.y` twice is still a repetition.
Any coding investment we do in `Order[T]` can pay off every time we implement it, so a little more abstraction is worth it:

````
OrderMatch[R:**]: {
  mut .lt: R; mut .eq: R; mut .gt: R;
  mut &&(onEq: mut MF[R]): mut OrderMatch[R]-> {
    .lt->this.lt;
    .eq->onEq#;
    .gt->this.gt; 
    }
  }
Order[T]:{
  read .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  read .close: read T; /// convert 'this' from type 'Order[T]' to type 'T'
  read <=>[R:**](other: read Order[T], m: mut OrderMatch[R]): R ->
    this.cmp(this.close, other.close, m);
  read ==(other: read T): Bool -> this.cmp(this.close,other,{.lt->False; .eq->True; .gt->False;});
  //and 9 more methods <,>,<=,>= etc implemented using .cmp
  }
Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> Points:{#(x: Nat, y: Nat): Point -> Point: Order[Point]{ 'self
  .x: Nat -> x; .y: Nat -> y;
  .cmp t0, t1, m -> t0.x<=>(t1.x,m &&{t0.y<=>(t1.y,m)});
  .close->self;
  }}
````
And here we have it.
- Method `OrderMatch[R]&&` makes it easier to compose matchers by only overriding the `.eq` case.
- Method `Order[T]<=>`  is easy to use, while `Order[T].cmp` is easy to define.
With both, we can define `.cmp` by using `<=>` on the sub components.

With this implementation strategy, it is easy to define combinations of multiple orderings. In this example, if the `x` coordinates are the same, the `y` coordinate is used.
Similarly, we could have a `Car` with a `.speed` and a `.colour`.
If the two cars differ in `speed`, the faster car is preferred.
Only if both cars have the same `.speed`, the `.colour` preference is considered to determine which one is better.
It is common to compare entities using multiple criteria as shown above. This pattern combines two ordering results lexicographically, similar to sorting by multiple columns.

### Understanding `OrderBy[T,K]` and `OrderBy[T]`

What if we want to find the max of a list of `Nat`?
As you may have guessed, `Flow[E]` has all the methods we may even want and more.
We can simply do `myListNats.flow.max{::}.list`
This will return the list of all the numbers that are equally the max.
If `myListNats` is `Lists#(1,2,3,3,2,1,1,0)` we would get `Lists#(3,3)`.
But... how can that work?
`Flow[E]` has no way to know if the element `[E]` implements `Order[E]` or not.
This is where the `{::}` parameter comes handy again.
Since in the call site `E` is a `Nat` and `Nat` implements `Order[Nat]`, the identity function `{x->x}` can take a `Nat` and produce an `Order[Nat]`.
The parameter of `.max` could logically be just of type `F[E,Order[E]]`, but to help the inference and to add some useful features, we define a custom type `OrderBy`.
````
OrderBy[T,K]:{ #(read T): read Order[K]; }
OrderBy[T]:OrderBy[T,T]{
  .cmp[R:**](t0: read T, t1: read T, m: mut OrderMatch[R]): R;
  # t -> { .close -> t; .cmp t0,t1,m -> this.cmp(t0,t1,m) };
}
````
Those two types are designed to cooperate well with the type inference and syntactic sugar.
If we have our iconic `Person:Order[Person]` with an `.age:Nat`, we can write
`{::}` to get an `OrderBy[Person,Person]` and
`{::.age}` to get an `OrderBy[Person,Nat]`.
On the other side, if we want to give a top level name for a specific way to order persons, we can do it by using `OrderBy[Person]`:
```
ByCats:OrderBy[Person]{
  p1,p2,m -> p1.cats.flow.map{::.weight}.sum 0 <=> (p2.cats.flow.map{::.weight}.sum 0, m);
  }
```
And now we can use `ByCats` to compare two persons based on who own more cats, by total weight of course.


`OrderBy[T,K]` also offers some useful utilities:
````
OrderBy[T,K]:{
  #(read T): read Order[K];
  .then[K0](next: OrderBy[T,K0]): OrderBy[T] -> 
    {t0,t1,m -> this#t0 <=> ( this#t1, m &&{ next#t0<=>(next#t1,m)}) };
  .view[A](f: F[read A,read T]): OrderBy[A] ->
    {a0,a1,m-> this#(f#a0)<=>(this#(f#a1),m)};    
  }
````
- Method `.then` lexicographically composes the `OrderBy` with another one.
Example usage: `{::.age}.then{::.name}` would compare a person by age first and name second. `{::.age}.then ByCats` would compare by name first and by total cats weight second.
- Method `.view` allows to compare entities of type `A` if we can convert them into a `T` for witch we have an `OrderBy`.
Example usage: `ByCats.view{::.driver}`
would compare a `Car` by the total cats weight of its `.driver`.

Here some more boring examples:
```
Persons:{#(name:Str, age:Nat):Person -> Person:{.name: Str -> name; .age: Nat -> age }}
Older:OrderBy[Person]{ p1,p2,m -> p1.age <=> (p2.age, m) }
OlderLonger:OrderBy[Person]{ p1,p2,m -> p1.age <=> (p2.age, m&&{p1.name.size <=> (p2.name.size,m)} }
```

### Comparators and Flows: `.max`, `.min`, `.sort` and `.distinct`

`Flow[E]` offers methods `.max`, and `.min` to find the biggest and smallest element `E`.
Finding the max from some elements is not as obvious as it looks; there are two main corner cases:

- The flow may be empty. In this case, there is no such thing as a biggest/smallest element.
- The flow can contain multiple elements that are equally the biggest/smallest.

Given this, the best design for `.max` and `.min` is to work as filters, and just remove all the elements that are not the max or the min.
In this way, the empty flow would stay empty, and all the biggest/smallest elements would be preserved.
Assuming a car type with a `Person` driver, here you can see some interesting usage examples.
```
myCars.flow
  .max{::}//if Car implements Order[Car]
  .list //to get the list of all the max cars
myCars.flow
  .max{::driver}//comparing drivers. Ok since Person implements Order[Person]
  .get //this requires that there is exactly one max
myCars.flow
  .max({::driver}.then Older) //here we check using the Older comparator
  .opt//this requires that there is exactly zero or one max
myCars.flow
  .max(OrderByCaseUnsensitive.view{::driver.name}.then {::driver.age}) //here names ignoring case
  .first //this gives us an Optional[Car] and allows for further max cars to be discarded.

myCars.flow
  .max(OrderByCaseUnsensitive.view{::driver.name})
  .max{::driver.age}//this example has the same behaviour of the one above
  .first//like with .filter: we can divide two conditions on two calls if we prefer

myCars.flow
  .max OrderBy[Car]{c1,c2,m->...}// to write a comparator by hand
  .min {::} //to use the conventional comparator on the equally maximal cars
  .list
```

Flows offer method `.sort` to sort the elements.
It takes the same parameters as `.max`/`.min`.

Flows also offer method `.distinct` to remove duplicates, and `.sortDistinct` to sort while removing duplicates.

Note how all of those methods take some ordering criteria.
Those `Flow[E]` methods are not enforcing a unique kind of ordering, they ask the user to provide the ordering.

### Maps, sets and hashing.

Hash maps (or hash tables) are one of the most popular data structures in programming.
They are a data structure that stores key-element pairs, allowing for fast retrieval of elements based on keys. It uses a hash function to compute an index (or "hash") from each key, which determines where the element is stored in a large private list. This allows for nearly constant-time complexity for lookups.
Fearless `Map[K,E]` uses hashing too. We have already seen that stacks and lists contain elements. Maps also contain elements, but connected to keys. Maps are a way to link keys to elements, similar to how a dictionary associates words with definitions. For example, a map could link a person's name (the key) to their phone number (the element).

It’s no coincidence that we have both a `Map[K,E]` type and a `Flow[E].map` method.
They both represent the idea of connecting one kind of value with another kind.
The `.map` method transforms a value representing the input of one stage of a computation into a value representing the output of such computation.
In a similar way, the `Map[K,E]` type holds a mapping between keys and elements, connecting distinct pieces of data.

As mentioned, `Map[K,E]` needs a hash function for the key `K`. 
Since ordering and hashing has to live together, the standard library defines
`OrderHash[T]` implementing `Order[T]`.
The `OrderHash[T].hash` method plays this role by computing a numeric summary of an object. Although it's impossible for a hash function to uniquely identify each distinct object due to the infinite number of possible objects and the finite number of `Nat` values, a well-designed hash function approximates this by minimising situations where distinct objects have the same hash value. Objects are considered distinct if the `==` operation returns `False`.
Two objects that are equal via `==` must have the same hash value. This consistency is required for the map to work correctly.
Implementing the `.hash` method by returning zero is inefficient but technically correct: since all objects will have the same hash code, all equal objects will also trivially have the same hash code.
The map attempts to use the hash code as a fast screening test to quickly differentiate objects, and uses the slower `==` only when needed.
The default zero `.hash` method de facto disables this crucial optimisation. Instead, a good hash function spreads out objects evenly across the Nat numbers  to maximise the efficiency of the map operations.
Having the `.hash` method within the `OrderHash[T]` type helps maintain alignment between the behaviours of hashing and equality. By encapsulating both within the same type, it simplifies the enforcement of the principle that equal objects must have identical hash codes, thereby supporting more predictable and reliable map behaviour.

Ideally, we would just need this
````
OrderHash[T]:Order[T]{ read .hash: Nat; }
````
But, it turns out that by adding just a little more we can get a much better payoff:

````
OrderHash[T]:Order[T],ToStr{
  read .hash: Nat;
  read .close(t: read T): read OrderHash[T];
  read .assertEq(expected: read T):Void -> ...;
  read .assertEq(expected: read T, msg:F[Str]):Void -> ...;
  read .assertNe(expected: read T):Void -> ...;
  ...
}
````
By adding `ToStr` we are able to automatically derive 20 assert methods helping to check expectations over `T`; the conversion to string is needed for decent error message.
Moreover this allows maps to be much more consistent with lists when it comes to printing: both need to just take a way to print the element; all the functionalities about map keys are provided once and for all at map initialisation time.
Note how we also add `.close` in the other direction: before we have seen 
`.close:T` allowing to turn `Order[T]` into `T`. This one allows to turn a `T` parameter into an `OrderHash[T]`. With this we can convert in both directions.
Again, needed in the error messages to turn a `T` into an `OrderHash[T]` that possess a `.str` method.

````
//usage
Persons: { #(age: Num, name: Str):Person -> Person: OrderHash[Person]{
  .age: Nat    -> age;
  .name: Str   -> name;
  .cmp p1,p2,m -> p1.age <=> (p2.age, m && { p1.name <=> (p2.name,m) });
  .hash h      -> this.age.hash.hashWith(this.name.hash);
  .str         -> `Person[age=`+this.age+`, name=`+this.name+`]`;
  }
}
```
With such a `Person` type, we can define a map from persons to address:

```
Maps#({::},   // this {::} is expanded as OrderHashBy[Person]{x->x}
  Persons#(25,`Bob`),`Toronto 34b Warden St.`,
  Persons#(34,`Alice`),`Wellington 134 Kelburn parade`,
  ...
  )
```
As you can see, maps take an `OrderHashBy` exactly like we have seen with `OrderBy` and `ToStrBy`.
`Map[K,E]` has many operations, similarly to `List[E]`.
We have `.size`, `.isEmpty` and `.get`.
However, `.get` does not take an index of type `Nat` but a key of type `K`.
For example, with the map declared above

```
myMap.isEmpty //false
myMap.size //2
myMap.get(Persons#(34,`Alice`)) // `Wellington 134 Kelburn parade`
```
If the key is not present in the map, `.get` will cause an error.
We can instead use `.opt` to extract an optional `Opt[E]` result. For example
```
myMap.get(Persons#('Neil Armstrong',38)) // error
myMap.opt(Persons#('Neil Armstrong',38)).or 'Moon' // alternative default value.
```

Maps can have `mut`, `imm` or `read` elements; but only immutable keys. This is because the implementation of `Map[K,E]` needs to assume that the result of `.hash` and `==` is consistent over time.

We can flow on a map, but since both keys and elements are present, the flow method takes a function mapping keys and elements in some value.
For example
```myMap.flow{k,e-> k.name + e }.list```
will return
```List#(`BobToronto 34b Warden St.`,`AliceWellington 134 Kelburn parade`)```

Note how the order of the flow is the same as the insertion order.

We can also create a map from a flow, using method `.mapping` as shown below:
```
myPersons.flow
  .map{..}
  .filter{..}
  .mapping({::},{
    .key e  -> e.name;
    .elem e -> e;
    })
```
Here we pass two parameters: a `OrderHashBy`, that as usual can be the identity if our keys implement `OrderHash[K]`, and a literal specifying how to create the key and the element from the objects inside the flow.

Finally, sets of type `Set[K]` are another application of hashing. Instead of mapping keys to elements, it simply remembers if a key is present or not. That is, `Set[K]` most important methods are `.size`, `.isEmpty` and `.contains`.
Sets also support `.flow`, working exactly as for lists. Again, the order of the flow is the same as the insertion order.
See below some examples of using sets.
```
Sets#({::},1,2,3,4,5)//set of 5 numbers
Sets#(OrderBy[Str]{s1,s2,m->...},'a','aa','aaaaa')//Does not compile.
Sets#(StrSizeOrder,'a','aa','aaaaa')//good
StrSizeOrder:OrderHashBy[Str]{
  t0,t1,m-> t0.size<=>(t1.size,m);
  .hash s->s.size.hash;
  .str s->s; 
  }//Here we manually define an ordering for strings using their size.
```
Note how our first attempt for a set with custom ordering does not compile.
We need to use `OrderHashBy` and not just `OrderBy`.

We can create a set from a flow by calling `.set{::}`, or passing a specific order.

In the same way there is an `EList[E]` type that is an editable variant of `List[E]`, there are types `EMap[K,E]` and `ESet[E]`. As for `EList[E]`, they are rarely used so we will discuss them (much) later.

### List, Opt and ordering.

Finally, `List[E]` and `Opt[E]` do not implement `Order[E]` or `OrderHash[E]`.
They do not need to and they may not contain ordered elements.
If I want to sort a list of ordered elements I can do
myList.flow
  .sort{::}
  .list

However, how to sort a list of lists?

### Introducing `Order[T,E]` and `OrderHash[T,E]`.

To order a collection we need to reason on two generic types:
- The type of the current collection `T`.
- The type of the collection elements `E`.
The idea is that by providing an `OrderBy` for the elements, we can produce an
`Order` for the collection.
```
Order[T,E:*]: {
  read .close: read T;
  read .cmp[K,R:**](by: OrderBy[imm E,K], t0: read T, t1: read T, m: mut OrderMatch[R]): R;

  read .order[K](by: OrderBy[imm E,K]): read Order[T] -> {
    .close -> this.close;
    .cmp t0,t1,m -> this.cmp(by,t0,t1,m);
  };
}
````
The order code above requires to implement `.close` and `.cmp`, and implements method
`.order(by)` that takes an `OrderBy` for the element and produces an `Order[T]` by delegating to the `.close` and `.cmp` methods we just defined.
The core of this approach is that the outer `.cmp` takes an explicit `by` parameter, while the inner `.cmp` receives it since it is captured in the object literal.

For example, `Opt[E:*]` implements `Order[Opt[E],E]` (via `_Opt[E]`, via `DataType`).
That is, we can now understand much better the code of `Opt` shown before:
````
Opt[E:*]: _Opt[E]{
  ...
  .close->this; //shows the type system that T is just Opt[E]
  .cmp by, a, b, m -> a.match{
    .empty -> b.match{ .empty -> m.eq; .some _ -> m.lt; };
    .some ea -> b.match{
      .empty   -> m.gt;
      .some eb -> by#ea <=> (by#eb, m);
      }
    };
````

Type `OrderHash[T,E]` serves exactly the same role of `Order[T,E]` but at the `OrderHash` level.
````
OrderHash[T,E:*]:Order[T,E],ToStr[E]{
  read .hash[K](by: OrderHashBy[imm E,K]): Nat;
  read .close(t: read T): read OrderHash[T,E];
  read .orderHash[K](by: OrderHashBy[imm E,K]): read OrderHash[T] -> {
    .close -> this.close;
    .close t -> this.close(t).orderHash(by);
    .cmp t0,t1,m -> this.cmp(by,t0,t1,m);
    .hash -> this.hash by;
    .str  -> this.str by;
    };
  }
````
That help us to understand those other already seen `Opt` methods:
````
Opt[E:*]: _Opt[E]{
  ...
  .str  by  -> this.match{
    .empty -> `Opt[]`;
    .some x -> `Opt[`+(by#x)+`]`;
  };
  .hash by -> this.match{
    .empty  -> 0;
    .some e -> 31.aluAddWrap(by#e.hash);
    };
  .close->this; .close->::;
````
- We can not conceptually define `.str` on an optional without arguments.
`OrderHash[T,E:*]` implements `ToStr[E]`, guiding us to implement `.str by`
using the `by` argument to turn the optional content into a `ToStr`.
- Similarly, we can not conceptually define `.hash` on an optional without arguments.
`OrderHash[T,E:*]` 
guides us to implement `read .hash[K](by: OrderHashBy[imm E,K]): Nat`
using the `by` argument to turn the optional content into an `OrderHash`, so that we can call `.hash` on it.
- Finally, to enable all of this support we need to allow the needed type conversions.
The line of code `.close->this; .close->::;`
is a standard way to do this in fearless; implementing the two versions of `.close` in a single line.

`List[E]`, `Map[K,E]` and `Opt[E]` implement `OrderHash[List[E],E]`, `OrderHash[Map[K,E],E]`, and `OrderHash[Opt[E],E]`.
Thus, we can use the method `List[E].order`, taking a function from `E` to `Order[E]`. That is, if myList is a `List[List[E]]` and `E` implements `Order[E]` or `OrderHash[E]`, we can simply write:
```
myList.flow
  .sort{::order{::}}
  .list
```
The same exact code would work for a `List[Opt[E]]`.

`Set[E]` knows how to order its keys, so `Set[E]` implements `OrderHash[E]` directly.
On the other side, a map knows how to order the keys, but not the elements; thus falling in the same group of `List[E]` and `Opt[E]`.

Remember again how this is the same pattern we have seen for strings:
`myList.str{::}` will work if myList is of type `List[E]` and `E` implements `ToStr`. If myList is of type `List[List[E]]` we would need to write
`myList.str{::str{::}}`.
Overall, this is also similar to how `.as` works for nested lists.


OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}