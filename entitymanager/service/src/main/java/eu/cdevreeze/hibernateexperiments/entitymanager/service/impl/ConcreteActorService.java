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

import module eu.cdevreeze.hibernateexperiments.entitymanager.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.ActorEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.ActorEntity_;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.ActorService;

/**
 * Concrete {@link ActorService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteActorService implements ActorService {

    private final EntityManagerFactory emf;

    public ConcreteActorService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Actor> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select act from Actor act where act.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getActorEntityGraph())
                    .setParameter(1, id)
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Actor> findByFilmId(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // This shows that JPQL is essentially an elegant object-oriented SQL dialect
            // This is even the case without path expressions to navigate between associations
            String qlString = """
                    select act
                      from Actor act
                     inner join FilmActor fa on fa.actor.id = act.id
                     where fa.film.id = ?1
                    """;

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getActorEntityGraph())
                    .setParameter(1, filmId)
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Actor> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select act from Actor act";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getActorEntityGraph())
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private static EntityGraph<ActorEntity> getActorEntityGraph() {
        return ActorEntity_.class_.createEntityGraph();
    }
}
