
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

Furthermore, this project uses [Java Modules](https://dev.java/learn/modules/) in order to mimic
a modular approach in large code bases, to guard against certain cases of
[bit rot](https://infodation.com/en/blogs/bit-rot-the-silent-killer-of-software-systems) due to
poor management of internal application dependencies.

## Prerequisites

To run code in this project (console programs or unit tests), first make sure to have a locally running
[PostgreSQL Docker container](https://hub.docker.com/_/postgres) containing the
[Pagila sample database](https://github.com/devrimgunduz/pagila). Instructions on how to run the
PostgreSQL Docker container with the Pagila database can also be found at
[Pagila sample database](https://github.com/devrimgunduz/pagila).

In the remainder of this "readme" file the Pagila database setup will not be mentioned anymore,
so please keep this necessary prerequisite in mind.

Another requirement is a [Java 25 JDK](https://www.oracle.com/java/technologies/downloads/#java25).
Consider using [sdkman](https://sdkman.io/) for managing local JDK installations.

## Project structure

This project contains several subprojects, in direct subdirectories of the project root.
Each subproject is a Maven subproject ("Maven module") of the root as Maven POM project.

The subprojects explore a different way of using the Hibernate ORM, e.g. using native SQL or
JPQL/Criteria API, or the use of `EntityAgent` versus `EntityManager`.

The subprojects include [ArchUnit](https://www.archunit.org/) unit tests, to check architectural
"decisions" that the (sub)project should adhere to.

The subprojects are themselves Maven multi-module projects, with one module being a Java Module
exposing the service layer API and the other one consuming it. Although overkill in this case,
it shows how in a larger code base service layer implementation details can be hidden from code
consuming the service layer.

## Running the console programs and unit tests

As follows from the project structure, the subprojects can be considered "Maven projects" (that
happen to share the same parent, namely the project root in this case).

Note: the code base uses *Java 25* and its module imports. As a consequence, "mvn clean verify" may fail,
depending on the Maven version used. Consider using Maven via [sdkman](https://sdkman.io/), and
run this Maven command as follows (provided "MAVEN_HOME" has been set correctly using "sdkman"):

```bash
$MAVEN_HOME/bin/mvn clean verify
```


