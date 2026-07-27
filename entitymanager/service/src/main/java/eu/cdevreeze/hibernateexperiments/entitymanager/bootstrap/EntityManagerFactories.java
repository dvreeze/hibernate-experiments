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

package eu.cdevreeze.hibernateexperiments.entitymanager.bootstrap;

import com.google.common.base.Preconditions;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.*;
import jakarta.persistence.*;

/**
 * Factory of {@link EntityManagerFactory} instances.
 *
 * @author Chris de Vreeze
 */
public class EntityManagerFactories {

    private EntityManagerFactories() {
        // Non-instantiable
    }

    public static EntityManagerFactory createEntityManagerFactory(String persistenceUnitName) {
        PersistenceConfiguration persistenceConfig = new PersistenceConfiguration(persistenceUnitName)
                .transactionType(PersistenceUnitTransactionType.RESOURCE_LOCAL)
                .defaultToOneFetchType(FetchType.LAZY)
                .provider("org.hibernate.jpa.HibernatePersistenceProvider")
                .property(PersistenceConfiguration.JDBC_DRIVER, "org.postgresql.Driver") // no connection pooling
                .property(Persistence.ConnectionProperties.JDBC_URL, "jdbc:postgresql://localhost:5432/pagila")
                .property(Persistence.ConnectionProperties.JDBC_USER, "postgres")
                .property(Persistence.ConnectionProperties.JDBC_PASSWORD, "postgres") // don't do this in production!
                .schemaManagementDatabaseAction(SchemaManagementAction.VALIDATE)
                .managedClass(AddressEntity.class)
                .managedClass(CityEntity.class)
                .managedClass(CountryEntity.class)
                .managedClass(ActorEntity.class)
                .managedClass(CategoryEntity.class)
                .managedClass(FilmEntity.class)
                .managedClass(FilmActorEntity.class)
                .managedClass(FilmCategoryEntity.class)
                .managedClass(LanguageEntity.class);
        Preconditions.checkState(
                persistenceConfig.defaultToOneFetchType() == FetchType.LAZY,
                "By all means, the default to-one fetch type must be set to LAZY"
        );
        return persistenceConfig.createEntityManagerFactory();
    }
}
