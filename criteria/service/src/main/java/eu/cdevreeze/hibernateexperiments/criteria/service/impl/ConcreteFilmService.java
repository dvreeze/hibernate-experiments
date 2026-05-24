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

package eu.cdevreeze.hibernateexperiments.criteria.service.impl;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.criteria.entity.*;
import eu.cdevreeze.hibernateexperiments.criteria.model.Film;
import eu.cdevreeze.hibernateexperiments.criteria.service.FilmService;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.JoinType;
import org.hibernate.StatelessSession;
import org.hibernate.query.criteria.*;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

import java.util.Map;

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
            HibernateCriteriaBuilder cb = entityAgent.unwrap(StatelessSession.class).getCriteriaBuilder();

            JpaCriteriaQuery<String> resultQuery = createFilmQuery(cb);

            return entityAgent.createQuery(resultQuery)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Film.WithActorsAndCategories.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private JpaCriteriaQuery<String> createFilmQuery(HibernateCriteriaBuilder cb) {
        JpaCriteriaQuery<String> cq = cb.createQuery(String.class);
        JpaRoot<FilmEntity> filmRoot = cq.from(FilmEntity.class);
        JpaJoin<FilmEntity, LanguageEntity> filmLanguage = filmRoot.join(FilmEntity_.language, JoinType.LEFT);
        JpaJoin<FilmEntity, LanguageEntity> filmOriginalLanguage = filmRoot.join(FilmEntity_.originalLanguage, JoinType.LEFT);

        // See https://thorben-janssen.com/hibernate-tip-subquery-criteriaquery/
        JpaSubQuery<String> actorsSubquery = cq.subquery(String.class);
        JpaRoot<FilmActorEntity> filmActorRoot = actorsSubquery.from(FilmActorEntity.class);
        JpaJoin<FilmActorEntity, ActorEntity> actorJoin = filmActorRoot.join(ActorEntity.class);
        actorJoin.on(cb.equal(filmActorRoot.get(FilmActorEntity_.actorId), actorJoin.get(ActorEntity_.id)));
        actorsSubquery.where(cb.equal(filmActorRoot.get(FilmActorEntity_.filmId), filmRoot.get(FilmEntity_.id)));

        actorsSubquery.select(
                cb.jsonArrayAgg(
                        cb.jsonObject(
                                Map.ofEntries(
                                        Map.entry("id", actorJoin.get(ActorEntity_.id)),
                                        Map.entry("firstName", actorJoin.get(ActorEntity_.firstName)),
                                        Map.entry("lastName", actorJoin.get(ActorEntity_.lastName)),
                                        Map.entry("lastUpdate", actorJoin.get(ActorEntity_.lastUpdate))
                                )
                        )
                )
        );

        // See https://thorben-janssen.com/hibernate-tip-subquery-criteriaquery/
        JpaSubQuery<String> categoriesSubquery = cq.subquery(String.class);
        JpaRoot<FilmCategoryEntity> filmCategoryRoot = categoriesSubquery.from(FilmCategoryEntity.class);
        JpaJoin<FilmCategoryEntity, CategoryEntity> categoryJoin = filmCategoryRoot.join(CategoryEntity.class);
        categoryJoin.on(cb.equal(filmCategoryRoot.get(FilmCategoryEntity_.categoryId), categoryJoin.get(CategoryEntity_.id)));
        categoriesSubquery.where(cb.equal(filmCategoryRoot.get(FilmCategoryEntity_.filmId), filmRoot.get(FilmEntity_.id)));

        categoriesSubquery.select(
                cb.jsonArrayAgg(
                        cb.jsonObject(
                                Map.ofEntries(
                                        Map.entry("id", categoryJoin.get(CategoryEntity_.id)),
                                        Map.entry("name", categoryJoin.get(CategoryEntity_.name)),
                                        Map.entry("lastUpdate", categoryJoin.get(CategoryEntity_.lastUpdate))
                                )
                        )
                )
        );

        // Below, note the case clause (see https://stackoverflow.com/questions/10236445/if-case-statement-in-jpa-criteria-builder)
        // Also note the "null" literal there, to prevent an NPE

        cq.select(
                cb.jsonObject(
                        Map.of(
                                "film",
                                cb.jsonObject(
                                        Map.ofEntries(
                                                Map.entry("id", filmRoot.get(FilmEntity_.id)),
                                                Map.entry("title", filmRoot.get(FilmEntity_.title)),
                                                Map.entry("description", filmRoot.get(FilmEntity_.description)),
                                                Map.entry("releaseYear", filmRoot.get(FilmEntity_.releaseYear)),
                                                Map.entry("language",
                                                        cb.jsonObject(
                                                                Map.of(
                                                                        "id", filmLanguage.get(LanguageEntity_.id),
                                                                        "name", filmLanguage.get(LanguageEntity_.name),
                                                                        "lastUpdate", filmLanguage.get(LanguageEntity_.lastUpdate)
                                                                )
                                                        )),
                                                Map.entry("originalLanguage",
                                                        cb.selectCase()
                                                                .when(cb.isNull(filmRoot.get(FilmEntity_.originalLanguage).get(LanguageEntity_.id)), cb.literal(null))
                                                                .otherwise(
                                                                        cb.jsonObject(
                                                                                Map.of(
                                                                                        "id", filmOriginalLanguage.get(LanguageEntity_.id),
                                                                                        "name", filmOriginalLanguage.get(LanguageEntity_.name),
                                                                                        "lastUpdate", filmOriginalLanguage.get(LanguageEntity_.lastUpdate)
                                                                                )
                                                                        )
                                                                )
                                                ),
                                                Map.entry("rentalDuration", filmRoot.get(FilmEntity_.rentalDuration)),
                                                Map.entry("rentalRate", filmRoot.get(FilmEntity_.rentalRate)),
                                                Map.entry("length", filmRoot.get(FilmEntity_.length)),
                                                Map.entry("replacementCost", filmRoot.get(FilmEntity_.replacementCost)),
                                                Map.entry("rating", filmRoot.get(FilmEntity_.rating)),
                                                Map.entry("lastUpdate", filmRoot.get(FilmEntity_.lastUpdate)),
                                                Map.entry("specialFeatures", cb.jsonArray()),
                                                Map.entry("fullText", cb.literal(""))
                                        )
                                ),
                                "actors",
                                actorsSubquery,
                                "categories",
                                categoriesSubquery
                        )
                )
        );
        return cq;
    }
}
