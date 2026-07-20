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
import eu.cdevreeze.hibernateexperiments.repository.model.Language;
import jakarta.persistence.Entity;

/**
 * Language JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Language")
@Table(name = "Language")
public class LanguageEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "language_id_seq")
    @SequenceGenerator(name = "language_id_seq", sequenceName = "language_language_id_seq", allocationSize = 1)
    @Column(name = "language_id")
    private Integer id;

    @Basic(optional = false)
    @Column(columnDefinition = "bpchar")
    private String name;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Language toModelObject() {
        return new Language(
                Objects.requireNonNull(id),
                Objects.requireNonNull(name),
                Objects.requireNonNull(lastUpdate)
        );
    }
}
