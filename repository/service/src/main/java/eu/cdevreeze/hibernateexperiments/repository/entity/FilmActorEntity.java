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

import module jakarta.persistence;
import module java.base;
import eu.cdevreeze.hibernateexperiments.repository.model.FilmActor;
import jakarta.persistence.Entity;

/**
 * Film-actor JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "FilmActor")
@Table(name = "Film_Actor")
@IdClass(FilmActorKey.class)
public class FilmActorEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    // We intentionally kept the primary key "specification" simple, at the expense of having no associations.

    @Id
    @Column(name = "actor_id")
    private Integer actorId;

    @Id
    @Column(name = "film_id")
    private Integer filmId;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public Integer getActorId() {
        return actorId;
    }

    public void setActorId(Integer actorId) {
        this.actorId = actorId;
    }

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public FilmActor toModelObject() {
        // Mind the order of IDs, which differs between entity and model object
        return new FilmActor(
                Objects.requireNonNull(filmId),
                Objects.requireNonNull(actorId),
                Objects.requireNonNull(lastUpdate)
        );
    }
}
