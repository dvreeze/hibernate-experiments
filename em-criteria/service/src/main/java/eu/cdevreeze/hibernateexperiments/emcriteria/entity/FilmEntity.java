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

package eu.cdevreeze.hibernateexperiments.emcriteria.entity;

import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emcriteria.model.Film;
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
    private Integer id;

    @Basic(optional = false)
    private String title;

    private String description;

    @Column(name = "release_year", columnDefinition = "year")
    private Year releaseYear;

    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private LanguageEntity language;

    @ManyToOne
    @JoinColumn(name = "original_language_id")
    private LanguageEntity originalLanguage;

    @Basic(optional = false)
    @Column(name = "rental_duration")
    private Short rentalDuration;

    @Basic(optional = false)
    @Column(name = "rental_rate")
    private BigDecimal rentalRate;

    private Short length;

    @Basic(optional = false)
    @Column(name = "replacement_cost")
    private BigDecimal replacementCost;

    private String rating;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    // TODO Special features and full text

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Year getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Year releaseYear) {
        this.releaseYear = releaseYear;
    }

    public LanguageEntity getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEntity language) {
        this.language = language;
    }

    public LanguageEntity getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(LanguageEntity originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public Short getRentalDuration() {
        return rentalDuration;
    }

    public void setRentalDuration(Short rentalDuration) {
        this.rentalDuration = rentalDuration;
    }

    public BigDecimal getRentalRate() {
        return rentalRate;
    }

    public void setRentalRate(BigDecimal rentalRate) {
        this.rentalRate = rentalRate;
    }

    public Short getLength() {
        return length;
    }

    public void setLength(Short length) {
        this.length = length;
    }

    public BigDecimal getReplacementCost() {
        return replacementCost;
    }

    public void setReplacementCost(BigDecimal replacementCost) {
        this.replacementCost = replacementCost;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

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
                Optional.ofNullable(length).map(Short::intValue).orElse(null),
                Objects.requireNonNull(replacementCost),
                rating,
                Objects.requireNonNull(lastUpdate),
                ImmutableList.of(),
                ""
        );
    }
}
