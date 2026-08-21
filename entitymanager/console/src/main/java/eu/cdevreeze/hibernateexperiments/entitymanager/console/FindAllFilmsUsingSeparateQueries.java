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

package eu.cdevreeze.hibernateexperiments.entitymanager.console;

import module eu.cdevreeze.hibernateexperiments.entitymanager.service;
import module java.base;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

/**
 * Program finding all films in the database, using separate queries.
 *
 * @author Chris de Vreeze
 */
public class FindAllFilmsUsingSeparateQueries {

    static void main(String... args) {
        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(new GuavaModule())
                .build();

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            FilmService filmService = ConcreteFilmServiceUsingSeparateQueriesFactory.create(emf);

            List<Film> films = filmService.findAllFilms();

            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, films);
        }
    }
}
