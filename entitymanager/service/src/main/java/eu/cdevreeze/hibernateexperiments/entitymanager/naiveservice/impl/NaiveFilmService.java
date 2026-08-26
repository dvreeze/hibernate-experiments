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

package eu.cdevreeze.hibernateexperiments.entitymanager.naiveservice.impl;

import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.naiveservice.FilmService;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

/**
 * Inefficient {@link FilmService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class NaiveFilmService implements FilmService {

    private final EntityManagerFactory emf;

    public NaiveFilmService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<FilmEntity> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .getResultList() // to be on the safe side
                    .stream()
                    .sorted(Comparator.comparingLong(FilmEntity::getId))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Optional<FilmEntity> findFilm(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f where f.id = ?1";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, filmId)
                    .getResultList() // to be on the safe side
                    .stream()
                    .min(Comparator.comparingLong(FilmEntity::getId));
        });
    }

    @Override
    public ImmutableList<FilmEntity> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setParameter(1, actorId)
                    .getResultList() // to be on the safe side
                    .stream()
                    .sorted(Comparator.comparingLong(FilmEntity::getId))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
