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
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.criteria.entity.*;
import eu.cdevreeze.hibernateexperiments.criteria.service.AddressService;
import jakarta.persistence.criteria.JoinType;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

/**
 * Alternative {@link AddressService} implementation that uses CTEs (and JSON result sets) internally.
 *
 * @author Chris de Vreeze
 */
public final class AlternativeAddressService implements AddressService {

    // For nested JSON results with JSON objects and nested arrays, see https://forums.oracle.com/ords/apexds/post/complex-nested-json-structure-8286
    // This is interesting for queries returning films and their actors

    // Guava Jackson Module not needed here
    private final JsonMapper jsonMapper = JsonMapper.builder()
            .build();

    private final EntityManagerFactory emf;

    public AlternativeAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    // TODO
    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.id), id));
            cq.select(address);

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    // TODO
    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.id), cityId));
            cq.select(address);

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    // TODO
    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.country).get(CountryEntity_.id), countryId));
            cq.select(address);

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    // TODO
    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            CriteriaBuilder cb = entityAgent.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.select(address);

            EntityGraph<AddressEntity> entityGraph = getAddressEntityGraph();

            // This sets the load graph, not the fetch graph
            // Yet that makes no difference here since we configured lazy fetching for all entity associations
            return entityAgent.createQuery(cq)
                    .setEntityGraph(entityGraph)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            HibernateCriteriaBuilder cb = entityAgent.unwrap(StatelessSession.class).getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cteCq = cb.createQuery(Tuple.class);
            JpaRoot<CityEntity> city = cteCq.from(CityEntity.class);
            JpaJoin<CityEntity, CountryEntity> cityCountry = city.join(CityEntity_.country, JoinType.INNER);
            cteCq.where(cb.equal(cityCountry.get(CountryEntity_.id), countryId));
            cteCq.select(cb.tuple(
                    city.get(CityEntity_.id).alias("cityId"),
                    city.get(CityEntity_.city).alias("city"),
                    cityCountry.get(CountryEntity_.id).alias("countryId"),
                    cityCountry.get(CountryEntity_.country).alias("countryName"),
                    cityCountry.get(CountryEntity_.lastUpdate).alias("lastUpdateTime"),
                    city.get(CityEntity_.lastUpdate).alias("lastUpdate")
            ));

            JpaCriteriaQuery<String> cq = cb.createQuery(String.class);
            JpaCteCriteria<Tuple> cteCriteria = cq.with("Cit", cteCq);
            JpaRoot<Tuple> tupleFromCte = cq.from(cteCriteria);
            cq.select(
                    cb.jsonObject(
                            Map.of(
                                    "id", tupleFromCte.get("cityId"),
                                    "city", tupleFromCte.get("city"),
                                    "country", cb.jsonObject(
                                            Map.of(
                                                    "id", tupleFromCte.get("countryId"),
                                                    "country", tupleFromCte.get("countryName"),
                                                    "lastUpdate", tupleFromCte.get("lastUpdateTime")
                                            )
                                    ),
                                    "lastUpdate", tupleFromCte.get("lastUpdate")
                            )
                    )
            );

            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, City.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            HibernateCriteriaBuilder cb = entityAgent.unwrap(StatelessSession.class).getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cteCq = cb.createQuery(Tuple.class);
            JpaRoot<CountryEntity> country = cteCq.from(CountryEntity.class);
            cteCq.select(cb.tuple(
                    country.get(CountryEntity_.id).alias("countryId"),
                    country.get(CountryEntity_.country).alias("countryName"),
                    country.get(CountryEntity_.lastUpdate).alias("lastUpdateTime")
            ));

            JpaCriteriaQuery<String> cq = cb.createQuery(String.class);
            JpaCteCriteria<Tuple> cteCriteria = cq.with("Cty", cteCq);
            JpaRoot<Tuple> tupleFromCte = cq.from(cteCriteria);
            cq.select(
                    cb.jsonObject(
                            Map.of(
                                    "id", tupleFromCte.get("countryId"),
                                    "country", tupleFromCte.get("countryName"),
                                    "lastUpdate", tupleFromCte.get("lastUpdateTime")
                            )
                    )
            );

            return entityAgent.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Country.class))
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
}
