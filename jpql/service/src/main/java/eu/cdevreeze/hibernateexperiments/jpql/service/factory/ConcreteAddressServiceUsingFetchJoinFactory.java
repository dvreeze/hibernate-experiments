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

package eu.cdevreeze.hibernateexperiments.jpql.service.factory;

import module java.base;
import eu.cdevreeze.hibernateexperiments.jpql.service.AddressService;
import eu.cdevreeze.hibernateexperiments.jpql.service.impl.ConcreteAddressServiceUsingFetchJoin;
import jakarta.persistence.EntityManagerFactory;

/**
 * Factory of {@link ConcreteAddressServiceUsingFetchJoin} objects.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressServiceUsingFetchJoinFactory {

    private ConcreteAddressServiceUsingFetchJoinFactory() {
        // Non-instantiable
    }

    public static AddressService create(EntityManagerFactory emf) {
        return new ConcreteAddressServiceUsingFetchJoin(emf);
    }
}
