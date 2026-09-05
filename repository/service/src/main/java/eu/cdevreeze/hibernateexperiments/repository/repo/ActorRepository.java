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
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;

/**
 * {@link ActorEntity}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface ActorRepository {

    @Find
    Optional<ActorEntity> findById(Integer id);

    @Query("select a from Actor a join FilmActor fa on (fa.actor.id = a.id) where fa.film.id = :filmId")
    List<ActorEntity> findByFilmId(Integer filmId);

    @Find
    List<ActorEntity> findAllActors();
}
