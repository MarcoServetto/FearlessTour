package chaptersOfZeroToHero;


import org.junit.jupiter.api.Test;
import static testHelpers.TourHelper.runFiles;
final class ZH_029Chapter05Secrets{
/*START
--CHAPTER-- Chapter 5
--SECTION-- Keeping secrets

# Chapter 5

### Using capabilities to enforce a range of properties

In Chapter 3 we have seen that an object capability is the permission to observe.
Code that receives no `mut` capability can only compute with what it has been given:
`Squares.of(5)` is `25` today, tomorrow, on my computer and on yours.

Being deterministic is a property that we get for free, just by not giving capabilities to a chunk of code.
In this chapter we will see that the same idea can enforce many other properties.
We decide what a chunk of code receives, and the type system does the rest.

We start with a property that may sound impossible:
a chunk of code holds a secret, computes with that secret, and still learns nothing about it.

### A game with hidden information

Let's write a card game called **higher or lower**.
On the table there is a card that everybody can see, and the next card of the deck, face down.
The player guesses if the next card is higher or lower than the card shown.
Then the next card is turned, and a correct guess scores a point.

We want people to write their own players: small objects that we call **bots**.
We will collect bots from many people and make them compete.
A bot is an object implementing `Player`:
```
Player: { .higher(t: Table): Bool }
```
Method `.higher` returns `True` to guess higher and `False` to guess lower.

Here is the challenge: the bot receives the table, and the table holds the next card.
A bot looking at the next card would win every round.

We could give each bot a special version of the table, without the next card.
But other games have much more hidden information: the cards in the hand of each player, the deck, the moves of the opponents.
For those games we would need a different version of the game for every player.
A single mistake in any of those versions would leak hidden information.

I prefer a different plan: we keep a single table, and we mark the next card as **secret**.
Then we make sure that a bot can hold a secret but cannot look inside it.

### Secrets

Here is our first attempt.
Our project has two packages: package `cards` holds the game, and package `game` holds the bots and the `Main`.
-------------------------*/@Test void naiveSecret(){ runFiles("""
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: { #[E](e: E): Secret[E] -> { .map f -> this#(f#e); .open _ -> e } }

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use base.Lists as Lists;
use base.List as List;
use cards.Player as Player;
use cards.Referee as Referee;
use cards.Table as Table;
use cards.Tables as Tables;

Cautious: Player{ t -> t.shown < 7 }
Rounds: { #: List[Table] -> Lists#(Tables#(3, 9), Tables#(9, 5), Tables#(5, 12), Tables#(12, 2), Tables#(6, 4)) }
Test: Main{ sys -> sys.out.println("Cautious: " + (Referee.play(Cautious, Rounds#))) }
//PRINT|Cautious: 4
"""); }/*--------------------------------------------

Package `game` uses package `cards`.
A package can only use packages with a lower rank, and `_rank_app001` is a lower rank than `_rank_app`.

A `Secret[E]` holds an element of type `E`.
Method `.open` gives back the element, but `.open` takes an argument of type `_Key`.
As we have seen in Chapter 3, `_Key` is a package private type: only code in package `cards` can refer to `_Key`.
Thus, only the code of package `cards` can open a secret.

Method `.map` allows any code to compute with the element, but the result of `.map` is a secret again.
The method `Secrets#` creates a secret holding the given element.

A `Table` has a card `.shown`, that everybody can see, and a secret card `.next`.
`Referee.play` plays one round for each table in the list, and counts the correct guesses.
Method `Referee.round` is the only code opening the secret: it opens the next card to check the guess.

The bot `Cautious` guesses higher when the card shown is lower than `7`.
`Cautious` guesses right in four rounds out of five.

### Cheating bots

Now, let's put on our evil hat and write some bots that try to cheat.
The first bot tries to open the secret, creating its own key:
-------------------------*/@Test void peek(){ runFiles("""
//OMIT_START
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: { #[E](e: E): Secret[E] -> { .map f -> this#(f#e); .open _ -> e } }

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use cards.Player as Player;
Test: Main{ sys -> sys.out.println("Peek") }
//OMIT_END
Peek: Player{ t -> t.next.open{} > (t.shown) }
//ERROR|In file: [###]_game/_rank_app.fear
//ERROR|
//ERROR|006| Peek: Player{ t -> t.next.open{} > (t.shown) }
//ERROR|   |                               ^^
//ERROR|
//ERROR|While inspecting object literal instance of "cards._Key"
//ERROR|Object literal instance of "cards._Key" implements private type "cards._Key".
//ERROR|Private types can only be implemented in their own package.
//ERROR|Object literal instance of "cards._Key" is defined in package "game".
//ERROR|Type "_Key" is defined in package "cards".
//ERROR|Error 7 WellFormedness
"""); }/*--------------------------------------------

The object literal `{}` would be a new object implementing `_Key`, and package private types can only be implemented in their own package.

The second bot tries to copy the card out of the secret, into a `Var`:
-------------------------*/@Test void smuggler(){ runFiles("""
//OMIT_START
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: { #[E](e: E): Secret[E] -> { .map f -> this#(f#e); .open _ -> e } }

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use base.Nat as Nat;
use base.Block as Block;
use cards.Player as Player;
Test: Main{ sys -> sys.out.println("Smuggler") }
//OMIT_END
Smuggler: Player{ t -> Block#
  .var[Nat] seen= {0}
  .do { t.next.map{ n -> seen.set(n) } }
  .return { seen.get > (t.shown) }
  }
//ERROR|In file: [###]_game/_rank_app.fear
//ERROR|
//ERROR|008| Smuggler: Player{ t -> Block#
//ERROR|009|   .var[Nat] seen= {0}
//ERROR|010|   .do { t.next.map{ n -> seen.set(n) } }
//ERROR|   |         ------------~~~~~^^^^^~~~~~~--
//ERROR|011|   .return { seen.get > (t.shown) }
//ERROR|
//ERROR|While inspecting parameter "seen" > "#(_)" line 10 > "#" line 10 > "#(_,_)" line 8 > ".higher(_)" line 8
//ERROR|Parameter "seen" has type "mut base.Var[Nat]".
//ERROR|Parameter "seen" can observe mutation; thus it cannot be captured in the "imm" object literal instance of "base.F[Nat,base.Void]" (line 10).
//ERROR|Hint: capture an immutable copy instead, or move this use outside the object literal.
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|seen
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------

The parameter `f` of `.map` has type `F[E, R]`, and thus it is `imm`.
An `imm` object literal cannot capture `seen`, since `seen` is a `mut Var[Nat]`.
That is, the function we give to `.map` cannot store anything anywhere.
The only way for that function to tell something is its result, and `.map` turns that result into a new secret.

The third bot is sneakier.
Remember from Chapter 4: `Try#` needs no capability, and catches deterministic errors.
The bot throws an error when the next card is higher:
-------------------------*/@Test void throwerWins(){ runFiles("""
//OMIT_START
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: { #[E](e: E): Secret[E] -> { .map f -> this#(f#e); .open _ -> e } }

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use base.Lists as Lists;
use base.List as List;
use base.Try as Try;
use base.Error as Error;
use base.True as True;
use base.False as False;
use cards.Player as Player;
use cards.Referee as Referee;
use cards.Table as Table;
use cards.Tables as Tables;
//OMIT_END
Thrower: Player{ t -> Try#{ t.next.map{ n -> (n > (t.shown)).if{ .then -> Error.msg "higher"; .else -> n } } }
  .run{ .ok _ -> False; .info _ -> True } }
//OMIT_START
Rounds: { #: List[Table] -> Lists#(Tables#(3, 9), Tables#(9, 5), Tables#(5, 12), Tables#(12, 2), Tables#(6, 4)) }
Test: Main{ sys -> sys.out.println("Thrower: " + (Referee.play(Thrower, Rounds#))) }
//OMIT_END
//PRINT|Thrower: 5
"""); }/*--------------------------------------------

`Thrower` guesses right in all five rounds.
The error escapes from `.map`, and `Try#` turns the error into an `Action` that `Thrower` can inspect.
Our secrets leak!

### Secrets that keep errors secret

The fix is in `Secrets`: `.map` runs the function inside a `Try#`.
If the function fails, `.map` returns a failed secret, holding the `Info` of the error.
Calling `.map` on a failed secret gives a failed secret, and calling `.open` on a failed secret throws the error again.
-------------------------*/@Test void throwerLoses(){ runFiles("""
//OMIT_START
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
//OMIT_END
Secrets: {
  #[E](e: E): Secret[E] -> { .map f -> this.try{f#e}; .open _ -> e };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .open _ -> Error!i };
  }
//OMIT_START

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Try as Try;
use base.Error as Error;
use base.True as True;
use base.False as False;
use cards.Player as Player;
use cards.Referee as Referee;
use cards.Table as Table;
use cards.Tables as Tables;

Cautious: Player{ t -> t.shown < 7 }
Thrower: Player{ t -> Try#{ t.next.map{ n -> (n > (t.shown)).if{ .then -> Error.msg "higher"; .else -> n } } }
  .run{ .ok _ -> False; .info _ -> True } }
Rounds: { #: List[Table] -> Lists#(Tables#(3, 9), Tables#(9, 5), Tables#(5, 12), Tables#(12, 2), Tables#(6, 4)) }
Test: Main{ sys -> Block#
  .do {sys.out.println("Cautious: " + (Referee.play(Cautious, Rounds#)))}
  .return {sys.out.println("Thrower: " + (Referee.play(Thrower, Rounds#)))}
  }
//OMIT_END
//PRINT|Cautious: 4
//PRINT|Thrower: 3
"""); }/*--------------------------------------------

Now `Try#{...}` in `Thrower` always succeeds, so `Thrower` always guesses lower.
`Thrower` scores three points: exactly as a bot that always guesses lower without looking at anything.

### What a bot can still do

Can a bot still do something with a secret?
A bot can print the secret card with `Debug#`:
-------------------------*/@Test void shouter(){ runFiles("""
//OMIT_START
//in file _cards/_rank_app001.fear
use base.Nat as Nat;
use base.Bool as Bool;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> { .map f -> this.try{f#e}; .open _ -> e };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .open _ -> Error!i };
  }

Table: { .shown: Nat; .next: Secret[Nat]; }
Tables: { #(shown: Nat, next: Nat): Table -> { .shown -> shown; .next -> Secrets#next } }
Player: { .higher(t: Table): Bool }
Referee: {
  .play(p: Player, ts: List[Table]): Nat -> ts.flow.map{t -> this.round(p, t)}.sum 0;
  .round(p: Player, t: Table): Nat -> (p.higher t == (t.next.open(_Key) > (t.shown)))
    .if{ .then -> 1; .else -> 0 };
  }

//in file _game/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Debug as Debug;
use base.False as False;
use cards.Player as Player;
use cards.Referee as Referee;
use cards.Table as Table;
use cards.Tables as Tables;
//OMIT_END
Shouter: Player{ t -> Block#(t.next.map{ n -> Debug#("Shouter sees " + n) }, False) }
//OMIT_START
Rounds: { #: List[Table] -> Lists#(Tables#(3, 9), Tables#(9, 5), Tables#(5, 12), Tables#(12, 2), Tables#(6, 4)) }
Test: Main{ sys -> sys.out.println("Shouter: " + (Referee.play(Shouter, Rounds#))) }
//OMIT_END
//PRINT|Shouter sees 9
//PRINT|Shouter sees 5
//PRINT|Shouter sees 12
//PRINT|Shouter sees 2
//PRINT|Shouter sees 4
//PRINT|Shouter: 3
"""); }/*--------------------------------------------

The person reading the output sees all the cards, but `Shouter` does not.
`Debug#` works as the logs of Chapter 3: any code can print with `Debug#`, but the program has no way to observe what was printed.

A bot can also make `.map` fail with a non-deterministic error, for example a stack overflow only when the next card is higher.
`Try#` does not catch non-deterministic errors, so the error stops the whole program.
Only `sys.try` can catch non-deterministic errors, and the bot has no `sys`.
In the same way, a bot can make `.map` run forever only when the next card is higher.
Then the game never ends, but no code without capabilities can notice that a computation does not end.
In both cases, the bot ruins the game, but the bot learns nothing.

### The property

Let's look again at what a chunk of code without `mut` capabilities can do with a secret.
That code has three ways to tell something about the secret: the result of a method, an error, and a change to a `mut` object.
`.map` closes all three ways:
- the result of the function is a new secret;
- `.map` catches the deterministic errors with `Try#`;
- the function given to `.map` is `imm`, so the function cannot change `mut` objects.

What is left are non-deterministic errors and computations that do not end, and only code with capabilities can observe those.
Thus, we get the following property:

**A chunk of code that receives secrets and no `mut` capability can compute with those secrets, but whatever that chunk of code computes from those secrets stays secret.**

The words "no `mut` capability" are crucial.
If a bot received `sys`, the bot could call `sys.try` around a `.map` that overflows the stack only when the next card is higher.
`sys.try` would catch the error, and the bot would know the next card is higher.
That is, we must never give `mut` capabilities to code that receives secrets.

The only code that sees the cards is the code of package `cards`, holding the key.
That code decides what to reveal: for example, the score tells how many guesses were right.

This chapter has shown one property, but the recipe is general.
A `Main` receives `sys`, the whole world.
Then, `Main` decides which capabilities each chunk of code receives, and the type system makes sure that each chunk of code can do nothing more.
By choosing what to give, we choose the properties each chunk of code has.
END*/
}
