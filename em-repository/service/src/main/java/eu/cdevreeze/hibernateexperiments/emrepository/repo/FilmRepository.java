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

package eu.cdevreeze.hibernateexperiments.emrepository.repo;

import module java.base;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.FilmEntity;
import eu.cdevreeze.hibernateexperiments.emrepository.model.Film;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.stateful.Refresh;

/**
 * {@link Film}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface FilmRepository {

    // This makes the repository a stateful one
    @Refresh
    void refresh(FilmEntity film);

    // In reality this would return too many results
    @Query("""
            select f from Film f
              left join fetch f.filmActors fac
              left join fetch fac.actor
              left join fetch f.filmCategories fca
              left join fetch fca.category
              left join fetch f.language
              left join fetch f.originalLanguage""")
    List<FilmEntity> findAllFilms();

    @Query("""
            select f from Film f
              left join fetch f.filmActors fac
              left join fetch fac.actor
              left join fetch f.filmCategories fca
              left join fetch fca.category
              left join fetch f.language
              left join fetch f.originalLanguage
             where f.id = :filmId""")
    Optional<FilmEntity> findFilm(int filmId);

    @Query("""
            select f from Film f
              left join fetch f.filmActors fac
              left join fetch fac.actor
              left join fetch f.filmCategories fca
              left join fetch fca.category
              left join fetch f.language
              left join fetch f.originalLanguage
              left join f.filmActors fa
             where fa.actor.id = :actorId""")
    List<FilmEntity> findFilmsByActorId(int actorId);
}
