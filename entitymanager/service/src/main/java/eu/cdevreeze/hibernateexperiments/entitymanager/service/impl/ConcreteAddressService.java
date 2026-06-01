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
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Address;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.City;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Country;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.AddressService;

/**
 * Concrete {@link AddressService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressService implements AddressService {

    // TODO Method TypedQuery.setEntityGraph confuses me. It is in the (current) JPA 4.0 spec.
    // Yet it is not in the (current) JPA 4.0 API documentation.
    // Also, what does it mean with "returning only one result"? What I did below still seems to work in avoiding the 1 + N problem.

    private final EntityManagerFactory emf;

    public ConcreteAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setEntityGraph(entityGraph)
                    .setParameter(1, id)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.city.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setEntityGraph(entityGraph)
                    .setParameter(1, cityId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad where ad.city.country.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setEntityGraph(entityGraph)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select ad from Address ad";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select c from City c where c.country.id = ?1";

            EntityGraph<CityEntity> entityGraph = getCityEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, CityEntity.class)
                    .setEntityGraph(entityGraph)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select c from Country c";

            EntityGraph<CountryEntity> entityGraph = getCountryEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityManager.createQuery(qlString, CountryEntity.class)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<AddressEntity> getAddressEntityGraph() {
        EntityGraph<AddressEntity> entityGraph = AddressEntity_.class_.createEntityGraph();

        entityGraph.addAttributeNode(AddressEntity_.city);

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<CityEntity> citySubgraph = entityGraph.addSubgraph(AddressEntity_.city);
        citySubgraph.addAttributeNode(CityEntity_.country);

        return entityGraph;
    }

    private EntityGraph<CityEntity> getCityEntityGraph() {
        EntityGraph<CityEntity> entityGraph = CityEntity_.class_.createEntityGraph();
        entityGraph.addAttributeNode(CityEntity_.country);
        return entityGraph;
    }

    private EntityGraph<CountryEntity> getCountryEntityGraph() {
        return CountryEntity_.class_.createEntityGraph();
    }
}
