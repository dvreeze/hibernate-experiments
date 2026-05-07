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

import static jakarta.persistence.sql.ResultSetMapping.column;
import static jakarta.persistence.sql.ResultSetMapping.constructor;

/**
 * Alternative {@link AddressService} implementation that uses CTEs (and JSON result sets) internally.
 *
 * @author Chris de Vreeze
 */
public final class AlternativeAddressService implements AddressService {

    // TODO
    // For nested JSON results with JSON objects and nested arrays, see https://forums.oracle.com/ords/apexds/post/complex-nested-json-structure-8286
    // This is interesting for queries returning films and their actors

    private final EntityManagerFactory emf;

    public AlternativeAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String sqlString = """
                    select ad.address_id, ad.address, ad.address2, ad.district,
                           ci.city_id, ci.city,
                           co.country_id, co.country, co.last_update as co_last_update,
                           ci.last_update as ci_last_update,
                           ad.postal_code, ad.phone, ad.last_update as ad_last_update
                      from address ad
                     inner join city ci on (ad.city_id = ci.city_id)
                     inner join country co on (ci.country_id = co.country_id)
                     where ad.address_id = ?1;
                    """;

            ResultSetMapping<Address> rsMapping = getAddressResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, id)
                    .getResultStream()
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, or multiset
            String sqlString = """
                    with
                        ct as materialized (
                            select city_id, city, country_id, last_update as ci_last_update
                              from city
                             where city_id = ?1
                        )
                    select ad.address_id, ad.address, ad.address2, ad.district,
                           ct.city_id, ct.city,
                           co.country_id, co.country, co.last_update as co_last_update,
                           ct.ci_last_update,
                           ad.postal_code, ad.phone, ad.last_update as ad_last_update
                      from address ad
                     inner join ct on (ad.city_id = ct.city_id)
                     inner join country co on (ct.country_id = co.country_id)
                    """;

            ResultSetMapping<Address> rsMapping = getAddressResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, cityId)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, or multiset
            String sqlString = """
                    with
                        cty as materialized (
                            select country_id, country, last_update as co_last_update
                              from country
                             where country_id = ?1
                        )
                    select ad.address_id, ad.address, ad.address2, ad.district,
                           ci.city_id, ci.city,
                           cty.country_id, cty.country, cty.co_last_update,
                           ci.last_update as ci_last_update,
                           ad.postal_code, ad.phone, ad.last_update as ad_last_update
                      from address ad
                     inner join city ci on (ad.city_id = ci.city_id)
                     inner join cty on (ci.country_id = cty.country_id)
                    """;

            ResultSetMapping<Address> rsMapping = getAddressResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String sqlString = """
                    select ad.address_id, ad.address, ad.address2, ad.district,
                           ci.city_id, ci.city,
                           co.country_id, co.country, co.last_update as co_last_update,
                           ci.last_update as ci_last_update,
                           ad.postal_code, ad.phone, ad.last_update as ad_last_update
                      from address ad
                     inner join city ci on (ad.city_id = ci.city_id)
                     inner join country co on (ci.country_id = co.country_id)
                    """;

            ResultSetMapping<Address> rsMapping = getAddressResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            // Just for fun, using CTE (common table expression); by the way, Hibernate HQL supports CTEs too!
            // Think of the CTE itself as just another table, or multiset
            String sqlString = """
                    with
                        cty as materialized (
                            select country_id, country, last_update as co_last_update
                              from country
                             where country_id = ?1
                        )
                    select ci.city_id, ci.city,
                           cty.country_id, cty.country, cty.co_last_update,
                           ci.last_update as ci_last_update
                      from city ci
                     inner join cty on (ci.country_id = cty.country_id)
                    """;

            ResultSetMapping<City> rsMapping = getCityResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            String sqlString = "select country_id, country, last_update as co_last_update from country";

            ResultSetMapping<Country> rsMapping = getCountryResultSetMapping();

            return entityAgent.createNativeQuery(sqlString, rsMapping)
                    .getResultStream()
                    .collect(ImmutableList.toImmutableList());
        });
    }

    private static ConstructorMapping<Country> getCountryResultSetMapping() {
        return constructor(
                Country.class,
                column("country_id", Long.class),
                column("country", String.class),
                column("co_last_update", Instant.class)
        );
    }

    private static ConstructorMapping<City> getCityResultSetMapping() {
        return constructor(
                City.class,
                column("city_id", Long.class),
                column("city", String.class),
                getCountryResultSetMapping(),
                column("ci_last_update", Instant.class)
        );
    }

    private static ConstructorMapping<Address> getAddressResultSetMapping() {
        return constructor(
                Address.class,
                column("address_id", Long.class),
                column("address", String.class),
                column("address2", String.class),
                column("district", String.class),
                getCityResultSetMapping(),
                column("postal_code", String.class),
                column("phone", String.class),
                column("ad_last_update", Instant.class)
        );
    }
}
