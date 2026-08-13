
# Using the Hibernate ORM effectively

*Expected audience*: mostly Java developers with at least some experience with Hibernate ORM.

*Motivation for this presentation*: I have encountered Hibernate ORM in many projects, mostly struggling
with it instead of using it effectively. Typically, my co-workers struggled with Hibernate ORM as well.
It took me many years before starting to understand that this does not have to be that way, and I would
like to share those insights. Hence, this presentation.

*Question* to the audience: at work, *who uses (or has used) Hibernate ORM*, or at least *Jakarta Persistence* (or its predecessor)?

## Example used in this presentation

The example used throughout this talk is as follows. It uses 3 related JPA entities, and a query method.
This example uses (a small part of) the [sample Pagila database](https://github.com/devrimgunduz/pagila/tree/master).

Entity class `AddressEntity`:

```java
@Entity(name = "Address")
@Table(name = "Address")
public class AddressEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_seq")
    @SequenceGenerator(name = "address_id_seq", sequenceName = "address_address_id_seq", allocationSize = 1)
    @Column(name = "address_id")
    private Integer id;

    @Basic(optional = false)
    private String address;

    private String address2;

    @Basic(optional = false)
    private String district;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @Column(name = "postal_code")
    private String postalCode;

    @Basic(optional = false)
    private String phone;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Entity class `CityEntity`:

```java
@Entity(name = "City")
@Table(name = "City")
public class CityEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "city_id_seq")
    @SequenceGenerator(name = "city_id_seq", sequenceName = "city_city_id_seq", allocationSize = 1)
    @Column(name = "city_id")
    private Integer id;

    @Basic(optional = false)
    private String city;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id", nullable = false)
    private CountryEntity country;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Entity class `CountryEntity`:

```java
@Entity(name = "Country")
@Table(name = "Country")
public class CountryEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "country_id_seq")
    @SequenceGenerator(name = "country_id_seq", sequenceName = "country_country_id_seq", allocationSize = 1)
    @Column(name = "country_id")
    private Integer id;

    @Basic(optional = false)
    private String country;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Note that there are 2 categories of *JPA annotations*:
- *logical mapping annotations*, concerning the Java object model
  - e.g. `Entity`, `Id`, `ManyToOne`, `Basic` etc.
- *physical mapping annotations*, concerning the underlying relational database schema
    - e.g. `Table`, `Column`, `JoinTable`, `GeneratedValue` etc.

A "service" method querying an address by its technical primary key:

```java
public interface AddressService {

    // Flaw: result not technology-agnostic and poorly defined w.r.t. which associations are fetched
    Optional<AddressEntity> findById(long id);

    // Other methods
}

public final class ConcreteAddressService implements AddressService { // No separate DAO layer in this case

    private final EntityManagerFactory emf; // Instead of (typical) container-generated EntityManager proxy

    @Override
    public Optional<AddressEntity> findById(long id) {
        return emf.callInTransaction(entityManager -> { // Programmatic transaction demarcation in this case
            // Strictly, JPQL is not needed in this case
            // Besides, this implementation is flawed (spoiler alert)
            String qlString = "select ad from Address ad where ad.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    // More methods
}
```

*Question* to the audience: what does the following code do?

```java
// Outside the transactional Hibernate Session/EntityManager!

AddressEntity address = addressService.findById(id).orElseThrow(); // Assume "id" is an existing ID
String cityName = address.getCity().getCity();
```

Indeed, it throws a [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/).

## Introduction

When I say "Hibernate ORM", I could have said "Jakarta Persistence API" (formerly JPA).
Loosely we can still use the term JPA for Jakarta Persistence API.

*Hibernate ORM* is the reference implementation (and de-facto standard implementation) of the *Jakarta Persistence standard*.
Typically, we use Hibernate ORM through the Jakarta Persistence API, so in practice we could use both terms interchangeably.
Note that Hibernate 6+ uses the "jakarta" namespace (introduced in "JPA" version 3.0) instead of "javax" namespace.

Why this presentation? After all, most "enterprise" Java projects use Hibernate/JPA, so it should be familiar, right?
Yet, as said earlier, many Java developers struggle or have struggled with Hibernate, including myself,
and it does not have to be that way.

*Question* to the audience: *how happy and/or successful are you using Hibernate ORM*?

*Question* to the audience: *do/did you use alternatives to Hibernate* (such as jOOQ, Spring JdbcTemplate etc.)?

From my experience, the issue is that very many Java projects *use Hibernate ORM ineffectively*.
Such projects suffer from code maintenance issues and performance issues in production.

I have myself spent many years of Java and Scala development "hating" Hibernate, and not wanting to work with it.
Put differently, I have spent many years not understanding Hibernate ORM.

If this sounds familiar, what if we start using Hibernate ORM with *realistic expectations* about what this library is
and is not about? In particular, Hibernate ORM is *not about abstracting away the database*.
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
The *Criteria API* can in a way be seen as the "formal description" of that OO SQL dialect.

Personally, to me Hibernate is foremost about *representing a relational database as Java classes*, with a
well-defined *SQL dialect in terms of that Java database representation* as its query language.

Let's rewrite the service, using `EntityAgent` instead of `EntityManager`, thus getting the pros and cons
of no longer using a "persistence context" (also known as the "first-level cache"):

```java
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    @Override
    public Optional<AddressEntity> findById(long id) {
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // This implementation is (also) flawed; we do not get the related city and country
            String qlString = "select ad from Address ad where ad.id = ?1";

            return entityAgent.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    // More methods
}
```

## Hibernate ORM best practices

If you forget everything in this presentation, at least *remember this*:
To use Hibernate ORM effectively, go to [Hibernate tutorials by Thorben Janssen](https://thorben-janssen.com/tutorials/) for advice.
In particular, see [Hibernate performance tuning](https://thorben-janssen.com/hibernate-performance-tuning/),
although it is perfectly fine to disagree with some of the points made.

Useful advice from the Hibernate team can be found at [Hibernate ORM advice](https://docs.hibernate.org/orm/8.0/introduction/html_single/#advice).
It is probably even a good idea to read the entire (opinionated) [No-nonsense guide to Hibern8](https://docs.hibernate.org/orm/8.0/introduction/html_single/).

The [Hibernate ORM user guide](https://docs.hibernate.org/orm/8.0/userguide/html_single/) is probably best used
as reference material. The same as true for the [Jakarta Persistence specification](https://jakarta.ee/specifications/persistence/).

Recall the preceding flawed code:

```java
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    @Override
    public Optional<AddressEntity> findById(long id) {
        return emf.callInTransaction(entityManager -> {
            // Flawed: within the transactional Session the city and country associations are not fetched
            String qlString = "select ad from Address ad where ad.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    // More methods
}
```

What to do about this, if we want the city and country associations to be fetched as part of the result,
and we want to prevent the dreaded [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)?

We could feel urged to choose fetch type "eager" for both associations in the entity classes, but this
would affect all code using those entities. This could easily lead to a hidden explosion of fetched data where
only a small subset of that data is needed.

The Hibernate team ([Hibernate ORM short guide](https://docs.hibernate.org/orm/8.0/introduction/html_single/#many-to-one))
and experts like Thorben Janssen
([Choose the right fetch type](https://thorben-janssen.com/hibernate-performance-tuning/#avoid-unnecessary-queries--choose-the-right-fetchtype))
are very clear: *choose fetch type "lazy" for all entity associations*.

So, use the default fetch type for to-many associations, and *explicitly choose fetch type "lazy" for to-one associations*.
Jakarta Persistence 4.0 (and therefore Hibernate 8) makes this easy, by a global configuration setting!

Still, this does not solve our problem. Both the Hibernate team and Thorben Janssen also offer the following advice:
*use per-query fetching*.

That is, we could do "fetch joins" in our Hibernate/JPQL queries:

```java
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    @Override
    public Optional<AddressEntity> findById(long id) {
        return emf.callInTransaction(entityManager -> {
            // The city and country of the address are fetched as well
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co where ad.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    // More methods
}
```

Alternatively, we could specify a "fetch graph" or "load graph":

```java
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    @Override
    public Optional<AddressEntity> findById(long id) {
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.id = ?1";

            // Programmatically created entity graph
            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // In our case, there is no difference between load graph and fetch graph
            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    // More methods

    // Private methods (note the use of the generated metamodel)

    private EntityGraph<AddressEntity> getAddressEntityGraph() {
        EntityGraph<AddressEntity> entityGraph = AddressEntity_.class_.createEntityGraph();

        entityGraph.addAttributeNode(AddressEntity_.city);

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<CityEntity> citySubgraph = entityGraph.addSubgraph(AddressEntity_.city);
        citySubgraph.addAttributeNode(CityEntity_.country);

        return entityGraph;
    }
}
```

In both cases we used per-query fetching, be it through different means. This is in spirit similar to what
we did many years ago when we used to write all SQL ourselves: per SQL query we chose our "fetch joins" (getting no help
from the database access library to turn result sets into nested Java objects). So also in that regard
we should not abandon proven database querying practices, even when using Hibernate ORM, or better, especially
when using Hibernate ORM. Again, the library is not about abstracting away the database; it is about
*Java and the database working well together*.

In this case, our entities only used to-one associations, but in practice many associations are collection-valued
to-many associations. For example:

```java
// Hypothetical Order and LineItem entities (unrelated to the previous examples)

@Entity
public class Order {

    @Id
    private Long id;

    // Bidirectional association
    // Default fetch type Lazy; by all means leave it that way!
    @OneToMany(mappedBy = "order") // We can use a constant if there is a generated metamodel
    private List<LineItem> lineItems = new ArrayList<>();

    // ...
}

@Entity
public class LineItem {

    @Id
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    // ...
}
```

This brings us to what might be the main problem in production with (naive?) Hibernate ORM application code,
the dreaded *N + 1 select problem*. This problem can also be illustrated with to-one associations, but
to-many associations make a description of this problem more interesting.

Suppose we use JPQL/HQL to query for multiple orders, and we need their line items as well. Suppose each order can
have tens or hundreds of line items. The *N + 1 select problem* is the issue that first a collection of
orders is retrieved in one query, and then, per retrieved order, a query is run to fetch its line items.

Also in this case, the short story is: *all entity associations should be lazy* and *use per-query fetching*.
That way we prevent the N + 1 select problem, which in this case could lead to an explosion of generated SQL queries.
At the same time, we thus prevent the occurrence of [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)s.

Note that the query result could also have been a *DTO projection* instead of entities. See the next section about
that approach, which also prevents SQL query explosions as well as `LazyInitializationException`s.

Of course, much more can be said (and found) about effective use of Hibernate ORM, but this is a very good
start that should not be ignored in any project using Hibernate ORM.

Another rather general advice is: *keep things simple*. For example, keep the use of JPA annotations simple.
In particular, do not go overboard with *cascading* in association annotations. The less we make use of
cascading of operations to associated entities, the less we invite surprising Hibernate behavior.
Again, see [Thorben Janssen](https://thorben-janssen.com/tutorials/) for more on this (e.g.
[cascade type remove issues](https://thorben-janssen.com/avoid-cascadetype-delete-many-assocations/)).

The *common theme* here is to *avoid the creation by Hibernate ORM of unnecessary SQL*.

## Combining mutable JPA entities with immutable Java record DTOs

Let's leave the topic of JPA/Hibernate for a moment, and talk about Java in general.

Legacy old school Java is about imperative programming, mutable JavaBeans with getters and setters,
(implicit) nullability everywhere, and often lots of hidden implicit state.

*Modern Java* is about *functional programming*, including `java.util.stream.Stream` *pipelines*, *immutable Java records*,
and the use of type `Optional` and/or explicit nullability using *JSpecify* annotations.

As a good example, compare the old school `Date` and `Calendar` APIs with the Java 8 `java.time` API.
The latter is a huge improvement over the former, leading to client code that is easy to reason about,
which to a large part can be attributed to the *immutable* date and time concepts in the `java.time` API.

Hibernate/JPA *entities* are in this sense mutable *old school JavaBeans* with getters and setters. They carry a lot
of *hidden state*, such as the presence or absence of an open "persistence context", associations that may or
may not have been loaded, etc. So they are poor DTOs to pass across application layers. By contrast, immutable
Java records are extremely simple to reason about, since they can have only 1 state, namely the state
created by the constructor. Let's define some DTOs and use them in the `AddressService` interface.

Assume annotation `org.jspecify.annotations.NullMarked` to be set at the package level, thus specifying
that everything in that package is non-null unless specified otherwise.

Record class `Country`:

```java
public record Country(
        long id,
        String country,
        Instant lastUpdate
) {
}
```

Record class `City`:

```java
public record City(
        long id,
        String city,
        Country country,
        Instant lastUpdate
) {
}
```

And finally record class `Address` (with a nested record class for new addresses):

```java
public record Address(
        long id,
        String address1,
        @Nullable String address2,
        String district,
        City city,
        @Nullable String postalCode,
        String phone,
        Instant lastUpdate
) {

    public Optional<String> address2Option() {
        return Optional.ofNullable(address2);
    }

    public Optional<String> postalCodeOption() {
        return Optional.ofNullable(postalCode);
    }

    public record NewAddress(
            String address1,
            @Nullable String address2,
            String district,
            long cityId,
            @Nullable String postalCode,
            String phone,
            Instant lastUpdate
    ) {
    }
}
```

The adapted `AddressService` API:

```java
// Completely technology-agnostic service interface (also trivial to mock in presentation layer unit tests)
public interface AddressService {

    // Good: result is technology-agnostic and immutable, so extremely easy to reason about
    Optional<Address> findById(long id);

    // Other methods
}
```

Let's adapt `ConcreteAddressService` accordingly, assuming the existence of method `AddressEntity.toModelObject`:

```java
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    @Override
    public Optional<Address> findById(long id) {
        return emf.callInTransaction(entityManager -> {
            // The city and country of the address are fetched as well
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co where ad.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    // More methods
}
```

Here the address entities are internal to the transactional Hibernate `Session`, and they are converted to immutable DTOs
within the same `Session`.

Assume method `AddressEntity.toModelObject` trivially calls `CityEntity.toModelObject`, which trivially calls
`CountryEntity.toModelObject`. Now the same code above would work without the "fetch joins", yet with
additional Hibernate-generated queries to lazily load city and country associations. In a service call
returning multiple addresses this can easily lead to an explosion of generated SQL queries.

There are several ways to create DTOs from Hibernate/JPQL queries. In the example above entities were retrieved, which
were subsequently converted to immutable DTOs. Yet it is also possible to retrieve no entities at all and instead use
*DTO constructor calls in the query*. No example of the latter approach is shown here.

*Custom DTOs* can be used to reduce the number of data fields to retrieve. Recall the preceding section in which
different proper ways of preventing `LazyInitializationException`s were discussed. Using custom DTO projections is
one of those ways. Fortunately, immutable Java records make perfect DTO projections.

Also note that DTO projections can prevent the creation of too many entities to be managed by the persistence
context. Yet again recall that sometimes a `StatelessSession`/`EntityAgent` can be a better choice than
`Session`/`EntityManager`, thus circumventing the need for a potentially "expensive" persistence context entirely.

Are there ways to use a Hibernate/JPQL query to populate a nested Java DTO projection, without using any
intermediate fetched entities? After all, SQL itself is a very powerful query language, with support for *SQL/JSON*
and *Common Table Expressions*. It is not hard to imagine how JSON results can be converted easily to
nested Java DTOs, e.g. using libraries such as [Jackson](https://github.com/fasterxml/jackson).

Fortunately, *HQL* is also an extremely rich OO SQL dialect, including support for SQL/JSON and CTEs
(since Hibernate 8). Given that it is quite unlikely that Hibernate is swapped for another JPA implementation,
why not *stick to the JPA standard where feasible, and use Hibernate-specific features where needed*?
No example is given here, but SQL/JSON HQL querying without needing any (intermediate) fetched entities is feasible.

In cases where we want to temporarily turn an `EntityManager` into a Hibernate `Session` to get access
to `Session`-specific functionality, the correct way to do so is:

```java
Session session = entityManager.unwrap(Session.class);
```

Interface `Session` extends `EntityManager`, but casting is not a good alternative, because the `EntityManager` may
be a proxy that must be unproxied first before down-casting. The `unwrap` call takes care of this.

Since Hibernate 8, an analogous remark holds for `EntityAgent` and `StatelessSession`:

```java
StatelessSession statelessSession = entityAgent.unwrap(StatelessSession.class);
```

In summary, *JPQL/HQL querying returning (immutable) DTO projections can be done in many ways*, and comes
with *several advantages* w.r.t. "application architecture" and performance.

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
