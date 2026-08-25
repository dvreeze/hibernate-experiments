
# Using the Hibernate ORM effectively

## Introduction

... TODO ...

What I hope to achieve: show some Hibernate ORM best practices, and take away some "magic".
This is needed because there is far too much unnecessary "Hibernate pain" in many Java projects.

What are my own experiences with Hibernate ORM? Not too good, until I decided to learn Hibernate ORM.
I learned much from official Hibernate material, experts like Thorben Janssen, and combined that with
my experiences in a more functional programming style (initially in Scala, but also inspired by the
book Effective Java, 3rd Edition, by Joshua Bloch).

This presentation works with one example, which is adapted many times (in many cases improved).

The example uses plain Hibernate ORM (through the Jakarta Persistence 4.0 API) programmatically in a Java SE context.
So wiring/transaction annotations and the (automatic) creation of proxy objects for them are out of scope.
This may be a good thing when concentrating on Hibernate ORM itself.

The example uses Hibernate 8, which at the time of this writing is still in beta.

The example is more about querying than about the persistence context. Why? That will become clear later.

## How not to use Hibernate ORM

... TODO ...

Consider this poorly defined code in terms of what happens to the database:

```java
// Hypothetical transactional service method

public void deliver(OrderEntity order, Instant deliveryDate) {
    // ...
    order.setDeliveryDate(deliveryDate);
    // ...
}
```

Some ways that *Hibernate ORM should not be used* include:

- *Pretending that we only need to deal with Java objects, and that database synchronization is solely the task of Hibernate*
  - See above
- Using *eager loading of associations* at the entity level
  - Much more about that follows below
- Leaking a `Session` ("persistence context") *across threads or concurrent transactions*
  - Under the hood, such "infrastructural" state tends to depend on "thread locals"
- Using "infrastructural" *state* (such as a `Session`/`EntityManager`) *in a JPA entity*
  - This would lead to cyclical dependencies among application layers
  - It would also invite performance issues that are hard to find and fix

## Example used in this presentation

... TODO ...

The example uses a small part of [sample Pagila database](https://github.com/devrimgunduz/pagila/tree/master).

It shows a minimal transactional film service, with one public method to query for films of a given actor.

Insert the Java code of the relevant JPA entities. Mention 2 kinds of mapping annotations in JPA entities.

Show first example, and *ask audience* what is wrong with it. It will throw a `LazyInitializationException`.
This first example returns the films as JPA entities, without fetching any associations.

## Querying for custom projections

... TODO ...

First compare old school Java to modern Java, e.g. `Date`/`Calendar` versus `java.time` API.
Modern Java is more *functional*, uses more *immutable Java records* instead of JavaBeans with getters and
setters, is *less about side effects* and less about `null`, and more about *expressions* than *statements*.

Alas, JPA entities are more like old school JavaBeans with getters and setters. Moreover, they contain
lots of hidden implicit state, e.g. w.r.t. associations (loaded or not), the absence/presence of a persistence
context, etc.

Immutable Java records make great DTOs to be passed across application layers, though.

Insert code of an immutable film model, and conversions from JPA entities to this model.

Insert the Java code of an inefficient film service returning immutable film DTOs (`InefficientFilmService`,
using `EntityManager` and JPQL query strings).

*Ask audience* what is wrong with the implementation (too many queries), other than retrieving entities
in the same session where they are converted to immutable DTOs (therefore unnecessary flushing overhead).
That problem will be dealt with later.

## Per-query fetching

... TODO ...

Fixing the explosion of generated SQL.

Insert the Java code of `ConcreteFilmService` and `ConcreteFIlmServiceUsingFetchJoin`.

Show how this comes much closer to the
[correct way to fix a LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/).

Also show how we narrowly escaped the throwing of a `MultipleBagFetchException`. Show ways to deal with that.
So, insert Java code of `ConcreteFilmServiceUsingSeparateQueries`.

## Using Hibernate ORM without persistence context

... TODO ...

*Ask audience* if it is possible to use Hibernate ORM without persistence context overhead.

Show the same `ConcreteFilmServiceUsingSeparateQueries`, except that it uses an `EntityAgent` instead
of `EntityManager` (since Jakarta Persistence 4.0, but `StatelessSession` has existed for a long time).

## Exploiting richness of HQL

... TODO ...

Insert code of `AlternativeFilmService`, exploiting JSON support and/or CTEs.

Remember, JPQL and in particular HQL are very powerful *OO SQL dialects*.

## The type-safe metamodel

... TODO ...

Also show its use in `ConcreteFilmService` using Criteria API, and move on to Jakarta Data repositories,
with compile-time JPQL/HQL query string parsing/validation (through the Hibernate annotation processor).

## Testing Hibernate code

... TODO ...

How practical unit tests using an embedded H2 database give much bang for the buck.

## Schema validation and evolution

... TODO ...

## Conclusion

... TODO ...

We can benefit from the combination of JPA entities and immutable DTOs, and we can reduce much of
the "Hibernate magic" if we so choose. We can also get a lot of help from the compiler.

## References

... TODO ...

Some references:

From the Hibernate team or the Jakarte Persistence standard:

- [Hibernate ORM advice](https://docs.hibernate.org/orm/8.0/introduction/html_single/#advice) from the Hibernate ORM team
- [No-nonsense guide to Hibern8](https://docs.hibernate.org/orm/8.0/introduction/html_single/) (should probably be read from beginning to end)
- [Hibernate ORM user guide](https://docs.hibernate.org/orm/8.0/userguide/html_single/) as reference material
- [Jakarta Persistence specification](https://jakarta.ee/specifications/persistence/) as reference material
- [Hibernate ORM short guide](https://docs.hibernate.org/orm/8.0/introduction/html_single/#many-to-one)

From Hibernate expert Thorben Janssen:

- [Hibernate tutorials by Thorben Janssen](https://thorben-janssen.com/tutorials/)
- [Hibernate performance tuning](https://thorben-janssen.com/hibernate-performance-tuning/)
- [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)
- [Choose the right fetch type](https://thorben-janssen.com/hibernate-performance-tuning/#avoid-unnecessary-queries--choose-the-right-fetchtype)
- [MultipleBagFetchException](https://thorben-janssen.com/hibernate-tips-how-to-avoid-hibernates-multiplebagfetchexception/) and [fix MultipleBagFetchException](https://thorben-janssen.com/fix-multiplebagfetchexception-hibernate/)
- [cascade type remove issues](https://thorben-janssen.com/avoid-cascadetype-delete-many-assocations/)

Other links:

- [Hibernate ORM pitfalls or difficulties](https://www.quora.com/What-are-pitfalls-or-difficulties-in-using-Hibernate-as-ORM)
- [Stop using JPA/Hibernate](https://www.stemlaur.com/blog/2021/03/30/tech-hibern-hate/), in order to learn what critics have to say about Hibernate ORM

What is new in Hibernate ORM 8:

- [Jakarta Persistence 4.0 Milestone 1](https://in.relation.to/2026/01/20/JPA-4-M1/)
- [Jakarta Persistence 4.0 Milestone 2](https://in.relation.to/2026/04/23/JPA-4-M2/)

## Questions?

Time for questions from the audience.
