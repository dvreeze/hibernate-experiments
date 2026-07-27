
# Using the Hibernate ORM effectively

TODO Examples, examples ...

## Introduction

When I say "Hibernate ORM", I could have said "Jakarta Persistence API" (formerly JPA).
Loosely we can still use the term JPA for Jakarta Persistence API.

Hibernate ORM is the reference implementation (and de-facto standard implementation) of the Jakarta Persistence standard.
Often we use Hibernate ORM through the Jakarta Persistence API, so in practice we could use both terms interchangeably.

Why this presentation? After all, most "enterprise" Java projects use Hibernate/JPA.

The issue is that very many Java projects *use Hibernate ORM ineffectively*.
Such projects suffer from code maintenance issues and performance issues in production.

I have myself spent many years of Java and Scala development "hating" Hibernate, and not wanting to work with it.
Put differently, I have spent many years not understanding Hibernate ORM.

What if we start using Hibernate ORM with *realistic expectations* about what it can and cannot do for us?
In particular, Hibernate ORM is *not about abstracting away the database*.
The moment we start "working with the database" using Hibernate ORM, Hibernate ORM becomes a "productivity booster".

So that's what this presentation is about: using Hibernate ORM effectively. This is not about details
(that can be looked up anyway), but about a *mind set*.

## Question: what is the essence of Hibernate ORM?

Question to the audience: what is the essence of Hibernate ORM? What (features) do you think about first?

Did anyone mention Hibernate's `StatelessSession` (or Jakarta Persistence 4.0 `EntityAgent`)?
If `StatelessSession`/`EntityAgent` and `Session`/`EntityManager` are equally important, then Hibernate ORM
is not foremost about the "persistence context".

More fundamental is the *JPQL* query language, which is essentially an *OO SQL dialect*
(that abstracts away many DBMS-specific SQL dialect differences).
The *Criteria API* can be seen as the formal description of that OO SQL dialect.

## Hibernate ORM best practices

If you forget everything in this presentation, at least *remember this*:
To use Hibernate ORM effectively, go to [Hibernate tutorials by Thorben Janssen](https://thorben-janssen.com/tutorials/).
In particular, see [Hibernate performance tuning](https://thorben-janssen.com/hibernate-performance-tuning/),
although it is perfectly fine to disagree with some of the points made.

... TODO ...

## Combining mutable JPA entities with immutable Java record DTOs

... TODO ...

## Interesting new capabilities of Hibernate ORM

... TODO ...

## Conclusion

... TODO ...
