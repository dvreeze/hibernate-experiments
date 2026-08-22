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

package eu.cdevreeze.hibernateexperiments.repository.console;

import module eu.cdevreeze.hibernateexperiments.repository.service;
import module java.base;
import eu.cdevreeze.hibernateexperiments.repository.bootstrap.EntityManagerFactories;
import eu.cdevreeze.hibernateexperiments.repository.model.Film;
import eu.cdevreeze.hibernateexperiments.repository.service.FilmService;
import eu.cdevreeze.hibernateexperiments.repository.service.factory.FilmServiceFactory;
import jakarta.persistence.EntityManagerFactory;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.datatype.guava.GuavaModule;

/**
 * Program finding the film with a given ID, returning it with actors and categories.
 *
 * @author Chris de Vreeze
 */
public class FindFilm {

    static void main(String... args) {
        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(new GuavaModule())
                .build();

        System.setProperty("hibernate.query.hql.json_functions_enabled", "true");

        Objects.checkIndex(0, args.length);
        long filmId = Long.parseLong(args[0]);

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            FilmService filmService = FilmServiceFactory.create(emf);

            Optional<Film> films = filmService.findFilm(filmId);

            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, films);
        }
    }
}
