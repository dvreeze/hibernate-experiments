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

package eu.cdevreeze.hibernateexperiments.repository.repo;

import module java.base;
import eu.cdevreeze.hibernateexperiments.repository.model.Film;
import jakarta.data.repository.Repository;
import jakarta.persistence.EntityAgent;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

/**
 * {@link Film.WithActorsAndCategories}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface FilmRepository {

    EntityAgent entityAgent();

    default JsonMapper jsonMapper() {
        return JsonMapper.builder()
                .addModule(new GuavaModule())
                .build();
    }

    // In reality this would return too many results
    // HQL annotation not yet working here. Needs to use system property "hibernate.query.hql.json_functions_enabled".
    // @HQL(FIND_ALL_QL_STRING)
    default List<String> findAllFilmsWithActorsAndCategoriesAsJson() {
        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");
        return entityAgent()
                .createQuery(FIND_ALL_QL_STRING, String.class)
                .getResultList();
    }

    default List<Film.WithActorsAndCategories> findAllFilmsWithActorsAndCategories() {
        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");
        return findAllFilmsWithActorsAndCategoriesAsJson()
                .stream()
                .map(v -> jsonMapper().readValue(v, Film.WithActorsAndCategories.class))
                .toList();
    }

    // HQL annotation not yet working here. Needs to use system property "hibernate.query.hql.json_functions_enabled".
    // @HQL(FIND_BY_FILM_ID_QL_STRING)
    default Optional<String> findFilmWithActorsAndCategoriesAsJson(long filmId) {
        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");
        return entityAgent()
                .createQuery(FIND_BY_FILM_ID_QL_STRING, String.class)
                .setParameter("filmId", filmId)
                .getResultList()
                .stream()
                .findFirst();
    }

    default Optional<Film.WithActorsAndCategories> findFilmWithActorsAndCategories(long filmId) {
        return findFilmWithActorsAndCategoriesAsJson(filmId)
                .map(v -> jsonMapper().readValue(v, Film.WithActorsAndCategories.class));
    }

    // HQL annotation not yet working here. Needs to use system property "hibernate.query.hql.json_functions_enabled".
    // @HQL(FIND_BY_ACTOR_ID_QL_STRING)
    default List<String> findFilmsWithActorsAndCategoriesByActorIdAsJson(long actorId) {
        return entityAgent()
                .createQuery(FIND_BY_ACTOR_ID_QL_STRING, String.class)
                .setParameter("actorId", actorId)
                .getResultList();
    }

    default List<Film.WithActorsAndCategories> findFilmsWithActorsAndCategoriesByActorId(long actorId) {
        return findFilmsWithActorsAndCategoriesByActorIdAsJson(actorId)
                .stream()
                .map(v -> jsonMapper().readValue(v, Film.WithActorsAndCategories.class))
                .toList();
    }

    String FIND_ALL_QL_STRING = """
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

    String FIND_BY_FILM_ID_QL_STRING = FIND_ALL_QL_STRING + " where f.id = :filmId";

    String FIND_BY_ACTOR_ID_QL_STRING = FIND_ALL_QL_STRING +
            " where f.id in (select filmId from FilmActor where actorId = :actorId)";
}
