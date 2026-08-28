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
import org.hibernate.jpa.SpecHints;

/**
 * Concrete {@link AddressService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    public ConcreteAddressService(EntityManagerFactory emf) {
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getAddressEntityGraph()) // Not type-safe
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getAddressEntityGraph()) // Not type-safe
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getAddressEntityGraph()) // Not type-safe
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getAddressEntityGraph()) // Not type-safe
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getCityEntityGraph()) // Not type-safe
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
                    .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getCountryEntityGraph()) // Not type-safe
                    .getResultStream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Address add(Address.NewAddress address) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CityEntity cityEntity = findCityEntityById((int) address.cityId(), entityAgent);

            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setAddress(address.address1());
            addressEntity.setAddress2(address.address2());
            addressEntity.setDistrict(address.district());
            addressEntity.setCity(cityEntity);
            addressEntity.setPostalCode(address.postalCode());
            addressEntity.setPhone(address.phone());
            addressEntity.setLastUpdate(address.lastUpdate());

            entityAgent.insert(addressEntity);
            return addressEntity.toModelObject();
        });
    }

    private CityEntity findCityEntityById(int cityId, EntityAgent entityAgent) {
        CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
        CriteriaQuery<CityEntity> cq = cb.createQuery(CityEntity.class);

        Root<CityEntity> city = cq.from(CityEntity.class);
        cq.where(cb.equal(city.get(CityEntity_.id), cityId));
        cq.select(city);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityAgent.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, getCityEntityGraph()) // Not type-safe
                .getSingleResult();
    }

    private EntityGraph<AddressEntity> getAddressEntityGraph() {
        EntityGraph<AddressEntity> eg = AddressEntity_.class_.createEntityGraph();
        eg.addSubgraph(AddressEntity_.city).addAttributeNode(CityEntity_.country);
        return eg;
    }

    private EntityGraph<CityEntity> getCityEntityGraph() {
        EntityGraph<CityEntity> eg = CityEntity_.class_.createEntityGraph();
        eg.addAttributeNode(CityEntity_.country);
        return eg;
    }

    private EntityGraph<CountryEntity> getCountryEntityGraph() {
        return CountryEntity_.class_.createEntityGraph();
    }
}
