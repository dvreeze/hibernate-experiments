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
import eu.cdevreeze.hibernateexperiments.jpql.model.Actor;
import jakarta.persistence.Entity;

/**
 * Actor JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Actor")
@Table(name = "Actor")
public class ActorEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "actor_id_seq")
    @SequenceGenerator(name = "actor_id_seq", sequenceName = "actor_actor_id_seq", allocationSize = 1)
    @Column(name = "actor_id")
    private Long id;

    @Basic(optional = false)
    @Column(name = "first_name")
    private String firstName;

    @Basic(optional = false)
    @Column(name = "last_name")
    private String lastName;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Actor toModelObject() {
        return new Actor(
                Objects.requireNonNull(id),
                Objects.requireNonNull(firstName),
                Objects.requireNonNull(lastName),
                Objects.requireNonNull(lastUpdate)
        );
    }
}
