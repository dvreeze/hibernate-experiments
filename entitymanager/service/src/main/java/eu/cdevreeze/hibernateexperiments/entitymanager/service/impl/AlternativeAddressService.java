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
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.AddressService;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

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
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Addr as (
                            select ad.id as adId, ad.address as address, ad.address2 as address2, ad.district as district,
                                   ci.id as ciId, ci.city as city,
                                   co.id as coId, co.country as country, co.lastUpdate as coLastUpdate,
                                   ci.lastUpdate as ciLastUpdate,
                                   ad.postalCode as postalCode, ad.phone as phone, ad.lastUpdate as adLastUpdate
                              from Address ad inner join ad.city ci inner join ci.country co
                             where ad.id = ?1
                        )
                    select json_object(
                               'id': addr.adId,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.ciId,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.coId,
                                       'country': addr.country,
                                       'lastUpdate': addr.coLastUpdate
                                   ),
                                   'lastUpdate': addr.ciLastUpdate
                               ),
                               'postalCode': addr.postalCode,
                               'phone': addr.phone,
                               'lastUpdate': addr.adLastUpdate
                           )
                      from Addr addr
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .setParameter(1, id)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Addr as (
                            select ad.id as adId, ad.address as address, ad.address2 as address2, ad.district as district,
                                   ci.id as ciId, ci.city as city,
                                   co.id as coId, co.country as country, co.lastUpdate as coLastUpdate,
                                   ci.lastUpdate as ciLastUpdate,
                                   ad.postalCode as postalCode, ad.phone as phone, ad.lastUpdate as adLastUpdate
                              from Address ad inner join ad.city ci inner join ci.country co
                             where ci.id = ?1
                        )
                    select json_object(
                               'id': addr.adId,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.ciId,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.coId,
                                       'country': addr.country,
                                       'lastUpdate': addr.coLastUpdate
                                   ),
                                   'lastUpdate': addr.ciLastUpdate
                               ),
                               'postalCode': addr.postalCode,
                               'phone': addr.phone,
                               'lastUpdate': addr.adLastUpdate
                           )
                      from Addr addr
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .setParameter(1, cityId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Addr as (
                            select ad.id as adId, ad.address as address, ad.address2 as address2, ad.district as district,
                                   ci.id as ciId, ci.city as city,
                                   co.id as coId, co.country as country, co.lastUpdate as coLastUpdate,
                                   ci.lastUpdate as ciLastUpdate,
                                   ad.postalCode as postalCode, ad.phone as phone, ad.lastUpdate as adLastUpdate
                              from Address ad inner join ad.city ci inner join ci.country co
                             where co.id = ?1
                        )
                    select json_object(
                               'id': addr.adId,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.ciId,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.coId,
                                       'country': addr.country,
                                       'lastUpdate': addr.coLastUpdate
                                   ),
                                   'lastUpdate': addr.ciLastUpdate
                               ),
                               'postalCode': addr.postalCode,
                               'phone': addr.phone,
                               'lastUpdate': addr.adLastUpdate
                           )
                      from Addr addr
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .setParameter(1, countryId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Addr as (
                            select ad.id as adId, ad.address as address, ad.address2 as address2, ad.district as district,
                                   ci.id as ciId, ci.city as city,
                                   co.id as coId, co.country as country, co.lastUpdate as coLastUpdate,
                                   ci.lastUpdate as ciLastUpdate,
                                   ad.postalCode as postalCode, ad.phone as phone, ad.lastUpdate as adLastUpdate
                              from Address ad inner join ad.city ci inner join ci.country co
                        )
                    select json_object(
                               'id': addr.adId,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.ciId,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.coId,
                                       'country': addr.country,
                                       'lastUpdate': addr.coLastUpdate
                                   ),
                                   'lastUpdate': addr.ciLastUpdate
                               ),
                               'postalCode': addr.postalCode,
                               'phone': addr.phone,
                               'lastUpdate': addr.adLastUpdate
                           )
                      from Addr addr
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Cty as materialized (
                            select id as countryId, country as countryName, lastUpdate as lastUpdateTime
                              from Country
                             where id = ?1
                        )
                    select json_object(
                               'id': ci.id,
                               'city': ci.city,
                               'country': json_object('id': cty.countryId, 'country': cty.countryName, 'lastUpdate': cty.lastUpdateTime),
                               'lastUpdate': ci.lastUpdate
                           )
                      from City ci
                     inner join Cty cty on (ci.country.id = cty.countryId)
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .setParameter(1, countryId)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
                    .map(v -> jsonMapper.readValue(v, City.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String qlString = """
                    with
                        Cty as (
                            select id as countryId, country as countryName, lastUpdate as lastUpdateTime
                              from Country
                        )
                    select json_object('id': countryId, 'country': countryName, 'lastUpdate': lastUpdateTime)
                      from Cty
                    """;

            return entityManager.createQuery(qlString, String.class)
                    .getResultList() // works better than getResultStream (no duplicates)
                    .stream()
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
        String qlString = "select ci from City ci join fetch ci.country co where ci.id = :id";

        return entityManager.createQuery(qlString, CityEntity.class)
                .setParameter("id", cityId)
                .getSingleResult();
    }
}
