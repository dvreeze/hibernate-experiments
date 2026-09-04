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
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmActorEntity_;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmCategoryEntity_;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmEntity_;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Film;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.FlushModeType;

/**
 * Concrete {@link FilmService} implementation, clearing the session to prevent flushing data.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteFilmServiceUsingClearedSession implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmServiceUsingClearedSession(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            entityManager.addOption(FlushModeType.EXPLICIT); // strictly not needed, but does not hurt

            String qlString = "select f from Film f";

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            ImmutableList<Film> result = entityManager.createQuery(qlString, getEntityGraph())
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
            entityManager.clear();
            return result;
        });
    }

    @Override
    public Optional<Film> findFilm(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            entityManager.addOption(FlushModeType.EXPLICIT); // strictly not needed, but does not hurt

            String qlString = "select f from Film f where f.id = ?1";

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            Optional<Film> result = entityManager.createQuery(qlString, getEntityGraph())
                    .setParameter(1, filmId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .min(Comparator.comparingLong(Film::id));
            entityManager.clear();
            return result;
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            entityManager.addOption(FlushModeType.EXPLICIT); // strictly not needed, but does not hurt

            String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            ImmutableList<Film> result = entityManager.createQuery(qlString, getEntityGraph())
                    .setParameter(1, actorId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
            entityManager.clear();
            return result;
        });
    }

    private EntityGraph<FilmEntity> getEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }
}
