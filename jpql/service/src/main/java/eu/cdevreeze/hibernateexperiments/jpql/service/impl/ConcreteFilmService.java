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

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.model.Film;
import eu.cdevreeze.hibernateexperiments.jpql.service.FilmService;
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
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
