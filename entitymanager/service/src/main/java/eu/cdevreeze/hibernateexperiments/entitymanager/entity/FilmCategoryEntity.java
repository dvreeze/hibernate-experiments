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

package eu.cdevreeze.hibernateexperiments.entitymanager.entity;

import module jakarta.persistence;
import module java.base;
import jakarta.persistence.Entity;

/**
 * Film-category JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "FilmCategory")
@Table(name = "Film_Category")
public class FilmCategoryEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @EmbeddedId
    @AttributeOverride(name = "categoryId", column = @Column(name = "category_id"))
    @AttributeOverride(name = "filmId", column = @Column(name = "film_id"))
    private FilmCategoryKey filmCategoryKey;

    @MapsId("filmId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "film_id")
    private FilmEntity film;

    @MapsId("categoryId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public FilmCategoryKey getFilmCategoryKey() {
        return filmCategoryKey;
    }

    public void setFilmCategoryKey(FilmCategoryKey filmCategoryKey) {
        this.filmCategoryKey = filmCategoryKey;
    }

    public FilmEntity getFilm() {
        return film;
    }

    public void setFilm(FilmEntity film) {
        this.film = film;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }
}
