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

package eu.cdevreeze.hibernateexperiments.plainsql.service.impl;

import module eu.cdevreeze.hibernateexperiments.plainsql.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.plainsql.service.AddressService;
import tools.jackson.databind.json.JsonMapper;

/**
 * Alternative {@link AddressService} implementation that uses CTEs (and JSON result sets) internally.
 *
 * @author Chris de Vreeze
 */
public final class AlternativeAddressService implements AddressService {

    // TODO
    // For nested JSON results with JSON objects and nested arrays, see https://forums.oracle.com/ords/apexds/post/complex-nested-json-structure-8286
    // This is interesting for queries returning films and their actors

    // Guava Jackson Module not needed here
    private final JsonMapper jsonMapper = JsonMapper.builder()
            .build();

    private final EntityManagerFactory emf;

    public AlternativeAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        addr as (
                            select ad.address_id, ad.address, ad.address2, ad.district,
                                   ci.city_id, ci.city,
                                   co.country_id, co.country, co.last_update as co_last_update,
                                   ci.last_update as ci_last_update,
                                   ad.postal_code, ad.phone, ad.last_update as ad_last_update
                              from address ad
                             inner join city ci on (ad.city_id = ci.city_id)
                             inner join country co on (ci.country_id = co.country_id)
                             where ad.address_id = ?1
                        )
                    select json_object(
                               'id': addr.address_id,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.city_id,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.country_id,
                                       'country': addr.country,
                                       'lastUpdate': addr.co_last_update
                                   ),
                                   'lastUpdate': addr.ci_last_update
                               ),
                               'postalCode': addr.postal_code,
                               'phone': addr.phone,
                               'lastUpdate': addr.ad_last_update
                           )
                      from addr
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        addr as (
                            select ad.address_id, ad.address, ad.address2, ad.district,
                                   ci.city_id, ci.city,
                                   co.country_id, co.country, co.last_update as co_last_update,
                                   ci.last_update as ci_last_update,
                                   ad.postal_code, ad.phone, ad.last_update as ad_last_update
                              from address ad
                             inner join city ci on (ad.city_id = ci.city_id)
                             inner join country co on (ci.country_id = co.country_id)
                             where ci.city_id = ?1
                        )
                    select json_object(
                               'id': addr.address_id,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.city_id,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.country_id,
                                       'country': addr.country,
                                       'lastUpdate': addr.co_last_update
                                   ),
                                   'lastUpdate': addr.ci_last_update
                               ),
                               'postalCode': addr.postal_code,
                               'phone': addr.phone,
                               'lastUpdate': addr.ad_last_update
                           )
                      from addr
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .setParameter(1, cityId)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        addr as (
                            select ad.address_id, ad.address, ad.address2, ad.district,
                                   ci.city_id, ci.city,
                                   co.country_id, co.country, co.last_update as co_last_update,
                                   ci.last_update as ci_last_update,
                                   ad.postal_code, ad.phone, ad.last_update as ad_last_update
                              from address ad
                             inner join city ci on (ad.city_id = ci.city_id)
                             inner join country co on (ci.country_id = co.country_id)
                             where co.country_id = ?1
                        )
                    select json_object(
                               'id': addr.address_id,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.city_id,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.country_id,
                                       'country': addr.country,
                                       'lastUpdate': addr.co_last_update
                                   ),
                                   'lastUpdate': addr.ci_last_update
                               ),
                               'postalCode': addr.postal_code,
                               'phone': addr.phone,
                               'lastUpdate': addr.ad_last_update
                           )
                      from addr
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        addr as (
                            select ad.address_id, ad.address, ad.address2, ad.district,
                                   ci.city_id, ci.city,
                                   co.country_id, co.country, co.last_update as co_last_update,
                                   ci.last_update as ci_last_update,
                                   ad.postal_code, ad.phone, ad.last_update as ad_last_update
                              from address ad
                             inner join city ci on (ad.city_id = ci.city_id)
                             inner join country co on (ci.country_id = co.country_id)
                        )
                    select json_object(
                               'id': addr.address_id,
                               'address1': addr.address,
                               'address2': addr.address2,
                               'district': addr.district,
                               'city': json_object(
                                   'id': addr.city_id,
                                   'city': addr.city,
                                   'country': json_object(
                                       'id': addr.country_id,
                                       'country': addr.country,
                                       'lastUpdate': addr.co_last_update
                                   ),
                                   'lastUpdate': addr.ci_last_update
                               ),
                               'postalCode': addr.postal_code,
                               'phone': addr.phone,
                               'lastUpdate': addr.ad_last_update
                           )
                      from addr
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Address.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        cty as materialized (
                            select country_id, country, last_update as co_last_update
                              from country
                             where country_id = ?1
                        )
                    select json_object(
                               'id': ci.city_id,
                               'city': ci.city,
                               'country': json_object('id': cty.country_id, 'country': cty.country, 'lastUpdate': cty.co_last_update),
                               'lastUpdate': ci.last_update
                           )
                      from city ci
                     inner join cty on (ci.country_id = cty.country_id)
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, City.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, whether materialized or not
            String sqlString = """
                    with
                        cty as (
                            select country_id, country, last_update
                              from country
                        )
                    select json_object('id': cty.country_id, 'country': cty.country, 'lastUpdate': cty.last_update)
                      from cty
                    """;

            return entityAgent.createNativeQuery(sqlString, String.class)
                    .getResultStream()
                    .map(v -> jsonMapper.readValue(v, Country.class))
                    .collect(ImmutableList.toImmutableList());
        });
    }
}
