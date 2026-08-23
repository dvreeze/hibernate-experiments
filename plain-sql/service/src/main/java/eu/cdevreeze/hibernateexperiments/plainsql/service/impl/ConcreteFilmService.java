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

package eu.cdevreeze.hibernateexperiments.plainsql.service.impl;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.plainsql.model.Actor;
import eu.cdevreeze.hibernateexperiments.plainsql.model.Category;
import eu.cdevreeze.hibernateexperiments.plainsql.model.Film;
import eu.cdevreeze.hibernateexperiments.plainsql.model.Language;
import eu.cdevreeze.hibernateexperiments.plainsql.service.FilmService;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.sql.ConstructorMapping;
import jakarta.persistence.sql.ResultSetMapping;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import static jakarta.persistence.sql.ResultSetMapping.column;
import static jakarta.persistence.sql.ResultSetMapping.constructor;

/**
 * Concrete {@link FilmService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteFilmService implements FilmService {

    public record FilmRow(
            Integer filmId,
            String title,
            @Nullable String description,
            @Nullable Integer releaseYear,
            int languageId,
            String languageName,
            Instant languageLastUpdate,
            @Nullable Integer originalLanguageId,
            @Nullable String originalLanguageName,
            @Nullable Instant originalLanguageLastUpdate,
            int rentalDuration,
            BigDecimal rentalRate,
            @Nullable Integer length,
            BigDecimal replacementCost,
            @Nullable String rating,
            Instant lastUpdate,
            @Nullable Integer actorId,
            @Nullable String actorFirstName,
            @Nullable String actorLastName,
            @Nullable Instant actorLastUpdate,
            @Nullable Integer categoryId,
            @Nullable String categoryName,
            @Nullable Instant categoryLastUpdate
    ) {

        public static ImmutableList<Film> convertToFilms(List<FilmRow> rows) {
            return rows
                    .stream()
                    .collect(Collectors.groupingBy(FilmRow::filmId))
                    .values()
                    .stream()
                    .map(grp -> new Film(
                            grp.getFirst().filmId(),
                            grp.getFirst().title(),
                            grp.getFirst().description(),
                            grp.getFirst().releaseYear() == null ?
                                    null :
                                    Year.of(grp.getFirst().releaseYear()),
                            new Language(
                                    grp.getFirst().languageId(),
                                    grp.getFirst().languageName(),
                                    grp.getFirst().languageLastUpdate()
                            ),
                            grp.getFirst().originalLanguageId() == null ?
                                    null :
                                    new Language(
                                            grp.getFirst().originalLanguageId(),
                                            Objects.requireNonNull(grp.getFirst().originalLanguageName()),
                                            Objects.requireNonNull(grp.getFirst().originalLanguageLastUpdate())
                                    ),
                            grp.getFirst().rentalDuration(),
                            grp.getFirst().rentalRate(),
                            grp.getFirst().length(),
                            grp.getFirst().replacementCost(),
                            grp.getFirst().rating(),
                            grp.getFirst().lastUpdate(),
                            ImmutableList.of(),
                            "",
                            grp.stream()
                                    .filter(row -> row.actorId() != null)
                                    .map(row -> new Actor(
                                            row.actorId(),
                                            Objects.requireNonNull(row.actorFirstName()),
                                            Objects.requireNonNull(row.actorLastName()),
                                            Objects.requireNonNull(row.actorLastUpdate())
                                    ))
                                    .distinct()
                                    .sorted(Comparator.comparing(Actor::id))
                                    .collect(ImmutableList.toImmutableList()),
                            grp.stream()
                                    .filter(row -> row.categoryId() != null)
                                    .map(row -> new Category(
                                            row.categoryId(),
                                            Objects.requireNonNull(row.categoryName()),
                                            Objects.requireNonNull(row.categoryLastUpdate())
                                    ))
                                    .distinct()
                                    .sorted(Comparator.comparing(Category::id))
                                    .collect(ImmutableList.toImmutableList())
                    ))
                    .sorted(Comparator.comparing(Film::id))
                    .collect(ImmutableList.toImmutableList());
        }
    }

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            ResultSetMapping<FilmRow> rsMapping = getFilmRowResultMapping();

            List<FilmRow> rows = entityAgent.createNativeQuery(SQL_STRING, rsMapping).getResultList();

            return FilmRow.convertToFilms(rows);
        });
    }

    @Override
    public Optional<Film> findFilm(long filmId) {
        // Not ideal, because SQL string composition using string concatenation is error-prone
        String sqlString = SQL_STRING.strip() + " where f.film_id = ?1";

        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            ResultSetMapping<FilmRow> rsMapping = getFilmRowResultMapping();

            List<FilmRow> rows = entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, filmId)
                    .getResultList();

            return FilmRow.convertToFilms(rows).stream().findFirst();
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // Not ideal, because SQL string composition using string concatenation is error-prone
        String sqlString = SQL_STRING.strip() + " " + """
                where f.film_id in (
                    select fac.film_id
                      from film_actor fac
                     where fac.actor_id = ?1
                )
                """;

        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            ResultSetMapping<FilmRow> rsMapping = getFilmRowResultMapping();

            List<FilmRow> rows = entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, actorId)
                    .getResultList();

            return FilmRow.convertToFilms(rows);
        });
    }

    private static ConstructorMapping<FilmRow> getFilmRowResultMapping() {
        return constructor(
                FilmRow.class,
                column("film_id", Integer.class),
                column("title", String.class),
                column("description", String.class),
                column("release_year", Integer.class),
                column("language_id", Integer.class),
                column("language_name", String.class),
                column("language_last_update", Instant.class),
                column("orig_language_id", Integer.class),
                column("orig_language_name", String.class),
                column("orig_language_last_update", Instant.class),
                column("rental_duration", Integer.class),
                column("rental_rate", BigDecimal.class),
                column("length", Integer.class),
                column("replacement_cost", BigDecimal.class),
                column("rating", String.class),
                column("last_update", Instant.class),
                column("actor_id", Integer.class),
                column("actor_first_name", String.class),
                column("actor_last_name", String.class),
                column("actor_last_update", Instant.class),
                column("category_id", Integer.class),
                column("category_name", String.class),
                column("category_last_update", Instant.class)
        );
    }

    private static final String SQL_STRING = """
            select f.film_id as film_id, f.title, f.description, f.release_year as release_year,
                   lang.language_id as language_id, lang.name as language_name, lang.last_update as language_last_update,
                   origlang.language_id as orig_language_id, origlang.name as orig_language_name, origlang.last_update as orig_language_last_update,
                   f.rental_duration, f.rental_rate, f.length, f.replacement_cost,
                   f.rating, f.last_update,
                   ac.actor_id, ac.first_name as actor_first_name, ac.last_name as actor_last_name, ac.last_update as actor_last_update,
                   cat.category_id, cat.name as category_name, cat.last_update as category_last_update
              from Film f
              left join Film_Actor fac on (f.film_id = fac.film_id)
              left join Actor ac on (fac.actor_id = ac.actor_id)
              left join Film_Category fcat on (f.film_id = fcat.film_id)
              left join Category cat on (fcat.category_id = cat.category_id)
             inner join Language lang on (f.language_id = lang.language_id)
              left join Language origlang on (f.original_language_id = origlang.language_id)
            """;
}
