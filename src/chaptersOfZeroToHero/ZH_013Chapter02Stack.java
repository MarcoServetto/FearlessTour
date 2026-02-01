package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.run;
class ZH_013Chapter02Stack {
/*START
--CHAPTER-- Chapter 2
--SECTION-- Stack

### Stack: sequences of values

We have seen many kinds of single values: strings, numbers, directions, tanks.
`Opt` is kind of a different thing: it represents zero or one value.
Can we represent sequences of any amount of values?

In the world, we often organise books on shelves or line up to buy tickets.
Data too needs to be arranged in specific ways to be useful. This arrangement is handled by various structures we broadly call sequences.

Various kinds of sequences are possible.
Humans have categorised and named many of those, using names like Stack, List, Queues, Vectors or Arrays.
Each type of sequence manages and utilises data differently.

For example, consider neatly stacking chairs one atop another after a gathering.
You get a `Stack` of chairs.
If you want to add another chair, you will add it to the top of the stack. If you want to remove a chair, you will remove it from the top of the stack.
All kinds of stacks work just like chairs: the last chair added is the first one removed.

Stacks adhere to a last-in, first-out (LIFO) principle, making them ideal for tasks like undo mechanisms.
We will now see how to define Stacks of any kind of entities.

-------------------------*/@Test void stack1 () { run("""
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty;
  +(e: T): Stack[T] -> { .match m -> m.elem(e, this) };
  }
StackMatch[T,R]: {
  .empty: R;
  .elem(top:T, tail: Stack[T]): R;
  }
"""); }/*--------------------------------------------
As you can see, this code is similar to both peano numbers and optionals. It is kind of a hybrid.
The `.match` method and the `StackMatch` type are very similar to `.match` and `OptMatch` in optional.

The method `+` is similar to the method `.succ` on peano numbers, and the implementation is similar to the method `Opts#`.
Look again to the code of `Opt` and `Number` from before to see the similarities:
-------------------------*/@Test void optPeano () { run("""
Opt[T]: {
  .match[R](m: OptMatch[T,R]): R -> m.empty
  }
OptMatch[T,R]: {
  .empty: R;
  .some(t: T): R;
  }
Opts: {
  #[T](t: T): Opt[T] -> { .match m-> m.some(t) }
  }
Number: {
  .pred: Number;
  .succ: Number -> { this };
  +(other: Number): Number -> this.pred + (other.succ);
  *(other: Number): Number -> (this.pred * other) + other;
  }
Zero: Number{
  .pred   -> this.pred;
  + other -> other;
  * other -> this;
 }
"""); }/*--------------------------------------------
We can use the stack in many different ways. Some usage examples below:
- `Stack[Nat] + 1 + 2 + 3` is a stack of `Nat`. It contains `3`,`2`,`1`. Yes, in this order. `3` is the last element we inserted in the stack so it is the first element.
- `Stack[Opt[Nat]] + {} + {} + ( Opt#(3) )` is a stack of `Opt[Nat]`. It contains the optional containing `3`, and then two empty optionals.
- `Stack[Stack[Nat]] + {} + {} + ( Stack[Nat] + 3 )` is a stack of `Stack[Nat]`. It contains a stack with just the element `3`, and then two empty stacks.
Note how we can use `{}` both for the empty stack and the empty optional. The inference recognizes that the method `Stack[T]+` takes an optional in one case and a stack in another. Thus it infers that `{}` is an empty optional or an empty stack depending on the surrounding code.

Here we show how to use match to sum all the elements in a `Stack[Nat]`.
-------------------------*/@Test void stack2 () { run("""
//OMIT_START
use base.Nat as Nat;
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty;
  +(e: T): Stack[T] -> { .match(m) -> m.elem(e, this) };
  }
StackMatch[T,R]: {
  .empty: R;
  .elem(top:T, tail: Stack[T]): R;
  }
//OMIT_END
Example: {
  .sum(ns: Stack[Nat]): Nat -> ns.match{
    0; // equivalent to .empty -> 0;
    top, tail -> top + ( this.sum(tail) ); // equivalent to .elem(top, tail) -> ...;
    }
  }
"""); }/*--------------------------------------------
As you can see, we use the match to distinguish the two cases:
- summing all the elements of the empty stack gives zero.
- summing all the elements of a stack with a `top` and a `tail` can be done by summing the top with the sum of all the elements of the tail.

Look again at the code `top + ( this.sum(tail) )`. Here `this` is just `Example`, so this code is equivalent to `top + ( Example.sum(tail) )`.
That is, the code of method `Example.sum` is internally calling the method `Example.sum` again.
There is nothing special in this, any method can call any method, so `Example.sum` can call `Example.sum` internally.
A lot of people find this concept somewhat puzzling. To show that they find this behaviour to be more complex than a normal method call, they will refer to this as a **recursive** call and to the method `Example.sum` as a recursive method.
We will see very soon how this terminology (recursion) is not really that well defined.
We will now try to visualise the reduction of `Example.sum(Stack[Nat] + 1 + 2 + 3)`. It is a good exercise, also because it raises the question of how to represent the result of `Stack[Nat] + 1 + 2 + 3`.
So, let start reducing it.
1. `Stack[Nat] + 1 + 2 + 3`
2. `Stack[Nat]{.match(m) -> m.elem(1,Stack[Nat])} + 2 + 3`
3. `Stack[Nat]{.match(m) -> m.elem(2,Stack[Nat]{.match(m) -> m.elem(1,Stack[Nat]})} + 3`
4. `Stack[Nat]{.match(m) -> m.elem(3,Stack[Nat]{.match(m) -> m.elem(2,Stack[Nat]{.match(m) -> m.elem(1,Stack[Nat]})})}`

As you can see, this is **quite hard to read**.
Arguably, `Stack[Nat] + 1 + 2 + 3` was much more clear.
Visualizing reductions is great if it helps us to understand the semantic of the code. Geting stuck in the mud of redundant verbose value syntax would make visualizing reductions less useful.
To better visualize this method execution we will use a symbolic representation for stacks.

We will represent the result of `Stack[Nat] + 1 + 2 + 3` as `[3,2,1]`.


With this representation problem sorted out, we can now reduce

01. `Example.sum([3,2,1])`
02. `[3,2,1].match{ 0; top,tail -> top+(Example.sum(tail))}`
03. `{ 0; top,tail -> top+(Example.sum(tail))}.elem(3,[2,1])`
04. `3+(Example.sum([2,1]))`
05. `3+([2,1].match{ 0; top,tail -> top+(Example.sum(tail))})`
06. `3+ ({ 0; top,tail -> top+(Example.sum(tail))}.elem(2,[1]))`
07. `3+(2+(Example.sum([1])))`
08. `3+(2+([1].match{ 0; top,tail -> top+(Example.sum(tail))}))`
09. `3+(2+({ 0; top,tail -> top+(Example.sum(tail))}.elem(1,Stack[Nat])))`
10. `3+(2+(1+(Example.sum(Stack[Nat]))))`
11. `3+(2+(1+(Stack[Nat].match{ 0; top,tail -> top+(Example.sum(tail))}))`
12. `3+(2+(1+(0)))`
13. `3+(2+(1.pred+(0.succ)))`
14. `3+(2+(0+(0.succ)))`
15. `3+(2+(0+1))`
16. `3+(2+(1))`
17. ...
18. `3+(3)`
19. ...
20. `6`

This again, is long and verbose. When we have methods like `.match`, or methods with well understood behaviour, like `Num+`, we can simply skip some intermediate steps and get the following:

01. `Example.sum([3,2,1])`
02. `3+( Example.sum([2,1]) )`
03. `3+(2+( Example.sum([1]) ))`
04. `3+(2+(1+( Example.sum(Stack[Nat]) )))`
05. `3+(2+(1+0))`
06. `3+(2+1)`
07. `3+3`
08. `6`


An obvious operation to add to the stack is concatenation.
We can add a concatenation operation `++` between stacks as follows:

-------------------------*/@Test void stack3 () { run("""
Stack[T]: {
  .match[R](m: StackMatch[T,R]): R -> m.empty;
  ++(other: Stack[T]): Stack[T] -> other;
  +(e: T): Stack[T] -> {
    .match m -> m.elem(e, this);
    ++ other -> this ++ other  + e;
    };
  }
//OMIT_START
StackMatch[T,R]: {
  .empty: R;
  .elem(top:T, tail: Stack[T]): R;
  }
//OMIT_END
"""); }/*--------------------------------------------

Note how in the same way `+` adds the element at the top of the stack,
`++` adds all the elements at the top of the stack too.

This code shows an interesting use of `this`.
Inside method `Stack[T]+` we define a stack composed by the outer stack `this` and the top element `e`.
This means that the `this` binding in the body of `Stack[T]+` refers to the tail of the stack we are returning.
Thus, as for before, we write `.match(m) -> m.elem(e, this),` to implement the match method.
However, we use `e` and `this` also to implement the `Stack[T]++` method.

Consider the method body `this ++ other  + e`.
This code first calls `Stack[T]++`  with code `this ++ other`.
Then, `Stack[T]+` is called on the result of `this ++ other`. This adds `e` at the top of the result of `this ++ other`.
That is, the ultimate result will contain `e` as the first element. Remember that `e` was the first element of current stack.

With our compact stack representation, we can see the following reduction with body `this ++ other + e`.
1. `[1,2,3] ++ [4,5,6]`
2. `[2,3] ++ [4,5,6] + 1`
3. `[3] ++ [4,5,6] + 2 + 1`
4. `[] ++ [4,5,6] + 3 + 2 + 1`
5. `[4,5,6] + 3 + 2 + 1`
6. `[3,4,5,6] + 2 + 1`
7. `[2,3,4,5,6] + 1`
8. `[1,2,3,4,5,6]`

This body represents someone strong enough to lift the whole first stack of chairs and to land it on the second one.

You may wonder why
`[2,3] ++ [4,5,6] + 1` reduces to
`[3] ++ [4,5,6] + 2 + 1` and not to 
`[3] ++ [4,5,6] + 1 + 2`.

This is because the part that reduces is just `[2,3] ++ [4,5,6]`;
the ending ` + 1` will stay at the end.
That is, we replace `[2,3] ++ [4,5,6]` with `[3] ++ [4,5,6] + 2`
inside the expression `[2,3] ++ [4,5,6] + 1`.

Note that there are many alternative ways to write that body.
All of those ways would compile, but they do conceptually different operations. Some produce different ordering in the result, and some do not even terminate.

One obvious variant is to add parenthesis: `this ++ (other  + e)`.
1. `[1,2,3] ++ [4,5,6]`
2. `[2,3] ++ ([4,5,6] + 1)`
3. `[2,3] ++ [1,4,5,6]`
4. `[3] ++ ([1,4,5,6] + 2)`
5. `[3] ++ [2,1,4,5,6]`
6. `[] ++ ([2,1,4,5,6] + 3)`
7. `[] ++ [3,2,1,4,5,6]`
8. `[3,2,1,4,5,6]`

As you can see, instead of creating a large expression this version accumulates the results in the second argument directly.
This represents more closely what a person could do if they had to merge two stacks of chairs and if they were weak enough that they could only lift a single chair at any time.
Note how this version produces a different ordering in the result.

This is known in computer science as a `tail recursive algorithm`.
Some older languages require tail recursive algorithms for optimization reasons. This is usually not a concern in Fearless. It is still early to discuss **why and how** this is not a problem. Now we just clarify that we can avoid worrying about those ideas in a modern language like Fearless.

Note how if we explicitly pass the empty stack as the second argument, we can use it as an empty initial accumulator, and we get a reverse:
`[1,2,3] ++ []` reduces to `[3,2,1]`

Consider this other alternative body: `other ++ this + e`
01. `[1,2,3] ++ [4,5,6]`
02. `[4,5,6] ++ [2,3] + 1`
03. `[2,3] ++ [5,6] + 4 + 1`
04. `[5,6] ++ [3] + 2 + 4 + 1`
05. `[3] ++ [6] + 5 + 2 + 4 + 1`
06. `[6] ++ [] + 3 + 5 + 2 + 4 + 1`
07. `[] ++ [] + 6 + 3 + 5 + 2 + 4 + 1`
08. `[] + 6 + 3 + 5 + 2 + 4 + 1`
09. `[6] + 3 + 5 + 2 + 4 + 1`
10. `[3,6] + 5 + 2 + 4 + 1`
11. `[5,3,6] + 2 + 4 + 1`
12. `[2,5,3,6] + 4 + 1`
13. `[4,2,5,3,6] + 1`
14. `[1,4,2,5,3,6]`

Oh, wow, what a mess! This version basically alternates the content of the two stacks.
What happens if we add the parenthesis here?
Consider this other alternative body: `other ++ (this + e)`
1. `[1,2,3] ++ [4,5,6]`
2. `[4,5,6] ++ ([2,3] + 1)`
3. `[4,5,6] ++ [1,2,3]`
4. `[1,2,3] ++ ([5,6] + 4)`
5. `[1,2,3] ++ [4,5,6]`

Oh no! This version loops back to the original form. This means that this algorithm would go on reducing forever.
Intuitively, this is the case because we are now forcing immediate recomposition of the stack we just decomposed:
by doing `this + e` early, we go back to our original value, instead of slowly navigating toward the end stack.
For the same reason, also this following other body variation would not terminate:  `this + e ++ other`

We have one last variation to consider:
`other + e ++ this`
This also does not terminate:
Termination of `Stack[T]++` is only possible if the left element is an empty stack, but the result of `Stack[T]+` is always not empty.
That is, this implementation of `Stack[T]++` calls `Stack[T]++` in a way that is guaranteed to call back the same `Stack[T]++` implementation over and over.

As you can see, there are many ways to permute the `++` and the `+` call on `this`, `other` and `e`.
In order to learn to code, you need to learn to visualise the results of those calls.

We will soon see how **Testing** can be used to supplement the miserable visualisation skills of most humans.


Conceptually, `Stack[T]++` is similar to the peano `Number+` operation, and the whole stack concept can be seen as a peano number where some information is stored near each successor call.

END*/
}
