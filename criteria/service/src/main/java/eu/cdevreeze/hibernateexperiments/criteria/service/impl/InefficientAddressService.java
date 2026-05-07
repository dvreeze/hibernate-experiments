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
import eu.cdevreeze.hibernateexperiments.criteria.service.AddressService;

/**
 * The same as {@link ConcreteAddressService}, except for the absence of {@link EntityGraph}'s.
 * This minor code change alone makes the number of generated SQL queries explode!
 *
 * @author Chris de Vreeze
 */
public final class InefficientAddressService implements AddressService {

    // TODO Method TypedQuery.setEntityGraph confuses me. It is in the (current) JPA 4.0 spec.
    // Yet it is not in the (current) JPA 4.0 API documentation.
    // Also, what does it mean with "returning only one result"? What I did below still seems to work in avoiding the 1 + N problem.

    private final EntityManagerFactory emf;

    public InefficientAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.id), id));
            cq.select(address);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.id), cityId));
            cq.select(address);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.country).get(CountryEntity_.id), countryId));
            cq.select(address);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.select(address);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<CityEntity> cq = cb.createQuery(CityEntity.class);

            Root<CityEntity> city = cq.from(CityEntity.class);
            cq.where(cb.equal(city.get(CityEntity_.country).get(CountryEntity_.id), countryId));
            cq.select(city);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<CountryEntity> cq = cb.createQuery(CountryEntity.class);

            Root<CountryEntity> country = cq.from(CountryEntity.class);
            cq.select(country);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
