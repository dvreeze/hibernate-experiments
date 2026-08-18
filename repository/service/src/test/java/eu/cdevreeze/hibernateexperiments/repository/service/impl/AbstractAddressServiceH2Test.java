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

package eu.cdevreeze.hibernateexperiments.repository.service.impl;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.repository.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CountryEntity;
import eu.cdevreeze.hibernateexperiments.repository.model.Address;
import eu.cdevreeze.hibernateexperiments.repository.model.City;
import eu.cdevreeze.hibernateexperiments.repository.model.Country;
import eu.cdevreeze.hibernateexperiments.repository.service.AddressService;
import jakarta.persistence.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test of an {@link AddressService} implementation, using an embedded H2 database.
 *
 * @author Chris de Vreeze
 */
abstract class AbstractAddressServiceH2Test {

    private static EntityManagerFactory emf;

    protected abstract AddressService addressService(EntityManagerFactory emf);

    @BeforeEach
    void beforeEach() {
        emf = createEntityManagerFactory();
        fillInitialTestData(emf);
    }

    @AfterEach
    void afterEach() {
        emf.close();
    }

    @Test
    void testFindAllAddresses() {
        List<Address> addresses = addressService(emf).findAll();

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
    void testFindAddress() {
        Optional<Address> addressOption = addressService(emf).findById(226);

        assertTrue(addressOption.isPresent());
        assertEquals("810 Palghat (Palakkad) Boulevard", addressOption.get().address1());
        assertNull(addressOption.get().address2());
        assertEquals("Jaroslavl", addressOption.get().city().city());
        assertEquals("Jaroslavl", addressOption.get().district());
        assertEquals("73431", addressOption.get().postalCode());
        assertEquals("516331171356", addressOption.get().phone());
    }

    @Test
    void testFindAddressesByCityId() {
        ImmutableList<Address> addresses = addressService(emf).findByCityId(235);

        assertEquals(1, addresses.size());
        assertEquals("810 Palghat (Palakkad) Boulevard", addresses.getFirst().address1());
        assertNull(addresses.getFirst().address2());
        assertEquals("Jaroslavl", addresses.getFirst().city().city());
        assertEquals("Jaroslavl", addresses.getFirst().district());
        assertEquals("73431", addresses.getFirst().postalCode());
        assertEquals("516331171356", addresses.getFirst().phone());
    }

    @Test
    void testFindAddressesByCountryId() {
        ImmutableList<Address> addresses = addressService(emf).findByCountryId(80)
                .stream()
                .filter(a -> a.district().equals("Jaroslavl"))
                .collect(ImmutableList.toImmutableList());

        assertEquals(1, addresses.size());
        assertEquals("810 Palghat (Palakkad) Boulevard", addresses.getFirst().address1());
        assertNull(addresses.getFirst().address2());
        assertEquals("Jaroslavl", addresses.getFirst().city().city());
        assertEquals("Jaroslavl", addresses.getFirst().district());
        assertEquals("73431", addresses.getFirst().postalCode());
        assertEquals("516331171356", addresses.getFirst().phone());
    }

    @Test
    void testFindCitiesByCountry() {
        long countryId = 80L;
        List<City> cities = addressService(emf).findCitiesByCountryId(countryId);

        assertEquals(
                Set.of("Moscow", "Jaroslavl", "Ivanovo"),
                cities.stream().map(City::city).collect(Collectors.toSet())
        );
    }

    @Test
    void testFindAllCountries() {
        List<Country> countries = addressService(emf).findAllCountries();

        assertTrue(countries.stream().anyMatch(c -> c.country().equals("Russian Federation")));
    }

    @Test
    void testAddAddress() {
        Address.NewAddress newAddress = new Address.NewAddress(
                "250 Ulitsa Kirovo",
                null,
                "Yaroslavl",
                235, // Yaroslavl
                "41777",
                "904253967172",
                Instant.now()
        );

        Address address = addressService(emf).add(newAddress);

        assertNotNull(address);
        assertEquals("250 Ulitsa Kirovo", address.address1());
        assertNull(address.address2());
        assertEquals("Yaroslavl", address.district());
        assertEquals(235L, address.city().id());
        assertEquals("41777", address.postalCode());
        assertEquals("904253967172", address.phone());
    }

    private static EntityManagerFactory createEntityManagerFactory() {
        String persistenceUnitName = "pagilatestH2";
        return new PersistenceConfiguration(persistenceUnitName)
                .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .defaultToOneFetchType(FetchType.LAZY)
                .provider("org.hibernate.jpa.HibernatePersistenceProvider")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.h2.Driver") // no connection pooling, of course
                .property(Persistence.ConnectionProperties.JDBC_URL, "jdbc:h2:mem:test_db")
                .schemaManagementDatabaseAction(SchemaManagementAction.DROP_AND_CREATE)
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
