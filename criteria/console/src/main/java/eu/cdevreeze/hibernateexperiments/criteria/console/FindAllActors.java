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

package eu.cdevreeze.hibernateexperiments.criteria.console;

import module eu.cdevreeze.hibernateexperiments.criteria.service;
import module java.base;
import jakarta.persistence.EntityManagerFactory;

/**
 * Program finding all actors in the database.
 *
 * @author Chris de Vreeze
 */
public class FindAllActors {

    static void main(String... args) {
        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            ActorService actorService = ActorServiceFactory.create(emf);

            List<Actor> actors = actorService.findAll();

            actors.forEach(IO::println);
        }
    }
}
