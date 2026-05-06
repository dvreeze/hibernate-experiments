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

package eu.cdevreeze.hibernateexperiments.plainsql.service.impl;

import module eu.cdevreeze.hibernateexperiments.plainsql.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.plainsql.service.ActorService;

import static jakarta.persistence.sql.ResultSetMapping.column;
import static jakarta.persistence.sql.ResultSetMapping.constructor;

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
            String sqlString = """
                    select act.actor_id, act.first_name, act.last_name, act.last_update as act_last_update
                      from actor act
                     where act.actor_id = ?1;
                    """;

            ResultSetMapping<Actor> rsMapping = getActorResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Actor> findByFilmId(long filmId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String sqlString = """
                    select act.actor_id, act.first_name, act.last_name, act.last_update as act_last_update
                      from actor act
                     inner join film_actor fa on fa.actor_id = act.actor_id
                     where fa.film_id = ?1;
                    """;

            ResultSetMapping<Actor> rsMapping = getActorResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, filmId)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Actor> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String sqlString = """
                    select act.actor_id, act.first_name, act.last_name, act.last_update as act_last_update
                      from actor act;
                    """;

            ResultSetMapping<Actor> rsMapping = getActorResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private static ConstructorMapping<Actor> getActorResultSetMapping() {
        return constructor(
                Actor.class,
                column("actor_id", Long.class),
                column("first_name", String.class),
                column("last_name", String.class),
                column("act_last_update", Instant.class)
        );
    }
}
