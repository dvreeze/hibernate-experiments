
# Using the Hibernate ORM effectively

Expected audience: mostly Java developers with at least some experience with Hibernate ORM.

Motivation for this presentation: I have encountered Hibernate ORM in many projects, mostly struggling
with it instead of using it effectively. It took me many years before starting to understand that this does
not have to be that way, and I would like to share those insights. Hence, this presentation.

*Question* to the audience: at work, *who uses (or has used) Hibernate ORM*, or at least Jakarta Persistence (or its predecessor)?

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

## Introduction

When I say "Hibernate ORM", I could have said "Jakarta Persistence API" (formerly JPA).
Loosely we can still use the term JPA for Jakarta Persistence API.

Hibernate ORM is the reference implementation (and de-facto standard implementation) of the Jakarta Persistence standard.
Typically, we use Hibernate ORM through the Jakarta Persistence API, so in practice we could use both terms interchangeably.
Note that Hibernate 6+ uses the "jakarta" namespace (introduced in "JPA" version 3.0) instead of "javax" namespace.

Why this presentation? After all, most "enterprise" Java projects use Hibernate/JPA, so it should be familiar, right?

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

What to do about this, if we want the city and country associations to be fetched as part of the result?

We could feel urged to choose fetch type "eager" for both associations in the entity classes, but this
would affect all code using those entities. This could easily lead to a hidden explosion of fetched data where
only a small subset of that data is needed.

The Hibernate team ([Hibernate ORM short guide](https://docs.hibernate.org/orm/8.0/introduction/html_single/#many-to-one))
and experts like Thorben Janssen
([Choose the right fetch type](https://thorben-janssen.com/hibernate-performance-tuning/#avoid-unnecessary-queries--choose-the-right-fetchtype))
are very clear: *choose fetch type "lazy" for all entity associations*.

So, use the default fetch type for to-many associations, and *explicitly choose fetch type "lazy" for to-one associations*.
Jakarta Persistence 4.0 (and therefore Hibernate 8) makes this easy, by a global configuration setting!

Still, this does not solve our problem. Both the Hibernate team and Thorben Janssen also advice the following:
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
we did when we used to write all SQL ourselves: per SQL query we chose our "fetch joins". So also in that regard
we should not abandon proven database querying practices, even when using Hibernate ORM. Again, the library
is not about abstracting away the database; it is about Java and the database working well together.

In this case, our entities only used to-one associations, but in practice many associations are collection-valued
to-many associations. This brings us to what might be the main problem in production with (naive?) Hibernate
ORM application code, the dreaded *N + 1 problem*:

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

Again, the short story is: *all entity associations should be lazy* and *use per-query fetching*.
That way we prevent the N + 1 problem.

... TODO ...

... overall, keep entity configuration (via annotations) simple ...

## Combining mutable JPA entities with immutable Java record DTOs

... TODO ...

... there are multiple reasons why immutable Java records as query results are attractive (immutable, not too many queries or fields to query, etc.) ...

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
