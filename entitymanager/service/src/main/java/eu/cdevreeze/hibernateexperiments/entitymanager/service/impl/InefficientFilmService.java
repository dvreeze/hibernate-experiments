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

package eu.cdevreeze.hibernateexperiments.entitymanager.service.impl;

import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Film;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

/**
 * The same as {@link ConcreteFilmService}, except for the absence of {@link EntityGraph}'s.
 * This minor code change alone makes the number of generated SQL queries explode!
 *
 * @author Chris de Vreeze
 */
public final class InefficientFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public InefficientFilmService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .getResultList() // to be on the safe side
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
            String qlString = "select f from Film f where f.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, filmId)
                    .getResultList() // to be on the safe side
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .min(Comparator.comparingLong(Film::id));
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, actorId)
                    .getResultList() // to be on the safe side
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
