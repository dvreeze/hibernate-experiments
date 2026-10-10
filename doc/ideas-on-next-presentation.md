
# Ideas on the next presentation about Hibernate ORM

Idea: In the next presentation on Hibernate ORM 8+, *start with a mind set*:
- In this mindset, Hibernate ORM is certainly NOT about abstracting away the database
- In this mindset, Hibernate ORM is NOT foremost about the persistence context (there is `EntityAgent` as well)
  - Note that there is some internal "mismatch" in the persistence context between managed *entity instances versus sets of entities*; SQL is more set-oriented; both approaches can be and are indeed combined
- Yet Hibernate ORM is more about the notion of *entities* and a powerful *OO SQL dialect* around those entities (and their associations, and entity inheritance)
  - Note that with the separate *Jakarta Query* specification, Hibernate ORM seems to make the SQL-like query language more important rather than less important

Note that with this mind set, it becomes natural to follow *best practices* such as *lazy fetching* at the entity level combined with *per-query fetching*.

Also part of the mind set, as far as I am concerned (especially in large code bases):
- Keeping mutable entities local to database access code, and working with *immutable Java record DTOs* beyond that

Illustrate all this in the presentation as follows:
- Start with an entity model and immutable counterpart and do ad-hoc JPQL querying against those entities, returning entities or immutable Java records
  - So just like we (always) could do ad-hoc SQL querying, we could thus do ad-hoc JPQL querying
  - We could even use an in-memory H2 database to do this ad-hoc JPQL querying (but mind tweaks needed for that, such as META-INF/orm.xml)
- We could even "desuger" some JPQL queries to show even more resemblance to regular SQL; e.g. by replacing "path expressions" by explicit join conditions

What could also be part of the presentation is the use of *ArchUnit* to enforce certain best practices.

Maybe use Java Modules as well, if that adds anything useful to the presentation.

Before working on this presentation, let's make sure I have experimented a lot with Hibernate ORM in Quarkus applications, if only because most of the audience knows Quarkus.

Still, Quarkus is also evolving fast. Consider for example Panache versus the newer Quarkus Data (based on Jakarta Data). Still, I need to be aware of these standards and implementations.
