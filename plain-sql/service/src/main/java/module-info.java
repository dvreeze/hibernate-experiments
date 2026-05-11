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

/**
 * Module descriptor of the service layer API. The service layer API and immutable model are exported
 * (along with the JPA bootstrapping code). Other than JPA bootstrapping, implementation details are
 * not exported to any other modules.
 *
 * @author Chris de Vreeze
 */
module eu.cdevreeze.hibernateexperiments.plainsql.service {
    requires com.google.common;
    requires jakarta.persistence;
    requires org.jspecify;
    requires tools.jackson.databind;
    requires tools.jackson.datatype.guava;
    requires transitive eu.cdevreeze.hibernateexperiments.plainsql.model;

    exports eu.cdevreeze.hibernateexperiments.plainsql.bootstrap;
    exports eu.cdevreeze.hibernateexperiments.plainsql.service;
    exports eu.cdevreeze.hibernateexperiments.plainsql.service.factory;
}