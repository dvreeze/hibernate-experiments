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

package eu.cdevreeze.hibernateexperiments.emrepository.service.impl;

import module eu.cdevreeze.hibernateexperiments.emrepository.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.ActorEntity;
import eu.cdevreeze.hibernateexperiments.emrepository.model.Actor;
import eu.cdevreeze.hibernateexperiments.emrepository.repo.ActorRepository;
import eu.cdevreeze.hibernateexperiments.emrepository.repo._ActorRepository;
import eu.cdevreeze.hibernateexperiments.emrepository.service.ActorService;

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
            ActorRepository actorRepository = new _ActorRepository(entityManager);
            return actorRepository.findById((int) id).map(ActorEntity::toModelObject);
        });
    }

    @Override
    public ImmutableList<Actor> findByFilmId(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            ActorRepository actorRepository = new _ActorRepository(entityManager);
            return actorRepository.findByFilmId((int) filmId)
                    .stream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Actor> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            ActorRepository actorRepository = new _ActorRepository(entityManager);
            return actorRepository.findAllActors()
                    .stream()
                    .map(ActorEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
