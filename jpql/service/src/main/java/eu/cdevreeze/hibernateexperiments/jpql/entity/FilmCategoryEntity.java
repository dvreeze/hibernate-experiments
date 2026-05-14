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

package eu.cdevreeze.hibernateexperiments.jpql.entity;

import module jakarta.persistence;
import module java.base;
import eu.cdevreeze.hibernateexperiments.jpql.model.FilmCategory;
import jakarta.persistence.Entity;

/**
 * Film-category JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "FilmCategory")
@Table(name = "Film_Category")
@IdClass(FilmCategoryKey.class)
public class FilmCategoryEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @Id
    @Column(name = "film_id")
    private Integer filmId;

    @Id
    @Column(name = "category_id")
    private Integer categoryId;

    // Fetch type lazy, due to global configuration of the default fetch type for to-one associations
    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public Integer getFilmId() {
        return filmId;
    }

    public void setFilmId(Integer filmId) {
        this.filmId = filmId;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public FilmCategory toModelObject() {
        return new FilmCategory(
                Objects.requireNonNull(filmId),
                Objects.requireNonNull(categoryId),
                Objects.requireNonNull(lastUpdate)
        );
    }
}
