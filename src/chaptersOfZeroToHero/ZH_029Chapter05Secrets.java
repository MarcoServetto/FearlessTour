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
a chunk of code computes with some secret data, and still nothing about that data reaches the rest of the program.

### A report on secret data

A hospital ward keeps a list of its patients.
The bed of each patient is public, but the dose of medicine each patient takes is secret.
Every day the ward makes a report with two parts:
- `.board`: the list of the beds, written on the board in the corridor, where every visitor can read it;
- `.order`: the total of all the doses, for the pharmacy only.

We want to buy the code making the report from somebody else: a contractor, an intern, or a library found on the internet.
We call that code a **reporter**: an object implementing `Reporter`.
```
Reporter: { #(es: List[Patient]): Report }
```
A reporter needs the doses to compute the order.
But nothing about the doses must end up on the board.

### Secrets

Our project has two packages.
Package `clinic` holds the patients and a type `Secret`, and package `ward` holds the reporters and the `Main`.
-------------------------*/@Test void report(){ runFiles("""
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;

Honest: Reporter{ es -> Report{
  .board -> es.flow.map{::.bed}.join ", ";
  .order -> es.flow.map{::.dose}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Beds: { #: List[Patient] -> Lists#(Patients#("Bed A", 40), Patients#("Bed B", 50), Patients#("Bed C", 70)) }
Test: Main{ sys -> Block#
  .do {sys.out.println("Honest: " + (Honest#(Beds#).board))}
  .return {sys.out.println("Order: " + (Pharmacy.order(Honest#(Beds#))))}
  }
//PRINT|Honest: Bed A, Bed B, Bed C
//PRINT|Order: 160
"""); }/*--------------------------------------------

Package `ward` uses package `clinic`.
A package can only use packages with a lower rank, and `_rank_app001` is a lower rank than `_rank_app`.

A **secret** is an object of type `Secret[E]`, holding an element of type `E`.
Method `.open` gives back the element, but `.open` takes an argument of type `_Key`.
As we have seen in Chapter 3, `_Key` is a package private type: only code in package `clinic` can refer to `_Key`.
Thus, only the code of package `clinic` can open a secret.

Any code can compute with secrets using `.map` and `.zip`.
Method `.map` computes with the element of one secret, and method `.zip` computes with the elements of two secrets.
The result of `.map` and `.zip` is again a secret.

The method `Secrets#` creates a secret holding the given element.
In that secret, `.map` and `.zip` run the given function inside `Try#`.
If the function throws a deterministic error, the result is a **failed secret**, holding the `Info` of the error.
Calling `.map` or `.zip` on a failed secret gives a failed secret, and calling `.open` on a failed secret throws the error again.
`.zip` opens the other secret inside `Try#` too, so a failed other secret also gives a failed secret.

A `Patient` has a public `.bed` and a secret `.dose`.
`Pharmacy.order` opens the order of a report: it is the only code opening a secret.
Thus, the pharmacy learns the total of the doses, and nothing else.

The reporter `Honest` lists the beds on the board, and adds up the doses with `.zip`, starting from a secret `0`.

### Cheating reporters

Now, let's put on our evil hat and write some reporters that try to put doses on the board.
The first reporter tries to open a secret, creating its own key:
-------------------------*/@Test void peek(){ runFiles("""
//OMIT_START
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;
Test: Main{ sys -> sys.out.println("peek") }
//OMIT_END
Peek: Reporter{ es -> Report{
  .board -> es.flow.map{e -> e.bed + " " + (e.dose.open{})}.join ", ";
  .order -> Secrets#0;
  }}
//ERROR|In file: [###]_ward/_rank_app.fear
//ERROR|
//ERROR|019|   .board -> es.flow.map{e -> e.bed + " " + (e.dose.open{})}.join ", ";
//ERROR|   |                                                        ^^
//ERROR|
//ERROR|While inspecting object literal instance of "clinic._Key"
//ERROR|Object literal instance of "clinic._Key" implements private type "clinic._Key".
//ERROR|Private types can only be implemented in their own package.
//ERROR|Object literal instance of "clinic._Key" is defined in package "ward".
//ERROR|Type "_Key" is defined in package "clinic".
//ERROR|Error 7 WellFormedness
"""); }/*--------------------------------------------

The object literal `{}` would be a new object implementing `_Key`, and package private types can only be implemented in their own package.

The second reporter tries to put a star next to each bed whose dose is more than `50`:
-------------------------*/@Test void stars(){ runFiles("""
//OMIT_START
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;
Test: Main{ sys -> sys.out.println("stars") }
//OMIT_END
Stars: Reporter{ es -> Report{
  .board -> es.flow.map{e -> e.bed + (e.dose.map{d -> (d > 50).if{ .then -> "*"; .else -> "" }})}.join ", ";
  .order -> Secrets#0;
  }}
//ERROR|In file: [###]_ward/_rank_app.fear
//ERROR|
//ERROR|018| Stars: Reporter{ es -> Report{
//ERROR|019|   .board -> es.flow.map{e -> e.bed + (e.dose.map{d -> (d > 50).if{ .then -> "*"; .else -> "" }})}.join ", ";
//ERROR|   |                         -----~~~~~~^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-
//ERROR|020|   .order -> Secrets#0;
//ERROR|021|   }}
//ERROR|
//ERROR|While inspecting "#(_)" line 19 > ".board" line 19 > "#(_)" line 18
//ERROR|This call to method "base.Str+(_)" cannot typecheck.
//ERROR|Argument 1 has type "clinic.Secret[base.Str]".
//ERROR|That is not a subtype of "read base.ToStr" (the type required by the method signature).
//ERROR|Hint: Fearless has no operator precedence, so an expression like "a.get + b.get" parses as "(a.get + b).get", not "a.get + (b.get)". If this argument needed a method applied to it first, wrap it in parentheses.
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|-+(e.dose.map[imm,-.Str](-.F[Nat,-.Str]{(-)->d>[read](50).if[imm,-.Str](mut -.ThenElse[-.Str]{"*";""})}))
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------

The `.if` runs inside `.map`, so the result is a `Secret[Str]`, and a `Secret[Str]` can not be added to a `Str`.
In Fearless every choice is a method call, and a method call on the element of a secret can only happen inside `.map` or `.zip`.

The third reporter tries to copy each dose out of the secret, into a `Var`:
-------------------------*/@Test void smuggler(){ runFiles("""
//OMIT_START
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;
Test: Main{ sys -> sys.out.println("smuggler") }
//OMIT_END
Smuggler: Reporter{ es -> Report{
  .board -> es.flow.map{e -> Block#
    .var[Nat] seen= {0}
    .do { e.dose.map{d -> seen.set(d)} }
    .return { e.bed + " " + (seen.get) }
    }.join ", ";
  .order -> Secrets#0;
  }}
//ERROR|In file: [###]_ward/_rank_app.fear
//ERROR|
//ERROR|019|   .board -> es.flow.map{e -> Block#
//ERROR|020|     .var[Nat] seen= {0}
//ERROR|021|     .do { e.dose.map{d -> seen.set(d)} }
//ERROR|   |           -----------~~~~~^^^^^~~~~~~-
//ERROR|022|     .return { e.bed + " " + (seen.get) }
//ERROR|
//ERROR|While inspecting parameter "seen" > "#(_)" line 21 > "#" line 21 > "#(_,_)" line 19 > "#(_)" line 19 > ".board" line 19 > "#(_)" line 18
//ERROR|Parameter "seen" has type "mut base.Var[Nat]".
//ERROR|Parameter "seen" can observe mutation; thus it cannot be captured in the "imm" object literal instance of "base.F[Nat,base.Void]" (line 21).
//ERROR|Hint: capture an immutable copy instead, or move this use outside the object literal.
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|seen
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------

The parameter `f` of `.map` has type `F[E, R]`, and thus it is `imm`.
An `imm` object literal cannot capture `seen`, since `seen` is a `mut Var[Nat]`.
That is, the function we give to `.map` cannot store anything anywhere.

The fourth reporter throws an error when the dose is more than `50`.
Remember from Chapter 4: `Try#` needs no capability, and catches deterministic errors.
-------------------------*/@Test void starrer(){ runFiles("""
//OMIT_START
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;
Honest: Reporter{ es -> Report{
  .board -> es.flow.map{::.bed}.join ", ";
  .order -> es.flow.map{::.dose}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Beds: { #: List[Patient] -> Lists#(Patients#("Bed A", 40), Patients#("Bed B", 50), Patients#("Bed C", 70)) }
//OMIT_END
Starrer: Reporter{ es -> Report{
  .board -> es.flow.map{e -> Try#{ e.dose.map{d -> (d > 50).if{ .then -> Error.msg "high"; .else -> d } } }
    .run{ .ok _ -> e.bed; .info _ -> e.bed + "*" }}.join ", ";
  .order -> Secrets#0;
  }}
//OMIT_START
Test: Main{ sys -> Block#
  .do {sys.out.println("Starrer: " + (Starrer#(Beds#).board))}
  .return {sys.out.println("Order: " + (Pharmacy.order(Honest#(Beds#))))}
  }
//OMIT_END
//PRINT|Starrer: Bed A, Bed B, Bed C
//PRINT|Order: 160
"""); }/*--------------------------------------------

There are no stars on the board: the error never leaves `.map`.
`.map` catches the error and returns a failed secret, so the `Try#` of `Starrer` always succeeds.

The fifth reporter prints the doses with `Debug#`:
-------------------------*/@Test void shouter(){ runFiles("""
//OMIT_START
//in file _clinic/_rank_app001.fear
use base.Nat as Nat;
use base.Str as Str;
use base.List as List;
use base.F as F;
use base.Try as Try;
use base.Info as Info;
use base.Error as Error;

Secret[E]: {
  .map[R](f: F[E, R]): Secret[R];
  .zip[U, R](other: Secret[U], f: F[E, U, R]): Secret[R];
  .open(key: _Key): E;
  }
_Key: {}
Secrets: {
  #[E](e: E): Secret[E] -> {
    .map f -> this.try{f#e};
    .zip(o, f) -> this.try{f#(e, o.open(_Key))};
    .open _ -> e;
    };
  .try[E](f: F[E]): Secret[E] -> Try#{f#}.run{ .ok e -> this#e; .info i -> this.failed i };
  .failed[E](i: Info): Secret[E] -> { .map _ -> this.failed i; .zip(_, _) -> this.failed i; .open _ -> Error!i };
  }

Patient: { .bed: Str; .dose: Secret[Nat]; }
Patients: { #(bed: Str, dose: Nat): Patient -> { .bed -> bed; .dose -> Secrets#dose } }
Report: { .board: Str; .order: Secret[Nat]; }
Reporter: { #(es: List[Patient]): Report }
Pharmacy: { .order(r: Report): Nat -> r.order.open(_Key) }

//in file _ward/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use clinic.Patient as Patient;
use clinic.Patients as Patients;
use clinic.Report as Report;
use clinic.Reporter as Reporter;
use clinic.Secrets as Secrets;
use clinic.Pharmacy as Pharmacy;
Honest: Reporter{ es -> Report{
  .board -> es.flow.map{::.bed}.join ", ";
  .order -> es.flow.map{::.dose}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Beds: { #: List[Patient] -> Lists#(Patients#("Bed A", 40), Patients#("Bed B", 50), Patients#("Bed C", 70)) }
//OMIT_END
Shouter: Reporter{ es -> Report{
  .board -> es.flow.map{e -> Block#(e.dose.map{d -> Debug#(e.bed + " takes " + d)}, e.bed)}.join ", ";
  .order -> Secrets#0;
  }}
//OMIT_START
Test: Main{ sys -> Block#
  .do {sys.out.println("Shouter: " + (Shouter#(Beds#).board))}
  .return {sys.out.println("Order: " + (Pharmacy.order(Honest#(Beds#))))}
  }
//OMIT_END
//PRINT|Bed A takes 40
//PRINT|Bed B takes 50
//PRINT|Bed C takes 70
//PRINT|Shouter: Bed A, Bed B, Bed C
//PRINT|Order: 160
"""); }/*--------------------------------------------

The person reading the output sees all the doses, but `Shouter` does not, and the board has no dose on it.
`Debug#` works as the logs of Chapter 3: any code can print with `Debug#`, but the program has no way to observe what was printed.

A reporter can also make `.map` fail with a non-deterministic error, for example a stack overflow only when the dose is more than `50`.
`Try#` does not catch non-deterministic errors, so the error stops the whole program.
Only `sys.try` can catch non-deterministic errors, and the reporter has no `sys`.
In the same way, a reporter can make `.map` run forever only when the dose is more than `50`.
Then the report is never done, but no code without capabilities can notice that a computation does not end.
In both cases, the reporter ruins the report, but nothing about the doses reaches the board.

### The property

**A chunk of code that receives secrets and no `mut` capability can compute with those secrets, but whatever that chunk of code computes from those secrets stays secret.**

For our reporters, the type of `.board` is `Str`: the board does not depend on any dose, whatever the reporter does.

The words "no `mut` capability" are crucial.
A reporter receiving `sys` could call `sys.try` around a `.map` that overflows the stack only when the dose is more than `50`.
`sys.try` would catch the error, and the reporter would know which patients take more than `50`.

The only code that sees the doses is the code of package `clinic`, holding the key.
That code decides what to reveal: the pharmacy sees the total of the doses, and nothing else.

This chapter has shown one property, but the recipe is general.
A `Main` receives `sys`, the whole world.
Then, `Main` decides which capabilities each chunk of code receives, and the type system makes sure that each chunk of code can do nothing more.
By choosing what to give, we choose the properties each chunk of code has.
END*/
}
