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

package eu.cdevreeze.hibernateexperiments.emrepository.console;

import module eu.cdevreeze.hibernateexperiments.emrepository.service;
import module java.base;
import eu.cdevreeze.hibernateexperiments.emrepository.bootstrap.EntityManagerFactories;
import eu.cdevreeze.hibernateexperiments.emrepository.model.Actor;
import eu.cdevreeze.hibernateexperiments.emrepository.service.ActorService;
import eu.cdevreeze.hibernateexperiments.emrepository.service.factory.ActorServiceFactory;
import jakarta.persistence.EntityManagerFactory;

/**
 * Program finding all actors of a given film (given as film ID).
 *
 * @author Chris de Vreeze
 */
public class FindActorsByFilmId {

    static void main(String... args) {
        Objects.checkIndex(0, args.length);
        long filmId = Long.parseLong(args[0]);

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            ActorService actorService = ActorServiceFactory.create(emf);

            List<Actor> actors = actorService.findByFilmId(filmId);

            actors.forEach(IO::println);
        }
    }
}
