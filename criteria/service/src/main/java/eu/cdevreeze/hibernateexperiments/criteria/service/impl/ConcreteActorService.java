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

import module eu.cdevreeze.hibernateexperiments.criteria.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.criteria.entity.*;
import eu.cdevreeze.hibernateexperiments.criteria.service.ActorService;
import org.hibernate.jpa.SpecHints;

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
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<ActorEntity> cq = cb.createQuery(ActorEntity.class);

            Root<ActorEntity> actor = cq.from(ActorEntity.class);
            cq.where(cb.equal(actor.get(ActorEntity_.id), id));
            cq.select(actor);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getActorEntityGraph())
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Actor> findByFilmId(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // This shows that JPQL/Criteria is essentially an elegant object-oriented SQL dialect
            // This is even the case without path expressions to navigate between associations

            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<ActorEntity> cq = cb.createQuery(ActorEntity.class);

            Root<ActorEntity> actor = cq.from(ActorEntity.class);
            Join<ActorEntity, FilmActorEntity> filmActor = actor.join(FilmActorEntity.class, JoinType.INNER);
            filmActor.on(cb.equal(actor.get(ActorEntity_.id), filmActor.get(FilmActorEntity_.actor).get(ActorEntity_.id)));
            cq.where(cb.equal(filmActor.get(FilmActorEntity_.film).get(FilmEntity_.id), filmId));
            cq.select(actor);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getActorEntityGraph())
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Actor> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<ActorEntity> cq = cb.createQuery(ActorEntity.class);

            Root<ActorEntity> actor = cq.from(ActorEntity.class);
            cq.select(actor);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getActorEntityGraph())
                    .getResultStream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private static EntityGraph<ActorEntity> getActorEntityGraph() {
        return ActorEntity_.class_.createEntityGraph();
    }
}
