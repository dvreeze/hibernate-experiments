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

package eu.cdevreeze.hibernateexperiments.emcriteria.service.impl;

import module eu.cdevreeze.hibernateexperiments.emcriteria.model;
import module jakarta.persistence;
import module java.base;
import module org.hibernate.orm.core;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emcriteria.entity.*;
import eu.cdevreeze.hibernateexperiments.emcriteria.service.AddressService;
import jakarta.persistence.criteria.JoinType;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

import java.util.Map;
import java.util.Optional;

/**
 * Alternative {@link AddressService} implementation that uses CTEs (and JSON result sets) internally.
 *
 * @author Chris de Vreeze
 */
public final class AlternativeAddressService implements AddressService {

    // For nested JSON results with JSON objects and nested arrays, see https://forums.oracle.com/ords/apexds/post/complex-nested-json-structure-8286
    // This is interesting for queries returning films and their actors

    private final JsonMapper jsonMapper = JsonMapper.builder()
            .addModule(new GuavaModule())
            .build();

    private final EntityManagerFactory emf;

    public AlternativeAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            JpaRoot<AddressEntity> address = cq.from(AddressEntity.class);
            JpaJoin<AddressEntity, CityEntity> city = address.join(AddressEntity_.city, JoinType.INNER);
            JpaJoin<CityEntity, CountryEntity> country = city.join(CityEntity_.country, JoinType.INNER);
            cq.where(cb.equal(address.get(AddressEntity_.id), id));
            JpaCriteriaQuery<Tuple> tupleQuery =
                    createAddressTupleSelectClause(cb, cq, address, city, country);

            JpaCriteriaQuery<String> resultQuery = createAddressResultQuery(cb, tupleQuery);

            return session.createQuery(resultQuery)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            JpaRoot<AddressEntity> address = cq.from(AddressEntity.class);
            JpaJoin<AddressEntity, CityEntity> city = address.join(AddressEntity_.city, JoinType.INNER);
            JpaJoin<CityEntity, CountryEntity> country = city.join(CityEntity_.country, JoinType.INNER);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.id), cityId));
            JpaCriteriaQuery<Tuple> tupleQuery =
                    createAddressTupleSelectClause(cb, cq, address, city, country);

            JpaCriteriaQuery<String> resultQuery = createAddressResultQuery(cb, tupleQuery);

            return session.createQuery(resultQuery)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            JpaRoot<AddressEntity> address = cq.from(AddressEntity.class);
            JpaJoin<AddressEntity, CityEntity> city = address.join(AddressEntity_.city, JoinType.INNER);
            JpaJoin<CityEntity, CountryEntity> country = city.join(CityEntity_.country, JoinType.INNER);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.country).get(CountryEntity_.id), countryId));
            JpaCriteriaQuery<Tuple> tupleQuery =
                    createAddressTupleSelectClause(cb, cq, address, city, country);

            JpaCriteriaQuery<String> resultQuery = createAddressResultQuery(cb, tupleQuery);

            return session.createQuery(resultQuery)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

            JpaCriteriaQuery<Tuple> cq = cb.createQuery(Tuple.class);
            JpaRoot<AddressEntity> address = cq.from(AddressEntity.class);
            JpaJoin<AddressEntity, CityEntity> city = address.join(AddressEntity_.city, JoinType.INNER);
            JpaJoin<CityEntity, CountryEntity> country = city.join(CityEntity_.country, JoinType.INNER);
            JpaCriteriaQuery<Tuple> tupleQuery =
                    createAddressTupleSelectClause(cb, cq, address, city, country);

            JpaCriteriaQuery<String> resultQuery = createAddressResultQuery(cb, tupleQuery);

            return session.createQuery(resultQuery)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

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

            return session.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, City.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(Session.class, (Session session) -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            HibernateCriteriaBuilder cb = session.getCriteriaBuilder();

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

            return session.createQuery(cq)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Country.class))
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CityEntity> cq = cb.createQuery(CityEntity.class);

        Root<CityEntity> city = cq.from(CityEntity.class);
        cq.where(cb.equal(city.get(CityEntity_.id), cityId));
        cq.select(city);

        EntityGraph<CityEntity> entityGraph = CityEntity_.class_.createEntityGraph();
        entityGraph.addAttributeNode(CityEntity_.country);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityManager.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                .getSingleResult();
    }

    private JpaCriteriaQuery<Tuple> createAddressTupleSelectClause(
            HibernateCriteriaBuilder cb,
            JpaCriteriaQuery<Tuple> cq,
            JpaRoot<AddressEntity> address,
            JpaJoin<AddressEntity, CityEntity> addressCity,
            JpaJoin<CityEntity, CountryEntity> cityCountry
    ) {
        cq.select(cb.tuple(
                address.get(AddressEntity_.id).alias("addressId"),
                address.get(AddressEntity_.address).alias("address"),
                address.get(AddressEntity_.address2).alias("address2"),
                address.get(AddressEntity_.district).alias("district"),
                addressCity.get(CityEntity_.id).alias("cityId"),
                addressCity.get(CityEntity_.city).alias("city"),
                cityCountry.get(CountryEntity_.id).alias("countryId"),
                cityCountry.get(CountryEntity_.country).alias("country"),
                cityCountry.get(CountryEntity_.lastUpdate).alias("countryLastUpdate"),
                addressCity.get(CityEntity_.lastUpdate).alias("cityLastUpdate"),
                address.get(AddressEntity_.postalCode).alias("postalCode"),
                address.get(AddressEntity_.phone).alias("phone"),
                address.get(AddressEntity_.lastUpdate).alias("addressLastUpdate")
        ));
        return cq;
    }

    private JpaCriteriaQuery<String> createAddressResultQuery(HibernateCriteriaBuilder cb, JpaCriteriaQuery<Tuple> cteQuery) {
        JpaCriteriaQuery<String> cq = cb.createQuery(String.class);
        JpaCteCriteria<Tuple> cteCriteria = cq.with("Addr", cteQuery);
        JpaRoot<Tuple> tupleFromCte = cq.from(cteCriteria);
        cq.select(
                cb.jsonObject(
                        Map.of(
                                "id", tupleFromCte.get("addressId"),
                                "address1", tupleFromCte.get("address"),
                                "address2", tupleFromCte.get("address2"),
                                "district", tupleFromCte.get("district"),
                                "city",
                                cb.jsonObject(
                                        Map.of(
                                                "id", tupleFromCte.get("cityId"),
                                                "city", tupleFromCte.get("city"),
                                                "country", cb.jsonObject(
                                                        Map.of(
                                                                "id", tupleFromCte.get("countryId"),
                                                                "country", tupleFromCte.get("country"),
                                                                "lastUpdate", tupleFromCte.get("countryLastUpdate")
                                                        )
                                                ),
                                                "lastUpdate", tupleFromCte.get("cityLastUpdate")
                                        )
                                ),
                                "postalCode", tupleFromCte.get("postalCode"),
                                "phone", tupleFromCte.get("phone"),
                                "lastUpdate", tupleFromCte.get("addressLastUpdate")
                        )
                )
        );
        return cq;
    }
}
