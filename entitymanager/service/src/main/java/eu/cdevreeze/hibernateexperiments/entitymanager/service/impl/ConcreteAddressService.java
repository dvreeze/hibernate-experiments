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
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.*;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.AddressService;

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
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getAddressEntityGraph())
                    .setParameter(1, id)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.city.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getAddressEntityGraph())
                    .setParameter(1, cityId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.city.country.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getAddressEntityGraph())
                    .setParameter(1, countryId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getAddressEntityGraph())
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select c from City c where c.country.id = ?1";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getCityEntityGraph())
                    .setParameter(1, countryId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select c from Country c";

            // Note that the retrieval of managed JPA entities below causes "flushing" overhead, although there is no dirty state to flush

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, getCountryEntityGraph())
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Address add(Address.NewAddress address) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CityEntity cityEntity = findCityEntityById((int) address.cityId(), entityManager);

            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setAddress(address.address1());
            addressEntity.setAddress2(address.address2());
            addressEntity.setDistrict(address.district());
            addressEntity.setCity(cityEntity);
            addressEntity.setPostalCode(address.postalCode());
            addressEntity.setPhone(address.phone());
            addressEntity.setLastUpdate(address.lastUpdate());

            entityManager.persist(addressEntity);
            return addressEntity.toModelObject();
        });
    }

    private CityEntity findCityEntityById(int cityId, EntityManager entityManager) {
        String qlString = "select ci from City ci join fetch ci.country co where ci.id = :id";

        return entityManager.createQuery(qlString, CityEntity.class)
                .setParameter("id", cityId)
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
