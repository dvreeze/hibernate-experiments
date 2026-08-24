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
 * Program finding the films with a given actor (as ID), returning it with actors and categories.
 *
 * @author Chris de Vreeze
 */
public class FindFilmsByActorId {

    static void main(String... args) {
        JsonMapper jsonMapper = JsonMapper.builder()
                .addModule(new GuavaModule())
                .build();

        Objects.checkIndex(0, args.length);
        long actorId = Long.parseLong(args[0]);

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            FilmService filmService = FilmServiceFactory.create(emf);

            List<Film> films = filmService.findFilmsByActorId(actorId);

            jsonMapper.writerWithDefaultPrettyPrinter().writeValue(System.out, films);
        }
    }
}
