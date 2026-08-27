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
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.*;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Film;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;

import java.util.Optional;

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
            String qlString = "select f from Film f";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getEntityGraph())
                    .getResultStream()
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

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getEntityGraph())
                    .setParameter(1, filmId)
                    .getResultStream()
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

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getEntityGraph())
                    .setParameter(1, actorId)
                    .getResultStream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<FilmEntity> getEntityGraph() {
        EntityGraph<FilmEntity> entityGraph = FilmEntity_.class_.createEntityGraph();
        entityGraph.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        entityGraph.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        entityGraph.addAttributeNode(FilmEntity_.language);
        entityGraph.addAttributeNode(FilmEntity_.originalLanguage);
        return entityGraph;
    }
}
