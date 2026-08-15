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
import eu.cdevreeze.hibernateexperiments.jpql.model.Address;
import jakarta.persistence.Entity;

/**
 * Address JPA {@link Entity}.
 *
 * @author Chris de Vreeze
 */
@Entity(name = "Address")
@Table(name = "Address")
public class AddressEntity {

    // Note that the entity class is not Serializable
    // Note the absence of overridden equals and hashCode

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "address_id_seq")
    @SequenceGenerator(name = "address_id_seq", sequenceName = "address_address_id_seq", allocationSize = 1)
    @Column(name = "address_id")
    private Integer id;

    @Basic(optional = false)
    private String address;

    private String address2;

    @Basic(optional = false)
    private String district;

    // Fetch type lazy, due to global configuration of the default fetch type for to-one associations
    @ManyToOne(optional = false)
    @JoinColumn(name = "city_id", nullable = false)
    private CityEntity city;

    @Column(name = "postal_code")
    private String postalCode;

    @Basic(optional = false)
    private String phone;

    @Basic(optional = false)
    @Column(name = "last_update")
    private Instant lastUpdate;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(String address2) {
        this.address2 = address2;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public CityEntity getCity() {
        return city;
    }

    public void setCity(CityEntity city) {
        this.city = city;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Instant getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Instant lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Address toModelObject() {
        return new Address(
                Objects.requireNonNull(id),
                Objects.requireNonNull(address),
                address2,
                Objects.requireNonNull(district),
                Objects.requireNonNull(city).toModelObject(),
                postalCode,
                Objects.requireNonNull(phone),
                Objects.requireNonNull(lastUpdate)
        );
    }
}
