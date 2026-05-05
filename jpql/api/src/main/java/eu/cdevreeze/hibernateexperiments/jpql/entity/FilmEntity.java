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
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.jpql.model.Film;
import jakarta.persistence.Entity;

/**
 * Film JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Film")
@Table(name = "Film")
public class FilmEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "film_id_seq")
    @SequenceGenerator(name = "film_id_seq", sequenceName = "film_film_id_seq", allocationSize = 1)
    @Column(name = "film_id")
    private Long id;

    @Basic(optional = false)
    private String title;

    private String description;

    // Does this work out of the box?
    @Column(name = "release_year")
    private Year releaseYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @ManyToOne
    @JoinColumn(name = "original_language_id")
    private LanguageEntity originalLanguage;

    @Basic(optional = false)
    @Column(name = "rental_duration")
    private Integer rentalDuration;

    @Basic(optional = false)
    @Column(name = "rental_rate")
    private BigDecimal rentalRate;

    private Integer length;

    @Basic(optional = false)
    @Column(name = "replacement_cost")
    private BigDecimal replacementCost;

    private String rating;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // TODO Special features and full text

    public Film toModelObject() {
        return new Film(
                Objects.requireNonNull(id),
                Objects.requireNonNull(title),
                description,
                releaseYear,
                Objects.requireNonNull(language).toModelObject(),
                Optional.ofNullable(originalLanguage).map(LanguageEntity::toModelObject).orElse(null),
                Objects.requireNonNull(rentalDuration),
                rentalRate,
                length,
                Objects.requireNonNull(replacementCost),
                rating,
                Objects.requireNonNull(lastUpdate),
                ImmutableList.of(),
                ""
        );
    }
}
