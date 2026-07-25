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

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.ActorEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmActorEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.LanguageEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.model.Actor;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.ActorService;
import jakarta.persistence.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test of an {@link ActorService} implementation, using an embedded H2 database.
 *
 * @author Chris de Vreeze
 */
abstract class AbstractActorServiceH2Test {

    static {
        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");
    }

    private static EntityManagerFactory emf;

    protected abstract ActorService actorService(EntityManagerFactory emf);

    @BeforeAll
    static void beforeAll() {
        emf = createEntityManagerFactory();
        fillInitialTestData(emf);
    }

    @AfterAll
    static void afterAll() {
        if (emf != null) {
            emf.close();
        }
    }

    @Test
    void testFindAllActors() {
        List<Actor> actors = actorService(emf).findAll();

        assertEquals(3, actors.size());
        assertEquals(
                Set.of("Doe"),
                actors.stream().map(Actor::lastName).collect(Collectors.toSet())
        );
        assertEquals(
                Set.of("John", "Jane", "Bob"),
                actors.stream().map(Actor::firstName).collect(Collectors.toSet())
        );
    }

    @Test
    void testFindActor() {
        Optional<Actor> actorOption = actorService(emf).findById(2);

        assertTrue(actorOption.isPresent());
        assertEquals("Jane", actorOption.get().firstName());
        assertEquals("Doe", actorOption.get().lastName());
    }

    @Test
    void testFindActorByFilmId() {
        ImmutableList<Actor> actors = actorService(emf).findByFilmId(1);

        assertEquals(2, actors.size());
        assertEquals(
                Set.of("Doe"),
                actors.stream().map(Actor::lastName).collect(Collectors.toSet())
        );
        assertEquals(
                Set.of("John", "Jane"),
                actors.stream().map(Actor::firstName).collect(Collectors.toSet())
        );
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
                .managedClass(ActorEntity.class)
                .managedClass(FilmActorEntity.class)
                .managedClass(FilmEntity.class)
                .managedClass(LanguageEntity.class)
                .createEntityManagerFactory();
    }

    private static void fillInitialTestData(EntityManagerFactory emf) {
        emf.runInTransaction(EntityAgent.class, eh -> {
            ActorEntity john = new ActorEntity();
            john.setId(1);
            john.setFirstName("John");
            john.setLastName("Doe");
            john.setLastUpdate(Instant.now());
            eh.insert(john);

            ActorEntity jane = new ActorEntity();
            jane.setId(2);
            jane.setFirstName("Jane");
            jane.setLastName("Doe");
            jane.setLastUpdate(Instant.now());
            eh.insert(jane);

            ActorEntity bob = new ActorEntity();
            bob.setId(3);
            bob.setFirstName("Bob");
            bob.setLastName("Doe");
            bob.setLastUpdate(Instant.now());
            eh.insert(bob);

            LanguageEntity english = new LanguageEntity();
            english.setId(1);
            english.setName("English");
            english.setLastUpdate(Instant.now());
            eh.insert(english);

            FilmEntity film = new FilmEntity();
            film.setId(1);
            film.setTitle("Two wolves");
            film.setDescription(null);
            film.setReleaseYear(Year.of(2026));
            film.setLanguage(english);
            film.setOriginalLanguage(null);
            film.setRentalDuration((short) 120);
            film.setRentalRate(BigDecimal.valueOf(3.0));
            film.setLength((short) 120);
            film.setReplacementCost(BigDecimal.valueOf(2.5));
            film.setRating("G");
            film.setLastUpdate(Instant.now());
            eh.insert(film);

            FilmActorEntity filmActorJohn = new FilmActorEntity();
            filmActorJohn.setActorId(john.getId());
            filmActorJohn.setFilmId(film.getId());
            filmActorJohn.setLastUpdate(Instant.now());
            eh.insert(filmActorJohn);

            FilmActorEntity filmActorJane = new FilmActorEntity();
            filmActorJane.setActorId(jane.getId());
            filmActorJane.setFilmId(film.getId());
            filmActorJane.setLastUpdate(Instant.now());
            eh.insert(filmActorJane);
        });
    }
}
