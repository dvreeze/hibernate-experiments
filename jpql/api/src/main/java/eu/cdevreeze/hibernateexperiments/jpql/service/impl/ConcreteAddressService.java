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

import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.entity.*;
import eu.cdevreeze.hibernateexperiments.jpql.model.Address;
import eu.cdevreeze.hibernateexperiments.jpql.model.City;
import eu.cdevreeze.hibernateexperiments.jpql.model.Country;
import eu.cdevreeze.hibernateexperiments.jpql.service.AddressService;

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
        EntityGraph<AddressEntity> entityGraph = entityHandler.createEntityGraph(AddressEntity.class);

        entityGraph.addAttributeNode(AddressEntity_.CITY);

        // Be careful: type SubGraph is Hibernate-specific, whereas type Subgraph is part of JPA
        entityGraph.addSubgraph(AddressEntity_.CITY, CityEntity.class);

        return entityGraph;
    }

    private EntityGraph<CityEntity> getCityEntityGraph(EntityHandler entityHandler) {
        EntityGraph<CityEntity> entityGraph = entityHandler.createEntityGraph(CityEntity.class);
        entityGraph.addAttributeNode(CityEntity_.COUNTRY);
        return entityGraph;
    }

    private EntityGraph<CountryEntity> getCountryEntityGraph(EntityHandler entityHandler) {
        return entityHandler.createEntityGraph(CountryEntity.class);
    }
}
