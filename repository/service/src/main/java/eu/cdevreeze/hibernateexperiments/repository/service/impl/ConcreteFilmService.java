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

package eu.cdevreeze.hibernateexperiments.repository.service.impl;

import module eu.cdevreeze.hibernateexperiments.repository.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.repository.repo.FilmRepository;
import eu.cdevreeze.hibernateexperiments.repository.repo._FilmRepository;
import eu.cdevreeze.hibernateexperiments.repository.service.FilmService;

/**
 * Concrete {@link FilmService} implementation.
 * <p>
 * Unfortunately I do not yet know how to pass system property "hibernate.query.hql.json_functions_enabled" with value "true" to
 * the Hibernate (annotation) processor. So compile-time parsing of JDQL with JSON is nto working yet.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            FilmRepository filmRepository = new _FilmRepository(entityAgent);
            return filmRepository.findAllFilms()
                    .stream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Optional<Film> findFilm(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            FilmRepository filmRepository = new _FilmRepository(entityAgent);
            return filmRepository.findFilm(filmId);
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            FilmRepository filmRepository = new _FilmRepository(entityAgent);
            return filmRepository.findFilmsByActorId(actorId)
                    .stream()
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
