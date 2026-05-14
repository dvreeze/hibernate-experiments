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
import eu.cdevreeze.hibernateexperiments.plainsql.model.Country;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test of {@link ConcreteAddressService}.
 *
 * @author Chris de Vreeze
 */
class ConcreteAddressServiceTest {

    private static final PostgreSQLContainer<?> postgreSQLContainer =
            new PostgreSQLContainer<>("postgres:18-alpine")
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
            emf.close();
            postgreSQLContainer.stop();
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
        assertEquals(
                Set.of("23616", "73431"),
                addresses.stream().map(Address::postalCode).collect(Collectors.toSet())
        );
    }

    @Test
    void testFindCitiesByCountry() {
        AddressService addressService = new ConcreteAddressService(emf);

        long countryId = 80L;
        List<City> cities = addressService.findCitiesByCountryId(countryId);

        assertEquals(
                Set.of("Moscow", "Jaroslavl", "Ivanovo"),
                cities.stream().map(City::city).collect(Collectors.toSet())
        );
    }

    @Test
    void testFindAllCountries() {
        AddressService addressService = new ConcreteAddressService(emf);

        List<Country> countries = addressService.findAllCountries();

        assertTrue(countries.stream().anyMatch(c -> c.country().equals("Russian Federation")));
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
            String insertCountrySql = "insert into country (country_id, country) values (?1, ?2)";
            Long countryId = 80L;
            String countryName = "Russian Federation";
            eh.createNativeStatement(insertCountrySql)
                    .setParameter(1, countryId)
                    .setParameter(2, countryName)
                    .execute();

            String insertCitySql = "insert into city (city_id, city, country_id) values (?1, ?2, ?3)";
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, 225)
                    .setParameter(2, "Ivanovo")
                    .setParameter(3, countryId)
                    .execute();
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, 235)
                    .setParameter(2, "Jaroslavl")
                    .setParameter(3, countryId)
                    .execute();
            eh.createNativeStatement(insertCitySql)
                    .setParameter(1, 343)
                    .setParameter(2, "Moscow")
                    .setParameter(3, countryId)
                    .execute();

            String insertAddressSql =
                    """
                            insert into address (address_id, address, address2, district, city_id, postal_code, phone)
                            select ?1, ?2, ?3, ?4, city_id, ?5, ?6
                              from city
                             where city = ?7""";
            eh.createNativeStatement(insertAddressSql)
                    .setParameter(1, 50)
                    .setParameter(2, "46 Pjatigorsk Lane")
                    .setParameter(3, null)
                    .setParameter(4, "Moscow (City)")
                    .setParameter(5, "23616")
                    .setParameter(6, "262076994845")
                    .setParameter(7, "Moscow")
                    .execute();
            eh.createNativeStatement(insertAddressSql)
                    .setParameter(1, 226)
                    .setParameter(2, "810 Palghat (Palakkad) Boulevard")
                    .setParameter(3, null)
                    .setParameter(4, "Jaroslavl")
                    .setParameter(5, "73431")
                    .setParameter(6, "516331171356")
                    .setParameter(7, "Jaroslavl")
                    .execute();
        });
    }
}
