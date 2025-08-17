package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static tour.TourHelper.run;
class ZH_024Chapter03Collections {
/*START
--CHAPTER-- Chapter 3
--SECTION-- Collections

## Flows and related data types

As you can see, In Chapter 2 we showed how to build our own `Stack[E]` by hand and how to make the tank game using our own stack and `.map` methods. Here in Chapter 3 we have shown `List[E]` and how to make the tank game using `Flow[E]`.
We will conclude Chapter 3 showing many useful examples of flows and related data types, and many nice ways they can be used.

## Method `.map`
Flows can be used to transform lists in other lists.
When focusing on this aspect, it is important to keep in mind the size of the lists:
If the output list is supposed to be of the same size as the input list, we can use map.
For example:
```
Person:{ .name:Str, }
...
Names:{  #(ps: List[Person]): List[Str] -> ps.flow.map{::name }.list   }
```
The code above uses `.map` to turn the list of persons `ps` into a list containing just the names of those persons.
The code above uses again the `::` sugar. `{::name}` is equivalent to `{p->p.name}`.
Note the common pattern: we call `.flow` on our list, we call one or more operations (map in this case),
and finally call `.list` to collect the result into a new list.

## Method `.filter`
Sometimes we have elements that we do not like.
We can use `.filter` to take them away. For example:

```
Person:{ .name:Str, }
...
Doctors:{ #(ps: List[Person]): List[Person] ->ps.flow.filter{::name.startsWith `Dr.` }.list }
```

Here we only keep the persons whose name starts with `` `Dr.` ``.
That is, the list in output is always going to contain only elements from the original list; but not all elements may be present. Thus the list in output may be shorter than the list in input. Again, the size of the input/output guides our reasoning.

## Method `.flatMap`
Sometimes we want even more elements.
We can use `.flatMap`. For example:

```
Person:{  .name:Str, .cats: List[Cat],  }
Cat:{ .name: Str, }
...
AllCats:{  #(ps: List[Person]): List[Cat] -> ps.flow.flatMap{::cats.flow }.list  }
```
Here we extract all the cats owned by the persons in the list.
Note how we use  `{::cats.flow}` and not just `{::cats}`. Method `.flatMap` requires a lambda returning a flow.
In this example we also see how we change the type of our list: we take a `List[Person]` in input and we produce a `List[Cat]` in output.
While `.flatMap` is often used to add elements, it may also remove them. For example, if all the persons in the input list have no cats, the output will be the empty list.

## Method `.join`
We have seen how flows can be used to transform lists into other lists.
There are also many ways to extract single values from flows.
Flows of strings can be joined into a single string by providing the separator.
For example:
```
Person:{ .name:Str, }
...
AllNames:{ #(ps: List[Person]): Str ->ps.flow.map{::name }.join `, ` }
```
The code above first uses `.map` to extra the names of all the persons, and then joins all the names together; separated by the "comma and space" characters.

We can also compute the sum of the size of all the names as follows:
```
Person:{ .name:Str, }
...
SumSizes:{ #(ps: List[Person]): Str ->ps.flow.map{::name.size }.join 0 }
```

This sums all the sizes of all the names starting from zero.
That is, `.join` is a flexible method that can compose many different kinds of values and the values know how to be composed together. Simple and Unicode string and all number types are joinable. We will see a few other joinable types later in this guide

## Method `.fold`
A typical operation is to extract the "best" element from a flow.
But, what makes something the best? We can use the `.fold` method and accumulate the best element using an `.if`, as shown in the code below.
```
Person:{ .name:Str, .cats: List[Cat], }
Cat:{ .name: Str, }
...
Nicer :{ #(owner: Person, ps: List[Person]): Person->ps.flow
  .fold(owner, {acc, p -> 
    p.cats.size >= (acc.cats.size) .if {.then->p, .else->acc,  } 
  }}
```
The `.fold` works pretty much in the same way of the `.fold` method we have seen on the stack.
The method takes two parameters: an initial value and a lambda accumulating elements on that value.

## Methods `.any`, `.all`, `.none`

We often want to extract a boolean value from a flow by searching if some elements respect a predicate.
```
Person:{ .name:Str, .cats: List[Cat]}
Cat:{ .name: Str, }
...
Sad :{ #(ps: List[Person]): Bool -> ps.flow.any{::cats.isEmpty  } }
```
The `.any` checks if any of the elements satisfy the predicate `{::cats.isEmpty}`.
That is, the predicate is just a function returning a boolean.
Here we are searching if there is a person that sadly lacks cats.
This operation does not mindlessly search all the persons: as soon as the first catless person is found, we have our answer and we can return a result without any more searching.
Similarly, `.none` is true only if none of the elements respect the predicate.
Method `.all` instead checks that all of the elements respect the predicate, and it stops when it finds an element that does not.


### Lists
We have seen that we can call `.list` to produce a list out of our flows.
We can also use `Lists#(a,b,c,d...)` to create lists directly.
Lists are similar to the Stacks we discussed before, but they have different functionalities:


# Limited size and failures:
Lists contain any number of elements, from 0 up to the maximum positive number represented by an `Int`.
That is a big but not  unlimited number, and flows could in principle contain many more elements than that. This means that some operations may fail when we can not make a list with a number of elements bigger than that limit. For example `myFlow.list` may fail to create a list big enough to store all the elements of the flow.

A Fearless method can indicate failure by throwing an error.
Errors are not part of the basic semantic of Fearless, and they can be thrown using magic methods or convenience methods using magic methods internally.
For example, the method `Error.msg(Str)` will throw an error using that string as an error message, plus some more debugging information.
When a method throws an error the computation stops and the error is reported outside of the program. That is, the whole Fearless application stops and burns.
This is often the desired behaviour, especially when debugging.
However, this behaviour can be overridden using 
`Try#{ ../*code that can fail*|/.. }`. We will discuss the details of Try later, and for now we will assume all errors to simply obliterate the running code.

# Core List methods for Random Access: .size, .isEmpty and .get

With our home made `Stack[E]`, the only way to compute the amount of elements in the stack is to explore the whole stack and manually count the elements one by one.
Lists offers a method `List[E].size` that can return the number of elements in the list without the need of counting them. The size information is simply stored directly. For convenience, there is also a method `isEmpty` equivalent to calling `myList.size==0`.

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
`y = index / 5` and `x = index modulo 5`
This bidirectional mapping makes it easy to simulate a 2D grid using a flat array.

A type representing the grid could look like this:

```
Grid:{
  .inner: List[Elem],
  .get(x: Nat, y: Nat): Elem -> this.inner.get(y * 5 + x),
  .y(index: Nat): Nat -> index / 5,
  .x(index: Nat): Nat -> index.mod(5),
  }
```
Note: since `index` is a Nat, the division is rounded down (**integer division**).
For example `12/5 = 2`; and the modulo/`.mod` operation returns the reminder of the
integer division: `12 modulo 5 = 2`

This kind of encoding was crucial in the past for extracting performance benefits from primitive machines with severe memory constraints. Today, while less common, it remains critical in some contexts.

# The `.as` method
`List[E].as` is a method creating a new list where the elements have gone through some kind of transformation.
That is, the following three expressions are equivalent:

```
  List#(`3`,`4`,`5`)
  List#(1,2,3).as{::+ 2 .str}
  List#(1,2,3).flow.map{::+ 2 .str}.list
```

While method `List[E].as` mostly behaves as `.flow.map.list`, the compiler can optimise it to run faster in a few crucial cases.

# Lists and mutability
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
You can think of it as a hard container of soft elements, for example a table with soft and malleable clay sculptures on top.

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
This and a few other patterns are also optimised by the compiler to not create a new object but reuse the old one. Basically by calling this method we are guiding the type system.

- `List[mut Animal]`, also written as `imm List[mut Animal]` behaves exactly like a `List[Animal]`, that is, an immutable list of immutable elements, but the type system does not know about this. As for before, if we have an `animals: List[mut Animal]` and we need a `List[Animal]` we can just call `animals.as{::}`.
This last case often emerges from promotion: we may start with a `mut List[mut Animal]` and then turn it into immutable.

# Other core list methods: `+`, `++`, `.subList`

We can concatenate lists and elements using methods `++` and `+`.
The expression below are all equivalent:
```
List#(1,2,3,4)
List#(1,2) + 3 + 4
List#(1,2) ++ List(3,4)
List#(1) ++ List(2,3,4)
```
As you can see, we use `+` to concatenate list and element, and `++` to concatenate two lists.
Since `+` is a method of list, the following code does not work:
```
1 + List(2,3,4)
```
We need to instead wrap the `1` in a singleton list, as shown above.

Finally, we can use `.subList(start,end)` to get a sub part of a list. Method `.subList` does not clone the list, but creates a minimal wrapper object referring to the transformed indexes; thus using `.subList` on a very, very long list is still a cheap operation.

# List and withers: `.with`, `.without`, `.withAlso`
Suppose we have a list of 10 tanks, and we want to insert a new tank in the middle.
We could do `tanks.sublist(0,5)+ newTank++tanks.sublist(5,10)`, but it is verbose and counter intuitive.
The `List[E]` type offers dedicated methods for those kinds of operation.
To obtain the desired result we can just write `tanks.withAlso(5,newTank)`.
If we wanted to replace the tank in position 5, we could write `tanks.with(5,newTank)`; and if we wanted to remove the tank in position 5 to get a list of only 9 tanks, we could write `tanks.without(5)`.

Note how those 3 methods do not mutate the current list but create a new List that looks like the old one but with one specific change.
This is the recommended way to handle lists in Fearless, but in the rare case where either performance or observing mutation via aliasing is crucial, you can use the type `UList[E]` (updatable list) allowing to add, remove and update elements in place.
Sometimes `UList[E]` is used as a builder/accumulator to eventually create a List. Since using `UList[E]` is quite rare we do not discuss it here in detail.


### Comparators and equality
>NOTE to future Fearless: we chose for imm comparators and hash because:
> in the future, will support the normalisation on imm objects
> sorting a list and having it 'stop being sorted' or finding the max element
> and having it 'not so max any more' can be buggy
> In the future currentMax, currentSort etc could exist using `CurrentOrder[X]` or similar. 

> Map/Set can be optimised by computing the actual hash table when the first `.get` is called.
> In this way Maps/sets that are just `.flow` around, will be as fast as lists. 
> Issue: how to remove duplicates without making the hash table


The standard library provides a simple way to define and combine ordering operations (`<`, `<=`, `==`, `!=`, `>=`, `>`) through the types `Order` and `Comparator`.

#### Understanding Order

The type Order represents the outcome of a comparison between two elements:

```
Order:Sealed{
  .match[R:**](m: OrderMatch[R]): R,
  &&(o: read F[Order]): Order -> this,
  }
OrderMatch[R:**]: { .lt: R, .eq: R, .gt: R }
OrderLt:Order{::lt }
OrderEq:Order{.match(m) -> m.eq, &&(o)-> o# }
OrderGt:Order{::gt }
```

An instance of `Order` can be one of:

- `OrderLt`: indicating the first value is smaller than the second.
- `OrderEq`: indicating both values are equal.
- `OrderGt`: indicating the first value is greater than the second.

##### Combining Multiple Criteria

Often, we want to compare entities based on multiple criteria. For example, imagine we prefer faster cars, and if two cars are equally fast, we prefer the red one.

The method `Order&&` lets us combine these two criteria easily:

```
// Speed#(carA, carB) returns:
//   - OrderLt if carA is slower than carB
//   - OrderGt if carA is faster than carB
//   - OrderEq if both cars have equal speed
// ColorPreference#(carA, carB) returns:
//   - OrderGt if carA is red and carB is not
//   - OrderLt if carB is red and carA is not
//   - OrderEq if both are red or neither is red

Speed#(carA, carB) && { ColorPreference#(carA, carB) }
```
In this example:
If the two cars differ in speed, the faster car is preferred.
Only if both cars have the same speed, the color preference is considered to determine which one is better.
It is common to compare entities using multiple criteria as shown above. The method `Order&&` combines two ordering results lexicographically, similar to sorting by multiple columns.

#### Understanding `Order[T]` and `Comparator[T]`
```
Order[T]: {
  <=>(other: T): Order //only abstract method for this < other, this == other, this > other
   ==(other: T): Bool -> this <=> other .match{ .lt->False, .eq->True, .gt->False},
   <=(other: T): Bool -> this <=> other .match{ .lt->True,  .eq->True, .gt->False},
   >=(other: T): Bool -> this <=> other .match{ .lt->False, .eq->True, .gt->True },
    <(other: T): Bool -> this <=> other .match{ .lt->True,  .eq->False,.gt->False},
    >(other: T): Bool -> this <=> other .match{ .lt->False, .eq->False,.gt->True },
   !=(other: T): Bool -> this <=> other .match{ .lt->True,  .eq->False,.gt->True },
  }
Comparator[T]:F[T,T,Order],F[T,Order[T]]{
  #(t1)->{t2->this#(t1,t2)},
  && (other: Comparator[T]): Comparator[T] -> {a,b -> this#(a,b) && {other#(a,b)}}
  }
```

There are three kinds of `Order`, for less then, equal or greater than.
A type `T` can implement `Order[T]` to make itself ordered. Alternatively, we can implement `Comparator[T]` to compare two instances of type `T`.
The `Comparator[T]` type extends function `F[T,T,Order]`, but also function `F[T,Order[T]]`.
The idea is that an `Order[T]` is just a comparator that knows the first element to compare.
In this way when we require to compare elements of type `T`, we can just require a `F[T,Order[T]]`, and if `T` is ordered, we can just pass the identity function `{::}`; otherwise we can write a comparator by hand doing `Comparator[T]{a,b->...}`.

To make this more clear: methods needing to compare elements will often require a `F[T,Order[T]]`.
If our actual `T` implements order, we can simply pass `{::}`; inference will expand it to `Anon:F[T,Order[T]]{x->x}`.
In the code examples below we will see a lot of `{::}` and they all expand in this way.

As you can see, by implementing `Order[T]` we get a lot of utility methods to check for equality and ordering.
Method `Comparator[T]&&` works like `Order&&`, to facilitate ordering on multiple criteria, but it is more common to use `Order&&` as shown below, where we make a comparator comparing two persons by age and then by age and name size.
```
Persons:{#(name:Str, age:Nat):Person -> Person{.name: Str -> name,  .age: Nat -> age }}
Older:Comparator[Person]{ p1,p2 -> p1.age <=> (p2.age) }
OlderLonger:Comparator[Person]{ p1,p2 -> p1.age <=> (p2.age) && {p1.name.size <=> (p2.name.size)} }
```

It is often the case that to compare elements we can reuse and compose existing comparators.
Below we can see the Comparators factory, supporting just that.
```
Comparators:{
  #[T](o: F[T, Order[T]]): Comparator[T] ->
    {t1,t2->o#(t1)<=>(t2)},

  #[A,T](f: F[T,A],o: F[A,Order[A]]): Comparator[T] ->
    {t1,t2 -> o#(f#t1) <=> (f#t2)},
  #[A,B,T](
      fa: F[T,A],oa:F[A,Order[A]],
      fb: F[T,B],ob:F[B,Order[B]],
      ): Comparator[T] ->
    {t1,t2->oa#(fa#t1) <=> (fa#t2) && { ob#(fb#t1) <=> (fb#t2) }},
  ...//more overloads for more comparators components
  }
```
If we have a way `o` to convert a `T` into an `Order[T]`, for example the identity function, we can make a comparator by doing `Comparators#{..}`.
If we want to compare a `T`, we know how to view a `T` as an `A`, and we can order an `A`, we can compare a `T` as an `A` by doing for example `Comparators#({::age},{::})`.
If we have multiple observations we can combine them. For example if we want to compare persons by name and age we can do 
```
Comparators#({::name},{::},  {::age},{::})
//equivalent to
Comparator[Person]{p1,p2 -> p1.name <=> (p2.name)} && {p1,p2 -> p1.age && (p2.age)}
```

Below we show how to implement `Order[T]` for Person.
```
Persons:{#(name:Str, age:Num):Person -> Person:Order[Person]{
  .name: Str -> name,
  .age: Nat -> age,
  <=>(other)->this.age <=> (other.age) && {this.name <=> (other.name)},
  }}
```
As you can see, we use `Order&&` to combine orderings.
We could instead use `Comparators`
```
<=>(other)->Comparators#({::age},{::},  {::name},{::})#(this,other),
```
We can pass `{::}` because both `Num` and `Str` implement `Order[T]`.

As you can see, in this case it is not much more compact, but there are situations where having the comparator object itself is crucial.

### Comparators and Flows: `.max`, `.min`, `.sort` and `.distinct`

`Flow[E]` offers methods `.max`, and `.min` to find the biggest and smallest element `E`.
Finding the max from some elements is not as obvious as it looks; there are two main corner cases:

- The flow may be empty. In this case, there is no such thing as a biggest/smallest element.
- The flow can contain multiple elements that are equally the biggest/smallest.

Given this, the best design for `.max` and `.min` is to work as filters, and just remove all the elements that are not the max or the min.
In this way, the empty flow would stay empty, and all the biggest/smallest elements would be preserved.
There are many overloads of the methods `.max` and `.min`, mimicking the parameters and behaviour of `Comparators`.
Assuming a car type with a `Person` driver, here you can see some interesting usage examples.
```
myCars.flow
  .max{::}//if Car implements Order[Car]
  .list //to get the list of all the max cars
myCars.flow
  .max({::driver},{::})//comparing the drivers. Ok since Person implements Order[Person]
  .get //this requires that there is exactly one max
myCars.flow
  .max({::driver},Older) //here we check using the Older comparator
  .opt//this requires that there is exactly zero or one max
myCars.flow
  .max({::driver.name},CaseUnsensitiveComparator,  {::driver.age},{::}) //here names ignoring case
  .first //this gives us an Optional[Car] and allows for further max cars to be discarded.

myCars.flow
  .max({::driver.name},CaseUnsensitiveComparator)
  .max({::driver.age},{::})//this example has the same behaviour of the one above
  .first//like with .filter: we can divide two conditions on two calls if we prefer

myCars.flow
  .max Comparator[Car]{c1,c2->...}// to write a comparator by hand
  .min {::} //to use the conventional comparator on the equally maximal cars
  .list
```

Flows offer method `.sort` to sort the elements.
It takes the same parameters as `.max`/`.min`.

Flows also offer method `.distinct` to remove duplicates. Again it takes the same parameters as `.max`/`.min`/`.sort`.


### Maps, sets and hashing.

Hash maps (or hash tables) are one of the most popular data structures in programming.
They are a data structure that stores key-element pairs, allowing for fast retrieval of elements based on keys. It uses a hash function to compute an index (or "hash") from each key, which determines where the element is stored in a large private list. This allows for nearly constant-time complexity for lookups.
Fearless `Map[K,E]` is a hashmap. We have already seen that stacks and lists contain elements. Maps also contain elements, but connected to keys. Maps are a way to link keys to elements, similar to how a dictionary associates words with definitions. For example, a map could link a person's name (the key) to their phone number (the element).

It’s no coincidence that we have both a `Map[K,E]` type and a `Flow[E].map` method.
They both represent the idea of connecting one kind of value with another kind.
The `.map` method transforms a value representing the input of one stage of a computation into a value representing the output of such computation.
In a similar way, the `Map[K,E]` type holds a mapping between keys and elements, connecting distinct pieces of data.

As mentioned, `Map[K,E]` needs a hash function for the key `K`. The `OrderHash[T].hash` method plays this role by computing a numeric summary of an object. Although it's impossible for a hash function to uniquely identify each distinct object due to the infinite number of possible objects and the finite number of `Nat` values, a well-designed hash function approximates this by minimising situations where distinct objects have the same hash value. Objects are considered distinct if the `==` operation returns `False`.
Two objects that are equal via `==` must have the same hash value. This consistency is required for the map to work correctly.
Implementing the `.hash` method by returning zero is inefficient but technically correct: since all objects will have the same hash code, all equal objects will also trivially have the same hash code.
The map attempts to use the hash code as a fast screening test to quickly differentiate objects, and uses the slower `==` only when needed.
The default zero `.hash` method de facto disables this crucial optimisation. Instead, a good hash function spreads out objects evenly across the Nat numbers  to maximise the efficiency of the map operations.
Having the `.hash` method within the `OrderHash[T]` type helps maintain alignment between the behaviours of hashing and equality. By encapsulating both within the same type, it simplifies the enforcement of the principle that equal objects must have identical hash codes, thereby supporting more predictable and reliable map behaviour.

Below we show how we can use the `Hasher` type to define a good `.hash` method for `Person`.
> Nick: still does not like this java-style hash but lets move on for now (something something mut Summeriser[T])
```
OrderHash[T]: Order[T]{ .hash(h: Hasher): Nat }
Hasher: {
  #[A,B](a: OrderHash[A], b: OrderHash[B]): Nat -> a.hash(this) * 31 + (b.hash(this)),
  #[A,B,C](a: OrderHash[A], b: OrderHash[B], c: OrderHash[C]): Nat -> ...,
  ...//more overloads to compose more values
  .nat(v: Nat): Nat->..,
  .int(v: Int): Nat->..,
  .sStr(v: Str): Nat->..,
  ...//more methods for directly hashable types
  .list[E](l: List[E], f: F[E,OrderHash[E]])->..,
  .opt[E](o: Opt[E], f: F[E,OrderHash[E]])->..,
  ..//more methods for collections
  }
//usage
Persons: { #(name: Str, age: Num):Person -> Person: OrderHash[Person]{
  .name: Str -> name,
  .age: Nat  -> age,
  <=>(other) -> this.age <=> (other.age) && { this.name <=> (other.name) },
  .hash(h)   -> h#(this.age, this.name),//just list the relevant components
  }
}
```
With such a `Person` type, we can define a map from persons to address:

```
Maps#({::},   // this {::} is expanded as F[Person,OrderHash[Person]]{x->x}
  Persons#(`Bob`,25),`Toronto 34b Warden St.`,
  Persons#(`Alice`,34),`Wellington 134 Kelburn parade`,
  ...
  )
```
As you can see, maps take functions to get the `OrderHash[K]`.
`Map[K,E]` has many operations, similarly to `List[E]`.
We have `.size`, `.isEmpty` and `.get`.
However, `.get` does not take an index of type `Nat` but a key of type `K`.
For example, with the map declared above

```
myMap.isEmpty //false
myMap.size //2
myMap.get(Persons#('Alice',34)) // 'Wellington 134 Kelburn parade'
```
If the key is not present in the map, .get will cause an error.
We can instead use .opt to extract an optional Opt[E] result. For example
```
myMap.get(Persons#('Neil Armstrong',38)) // error
myMap.opt(Persons#('Neil Armstrong',38)).or 'Moon' // alternative default value.
```

Maps can have `mut`, `imm` or `read` elements; but only immutable keys. This is because the implementation of `Map[K,E]` needs to assume that the result of `.hash` and `==` is consistent over time.

We can flow on a map, but since both keys and elements are present, the flow method takes a function mapping keys and elements in some value.
For example
```myMap.flow{k,e-> k.name + e }.list```
will return ```List#('BobToronto 34b Warden St.','AliceWellington 134 Kelburn parade')```

Note how the order of the flow is the same as the insertion order.

We can also create a map from a flow, using method `.mapping` as shown below:
```
myPersons.flow
  .map{..}
  .filter{..}
  .mapping({::},{
    .key(e)->e.name,
    .elem(e)->e,
    })
```
Here we pass two parameters: a function from `K` to `Order[K]`, that as usual can be the identity if our keys implement `Order[K]`, and a literal specifying how to create the key and the element from the objects inside the flow.

Finally, sets of type `Set[K]` are another application of hashing. Instead of mapping keys to elements, it simply remembers if a key is present or not. That is, `Set[K]` most important methods are `.size`, `.isEmpty` and `.contains`.
Sets also support `.flow`, working exactly as for lists. Again, the order of the flow is the same as the insertion order.
See below some examples of using sets.
```
Sets#({::},1,2,3,4,5)//set of 5 numbers
Sets#(Comparator[Str]{s1,s2->s1.size<=>s2.size},'a','aa','aaaaa')//Does not compile.
Sets#(StrSizeOrder,'a','aa','aaaaa')//good
StrSizeOrder:F[Str,Order[Str]]{s1->{
  <=>(s2) -> s1.size <=> (s2.size),
  .hash   -> s1.size.hash,
  }}//Here we manually define an ordering for strings using their size.
```
Note how our first attempt for a set with custom comparison does not compile.
That comparator is a function from `Str` to `Order[Str]`, but here we need an `OrderHash[Str]`.

We can create a set from a flow by calling `.set{::}`, or passing a specific order.

In the same way there is an `UList[E]` type that is an updatable variant of `List[E]`, there are types `UMap[K,E]` and `USet[E]`. As for `UList[E]`, they are rarely used so we will discuss them (much) later.

### List, Opt and ordering.

Finally, `List[E]` and `Opt[E]` do not implement `Order[E]` or `OrderHash[E]`.
They do not need to and they may not contain ordered elements.
If I want to sort a list of ordered elements I can do
myList.flow
  .sort{::}
  .list

However, how to sort a list of lists?
#### Introducing `OrderHash[T,E]`.
To order a collection we need to reason on two generic types:
- The type of the current collection `T`.
- The type of the collection elements `E`.
```
OrderHash[T,E]:{ .order(f: F[E,OrderHash[E]]): OrderHash[T] }
```
The type `OrderHash[T,E]` does not order the collection directly, but can create an order for that collection if we provide a function ordering the collection elements.
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

This is similar to how all of those types can be converted into strings:
```
ToStr:{ read .str: Str }
ToStr[A]:{ read .str(f: F[A,Str]): Str }
ToStr[A,B]:{ read .str(fa: F[A,ToStr], fb: F[B,ToStr]): Str }
ToStr[A,B,C]:{ read .str(fa: F[A,ToStr], fb: F[B,ToStr], fc: F[C,ToStr]): Str }
...
```
Non generic types can implement `ToStr`, while generic types may implement `ToStr[A]`, `ToStr[A,B]` etc.
You just need to provide a function from `E` to `ToStr`.

`myList.str{::}` will work if myList is of type `List[E]` and `E` implements `ToStr`. If myList is of type `List[List[E]]` we would need to write
`myList.str{::str{::}}`.
Overall, this is also similar to how `.as` works for nested lists.

> Note UList should start with a Num current size AND should allow for 'holes', so .get
> can give exception in the middle of present items.
> Choice for strings with data obtained by string interpolation:
>  1 # string interpolation does not make strings but Proc:HasStr
>  2 # Proc:Str  ///winner (slow when recomputing, but very memory efficient when stored)
>  3 # inject Str! on naked #


OMIT_START
-------------------------*/@Test void anotherPackage() { run("fooBar","Test","""
package fooBar
alias base.Block as B,
alias base.Void as Void,
"""); }/*--------------------------------------------
OMIT_END
END*/
}