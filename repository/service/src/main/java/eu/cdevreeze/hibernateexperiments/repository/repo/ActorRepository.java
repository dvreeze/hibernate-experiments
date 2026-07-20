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

package eu.cdevreeze.hibernateexperiments.repository.repo;

import module java.base;
import eu.cdevreeze.hibernateexperiments.repository.entity.ActorEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.persistence.EntityAgent;

/**
 * {@link ActorEntity}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface ActorRepository {

    EntityAgent entityAgent();

    @Find
    Optional<ActorEntity> findById(Integer id);

    // Query annotation not yet working as advertised?
    default List<ActorEntity> findByFilmId(Integer filmId) {
        // Lost type-safe query parsing
        String qlString = "select a from Actor a join FilmActor fa on (fa.actorId = a.id) where fa.filmId = :filmId";
        return entityAgent()
                .createQuery(qlString, ActorEntity.class)
                .setParameter("filmId", filmId)
                .getResultList();
    }

    @Find
    List<ActorEntity> findAllActors();
}
