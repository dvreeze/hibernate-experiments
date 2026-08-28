
# Using the Hibernate ORM effectively

## Introduction

... TODO ...

What I hope to achieve: show some Hibernate ORM best practices, and take away some "magic".
This is needed because there is far too much unnecessary "Hibernate pain" in many Java projects.

What are my own experiences with Hibernate ORM? Not too good, until I decided to learn Hibernate ORM.
I learned much from official Hibernate material, experts like Thorben Janssen, and combined that with
my experiences in a more functional programming style (initially in Scala, but also inspired by the
book Effective Java, 3rd Edition, by Joshua Bloch). I'm not a Hibernate expert, but do have a "mind set"
that enables me to be productive with it. Details can be looked up in material written by experts.

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

@Transactional
public void deliver(OrderEntity order, Instant deliveryDate) {
    // ...
    order.setDeliveryDate(deliveryDate);
    // ...
}
```

*Ask audience* what the effect of this code is on the database.

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

Let's introduce the JPA entities relevant to our example.

Before doing so, first note that there are 2 categories of *JPA annotations* on entities:
- *logical mapping annotations*, concerning the Java object model
  - e.g. `Entity`, `Id`, `ManyToOne`, `Basic` etc.
- *physical mapping annotations*, concerning the underlying relational database schema
  - e.g. `Table`, `Column`, `JoinTable`, `GeneratedValue` etc.

First the `ActorEntity`:

```java
@Entity(name = "Actor")
@Table(name = "Actor")
public class ActorEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_id_seq")
    @SequenceGenerator(name = "actor_id_seq", sequenceName = "actor_actor_id_seq", allocationSize = 1)
    @Column(name = "actor_id")
    private Integer id;

    @Basic(optional = false)
    @Column(name = "first_name")
    private String firstName;

    @Basic(optional = false)
    @Column(name = "last_name")
    private String lastName;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Next the `CategoryEntity`:

```java
@Entity(name = "Category")
@Table(name = "Category")
public class CategoryEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_id_seq")
    @SequenceGenerator(name = "category_id_seq", sequenceName = "category_category_id_seq", allocationSize = 1)
    @Column(name = "category_id")
    private Integer id;

    @Basic(optional = false)
    private String name;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Next the `LanguageEntity`:

```java
@Entity(name = "Language")
@Table(name = "Language")
public class LanguageEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_id_seq")
    @SequenceGenerator(name = "language_id_seq", sequenceName = "language_language_id_seq", allocationSize = 1)
    @Column(name = "language_id")
    private Integer id;

    @Basic(optional = false)
    @Column(columnDefinition = "bpchar")
    private String name;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Next the `FilmActorEntity`, which represents the n-to-m association between films and actors:

```java
@Embeddable
public record FilmActorKey(Integer actorId, Integer filmId) { // Not serializable
}

@Entity(name = "FilmActor")
@Table(name = "Film_Actor")
public class FilmActorEntity { // Not serializable

    @EmbeddedId
    @AttributeOverride(name = "actorId", column = @Column(name = "actor_id"))
    @AttributeOverride(name = "filmId", column = @Column(name = "film_id"))
    private FilmActorKey filmActorKey;

    @MapsId("actorId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "actor_id")
    private ActorEntity actor;

    @MapsId("filmId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "film_id")
    private FilmEntity film;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Analogously, the `FilmCategoryEntity`, which represents the n-to-m association between films and categories:

```java
@Embeddable
public record FilmCategoryKey(Integer filmId, Integer categoryId) { // Not serializable
}

@Entity(name = "FilmCategory")
@Table(name = "Film_Category")
public class FilmCategoryEntity { // Not serializable

    @EmbeddedId
    @AttributeOverride(name = "categoryId", column = @Column(name = "category_id"))
    @AttributeOverride(name = "filmId", column = @Column(name = "film_id"))
    private FilmCategoryKey filmCategoryKey;

    @MapsId("filmId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "film_id")
    private FilmEntity film;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Finally, the `FilmEntity` itself:

```java
@Entity(name = "Film")
@Table(name = "Film")
public class FilmEntity { // Not serializable

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "film_id_seq")
    @SequenceGenerator(name = "film_id_seq", sequenceName = "film_film_id_seq", allocationSize = 1)
    @Column(name = "film_id")
    private Integer id;

    @Basic(optional = false)
    private String title;

    private String description;

    @Column(name = "release_year", columnDefinition = "year")
    private Year releaseYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_language_id")
    private LanguageEntity originalLanguage;

    @Basic(optional = false)
    @Column(name = "rental_duration")
    private Short rentalDuration;

    @Basic(optional = false)
    @Column(name = "rental_rate")
    private BigDecimal rentalRate;

    private Short length;

    @Basic(optional = false)
    @Column(name = "replacement_cost")
    private BigDecimal replacementCost;

    private String rating;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    @OneToMany(mappedBy = FilmActorEntity_.FILM)
    private Set<FilmActorEntity> filmActors;

    @OneToMany(mappedBy = FilmCategoryEntity_.FILM)
    private Set<FilmCategoryEntity> filmCategories;

    // Getters and setters, and possibly also overridden equals and hashCode
}
```

Let's now get to the first example of a transactional service querying for films (of a certain actor):

```java
public interface FilmService {

    ImmutableList<FilmEntity> findFilmsByActorId(long actorId);
}

public final class NaiveFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public NaiveFilmService(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<FilmEntity> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, actorId)
                    .getResultStream()
                    .sorted(Comparator.comparingLong(FilmEntity::getId))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
```

Now have a look at the following code:

```java
FilmService filmService = new NaiveFilmService(emf);

ImmutableList<FilmEntity> filmsOfActor = filmService.findFilmsByActorId(1L); // Assume the result is not empty
// Outside any transaction/Session:
var firstCategory = filmsOfActor.getFirst().getFilmCategories().iterator().next();
```

*Ask the audience* what is wrong with this small piece of code above. Indeed, a `LazyInitializationException`
is thrown.

## Querying for custom projections

... TODO ...

First compare old school Java to modern Java, e.g. `Date`/`Calendar` versus `java.time` API.
Modern Java is more *functional*, uses more *immutable Java records* instead of JavaBeans with getters and
setters, is *less about side effects* and less about `null`, and more about *expressions* than *statements*.
It is also about *Stream/Optional pipelines*.

Alas, JPA entities are more like old school JavaBeans with getters and setters. Moreover, they contain
lots of hidden implicit state, e.g. w.r.t. associations (loaded or not), the absence/presence of a persistence
context, etc. So they make very poor DTOs to be passed to the presentation layer.

Immutable Java records make great DTOs to be passed across application layers, though.

Let's introduce the immutable data model as immutable Java record DTOs. Assume a
`org.jspecify.annotations.NullMarked` annotation at the package level, in `package-info.java`.
The immutable model is as follows:

```java
public record Actor(long id, String firstName, String lastName, Instant lastUpdate) {
}

public record Category(long id, String name, Instant lastUpdate) {
}

public record Language(long id, String name, Instant lastUpdate) {
}

public record Film(
        long id,
        String title,
        @Nullable String description,
        @Nullable Year releaseYear,
        Language language,
        @Nullable Language originalLanguage,
        int rentalDuration,
        BigDecimal rentalRate,
        @Nullable Integer length,
        BigDecimal replacementCost,
        @Nullable String rating,
        Instant lastUpdate,
        @Nullable ImmutableList<String> specialFeatures,
        String fullText,
        ImmutableList<Actor> actors,
        ImmutableList<Category> categories
) {

    public Optional<String> descriptionOption() {
        return Optional.ofNullable(description);
    }

    public Optional<Year> releaseYearOption() {
        return Optional.ofNullable(releaseYear);
    }

    public Optional<Language> originalLanguageOption() {
        return Optional.ofNullable(originalLanguage);
    }

    public OptionalInt lengthOption() {
        return Optional.ofNullable(length).stream().mapToInt(i -> i).findFirst();
    }

    public Optional<String> ratingOption() {
        return Optional.ofNullable(rating);
    }

    public Optional<ImmutableList<String>> specialFeaturesOption() {
        return Optional.ofNullable(specialFeatures);
    }
}
```

Enhance the JPA entities with (trivial) conversion methods to that model:

```java
import static java.util.Objects.requireNonNull;

@Entity(name = "Actor")
@Table(name = "Actor")
public class ActorEntity {

    // ...

    public Actor toModelObject() {
        return new Actor(
                requireNonNull(id),
                requireNonNull(firstName),
                requireNonNull(lastName),
                requireNonNull(lastUpdate)
        );
    }
}

@Entity(name = "Category")
@Table(name = "Category")
public class CategoryEntity {

    // ...

    public Category toModelObject() {
        return new Category(requireNonNull(id), requireNonNull(name), requireNonNull(lastUpdate));
    }
}

@Entity(name = "Language")
@Table(name = "Language")
public class LanguageEntity {

    // ...

    public Language toModelObject() {
        return new Language(requireNonNull(id), requireNonNull(name), requireNonNull(lastUpdate));
    }
}
```

And finally:

```java
@Entity(name = "Film")
@Table(name = "Film")
public class FilmEntity {

    // ...

    // May cause LazyInitializationException
    public Film toModelObject() {
        return new Film(
                requireNonNull(id),
                requireNonNull(title),
                description,
                releaseYear,
                requireNonNull(language).toModelObject(),
                Optional.ofNullable(originalLanguage).map(LanguageEntity::toModelObject).orElse(null),
                requireNonNull(rentalDuration),
                rentalRate,
                Optional.ofNullable(length).map(Short::intValue).orElse(null),
                requireNonNull(replacementCost),
                rating,
                requireNonNull(lastUpdate),
                ImmutableList.of(),
                "",
                requireNonNull(filmActors).stream()
                        .map(FilmActorEntity::getActor)
                        .map(ActorEntity::toModelObject)
                        .collect(ImmutableList.toImmutableList()),
                requireNonNull(filmCategories).stream()
                        .map(FilmCategoryEntity::getCategory)
                        .map(CategoryEntity::toModelObject)
                        .collect(ImmutableList.toImmutableList())
        );
    }
}
```

The `FilmService` is now a Java service interface returning immutable DTOs rather than JPA entities:

```java
public interface FilmService {

    ImmutableList<Film> findFilmsByActorId(long actorId);
}
```

Now consider the following implementation of that service interface:

```java
public final class InefficientFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public InefficientFilmService(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, actorId)
                    .getResultStream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
```

*Ask audience* what is wrong with the implementation (too many queries), other than retrieving entities
in the same session where they are converted to immutable DTOs (therefore unnecessary flushing overhead).
That problem will be dealt with later.

## Per-query fetching

... TODO ...

Fixing the explosion of generated SQL by being explicit about what entities to fetch, in this case by
using an `EntityGraph` as "load graph":

```java
public final class ConcreteFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            return entityManager.createQuery(qlString, getEntityGraph())
                    .setParameter(1, actorId)
                    .getResultStream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<FilmEntity> getEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }
}
```

Alternatively, we can use "fetch joins" in the JPQL query:

```java
public final class ConcreteFilmServiceUsingFetchJoin implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmServiceUsingFetchJoin(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(entityManager -> {
            String qlString = """
                    select f from Film f
                      left join fetch f.filmActors fac
                      left join fetch fac.actor
                      left join fetch f.filmCategories fca
                      left join fetch fca.category
                      left join fetch f.language
                      left join fetch f.originalLanguage
                      left join f.filmActors fa
                     where fa.actor.id = ?1""";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, actorId)
                    .getResultStream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
```

Show how this comes much closer to the
[correct way to fix a LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/).

Also show how we narrowly escaped the throwing of a `MultipleBagFetchException`. Show ways to deal with that.

A good way to prevent the `MultipleBagFetchException` is shown in `ConcreteFilmServiceUsingSeparateQueries`:

```java
public final class ConcreteFilmServiceUsingSeparateQueries implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmServiceUsingSeparateQueries(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(entityManager -> {
            java.util.List<FilmEntity> filmEntities =
                    findFilmsByActorId(actorId, getFilmActorsEntityGraph(), entityManager);

            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilmsByActorId(actorId, getFilmCategoriesEntityGraph(), entityManager)
                            .stream()
                            .collect(Collectors.toMap(FilmEntity::getId, Function.identity()));

            filmEntities.forEach(filmEntity -> filmEntity.setFilmCategories(
                    Optional.ofNullable(filmEntityWithCategoriesMap.get(filmEntity.getId()))
                            .map(FilmEntity::getFilmCategories)
                            .orElse(java.util.Set.of())
            ));

            return filmEntities.stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private ImmutableList<FilmEntity> findFilmsByActorId(
            long actorId, EntityGraph<FilmEntity> eg, EntityManager entityManager) {
        String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

        return entityManager.createQuery(qlString, eg)
                .setParameter(1, actorId)
                .getResultStream()
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<FilmEntity> getFilmActorsEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }

    private EntityGraph<FilmEntity> getFilmCategoriesEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        return eg;
    }
}
```

## Using Hibernate ORM without persistence context

... TODO ...

*Ask audience* if it is possible to use Hibernate ORM without persistence context overhead.

This gets us to the same `ConcreteFilmServiceUsingSeparateQueries`, except that it uses an `EntityAgent` instead
of `EntityManager` (since Jakarta Persistence 4.0, but `StatelessSession` has already existed for a long time).

Here is the code:

```java
public final class ConcreteFilmServiceUsingSeparateQueries implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmServiceUsingSeparateQueries(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            java.util.List<FilmEntity> filmEntities =
                    findFilmsByActorId(actorId, getFilmActorsEntityGraph(), entityAgent);

            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilmsByActorId(actorId, getFilmCategoriesEntityGraph(), entityAgent)
                            .stream()
                            .collect(Collectors.toMap(FilmEntity::getId, Function.identity()));

            filmEntities.forEach(filmEntity -> filmEntity.setFilmCategories(
                    Optional.ofNullable(filmEntityWithCategoriesMap.get(filmEntity.getId()))
                            .map(FilmEntity::getFilmCategories)
                            .orElse(java.util.Set.of())
            ));

            return filmEntities.stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private ImmutableList<FilmEntity> findFilmsByActorId(
            long actorId, EntityGraph<FilmEntity> eg, EntityAgent entityAgent) {
        String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

        return entityAgent.createQuery(qlString, eg)
                .setParameter(1, actorId)
                .getResultStream()
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<FilmEntity> getFilmActorsEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }

    private EntityGraph<FilmEntity> getFilmCategoriesEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        return eg;
    }
}
```

Finally, we can reliably retrieve entities and convert them to the immutable model within the same
`Session`/`EntityManager` without triggering any flushing overhead.

Show how to use an `EntityAgent` (or, before Jakarta Persistence 4.0) its subtype `StatelessSession` for
CRUD operations. Note that both `EntityManager` and `EntityAgent` extend interface `EntityHandler`.
The latter contains the (relatively large) API common to both subtypes.

## Exploiting richness of HQL

... TODO ...

In Hibernate 8, the HQL language (which is a superset of JPQL) is such a powerful (OO) SQL dialect that
even Common Table Expressions and JSON are supported. Note that JSON results can be converted to immutable
model objects using a library such as [Jackson](https://github.com/fasterxml/jackson). This approach
is not shown here.

This requires a full `Session` or `StatelessSession`, instead of supertype `EntityManager` or `EntityAgent`.
For example:

```java
public final class AlternativeFilmService implements FilmService {

    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(StatelessSession.class, statelessSession -> {
            // ...
        }
    }
}
```

Remember, JPQL and in particular HQL are very powerful *OO SQL dialects*.

If need be, we can always fall back to native SQL queries, and still benefit from Hibernate (and increased
type-safety). In that case we would use `EntityHandler` method `createNativeQuery` rather than `createQuery`.

## The type-safe metamodel

... TODO ...

Let's show a `ConcreteFilmService` using the Criteria API with the metamodel, thus getting much compile-time
type-safety when using the Criteria API:

```java
public final class ConcreteFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

            Root<FilmEntity> film = cq.from(FilmEntity.class);
            SetJoin<FilmEntity, FilmActorEntity> filmActor = film.join(FilmEntity_.filmActors, JoinType.LEFT);
            cq.where(cb.equal(filmActor.get(FilmActorEntity_.actor).get(ActorEntity_.id), actorId));
            cq.select(film);

            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getEntityGraph()) // Not type-safe
                    .getResultStream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<FilmEntity> getEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }
}
```

Now move on to Jakarta Data repositories, with compile-time JPQL/HQL query string parsing/validation
(through the Hibernate annotation processor). Thus, we get compile-time query validation without the
verbosity of the Criteria API.

Let's show the "repository" code (returning entities):

```java
@jakarta.data.repository.Repository
public interface FilmRepository {

    @HQL("""
            select f from Film f
              left join fetch f.filmActors fac
              left join fetch fac.actor
              left join fetch f.filmCategories fca
              left join fetch fca.category
              left join fetch f.language
              left join fetch f.originalLanguage
              left join f.filmActors fa
             where fa.actor.id = :actorId""")
    List<FilmEntity> findFilmsByActorId(int actorId);
}
```

The service using this "repository":

```java
public final class ConcreteFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) { this.emf = emf; }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            FilmRepository filmRepository = new _FilmRepository(entityAgent);
            return filmRepository.findFilmsByActorId((int) actorId)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
```

For now, I had to use the Hibernate-specific `HQL` annotation, but with Hibernate 8.0 Final I do not expect
that to be the case anymore.

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

Some references follow below. When unclear about details of Hibernate/JPA usage, consider first consulting
the Jakarta Persistence Specification and Hibernate ORM User Guide, before anything else.

From the Jakarta Persistence standard:

- [Jakarta Persistence specification](https://jakarta.ee/specifications/persistence/) as reference material
- [Jakarta Persistence 4.0 Specification](https://jakarta.ee/specifications/persistence/4.0/jakarta-persistence-spec-4.0-m4)
- [Jakarta Persistence 4.0 API documentation](https://jakarta.ee/specifications/persistence/4.0/apidocs/jakarta.persistence/module-summary.html)

From the Hibernate team:

- [Hibernate ORM advice](https://docs.hibernate.org/orm/8.0/introduction/html_single/#advice) from the Hibernate ORM team
- [No-nonsense guide to Hibern8](https://docs.hibernate.org/orm/8.0/introduction/html_single/) (should probably be read from beginning to end)
- [Hibernate ORM user guide](https://docs.hibernate.org/orm/8.0/userguide/html_single/) as reference material
- [Hibernate ORM 8.0 API documentation](https://docs.hibernate.org/orm/8.0/javadocs/)
- [Many-to-one associations](https://docs.hibernate.org/orm/8.0/introduction/html_single/#many-to-one)

From Hibernate expert Thorben Janssen:

- [Hibernate tutorials by Thorben Janssen](https://thorben-janssen.com/tutorials/)
- [Hibernate performance tuning](https://thorben-janssen.com/hibernate-performance-tuning/)
- [LazyInitializationException](https://thorben-janssen.com/lazyinitializationexception/)
- [Choose the right fetch type](https://thorben-janssen.com/hibernate-performance-tuning/#avoid-unnecessary-queries--choose-the-right-fetchtype)
- [MultipleBagFetchException](https://thorben-janssen.com/hibernate-tips-how-to-avoid-hibernates-multiplebagfetchexception/) and [fix MultipleBagFetchException](https://thorben-janssen.com/fix-multiplebagfetchexception-hibernate/)
- [Cascade type remove issues](https://thorben-janssen.com/avoid-cascadetype-delete-many-assocations/)
- [Hibernate query spaces](https://thorben-janssen.com/hibernate-query-spaces/)
- [Read-only query hint](https://thorben-janssen.com/read-only-query-hint/)

Other links:

- [Hibernate ORM pitfalls or difficulties](https://www.quora.com/What-are-pitfalls-or-difficulties-in-using-Hibernate-as-ORM)
- [Stop using JPA/Hibernate](https://www.stemlaur.com/blog/2021/03/30/tech-hibern-hate/), in order to learn what critics have to say about Hibernate ORM

What is new in Hibernate ORM 8:

- [Jakarta Persistence 4.0 Milestone 1](https://in.relation.to/2026/01/20/JPA-4-M1/)
- [Jakarta Persistence 4.0 Milestone 2](https://in.relation.to/2026/04/23/JPA-4-M2/)

## Questions?

Time for questions from the audience.
