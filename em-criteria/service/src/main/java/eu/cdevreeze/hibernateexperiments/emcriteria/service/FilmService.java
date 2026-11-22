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

package eu.cdevreeze.hibernateexperiments.emcriteria.service;

import module java.base;
import eu.cdevreeze.hibernateexperiments.emcriteria.model.Film;

/**
 * Abstract {@link Film}-related service API.
 * <p>
 * The query method must return immutable collections.
 *
 * @author Chris de Vreeze
 */
public interface FilmService {

    // In reality this would return too many results
    List<Film> findAllFilms();

    Optional<Film> findFilm(long filmId);

    List<Film> findFilmsByActorId(long actorId);
}
