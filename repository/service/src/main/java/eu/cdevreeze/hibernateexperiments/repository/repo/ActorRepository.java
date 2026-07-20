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
import org.hibernate.annotations.processing.HQL;

/**
 * {@link ActorEntity}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface ActorRepository {

    EntityAgent entityAgent(); // Just in case we need it

    @Find
    Optional<ActorEntity> findById(Integer id);

    // Query annotation not yet working as advertised?
    @HQL("select a from Actor a join FilmActor fa on (fa.actorId = a.id) where fa.filmId = :filmId")
    List<ActorEntity> findByFilmId(Integer filmId);

    @Find
    List<ActorEntity> findAllActors();
}
