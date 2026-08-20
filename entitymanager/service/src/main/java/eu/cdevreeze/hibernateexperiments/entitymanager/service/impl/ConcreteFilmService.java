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
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.*;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Film;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.FilmService;
import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Subgraph;

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

            EntityGraph<FilmEntity> entityGraph = getEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
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

            EntityGraph<FilmEntity> entityGraph = getEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            // to be on the safe side
            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
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
            String qlString = "select f from Film f left join FilmActor fa on (f.id = fa.film.id) where fa.actor.id = ?1";

            EntityGraph<FilmEntity> entityGraph = getEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, FilmEntity.class)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                    .setParameter(1, actorId)
                    .getResultList() // to be on the safe side
                    .stream()
                    .map(FilmEntity::toModelObject)
                    .sorted(Comparator.comparingLong(Film::id))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<FilmEntity> getEntityGraph() {
        EntityGraph<FilmEntity> entityGraph = FilmEntity_.class_.createEntityGraph();

        entityGraph.addAttributeNode("filmActors");

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<FilmActorEntity> filmActorSubgraph = entityGraph.addSubgraph("filmActors");
        filmActorSubgraph.addAttributeNode(FilmActorEntity_.actor);

        entityGraph.addAttributeNode("filmCategories");

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<FilmCategoryEntity> filmCategorySubgraph = entityGraph.addSubgraph("filmCategories");
        filmCategorySubgraph.addAttributeNode(FilmCategoryEntity_.category);

        return entityGraph;
    }
}
