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

import eu.cdevreeze.hibernateexperiments.plainsql.model.Address;
import eu.cdevreeze.hibernateexperiments.plainsql.model.City;
import eu.cdevreeze.hibernateexperiments.plainsql.service.AddressService;
import jakarta.persistence.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test of {@link ConcreteAddressService}.
 *
 * @author Chris de Vreeze
 */
class ConcreteAddressServiceTest {

    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withInitScript("create-test-database.sql");

    private static EntityManagerFactory emf;

    @BeforeAll
    static void beforeAll() {
        postgreSQLContainer.start();
        emf = createEntityManagerFactory(
                postgreSQLContainer.getJdbcUrl(),
                postgreSQLContainer.getUsername(),
                postgreSQLContainer.getPassword()
        );
        fillInitialTestData(emf);
    }

    @AfterAll
    static void afterAll() {
        if (emf != null) {
            postgreSQLContainer.stop();
            emf.close();
        }
    }

    @Test
    void testFindAllAddresses() {
        AddressService addressService = new ConcreteAddressService(emf);

        List<Address> addresses = addressService.findAll();

        assertEquals(2, addresses.size());
        assertEquals(
                Set.of("Moscow", "Jaroslavl"),
                addresses.stream().map(Address::city).map(City::city).collect(Collectors.toSet())
        );
    }

    private static EntityManagerFactory createEntityManagerFactory(String jdbcUrl, String username, String password) {
        String persistenceUnitName = "pagilatest";
        return new PersistenceConfiguration(persistenceUnitName)
                .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .defaultToOneFetchType(FetchType.LAZY) // although we have no entities here
                .provider("org.hibernate.jpa.HibernatePersistenceProvider")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.postgresql.Driver") // no connection pooling
                .property(Persistence.ConnectionProperties.JDBC_URL, jdbcUrl)
                .property(Persistence.ConnectionProperties.JDBC_USER, username)
                .property(Persistence.ConnectionProperties.JDBC_PASSWORD, password)
                .property(Persistence.SchemaManagementProperties.SCHEMAGEN_DATABASE_ACTION, "validate")
                .createEntityManagerFactory();
    }

    private static void fillInitialTestData(EntityManagerFactory emf) {
        emf.runInTransaction(EntityAgent.class, eh -> {
            String insertCountrySql = "insert into country (country) values (?1)";
            String countryName = "Russian Federation";
            eh.createNativeStatement(insertCountrySql)
                    .setParameter(1, countryName)
                    .execute();
            Long countryId = eh.createNativeQuery("select country_id from country where country = ?1", Long.class)
                    .setParameter(1, countryName)
                    .getSingleResult();

            String insertCitySql = "insert into city (city, country_id) values (?1, ?2)";
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, "Moscow")
                    .setParameter(2, countryId)
                    .execute();
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, "Jaroslavl")
                    .setParameter(2, countryId)
                    .execute();
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, "Ivanovo")
                    .setParameter(2, countryId)
                    .execute();

            String insertAddressSql =
                    """
                            insert into address (address, address2, district, city_id, postal_code, phone)
                            select ?1, ?2, ?3, city_id, ?4, ?5
                              from city
                             where city = ?6""";
            eh.createNativeStatement(insertAddressSql)
                    .setParameter(1, "46 Pjatigorsk Lane")
                    .setParameter(2, null)
                    .setParameter(3, "Moscow (City)")
                    .setParameter(4, "23616")
                    .setParameter(5, "262076994845")
                    .setParameter(6, "Moscow")
                    .execute();
            eh.createNativeStatement(insertAddressSql)
                    .setParameter(1, "810 Palghat (Palakkad) Boulevard")
                    .setParameter(2, null)
                    .setParameter(3, "Jaroslavl")
                    .setParameter(4, "73431")
                    .setParameter(5, "516331171356")
                    .setParameter(6, "Jaroslavl")
                    .execute();
        });
    }
}
