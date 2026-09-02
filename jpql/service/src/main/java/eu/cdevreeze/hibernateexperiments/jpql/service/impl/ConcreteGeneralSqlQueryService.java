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

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.service.GeneralSqlQueryService;
import jakarta.persistence.EntityAgent;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.TypedQuery;

import java.util.stream.IntStream;

/**
 * Concrete {@link GeneralSqlQueryService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteGeneralSqlQueryService implements GeneralSqlQueryService {

    private final EntityManagerFactory emf;

    public ConcreteGeneralSqlQueryService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public <T> ImmutableList<T> executeSqlQuery(String qlString, Class<T> resultClass, ImmutableList<Object> args) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            TypedQuery<T> query = entityAgent.createNativeQuery(qlString, resultClass);

            IntStream.range(0, args.size()).forEach(idx -> query.setParameter(1 + idx, args.get(idx)));

            return query.getResultList() // works better than getResultStream (no duplicates)
                    .stream().collect(ImmutableList.toImmutableList());
        });
    }
}
