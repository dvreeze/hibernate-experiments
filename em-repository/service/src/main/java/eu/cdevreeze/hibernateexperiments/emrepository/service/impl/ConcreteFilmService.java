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

package eu.cdevreeze.hibernateexperiments.emrepository.service.impl;

import module eu.cdevreeze.hibernateexperiments.emrepository.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.emrepository.model.Film;
import eu.cdevreeze.hibernateexperiments.emrepository.repo.FilmRepository;
import eu.cdevreeze.hibernateexperiments.emrepository.repo._FilmRepository;
import eu.cdevreeze.hibernateexperiments.emrepository.service.FilmService;

/**
 * Concrete {@link FilmService} implementation.
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
        return emf.callInTransaction(entityManager -> {
            FilmRepository filmRepository = new _FilmRepository(entityManager);
            return filmRepository.findAllFilms()
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Optional<Film> findFilm(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            FilmRepository filmRepository = new _FilmRepository(entityManager);
            return filmRepository.findFilm((int) filmId).map(FilmEntity::toModelObject);
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            FilmRepository filmRepository = new _FilmRepository(entityManager);
            return filmRepository.findFilmsByActorId((int) actorId)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
