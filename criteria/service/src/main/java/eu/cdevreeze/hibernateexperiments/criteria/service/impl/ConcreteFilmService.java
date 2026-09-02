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

package eu.cdevreeze.hibernateexperiments.criteria.service.impl;

import module jakarta.persistence;
import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.criteria.entity.*;
import eu.cdevreeze.hibernateexperiments.criteria.model.Film;
import eu.cdevreeze.hibernateexperiments.criteria.service.FilmService;
import jakarta.persistence.criteria.JoinType;

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
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

            Root<FilmEntity> film = cq.from(FilmEntity.class);
            cq.select(film);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getEntityGraph()) // Not type-safe
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Optional<Film> findFilm(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

            Root<FilmEntity> film = cq.from(FilmEntity.class);
            cq.where(cb.equal(film.get(FilmEntity_.id), filmId));
            cq.select(film);

            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getEntityGraph()) // Not type-safe
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .min(Comparator.comparingLong(Film::id));
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

            Root<FilmEntity> film = cq.from(FilmEntity.class);
            SetJoin<FilmEntity, FilmActorEntity> filmActor = film.join(FilmEntity_.filmActors, JoinType.LEFT);
            cq.where(cb.equal(filmActor.get(FilmActorEntity_.actor).get(ActorEntity_.id), actorId));
            cq.select(film);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getEntityGraph()) // Not type-safe
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
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
