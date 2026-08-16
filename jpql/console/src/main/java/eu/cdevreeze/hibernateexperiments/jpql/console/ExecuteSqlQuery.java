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

package eu.cdevreeze.hibernateexperiments.jpql.console;

import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.bootstrap.EntityManagerFactories;
import eu.cdevreeze.hibernateexperiments.jpql.service.GeneralSqlQueryService;
import eu.cdevreeze.hibernateexperiments.jpql.service.factory.GeneralSqlQueryServiceFactory;
import jakarta.persistence.EntityManagerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

/**
 * Program executing a given native SQL query. This program takes a path to the file containing the
 * SQL query string. Currently, no query arguments can be passed.
 * <p>
 * Often, a good result type to choose is {@link jakarta.persistence.Tuple}.
 *
 * @author Chris de Vreeze
 */
public class ExecuteSqlQuery {

    static void main(String... args) throws IOException, ClassNotFoundException {
        Objects.checkIndex(1, args.length);
        Path sqlQueryPath = Path.of(args[0]);
        String sqlQuery = Files.readString(sqlQueryPath);
        Class<?> clazz = Class.forName(args[1]);

        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            GeneralSqlQueryService queryService = GeneralSqlQueryServiceFactory.create(emf);

            List<?> results = queryService.executeSqlQuery(sqlQuery, clazz, ImmutableList.of());

            results.forEach(IO::println);
        }
    }
}
