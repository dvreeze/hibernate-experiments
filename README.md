
# Hibernate ORM experiments

Hibernate ORM experiments against the [sample Pagila database](https://github.com/devrimgunduz/pagila/tree/master).

These experiments use the [Hibernate ORM 8.0](https://hibernate.org/orm/releases/8.0/) library,
implementing the [Jakarta Persistence 4.0](https://jakarta.ee/specifications/persistence/4.0/) standard.
At the time of this writing, both Hibernate 8.0 and JPA 4.0 are in development.

To get a feel for what JPA 4.0 has to offer, see [JPA 4.0 M1](https://in.relation.to/2026/01/20/JPA-4-M1/)
and [JPA 4.0 M2](https://in.relation.to/2026/04/23/JPA-4-M2/).

## A few notes about competing design forces

As a Java programmer it has taken me quite some time before getting some appreciation for the Hibernate
ORM (and the JPA standard). In all fairness, it has also taken me quite some time before getting a basic
understanding of how the Hibernate ORM can be used effectively. Maybe I had seen too many projects
where I was far from the only programmer who did not sufficiently grasp this powerful ORM.

One very good site about how to put Hibernate to good use is [thorben-janssen.com](https://thorben-janssen.com/).
It is certainly a site that I visit on a regular basis in order to sharpen my Hibernate ORM skills.
I can certainly also recommend Thorben Janssen's [Hibernate Tips Book](https://thorben-janssen.com/hibernate-tips-book/).

That being said, *JPA entity* classes are highly mutable Java classes, and in that sense remind of
"old school" "non-functional" Java. That is, the Java of *mutable JavaBeans* with getters and setters
(and default no-arg constructors), as opposed to the modern Java of (deeply) *immutable Java records*.
In other words, the Java of the legacy `Date`/`Calendar` API, instead of the more modern Java of the
`java.time` API modeling time-related concepts as immutable classes. To be fair, JPA itself nowadays
considers its use of the legacy `Date`/`Calendar` classes deprecated.

Much of my programming experience has been influenced strongly by:
* Internalizing many best practices mentioned in the book [Effective Java, 3rd Edition](https://thorben-janssen.com/review-effective-java-3rd-edition/), by Joshua Bloch
* Many years of [Scala](https://www.scala-lang.org/) programming

Languages like Scala and [Kotlin](https://kotlinlang.org/) certainly have had a big influence on the
evolution of the Java language. Take for example the move from *JavaBeans* to *Java records* in more
modern APIs. Java records clearly remind of Scala's *case classes*.

My experiences with internalizing *Effective Java* and (later) the use of *Scala* have made me a better
application developer. In particular:
* A more *functional* programming style (when not taking this to extremes) leads to code that is *easier to reason about*
  * Local reasoning about code becomes a breeze for *pure functions* that are *deterministic*, *total* and *free from side effects*
* Investing in the creation of a domain model as *immutable classes* makes the code almost follow automatically
  * And this code tends to be quite clear and low in bugs
  * An excellent (early) example of such an API is the [java.time API](https://docs.oracle.com/en/java/javase/25/docs/api///java.base/java/time/package-summary.html)
  * Of course, the [java.util.stream API](https://docs.oracle.com/en/java/javase/25/docs/api///java.base/java/util/stream/package-summary.html) underlies many more modern "FP" Java APIs
  * Even recently introduced low level APIs exemplify modern Java, exploiting *immutability* combined with the flexibility of *Java interfaces*; e.g.
    * [Foreign Function and Memory API](https://docs.oracle.com/en/java/javase/25/docs/api///java.base/java/lang/foreign/package-summary.html)
    * [Java Class File API](https://docs.oracle.com/en/java/javase/25/docs/api///java.base/java/lang/classfile/package-summary.html)

Ok, so I appreciate my experiences with the book Effective Java and the Scala language, but where does this
leave highly mutable *JPA entities*? Also, Hibernate's/JPA's *first level cache* is quite the opposite of
a functional "side-effect-free" programming paradigm. The first level cache is all about side effects.

So, in a Java code base that on the one hand tries to use a more functional programming style, but on the
other hand uses the Hibernate ORM, there seem to be opposing design forces at work. This is also something
explored in this project. The approach followed is one where *side effects are localized*. That also implies
that JPA entities are only used in local scopes, not escaping (transactional) service layer boundaries.

Also note that this project makes *nullability* explicit through the use of [JSpecify](https://jspecify.dev/)
annotations. In the approach just mentioned non-nullability is the norm in the project's Java packages,
except for the (localized) data access implementation code.

It is important to note that the Hibernate ORM and JPA are not necessarily about side effects. The first
level cache is not even the most fundamental feature of the ORM, considering that Hibernate also ships
with a [StatelessSession](https://docs.hibernate.org/orm/8.0/javadocs/org/hibernate/StatelessSession.html)
implementation, which seems to have finally made it to the JPA standard (4.0), as interface
[EntityAgent](https://jakarta.ee/specifications/persistence/4.0/apidocs/jakarta.persistence/jakarta/persistence/entityagent).
An `EntityAgent`/`StatelessSession` gives much more control over SQL execution and is far less about
side effects than an `EntityManager`/`Session`. See also
[this interesting confession](https://in.relation.to/2025/09/24/a-billion-dollar-mistake/), yet
JPA 4.0 seems to finally rectify this.

Summarized: this project is not just about experimenting with a modern Hibernate ORM, but also about
how to combine that with functional programming techniques.

Most of these subprojects do not lean heavily on Java annotations, in order to have a better feel
for what is happening at runtime.

## Using Java Modules

Besides experimenting with Hibernate 8 and FP practices, this project uses [Java Modules](https://dev.java/learn/modules/) in order
to mimic a modular approach in large code bases, to guard against certain cases of
[bit rot](https://infodation.com/en/blogs/bit-rot-the-silent-killer-of-software-systems) due to
poor management of internal application dependencies.

In a nutshell, some benefits of the Java Module system in application code are:
* Java *packages* are *first class citizens*, and their interdependencies and encapsulation are made explicit through *Java module descriptors*
* At runtime, the Java module path can roughly be seen as a very "disciplined class path", free from conflicts
* Java Modules can help in creating small executables, shipping with "minimal Java runtimes" (created with [jlink](https://www.baeldung.com/jlink))

Java Modules have a positive effect on the Java ecosystem. Recall the mess with Java XML library dependencies
in the past, and compare that to a disciplined use (whether directly or indirectly) of the `java.xml`
module that is part of umbrella module `java.se`.

Note that Java Modules and Maven dependencies are not the same, but they need to be kept in sync.
Java Modules know nothing about Maven coordinates and artifact versions, whereas Maven knows nothing about
Java packages. So Java Modules and Maven offer different perspectives on dependencies. Moreover, Java Modules
are a Java language feature, understood by the Java compiler (as well as the Java runtime and tooling).

The Java ecosystem is still not as module-friendly as desired. See [Java Modules](https://dev.java/learn/modules/) and
[Module System Woes](https://github.com/nipafx/module-system-woes) for much more information about
how to use them in practice and how to deal with common issues that might arise.
Speaking of which, the book [Java 9 Modularity](https://javamodularity.com/) is a real gem in that regard.
There are many possible hybrid scenarios, in which we can at least partially benefit from the use of modules.

## Prerequisites

To run (non-test) code in this project (i.e. the console programs), first make sure to have a locally running
[PostgreSQL Docker container](https://hub.docker.com/_/postgres) containing the
[Pagila sample database](https://github.com/devrimgunduz/pagila). Instructions on how to run the
PostgreSQL Docker container with the Pagila database can also be found at
[Pagila sample database](https://github.com/devrimgunduz/pagila).

Some unit tests use [Testcontainers](https://testcontainers.com/), in particular
the [Postgres Module](https://java.testcontainers.org/modules/databases/postgres/). This requires only
a Docker installation, which is required in any case, as mentioned above. Other unit tests use an
embedded [H2 database](https://www.h2database.com/html/main.html).

In the remainder of this "readme" file the Pagila database setup will not be mentioned anymore,
so please keep this necessary prerequisite (of Testcontainers requiring Docker) in mind.

Another requirement is a [Java 25 JDK](https://www.oracle.com/java/technologies/downloads/#java25).
Consider using [sdkman](https://sdkman.io/) for managing local JDK installations.

## Project structure

This project contains several subprojects, in direct subdirectories of the project root.
Each subproject is a Maven subproject ("Maven module") of the root as Maven POM project.

The subprojects explore a different way of using the Hibernate ORM, e.g. using native SQL or
JPQL/Criteria API, or the use of `EntityAgent` versus `EntityManager`.

The subprojects include [ArchUnit](https://www.archunit.org/) unit tests, to check architectural
"decisions" that the (sub)project should adhere to.

The subprojects are themselves Maven multi-module projects, each containing 3 modules that are also Java
Modules, namely one with an immutable data model, one with a service layer, and one consuming the
(data and) service layer. Although overkill in this case, it shows how in a larger code base service layer
implementation details can be hidden from code consuming the service layer, and how it can be prevented
that data classes perform any (potentially expensive) business logic.

## Running the console programs and unit tests

As follows from the project structure, the subprojects can be considered "Maven projects" (that
happen to share the same parent, namely the project root in this case).

Note: the code base uses *Java 25* and its module imports. As a consequence, "mvn clean verify" may fail,
depending on the Maven version used. Consider using Maven via [sdkman](https://sdkman.io/), and
run this Maven command as follows (provided "MAVEN_HOME" has been set correctly using "sdkman"):

```bash
$MAVEN_HOME/bin/mvn clean verify
```

## Runtime behavior analysis using the debugger

Sometimes we would like to know in more detail what is happening at runtime, e.g. what the actual transactional
boundaries are (provided the transaction is resource-local as opposed to a JTA transaction). One way to find
out is by using the debugger, and set breakpoints at locations where an `EntityManager` or `EntityAgent` is
present, and analyze Java expressions at those breakpoints such as:

```
entityManager.unwrap(org.hibernate.Session.class)
    .unwrap(org.hibernate.internal.SessionImpl.class)
    .getTransaction()
```

or:

```
entityAgent.unwrap(org.hibernate.StatelessSession.class)
    .unwrap(org.hibernate.internal.StatelessSessionImpl.class)
    .getTransaction()
```

Shorter:

```
entityManager.unwrap(org.hibernate.internal.SessionImpl.class).getTransaction()
```

or:

```
entityAgent.unwrap(org.hibernate.internal.StatelessSessionImpl.class).getTransaction()
```

Expressions such as these can be very handy in debugging sessions where Hibernate behavior is analyzed.
Note that in practice `EntityManager` or `EntityAgent` instances are proxy objects instead of Hibernate
`Session` or `StatelessSession` objects. In order to get these Hibernate objects, the `unwrap` calls
above are used. Just casting to Hibernate objects would not work in those cases.

## Overview of subprojects

The subprojects in this project are:
* plain-sql
* jpql
* criteria
* entitymanager
* em-criteria

Subproject *plain-sql* explores the use of native SQL queries in JPA 4.0. Compared to previous
versions of JPA, its support for type-safe native SQL querying has advanced quite a lot. Also,
this subproject uses JPA 4.0's `EntityAgent` rather than `EntityManager`, since the former suffices
for native SQL querying.

Subproject *jpql* explores the use of JPQL queries without any persistence context. That is, it uses
an `EntityAgent` instead of `EntityManager`. Compared to subproject *plain-sql*, we need JPA entities
and related bookkeeping (in JPA bootstrapping and Java Module descriptors), but get a more friendly
and less verbose (JPQL-based) querying experience in return.

Subproject *criteria* is like *jpql*, but it uses Criteria queries rather than JPQL query strings,
to get even more compile-time type-safety.

Subproject *entitymanager* is like *jpql*, but using an `EntityManager`.

Subproject *em-criteria* is like *criteria*, but using an `EntityManager`.

## A few words about Hibernate antipatterns

This section is written with knowledge coming from tutorials like
[common Hibernate mistakes crippling performance](https://thorben-janssen.com/common-hibernate-mistakes-cripple-performance/), combined with own experiences in production systems.

In the wild many projects using Hibernate/JPA suffer from shortcomings that lead to an explosion of
generated SQL statements and/or difficulty reasoning about the code. Among the antipatterns commonly
seen (in typical Hibernate projects using an `EntityManager`/`Session` rather than a `StatelessSession`) are:
* *Eager fetching* of associations specified (explicitly or implicitly) at the entity level
  * This is especially problematic in a large complex deeply nested ("cyclic graph") domain model
  * If there is a mix of eagerly fetched associations going upward and downward in object graphs, much more data may be eagerly fetched than expected
  * Another complication in this regard may be recursive entity types, where entities refer to other entities of the same type (directly or indirectly)
  * In a Hibernate 8.0+ (JPA 4.0+) project, by all means, configure the default fetching for many-to-one and one-to-one associations to *lazy*
  * In a Hibernate 7.X (or lower) project, by all means, explicitly configure each many-to-one and one-to-one association to use *lazy* fetching
* Failing to specify *per-query fetching*
  * This can be subtle: entity methods may contain "business logic" depending on associations of that entity, thus somewhat hiding (eager/lazy) fetching behavior
  * JPA entities should rather be "Java representations of database table rows", without any business logic
  * It is the business logic (in a transactional "service layer") that should decide what data is needed, not the JPA entities
  * Specifying fetching per query makes sense: we would do the same with native SQL, where we choose per query which tables are joined
* Going overboard with *cascading* of operations (specified at the entity level)
* Retrieving tons of entity fields in queries where simple (Java record) projections with only a few fields would suffice
  * Moreover, retrieving custom DTOs rather than (managed) entities helps avoid an explosion of generated SQL statements
* *Persistence contexts spanning multiple method calls* (including fine-grained "DAO calls"), thus making it hard to properly use the persistence context
  * Increasingly I find (mandatory) DAO layers behind the implementation of a transactional service layer problematic
  * After all, DAOs hide the *persistence context*, but the latter is (implicit) *program state* that we need to keep in mind instead of hide
  * In particular, sometimes we need to influence the persistence context through `EntityManager`/`Session` methods, or at least be able to reason about it
  * Yet unlike that DAO layer, the service layer itself should present itself through a technology-agnostic Java interface as API contract, hiding the use of JPA
* "Transactional service" methods with entity parameters and side effects on those entities in the method body, leading to unclear semantics that heavily depends on the calling context
  * Does an update to such a parameter entity in the method body lead to a database update due to "dirty checking"? That depends on the calling context.
  * If the caller is not another transactional service method (establishing a transactional `Session`), more likely than not the entity update is essentially a no-op
  * In other words, such methods by themselves have *unclear semantics* and are therefore not very useful
  * If a transactional Hibernate `Session` spans a *deep call chain* of service methods and DAO methods, code maintainability really suffers
* Overall: a mind set of wanting to see no SQL but just Java objects, leading to non-performant code in production
  * It looks so easy, and that is the pitfall: concise one-liner Java method call chains without considering the runtime costs (w.r.t. database and persistence context) and maintainability later on
  * Contrast this with development teams taking charge of the persistence layer, effectively using jOOQ or Hibernate, thus implementing performant maintainable business logic, even if that requires more code

Tutorials such as [common Hibernate mistakes crippling performance](https://thorben-janssen.com/common-hibernate-mistakes-cripple-performance/)
warn against many of these antipatterns. Fortunately we can make Hibernate work for us rather than against us,
but it may take quite some time to "repair" a project using Hibernate in a non-performant way.
