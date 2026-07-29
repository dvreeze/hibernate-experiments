
# Using the Hibernate ORM effectively

Expected audience: Java developers with at least some experience with Hibernate ORM.

TODO Examples, examples ...

## Introduction

When I say "Hibernate ORM", I could have said "Jakarta Persistence API" (formerly JPA).
Loosely we can still use the term JPA for Jakarta Persistence API.

Hibernate ORM is the reference implementation (and de-facto standard implementation) of the Jakarta Persistence standard.
Typically, we use Hibernate ORM through the Jakarta Persistence API, so in practice we could use both terms interchangeably.
Note that Hibernate 6+ uses the "jakarta" namespace (introduced in "JPA" version 3.0) instead of "javax" namespace.

Why this presentation? After all, most "enterprise" Java projects use Hibernate/JPA, so it should be familiar, right?

*Question* to the audience: at work, *who uses (or has used) Hibernate ORM*, or at least Jakarta Persistence (or its predecessor)?

*Question* to the audience: *how happy and/or successful are you using Hibernate ORM*?

*Question* to the audience: *do/did you use alternatives to Hibernate* (such as jOOQ, Spring JdbcTemplate etc.)?

From my experience, the issue is that very many Java projects *use Hibernate ORM ineffectively*.
Such projects suffer from code maintenance issues and performance issues in production.

I have myself spent many years of Java and Scala development "hating" Hibernate, and not wanting to work with it.
Put differently, I have spent many years not understanding Hibernate ORM.

If applicable, what if we start using Hibernate ORM with *realistic expectations* about what this library is and is not about?
In particular, Hibernate ORM is *not about abstracting away the database*.
(In retrospect, Ted Neward at least partly missed the point about Hibernate ORM with his "The Vietnam of Computer Science" article.)
The moment we start "working with the database" using Hibernate ORM, Hibernate ORM becomes a "productivity booster".

So that's what this presentation is about: *using Hibernate ORM effectively*. This is not about details
(that can be looked up anyway), but about a *mind set*.

## Question: what is the essence of Hibernate ORM?

*Question* to the audience: *what is the essence of Hibernate ORM*? What (features) do you think about first?

Did anyone mention Hibernate's `StatelessSession` (or Jakarta Persistence 4.0 `EntityAgent`)?
If `StatelessSession`/`EntityAgent` and `Session`/`EntityManager` are equally important, then Hibernate ORM
is not foremost about the "persistence context".

More fundamental is the *JPQL* query language, which is essentially an *OO SQL dialect*
(that abstracts away many DBMS-specific SQL dialect differences).
The *Criteria API* can be seen as the formal description of that OO SQL dialect.

## Hibernate ORM best practices

If you forget everything in this presentation, at least *remember this*:
To use Hibernate ORM effectively, go to [Hibernate tutorials by Thorben Janssen](https://thorben-janssen.com/tutorials/) for advice.
In particular, see [Hibernate performance tuning](https://thorben-janssen.com/hibernate-performance-tuning/),
although it is perfectly fine to disagree with some of the points made.

... TODO ...

... main problem: too many generated SQL queries (1 + N problem) ...

... all entity associations should have fetch type lazy (mind Hibernate 8 vs. pre-8) ...

... per query, the fetch graph should be chosen ...

... this is consistent with per-query choice of joined tables in plain SQL query result; again note that Hibernate is not about abstracting away the database ...

... overall, keep entity configuration (via annotations) simple ...

## Combining mutable JPA entities with immutable Java record DTOs

... TODO ...

... there are multiple reasons why Java records as query results are attractive (not too many queries, easy to populate result records) ...

... as an aside, Hibernate queries (but not JPQL queries) can also return JSON and use CTEs (again, JPQL/HQL is an OO SQL dialect) ...

## The metamodel, and type-safe querying

... TODO ...

... using the Criteria API, along with the generated metamodel, querying can reach a high level of compile-time type-safety ...

... the Criteria API is relatively hard to use, but Hibernate 8 implements Jakarta Data repositories ...

... thus, we can write JPQL/HQL strings in annotations, and have the Hibernate annotation processor parse the query at compile-time ...

## Interesting new capabilities of Hibernate ORM (version 8)

... TODO ...

... above, we have already mentioned some Hibernate 8 features: EntityAgent, Jakarta Data, default fetch type lazy possible for all associations, JSON results, etc. ...

## Hibernate ORM combined with Spring framework (or Spring Boot)

... TODO ...

... differences in philosophy between Jakarta Data and Spring Data ...

## Schema validation and evolution

... TODO ...

## Testing Hibernate application code

... TODO ... 

... generated in-memory H2 database gets us quite far; use orm.xml to override entity attributes where needed ...

## Conclusion

... TODO ...
