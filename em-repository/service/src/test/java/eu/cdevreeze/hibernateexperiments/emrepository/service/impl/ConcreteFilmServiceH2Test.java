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

package eu.cdevreeze.hibernateexperiments.emrepository.service.impl;

import eu.cdevreeze.hibernateexperiments.emrepository.service.FilmService;
import jakarta.persistence.EntityManagerFactory;

/**
 * Unit test of {@link ConcreteFilmService}, using an embedded H2 database.
 *
 * @author Chris de Vreeze
 */
class ConcreteFilmServiceH2Test extends AbstractFilmServiceH2Test {

    @Override
    protected FilmService filmService(EntityManagerFactory emf) {
        return new ConcreteFilmService(emf);
    }
}
