/*
 * Copyright 2026-2026 Chris de Vreeze
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.cdevreeze.hibernateexperiments.jpql.service.impl;

import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.entity.FilmActorEntity;
import eu.cdevreeze.hibernateexperiments.jpql.entity.FilmActorEntity_;
import eu.cdevreeze.hibernateexperiments.jpql.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.jpql.entity.FilmEntity_;
import eu.cdevreeze.hibernateexperiments.jpql.model.Film;
import eu.cdevreeze.hibernateexperiments.jpql.service.FilmService;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

import java.util.List;

/**
 * Concrete {@link FilmService} implementation.
 * <p>
 * The implementation has been inspired by
 * <a href="https://blog.jooq.org/jooq-3-15s-new-multiset-operator-will-change-how-you-think-about-sql/">jOOQ's multiset operator</a>,
 * which can be simulated by the database's SQL/JSON support.
 * <p>
 * The use of "json_object" inside "json_arrayagg" below, while being Hibernate HQL, has been deeply
 * inspired by Oracle's SQL/JSON support, as explained in this article:
 * <a href="https://oracle-base.com/articles/12c/sql-json-functions-12cr2">SQL/JSON generation functions in Oracle DB 12C</a>.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteFilmService implements FilmService {

    // For nested JSON results with JSON objects and nested arrays, see https://forums.oracle.com/ords/apexds/post/complex-nested-json-structure-8286
    // This is interesting for queries returning films and their actors

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .addModule(new GuavaModule())
            .build();

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film.WithActorsAndCategories> findAllFilmsWithActorsAndCategories() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Hibernate HQL, which extends JPQL

            return entityAgent.createQuery(QL_STRING, String.class)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Film.WithActorsAndCategories.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Optional<Film.WithActorsAndCategories> findFilmWithActorsAndCategories(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Hibernate HQL, which extends JPQL
            // Beautiful: no JPQL/HQL String concatenation!
            // Instead, turning the HQL into a criteria query, adding a where clause in a type-safe way!
            // See https://www.baeldung.com/hibernate-criteria-queries
            // Soon this will be part of JPA 4.0! See https://in.relation.to/2026/04/23/JPA-4-M2/

            HibernateCriteriaBuilder cb = entityAgent.unwrap(StatelessSession.class).getCriteriaBuilder();
            JpaCriteriaQuery<String> cq = cb.createQuery(QL_STRING, String.class);

            JpaRoot<? extends FilmEntity> filmRoot = cq.getRoot(0, FilmEntity.class);

            cq.where(cb.equal(filmRoot.get(FilmEntity_.id), filmId));

            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Film.WithActorsAndCategories.class))
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Film.WithActorsAndCategories> findFilmsWithActorsAndCategoriesByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Hibernate HQL, which extends JPQL
            // Beautiful: no JPQL/HQL String concatenation!
            // Instead, turning the HQL into a criteria query, adding a where clause in a type-safe way!
            // See https://www.baeldung.com/hibernate-criteria-queries
            // Soon this will be part of JPA 4.0! See https://in.relation.to/2026/04/23/JPA-4-M2/

            HibernateCriteriaBuilder cb = entityAgent.unwrap(StatelessSession.class).getCriteriaBuilder();
            JpaCriteriaQuery<String> cq = cb.createQuery(QL_STRING, String.class);

            JpaRoot<? extends FilmEntity> filmRoot = cq.getRoot(0, FilmEntity.class);

            JpaSubQuery<Integer> subquery = cq.subquery(Integer.class);
            JpaRoot<FilmActorEntity> subqueryRoot = subquery.from(FilmActorEntity.class);
            subquery.where(cb.equal(subqueryRoot.get(FilmActorEntity_.actorId), actorId));
            subquery.select(subqueryRoot.get(FilmActorEntity_.filmId));

            cq.where(cb.in(filmRoot.get(FilmEntity_.id), List.of(subquery)));

            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Film.WithActorsAndCategories.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private static final String QL_STRING = """
                    select json_object(
                               'film': json_object(
                                   'id': f.id,
                                   'title': f.title,
                                   'description': f.description,
                                   'releaseYear': f.releaseYear,
                                   'language': json_object(
                                       'id': l1.id,
                                       'name': l1.name,
                                       'lastUpdate': l1.lastUpdate
                                   ),
                                   'originalLanguage':
                                       case
                                           when f.originalLanguage.id is null
                                           then null
                                           else json_object(
                                                    'id': l2.id,
                                                    'name': l2.name,
                                                    'lastUpdate': l2.lastUpdate
                                                )
                                       end,
                                   'rentalDuration': f.rentalDuration,
                                   'rentalRate': f.rentalRate,
                                   'length': f.length,
                                   'replacementCost': f.replacementCost,
                                   'rating': f.rating,
                                   'lastUpdate': f.lastUpdate,
                                   'specialFeatures': json_array(),
                                   'fullText': ''
                               ),
                               'actors':
                                   (select json_arrayagg(
                                              json_object(
                                                  'id': a.id,
                                                  'firstName': a.firstName,
                                                  'lastName': a.lastName,
                                                  'lastUpdate': a.lastUpdate
                                              )
                                          )
                                     from FilmActor fa
                                    inner join Actor a on (fa.actorId = a.id)
                                    where fa.filmId = f.id
                               ),
                               'categories':
                                   (select json_arrayagg(
                                               json_object(
                                                   'id': c.id,
                                                   'name': c.name,
                                                   'lastUpdate': c.lastUpdate
                                               )
                                           )
                                     from FilmCategory fc
                                    inner join Category c on (fc.categoryId = c.id)
                                    where fc.filmId = f.id
                               )
                           )
                      from Film f
                      left join f.language l1
                      left join f.originalLanguage l2
            """;
}
