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
import eu.cdevreeze.hibernateexperiments.plainsql.model.Film;
import eu.cdevreeze.hibernateexperiments.plainsql.service.FilmService;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

/**
 * Concrete {@link FilmService} implementation.
 * <p>
 * The implementation has been inspired by
 * <a href="https://blog.jooq.org/jooq-3-15s-new-multiset-operator-will-change-how-you-think-about-sql/">jOOQ's multiset operator</a>,
 * which can be simulated by the database's SQL/JSON support.
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
            String sqlString = """
                            select json_object(
                                       'film': json_object(
                                           'id': f.film_id,
                                           'title': f.title,
                                           'description': f.description,
                                           'releaseYear': f.release_year,
                                           'language': json_object(
                                               'id': l1.language_id,
                                               'name': l1.name,
                                               'lastUpdate': l1.last_update
                                           ),
                                           'originalLanguage':
                                               case
                                                   when f.original_language_id is null
                                                   then null
                                                   else json_object(
                                                            'id': l2.language_id,
                                                            'name': l2.name,
                                                            'lastUpdate': l2.last_update
                                                        )
                                               end,
                                           'rentalDuration': f.rental_duration,
                                           'rentalRate': f.rental_rate,
                                           'length': f.length,
                                           'replacementCost': f.replacement_cost,
                                           'rating': f.rating,
                                           'lastUpdate': f.last_update,
                                           'specialFeatures': json_array(),
                                           'fullText': ''
                                       ),
                                       'actors': json_array(
                                           select json_object(
                                                      'id': a.actor_id,
                                                      'firstName': a.first_name,
                                                      'lastName': a.last_name,
                                                      'lastUpdate': a.last_update
                                                  )
                                             from film_actor fa
                                            inner join actor a on (fa.actor_id = a.actor_id)
                                            where fa.film_id = f.film_id
                                       ),
                                       'categories': json_array(
                                           select json_object(
                                                      'id': c.category_id,
                                                      'name': c.name,
                                                      'lastUpdate': c.last_update
                                                  )
                                             from film_category fc
                                            inner join category c on (fc.category_id = c.category_id)
                                            where fc.film_id = f.film_id
                                       )
                                   )
                              from Film f
                              left join Language l1 on (f.language_id = l1.language_id)
                              left join Language l2 on (f.original_language_id = l2.language_id)
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Film.WithActorsAndCategories.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
