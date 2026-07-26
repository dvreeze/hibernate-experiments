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

package eu.cdevreeze.hibernateexperiments.jpql;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaEnumConstant;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import jakarta.persistence.*;

import java.util.Optional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;

/**
 * Hibernate architecture test.
 *
 * @author Chris de Vreeze
 */
@AnalyzeClasses(
        packagesOf = HibernateArchitectureTest.class,
        importOptions = ImportOption.DoNotIncludeTests.class
)
class HibernateArchitectureTest {

    private static DescribedPredicate<JavaAnnotation<?>> isEagerToManyAssociation() {
        return new DescribedPredicate<>("eager to-many association") {

            @Override
            public boolean test(JavaAnnotation<?> javaAnnotation) {
                String annotationName = javaAnnotation.getRawType().getFullName();
                boolean isToManyAssociation = annotationName.equals("jakarta.persistence.OneToMany")
                        || annotationName.equals("jakarta.persistence.ManyToMany");
                Optional<Object> optionalFetchType = javaAnnotation.get("fetch");
                return isToManyAssociation && optionalFetchType.isPresent() &&
                        optionalFetchType.get() instanceof JavaEnumConstant enumConstant &&
                        enumConstant.name().equals("EAGER");
            }
        };
    }

    private static DescribedPredicate<JavaAnnotation<?>> isExplicitlyEagerToOneAssociation() {
        return new DescribedPredicate<>("explicit eager to-one association") {

            @Override
            public boolean test(JavaAnnotation<?> javaAnnotation) {
                String annotationName = javaAnnotation.getRawType().getFullName();
                boolean isToOneAssociation = annotationName.equals("jakarta.persistence.OneToOne")
                        || annotationName.equals("jakarta.persistence.ManyToOne");
                Optional<Object> optionalFetchType = javaAnnotation.get("fetch");
                return isToOneAssociation && optionalFetchType.isPresent() &&
                        optionalFetchType.get() instanceof JavaEnumConstant enumConstant &&
                        enumConstant.name().equals("EAGER");
            }
        };
    }

    // Jakarta Persistence entities etc. must not depend on "infrastructure objects".

    @ArchTest
    static final ArchRule entitiesShouldNotDependOnEntityManager =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .or().areAnnotatedWith(Embedded.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .should().onlyDependOnClassesThat().areNotAssignableTo(EntityManager.class);

    @ArchTest
    static final ArchRule entitiesShouldNotDependOnEntityManagerFactory =
            classes()
                    .that().areAnnotatedWith(Entity.class)
                    .or().areAnnotatedWith(MappedSuperclass.class)
                    .or().areAnnotatedWith(Embedded.class)
                    .or().areAnnotatedWith(Embeddable.class)
                    .should().onlyDependOnClassesThat().areNotAssignableTo(EntityManagerFactory.class);

    @ArchTest
    static final ArchRule toManyAssociationsShouldNotBeEager =
            fields()
                    .that().areAnnotatedWith(OneToMany.class)
                    .or().areAnnotatedWith(ManyToMany.class)
                    .should().notBeAnnotatedWith(isEagerToManyAssociation())
                    .allowEmptyShould(true);

    // This is not enough. It should also be checked that the default for to-one associations has been set to lazy.
    @ArchTest
    static final ArchRule toOneAssociationsShouldNotExplicitlyBeEager =
            fields()
                    .that().areAnnotatedWith(OneToOne.class)
                    .or().areAnnotatedWith(ManyToOne.class)
                    .should().notBeAnnotatedWith(isExplicitlyEagerToOneAssociation());
}
