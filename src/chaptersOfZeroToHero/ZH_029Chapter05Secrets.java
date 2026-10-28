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

A company keeps a list of its employees.
The role of each employee is public, but the salary is secret.
Every month the company makes a report with two parts:
- `.page`: the list of the staff, posted on the company intranet, where everybody can read it;
- `.payroll`: the total of all the salaries, for the human resources office only.

We want to buy the code making the report from somebody else: a contractor, an intern, or a library found on the internet.
We call that code a **reporter**: an object implementing `Reporter`.
```
Reporter: { #(es: List[Employee]): Report }
```
A reporter needs the salaries to compute the payroll.
But nothing about the salaries must end up on the page.

### Secrets

Our project has two packages.
Package `hr` holds the employees and a type `Secret`, and package `company` holds the reporters and the `Main`.
-------------------------*/@Test void report(){ runFiles("""
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;

Honest: Reporter{ es -> Report{
  .page -> es.flow.map{::.role}.join ", ";
  .payroll -> es.flow.map{::.salary}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Staff: { #: List[Employee] -> Lists#(Employees#("Cook", 4000), Employees#("Driver", 5000), Employees#("Clerk", 7000)) }
Test: Main{ sys -> Block#
  .do {sys.out.println("Honest: " + (Honest#(Staff#).page))}
  .return {sys.out.println("Payroll: " + (Hr.payroll(Honest#(Staff#))))}
  }
//PRINT|Honest: Cook, Driver, Clerk
//PRINT|Payroll: 16000
"""); }/*--------------------------------------------

Package `company` uses package `hr`.
A package can only use packages with a lower rank, and `_rank_app001` is a lower rank than `_rank_app`.

A **secret** is an object of type `Secret[E]`, holding an element of type `E`.
Method `.open` gives back the element, but `.open` takes an argument of type `_Key`.
As we have seen in Chapter 3, `_Key` is a package private type: only code in package `hr` can refer to `_Key`.
Thus, only the code of package `hr` can open a secret.

Any code can compute with secrets using `.map` and `.zip`.
Method `.map` computes with the element of one secret, and method `.zip` computes with the elements of two secrets.
The result of `.map` and `.zip` is again a secret.

The method `Secrets#` creates a secret holding the given element.
In that secret, `.map` and `.zip` run the given function inside `Try#`.
If the function throws a deterministic error, the result is a **failed secret**, holding the `Info` of the error.
Calling `.map` or `.zip` on a failed secret gives a failed secret, and calling `.open` on a failed secret throws the error again.
`.zip` opens the other secret inside `Try#` too, so a failed other secret also gives a failed secret.

An `Employee` has a public `.role` and a secret `.salary`.
`Hr.payroll` opens the payroll of a report: it is the only code opening a secret.
Thus, the human resources office learns the total of the salaries, and nothing else.

The reporter `Honest` lists the roles on the page, and adds up the salaries with `.zip`, starting from a secret `0`.

### Cheating reporters

Now, let's put on our evil hat and write some reporters that try to put salaries on the page.
The first reporter tries to open a secret, creating its own key:
-------------------------*/@Test void peek(){ runFiles("""
//OMIT_START
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;
Test: Main{ sys -> sys.out.println("peek") }
//OMIT_END
Peek: Reporter{ es -> Report{
  .page -> es.flow.map{e -> e.role + " " + (e.salary.open{})}.join ", ";
  .payroll -> Secrets#0;
  }}
//ERROR|In file: [###]_company/_rank_app.fear
//ERROR|
//ERROR|019|   .page -> es.flow.map{e -> e.role + " " + (e.salary.open{})}.join ", ";
//ERROR|   |                                                          ^^
//ERROR|
//ERROR|While inspecting object literal instance of "hr._Key"
//ERROR|Object literal instance of "hr._Key" implements private type "hr._Key".
//ERROR|Private types can only be implemented in their own package.
//ERROR|Object literal instance of "hr._Key" is defined in package "company".
//ERROR|Type "_Key" is defined in package "hr".
//ERROR|Error 7 WellFormedness
"""); }/*--------------------------------------------

The object literal `{}` would be a new object implementing `_Key`, and package private types can only be implemented in their own package.

The second reporter tries to put a star next to each employee earning more than `5000`:
-------------------------*/@Test void stars(){ runFiles("""
//OMIT_START
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;
Test: Main{ sys -> sys.out.println("stars") }
//OMIT_END
Stars: Reporter{ es -> Report{
  .page -> es.flow.map{e -> e.role + (e.salary.map{s -> (s > 5000).if{ .then -> "*"; .else -> "" }})}.join ", ";
  .payroll -> Secrets#0;
  }}
//ERROR|In file: [###]_company/_rank_app.fear
//ERROR|
//ERROR|018| Stars: Reporter{ es -> Report{
//ERROR|019|   .page -> es.flow.map{e -> e.role + (e.salary.map{s -> (s > 5000).if{ .then -> "*"; .else -> "" }})}.join ", ";
//ERROR|   |                        -----~~~~~~~^~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-
//ERROR|020|   .payroll -> Secrets#0;
//ERROR|021|   }}
//ERROR|
//ERROR|While inspecting "#(_)" line 19 > ".page" line 19 > "#(_)" line 18
//ERROR|This call to method "base.Str+(_)" cannot typecheck.
//ERROR|Argument 1 has type "hr.Secret[base.Str]".
//ERROR|That is not a subtype of "read base.ToStr" (the type required by the method signature).
//ERROR|Hint: Fearless has no operator precedence, so an expression like "a.get + b.get" parses as "(a.get + b).get", not "a.get + (b.get)". If this argument needed a method applied to it first, wrap it in parentheses.
//ERROR|
//ERROR|Compressed relevant code with inferred types: (compression indicated by `-`)
//ERROR|-+(e.salary.map[imm,-.Str](-.F[Nat,-.Str]{(-)->s>[read](5000).if[imm,-.Str](mut -.ThenElse[-.Str]{"*";""})}))
//ERROR|Error 8 TypeError
"""); }/*--------------------------------------------

The `.if` runs inside `.map`, so the result is a `Secret[Str]`, and a `Secret[Str]` can not be added to a `Str`.
In Fearless every choice is a method call, and a method call on the element of a secret can only happen inside `.map` or `.zip`.

The third reporter tries to copy each salary out of the secret, into a `Var`:
-------------------------*/@Test void smuggler(){ runFiles("""
//OMIT_START
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;
Test: Main{ sys -> sys.out.println("smuggler") }
//OMIT_END
Smuggler: Reporter{ es -> Report{
  .page -> es.flow.map{e -> Block#
    .var[Nat] seen= {0}
    .do { e.salary.map{s -> seen.set(s)} }
    .return { e.role + " " + (seen.get) }
    }.join ", ";
  .payroll -> Secrets#0;
  }}
//ERROR|In file: [###]_company/_rank_app.fear
//ERROR|
//ERROR|019|   .page -> es.flow.map{e -> Block#
//ERROR|020|     .var[Nat] seen= {0}
//ERROR|021|     .do { e.salary.map{s -> seen.set(s)} }
//ERROR|   |           -------------~~~~~^^^^^~~~~~~-
//ERROR|022|     .return { e.role + " " + (seen.get) }
//ERROR|
//ERROR|While inspecting parameter "seen" > "#(_)" line 21 > "#" line 21 > "#(_,_)" line 19 > "#(_)" line 19 > ".page" line 19 > "#(_)" line 18
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

The fourth reporter throws an error when the salary is more than `5000`.
Remember from Chapter 4: `Try#` needs no capability, and catches deterministic errors.
-------------------------*/@Test void starrer(){ runFiles("""
//OMIT_START
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;
Honest: Reporter{ es -> Report{
  .page -> es.flow.map{::.role}.join ", ";
  .payroll -> es.flow.map{::.salary}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Staff: { #: List[Employee] -> Lists#(Employees#("Cook", 4000), Employees#("Driver", 5000), Employees#("Clerk", 7000)) }
//OMIT_END
Starrer: Reporter{ es -> Report{
  .page -> es.flow.map{e -> Try#{ e.salary.map{s -> (s > 5000).if{ .then -> Error.msg "rich"; .else -> s } } }
    .run{ .ok _ -> e.role; .info _ -> e.role + "*" }}.join ", ";
  .payroll -> Secrets#0;
  }}
//OMIT_START
Test: Main{ sys -> Block#
  .do {sys.out.println("Starrer: " + (Starrer#(Staff#).page))}
  .return {sys.out.println("Payroll: " + (Hr.payroll(Honest#(Staff#))))}
  }
//OMIT_END
//PRINT|Starrer: Cook, Driver, Clerk
//PRINT|Payroll: 16000
"""); }/*--------------------------------------------

There are no stars on the page: the error never leaves `.map`.
`.map` catches the error and returns a failed secret, so the `Try#` of `Starrer` always succeeds.

The fifth reporter prints the salaries with `Debug#`:
-------------------------*/@Test void shouter(){ runFiles("""
//OMIT_START
//in file _hr/_rank_app001.fear
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

Employee: { .role: Str; .salary: Secret[Nat]; }
Employees: { #(role: Str, salary: Nat): Employee -> { .role -> role; .salary -> Secrets#salary } }
Report: { .page: Str; .payroll: Secret[Nat]; }
Reporter: { #(es: List[Employee]): Report }
Hr: { .payroll(r: Report): Nat -> r.payroll.open(_Key) }

//in file _company/_rank_app.fear
use base.Main as Main;
use base.Block as Block;
use base.Lists as Lists;
use base.List as List;
use base.Nat as Nat;
use base.Try as Try;
use base.Error as Error;
use base.Debug as Debug;
use hr.Employee as Employee;
use hr.Employees as Employees;
use hr.Report as Report;
use hr.Reporter as Reporter;
use hr.Secrets as Secrets;
use hr.Hr as Hr;
Honest: Reporter{ es -> Report{
  .page -> es.flow.map{::.role}.join ", ";
  .payroll -> es.flow.map{::.salary}.fold({Secrets#0}, {acc, s -> acc.zip(s, {a, b -> a + b})});
  }}
Staff: { #: List[Employee] -> Lists#(Employees#("Cook", 4000), Employees#("Driver", 5000), Employees#("Clerk", 7000)) }
//OMIT_END
Shouter: Reporter{ es -> Report{
  .page -> es.flow.map{e -> Block#(e.salary.map{s -> Debug#(e.role + " earns " + s)}, e.role)}.join ", ";
  .payroll -> Secrets#0;
  }}
//OMIT_START
Test: Main{ sys -> Block#
  .do {sys.out.println("Shouter: " + (Shouter#(Staff#).page))}
  .return {sys.out.println("Payroll: " + (Hr.payroll(Honest#(Staff#))))}
  }
//OMIT_END
//PRINT|Cook earns 4000
//PRINT|Driver earns 5000
//PRINT|Clerk earns 7000
//PRINT|Shouter: Cook, Driver, Clerk
//PRINT|Payroll: 16000
"""); }/*--------------------------------------------

The person reading the output sees all the salaries, but `Shouter` does not, and the page has no salary on it.
`Debug#` works as the logs of Chapter 3: any code can print with `Debug#`, but the program has no way to observe what was printed.

A reporter can also make `.map` fail with a non-deterministic error, for example a stack overflow only when the salary is more than `5000`.
`Try#` does not catch non-deterministic errors, so the error stops the whole program.
Only `sys.try` can catch non-deterministic errors, and the reporter has no `sys`.
In the same way, a reporter can make `.map` run forever only when the salary is more than `5000`.
Then the report is never done, but no code without capabilities can notice that a computation does not end.
In both cases, the reporter ruins the report, but nothing about the salaries reaches the page.

### The property

**A chunk of code that receives secrets and no `mut` capability can compute with those secrets, but whatever that chunk of code computes from those secrets stays secret.**

For our reporters, the type of `.page` is `Str`: the page does not depend on any salary, whatever the reporter does.

The words "no `mut` capability" are crucial.
A reporter receiving `sys` could call `sys.try` around a `.map` that overflows the stack only when the salary is more than `5000`.
`sys.try` would catch the error, and the reporter would know which employees earn more than `5000`.

The only code that sees the salaries is the code of package `hr`, holding the key.
That code decides what to reveal: the human resources office sees the total of the salaries, and nothing else.

This chapter has shown one property, but the recipe is general.
A `Main` receives `sys`, the whole world.
Then, `Main` decides which capabilities each chunk of code receives, and the type system makes sure that each chunk of code can do nothing more.
By choosing what to give, we choose the properties each chunk of code has.
END*/
}
