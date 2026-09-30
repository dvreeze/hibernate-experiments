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

package eu.cdevreeze.hibernateexperiments.repository.entity;

import jakarta.persistence.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity listener logging creation/deletion of JPA sessions and session factories.
 *
 * @author Chris de Vreeze
 */
@EntityListener
public class MyEntityListener {

    // The alternative with EntityListeners annotation on the package did not work for PostCreate/PreClose.

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MyEntityListener() {
        logger.debug("MyEntityListener created");
    }

    @PostCreate
    public void postCreate(EntityManagerFactory emf) {
        logger.debug("PostCreate EntityManagerFactory {}", emf);
    }

    @PostCreate
    public void postCreate(EntityManager em) {
        logger.debug("PostCreate EntityManager {}", em);
    }

    @PostCreate
    public void postCreate(EntityAgent ea) {
        logger.debug("PostCreate EntityAgent {}", ea);
    }

    @PreClose
    public void preClose(EntityManagerFactory emf) {
        logger.debug("PreClose EntityManagerFactory {}", emf);
    }

    @PreClose
    public void preClose(EntityManager em) {
        logger.debug("PreClose EntityManager {}", em);
    }

    @PreClose
    public void preClose(EntityAgent ea) {
        logger.debug("PreClose EntityAgent {}", ea);
    }

    @PostLoad
    public void postLoad(Object obj) {
        logger.debug("PostLoad {}", obj);
    }
}
