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

/**
 * Program finding all countries in the database.
 *
 * @author Chris de Vreeze
 */
public class FindAllCountries {

    static void main(String... args) {
        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            AddressService addressService = AddressServiceFactory.create(emf);

            List<Country> countries = addressService.findAllCountries();

            countries.forEach(IO::println);
        }
    }
}
