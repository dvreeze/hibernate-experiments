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

package eu.cdevreeze.hibernateexperiments.plainsql.console;

import module java.base;
import eu.cdevreeze.hibernateexperiments.plainsql.bootstrap.EntityManagerFactories;
import eu.cdevreeze.hibernateexperiments.plainsql.model.City;
import eu.cdevreeze.hibernateexperiments.plainsql.service.AddressService;
import eu.cdevreeze.hibernateexperiments.plainsql.service.impl.ConcreteAddressService;
import jakarta.persistence.EntityManagerFactory;

/**
 * Program finding all cities of a given country (given as country ID).
 *
 * @author Chris de Vreeze
 */
public class FindCitiesByCountryId {

    static void main(String... args) {
        Objects.checkIndex(0, args.length);
        long countryId = Long.parseLong(args[0]);

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            AddressService addressService = new ConcreteAddressService(emf);

            List<City> cities = addressService.findCitiesByCountryId(countryId);

            cities.forEach(System.out::println);
        }
    }
}
