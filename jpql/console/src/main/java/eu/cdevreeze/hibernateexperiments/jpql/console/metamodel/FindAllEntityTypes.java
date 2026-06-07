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

package eu.cdevreeze.hibernateexperiments.jpql.console.metamodel;

import module eu.cdevreeze.hibernateexperiments.jpql.service;
import module java.base;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.metamodel.Attribute;
import jakarta.persistence.metamodel.EntityType;
import jakarta.persistence.metamodel.Metamodel;

import java.lang.annotation.Annotation;

/**
 * Program finding all entity types.
 *
 * @author Chris de Vreeze
 */
public class FindAllEntityTypes {

    static void main(String... args) {
        try (EntityManagerFactory emf = EntityManagerFactories.createEntityManagerFactory("pagila")) {
            Metamodel metamodel = Objects.requireNonNull(emf.getMetamodel());

            List<EntityType<?>> entityTypes = metamodel.getEntities()
                    .stream()
                    .sorted(Comparator.comparing(EntityType::getName))
                    .toList();

            IO.println("Found the following entity types in the persistence unit:");
            IO.println();
            entityTypes.forEach(tpe -> {
                IO.println(tpe + ":");
                IO.print("\tPersistence type: ");
                IO.println(tpe.getPersistenceType());
                IO.print("\tName: ");
                IO.println(tpe.getName());
                IO.print("\tJava class: ");
                IO.println(tpe.getBindableJavaType().getName());
                IO.println("\tAttributes: ");

                tpe.getAttributes().forEach(attr -> {
                    IO.print("\t\t");
                    IO.println(attr.toString());
                    IO.println("\t\t\tName: " + attr.getName());
                    IO.println("\t\t\tDeclaring type: " + attr.getDeclaringType());
                    IO.println("\t\t\tJava type: " + attr.getJavaType());
                    IO.println("\t\t\tJava member: " + attr.getJavaMember());
                    IO.println("\t\t\tPersistent attribute type: " + attr.getPersistentAttributeType());
                    IO.println("\t\t\tAssociation: " + attr.isAssociation());
                    IO.println("\t\t\tCollection: " + attr.isCollection());
                    printAnnotations(attr);
                });
            });
        }
    }

    private static void printAnnotations(Attribute<?, ?> attr) {
        Member attrAsMember = attr.getJavaMember();
        IO.println("\t\t\tAnnotations:");

        if (attrAsMember instanceof Field attrField) {
            for (Annotation annotation : attrField.getAnnotations()) {
                IO.println("\t\t\t\tAnnotation: " + annotation);
            }
        } else if (attrAsMember instanceof Method attrMethod) {
            for (Annotation annotation : attrMethod.getAnnotations()) {
                IO.println("\t\t\t\tAnnotation: " + annotation);
            }
        }
    }
}
