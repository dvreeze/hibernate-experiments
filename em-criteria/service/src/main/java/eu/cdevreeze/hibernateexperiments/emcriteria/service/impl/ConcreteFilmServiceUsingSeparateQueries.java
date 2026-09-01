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

package eu.cdevreeze.hibernateexperiments.emcriteria.service.impl;

import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emcriteria.entity.*;
import eu.cdevreeze.hibernateexperiments.emcriteria.model.Film;
import eu.cdevreeze.hibernateexperiments.emcriteria.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.criteria.*;

import java.util.Optional;

/**
 * Concrete {@link FilmService} implementation that uses multiple JPQL queries in order to prevent
 * a {@link MultipleBagFetchException} in an effective way.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteFilmServiceUsingSeparateQueries implements FilmService {

    private final EntityManagerFactory emf;

    public ConcreteFilmServiceUsingSeparateQueries(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public ImmutableList<Film> findAllFilms() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            java.util.List<FilmEntity> filmEntities = findAllFilms(getFilmActorsEntityGraph(), entityManager);

            // TODO Not like this!
            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findAllFilms(getFilmCategoriesEntityGraph(), entityManager)
                            .stream()
                            .collect(Collectors.toMap(FilmEntity::getId, Function.identity()));

            filmEntities.forEach(filmEntity -> filmEntity.setFilmCategories(
                    Optional.ofNullable(filmEntityWithCategoriesMap.get(filmEntity.getId()))
                            .map(FilmEntity::getFilmCategories)
                            .orElse(java.util.Set.of())
            ));

            return filmEntities
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
            java.util.List<FilmEntity> filmEntities = findFilm(filmId, getFilmActorsEntityGraph(), entityManager)
                    .stream()
                    .toList();

            // TODO Not like this!
            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilm(filmId, getFilmCategoriesEntityGraph(), entityManager)
                            .stream()
                            .collect(Collectors.toMap(FilmEntity::getId, Function.identity()));

            filmEntities.forEach(filmEntity -> filmEntity.setFilmCategories(
                    Optional.ofNullable(filmEntityWithCategoriesMap.get(filmEntity.getId()))
                            .map(FilmEntity::getFilmCategories)
                            .orElse(java.util.Set.of())
            ));

            return filmEntities
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .min(Comparator.comparingLong(Film::id));
        });
    }

    @Override
    public ImmutableList<Film> findFilmsByActorId(long actorId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            java.util.List<FilmEntity> filmEntities = findFilmsByActorId(actorId, getFilmActorsEntityGraph(), entityManager);

            // TODO Not like this!
            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilmsByActorId(actorId, getFilmCategoriesEntityGraph(), entityManager)
                            .stream()
                            .collect(Collectors.toMap(FilmEntity::getId, Function.identity()));

            filmEntities.forEach(filmEntity -> filmEntity.setFilmCategories(
                    Optional.ofNullable(filmEntityWithCategoriesMap.get(filmEntity.getId()))
                            .map(FilmEntity::getFilmCategories)
                            .orElse(java.util.Set.of())
            ));

            return filmEntities
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private ImmutableList<FilmEntity> findAllFilms(EntityGraph<FilmEntity> eg, EntityManager entityManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> film = cq.from(FilmEntity.class);
        cq.select(film);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityManager.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, eg) // Not type-safe
                .getResultStream()
                .collect(ImmutableList.toImmutableList());
    }

    private Optional<FilmEntity> findFilm(long filmId, EntityGraph<FilmEntity> eg, EntityManager entityManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> film = cq.from(FilmEntity.class);
        cq.where(cb.equal(film.get(FilmEntity_.id), filmId));
        cq.select(film);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityManager.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, eg) // Not type-safe
                .getResultStream()
                .min(Comparator.comparingLong(FilmEntity::getId));
    }

    private ImmutableList<FilmEntity> findFilmsByActorId(long actorId, EntityGraph<FilmEntity> eg, EntityManager entityManager) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<FilmEntity> cq = cb.createQuery(FilmEntity.class);

        Root<FilmEntity> film = cq.from(FilmEntity.class);
        SetJoin<FilmEntity, FilmActorEntity> filmActor = film.join(FilmEntity_.filmActors, JoinType.LEFT);
        cq.where(cb.equal(filmActor.get(FilmActorEntity_.actor).get(ActorEntity_.id), actorId));
        cq.select(film);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityManager.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, eg) // Not type-safe
                .getResultStream()
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<FilmEntity> getFilmActorsEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmActors).addAttributeNode(FilmActorEntity_.actor);
        eg.addAttributeNode(FilmEntity_.language);
        eg.addAttributeNode(FilmEntity_.originalLanguage);
        return eg;
    }

    private EntityGraph<FilmEntity> getFilmCategoriesEntityGraph() {
        EntityGraph<FilmEntity> eg = FilmEntity_.class_.createEntityGraph();
        eg.addElementSubgraph(FilmEntity_.filmCategories).addAttributeNode(FilmCategoryEntity_.category);
        return eg;
    }
}
