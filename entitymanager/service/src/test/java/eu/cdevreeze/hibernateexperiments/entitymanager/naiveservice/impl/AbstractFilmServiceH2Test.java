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

package eu.cdevreeze.hibernateexperiments.entitymanager.naiveservice.impl;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.*;
import eu.cdevreeze.hibernateexperiments.entitymanager.naiveservice.FilmService;
import jakarta.persistence.*;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test of an {@link FilmService} implementation, using an embedded H2 database.
 *
 * @author Chris de Vreeze
 */
abstract class AbstractFilmServiceH2Test {

    static {
        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");
    }

    private static EntityManagerFactory emf;

    protected abstract FilmService filmService(EntityManagerFactory emf);

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
    void testFindAllFilms() {
        List<FilmEntity> films = filmService(emf).findAllFilms();

        assertEquals(1, films.size());
        assertEquals("Two wolves", films.getFirst().getTitle());
        assertEquals("G", films.getFirst().getRating());
        assertEquals((short) 120, films.getFirst().getLength());
    }

    @Test
    void testFindFilm() {
        Optional<FilmEntity> filmOption = filmService(emf).findFilm(1);

        assertTrue(filmOption.isPresent());
        assertEquals("Two wolves", filmOption.get().getTitle());
        assertEquals("G", filmOption.get().getRating());
        assertEquals((short) 120, filmOption.get().getLength());
    }

    @Test
    void testFindNoFilm() {
        Optional<FilmEntity> filmOption = filmService(emf).findFilm(2);

        assertFalse(filmOption.isPresent());
    }

    @Test
    void testFindFilmsByActorId() {
        ImmutableList<FilmEntity> films = filmService(emf).findFilmsByActorId(1);

        assertEquals(1, films.size());
        assertEquals("Two wolves", films.getFirst().getTitle());
        assertEquals("G", films.getFirst().getRating());
        assertEquals((short) 120, films.getFirst().getLength());
    }

    @Test
    void testFindNoFilmsByActorId() {
        ImmutableList<FilmEntity> films = filmService(emf).findFilmsByActorId(3);

        assertEquals(0, films.size());
    }

    @Test
    void testLazyInitializationException() {
        ImmutableList<FilmEntity> filmsOfActor = filmService(emf).findFilmsByActorId(1);
        assertThrows(LazyInitializationException.class, () -> {
            // Outside any transaction/Session:
            var firstCategory = filmsOfActor.getFirst().getFilmCategories().iterator().next();
            assertNotNull(firstCategory); // Not reached
        });
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
                .managedClass(CategoryEntity.class)
                .managedClass(FilmCategoryEntity.class)
                .managedClass(LanguageEntity.class)
                .managedClass(FilmEntity.class)
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
            filmActorJohn.setActor(john);
            filmActorJohn.setFilm(film);
            filmActorJohn.setLastUpdate(Instant.now());
            eh.insert(filmActorJohn);

            FilmActorEntity filmActorJane = new FilmActorEntity();
            filmActorJane.setActor(jane);
            filmActorJane.setFilm(film);
            filmActorJane.setLastUpdate(Instant.now());
            eh.insert(filmActorJane);

            CategoryEntity category = new CategoryEntity();
            category.setId(1);
            category.setName("Action");
            category.setLastUpdate(Instant.now());
            eh.insert(category);

            FilmCategoryEntity filmCategory = new FilmCategoryEntity();
            filmCategory.setFilm(film);
            filmCategory.setCategory(category);
            filmCategory.setLastUpdate(Instant.now());
            eh.insert(filmCategory);
        });
    }
}
