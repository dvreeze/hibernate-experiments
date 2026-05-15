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

import eu.cdevreeze.hibernateexperiments.jpql.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.jpql.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.jpql.entity.CountryEntity;
import eu.cdevreeze.hibernateexperiments.jpql.model.Address;
import eu.cdevreeze.hibernateexperiments.jpql.model.City;
import eu.cdevreeze.hibernateexperiments.jpql.model.Country;
import eu.cdevreeze.hibernateexperiments.jpql.service.AddressService;
import jakarta.persistence.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.Instant;
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
                .managedClass(AddressEntity.class)
                .managedClass(CityEntity.class)
                .managedClass(CountryEntity.class)
                .createEntityManagerFactory();
    }

    private static void fillInitialTestData(EntityManagerFactory emf) {
        emf.runInTransaction(EntityAgent.class, eh -> {
            CountryEntity countryEntity = new CountryEntity();
            countryEntity.setId(80);
            countryEntity.setCountry("Russian Federation");
            countryEntity.setLastUpdate(Instant.now());
            eh.upsert(countryEntity);

            CityEntity ivanovo = new CityEntity();
            ivanovo.setId(225);
            ivanovo.setCity("Ivanovo");
            ivanovo.setCountry(countryEntity);
            ivanovo.setLastUpdate(Instant.now());
            eh.upsert(ivanovo);

            CityEntity jaroslavl = new CityEntity();
            jaroslavl.setId(235);
            jaroslavl.setCity("Jaroslavl");
            jaroslavl.setCountry(countryEntity);
            jaroslavl.setLastUpdate(Instant.now());
            eh.upsert(jaroslavl);

            CityEntity moscow = new CityEntity();
            moscow.setId(343);
            moscow.setCity("Moscow");
            moscow.setCountry(countryEntity);
            moscow.setLastUpdate(Instant.now());
            eh.upsert(moscow);

            AddressEntity address1 = new AddressEntity();
            address1.setId(50);
            address1.setAddress("46 Pjatigorsk Lane");
            address1.setCity(moscow);
            address1.setDistrict("Moscow (City)");
            address1.setPostalCode("23616");
            address1.setPhone("262076994845");
            address1.setLastUpdate(Instant.now());
            eh.upsert(address1);

            AddressEntity address2 = new AddressEntity();
            address2.setId(226);
            address2.setAddress("810 Palghat (Palakkad) Boulevard");
            address2.setCity(jaroslavl);
            address2.setDistrict("Jaroslavl");
            address2.setPostalCode("73431");
            address2.setPhone("516331171356");
            address2.setLastUpdate(Instant.now());
            eh.upsert(address2);
        });
    }
}
