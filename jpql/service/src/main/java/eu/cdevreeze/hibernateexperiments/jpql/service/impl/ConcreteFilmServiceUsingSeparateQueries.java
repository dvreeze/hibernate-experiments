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

import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.entity.*;
import eu.cdevreeze.hibernateexperiments.jpql.model.Film;
import eu.cdevreeze.hibernateexperiments.jpql.service.FilmService;
import jakarta.persistence.*;

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
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            java.util.List<FilmEntity> filmEntities = findAllFilms(getFilmActorsEntityGraph(), entityAgent);

            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findAllFilms(getFilmCategoriesEntityGraph(), entityAgent)
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
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            java.util.List<FilmEntity> filmEntities = findFilm(filmId, getFilmActorsEntityGraph(), entityAgent)
                    .stream()
                    .toList();

            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilm(filmId, getFilmCategoriesEntityGraph(), entityAgent)
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
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            java.util.List<FilmEntity> filmEntities = findFilmsByActorId(actorId, getFilmActorsEntityGraph(), entityAgent);

            java.util.Map<Integer, FilmEntity> filmEntityWithCategoriesMap =
                    findFilmsByActorId(actorId, getFilmCategoriesEntityGraph(), entityAgent)
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

    private ImmutableList<FilmEntity> findAllFilms(EntityGraph<FilmEntity> entityGraph, EntityAgent entityAgent) {
        String qlString = "select f from Film f";

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityAgent.createQuery(qlString, FilmEntity.class)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                .getResultList() // to be on the safe side
                .stream()
                .collect(ImmutableList.toImmutableList());
    }

    private Optional<FilmEntity> findFilm(long filmId, EntityGraph<FilmEntity> entityGraph, EntityAgent entityAgent) {
        String qlString = "select f from Film f where f.id = ?1";

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        // to be on the safe side
        return entityAgent.createQuery(qlString, FilmEntity.class)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                .setParameter(1, filmId)
                .getResultList() // to be on the safe side
                .stream()
                .min(Comparator.comparingLong(FilmEntity::getId));
    }

    private ImmutableList<FilmEntity> findFilmsByActorId(long actorId, EntityGraph<FilmEntity> entityGraph, EntityAgent entityAgent) {
        String qlString = "select f from Film f left join f.filmActors fa where fa.actor.id = ?1";

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityAgent.createQuery(qlString, FilmEntity.class)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                .setParameter(1, actorId)
                .getResultList() // to be on the safe side
                .stream()
                .collect(ImmutableList.toImmutableList());
    }

    private EntityGraph<FilmEntity> getFilmActorsEntityGraph() {
        EntityGraph<FilmEntity> entityGraph = FilmEntity_.class_.createEntityGraph();

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<FilmActorEntity> filmActorSubgraph = entityGraph.addElementSubgraph(FilmEntity_.filmActors);
        filmActorSubgraph.addAttributeNode(FilmActorEntity_.actor);

        entityGraph.addAttributeNode(FilmEntity_.language);
        entityGraph.addAttributeNode(FilmEntity_.originalLanguage);

        return entityGraph;
    }

    private EntityGraph<FilmEntity> getFilmCategoriesEntityGraph() {
        EntityGraph<FilmEntity> entityGraph = FilmEntity_.class_.createEntityGraph();

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<FilmCategoryEntity> filmCategorySubgraph = entityGraph.addElementSubgraph(FilmEntity_.filmCategories);
        filmCategorySubgraph.addAttributeNode(FilmCategoryEntity_.category);

        return entityGraph;
    }
}
