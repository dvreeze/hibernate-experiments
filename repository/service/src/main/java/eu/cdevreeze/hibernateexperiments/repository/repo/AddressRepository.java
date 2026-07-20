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

package eu.cdevreeze.hibernateexperiments.repository.repo;

import module java.base;
import eu.cdevreeze.hibernateexperiments.repository.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CountryEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Repository;
import jakarta.persistence.EntityAgent;

/**
 * {@link AddressEntity}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface AddressRepository {

    EntityAgent entityAgent();

    @Find
    Optional<AddressEntity> findById(Integer id);

    // Query annotation not yet working as advertised?
    default List<AddressEntity> findByCityId(Integer cityId) {
        // Lost type-safe query parsing
        String qlString = "select ad from Address ad join fetch ad.city ct join fetch ct.country co where ct.id = :cityId";
        return entityAgent()
                .createQuery(qlString, AddressEntity.class)
                .setParameter("cityId", cityId)
                .getResultList();
    }

    // Query annotation not yet working as advertised?
    default List<AddressEntity> findByCountryId(Integer countryId) {
        // Lost type-safe query parsing
        String qlString = "select ad from Address ad join fetch ad.city ct join fetch ct.country co where co.id = :countryId";
        return entityAgent()
                .createQuery(qlString, AddressEntity.class)
                .setParameter("countryId", countryId)
                .getResultList();
    }

    // Query annotation not yet working as advertised?
    // In reality this would return too many results
    default List<AddressEntity> findAllAddresses() {
        // Lost type-safe query parsing
        String qlString = "select ad from Address ad join fetch ad.city ct join fetch ct.country co";
        return entityAgent()
                .createQuery(qlString, AddressEntity.class)
                .getResultList();
    }

    // Query annotation not yet working as advertised?
    default List<CityEntity> findCitiesByCountryId(Integer countryId) {
        // Lost type-safe query parsing
        String qlString = "select ci from City ci join fetch ci.country co where co.id = :countryId";
        return entityAgent()
                .createQuery(qlString, CityEntity.class)
                .setParameter("countryId", countryId)
                .getResultList();
    }

    @Find
    List<CountryEntity> findAllCountries();
}
