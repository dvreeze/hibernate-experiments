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

import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.criteria.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.criteria.entity.AddressEntity_;
import eu.cdevreeze.hibernateexperiments.criteria.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.criteria.entity.CityEntity_;
import eu.cdevreeze.hibernateexperiments.criteria.entity.CountryEntity;
import eu.cdevreeze.hibernateexperiments.criteria.entity.CountryEntity_;
import eu.cdevreeze.hibernateexperiments.criteria.model.Address;
import eu.cdevreeze.hibernateexperiments.criteria.model.City;
import eu.cdevreeze.hibernateexperiments.criteria.model.Country;
import eu.cdevreeze.hibernateexperiments.criteria.service.AddressService;

/**
 * Concrete {@link AddressService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressService implements AddressService {

    // TODO Use Criteria API

    private final EntityManagerFactory emf;

    public ConcreteAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select ad from Address ad where ad.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .setParameter(1, id)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select ad from Address ad where ad.city.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .setParameter(1, cityId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select ad from Address ad where ad.city.country.id = ?1";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select ad from Address ad";

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select c from City c where c.couontry.id = ?1";

            EntityGraph<CityEntity> entityGraph = getCityEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String qlString = "select c from Country";

            EntityGraph<CountryEntity> entityGraph = getCountryEntityGraph(entityAgent);

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(qlString, entityGraph)
                    .getResultStream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private EntityGraph<AddressEntity> getAddressEntityGraph(EntityHandler entityHandler) {
        EntityGraph<AddressEntity> entityGraph = AddressEntity_.class_.createEntityGraph();

        entityGraph.addAttributeNode(AddressEntity_.city);

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        Subgraph<CityEntity> citySubgraph = entityGraph.addSubgraph(AddressEntity_.city);
        citySubgraph.addAttributeNode(CityEntity_.country);

        return entityGraph;
    }

    private EntityGraph<CityEntity> getCityEntityGraph(EntityHandler entityHandler) {
        EntityGraph<CityEntity> entityGraph = CityEntity_.class_.createEntityGraph();
        entityGraph.addAttributeNode(CityEntity_.country);
        return entityGraph;
    }

    private EntityGraph<CountryEntity> getCountryEntityGraph(EntityHandler entityHandler) {
        return CountryEntity_.class_.createEntityGraph();
    }
}
