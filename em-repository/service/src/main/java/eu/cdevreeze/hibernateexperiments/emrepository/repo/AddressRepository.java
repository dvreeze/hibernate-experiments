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

package eu.cdevreeze.hibernateexperiments.emrepository.repo;

import module java.base;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.emrepository.entity.CountryEntity;
import jakarta.data.repository.Find;
import jakarta.data.repository.Query;
import jakarta.data.repository.Repository;
import jakarta.data.repository.stateful.Persist;
import jakarta.data.repository.stateful.Refresh;

/**
 * {@link AddressEntity}-related Jakarta Data Repository.
 *
 * @author Chris de Vreeze
 */
@Repository
public interface AddressRepository {

    // This makes the repository a stateful one
    @Refresh
    void refresh(AddressEntity address);

    // Query annotation not yet working as advertised?
    @Query("select ad from Address ad join fetch ad.city ct join fetch ct.country co where ad.id = :id")
    Optional<AddressEntity> findById(Integer id);

    // Query annotation not yet working as advertised?
    @Query("select ad from Address ad join fetch ad.city ct join fetch ct.country co where ct.id = :cityId")
    List<AddressEntity> findByCityId(Integer cityId);

    // Query annotation not yet working as advertised?
    @Query("select ad from Address ad join fetch ad.city ct join fetch ct.country co where co.id = :countryId")
    List<AddressEntity> findByCountryId(Integer countryId);

    // Query annotation not yet working as advertised?
    // In reality this would return too many results
    @Query("select ad from Address ad join fetch ad.city ct join fetch ct.country co")
    List<AddressEntity> findAllAddresses();

    // Query annotation not yet working as advertised?
    @Query("select ci from City ci join fetch ci.country co where co.id = :countryId")
    List<CityEntity> findCitiesByCountryId(Integer countryId);

    // Query annotation not yet working as advertised?
    @Query("select ci from City ci join fetch ci.country co where ci.id = :id")
    Optional<CityEntity> findCityById(Integer id);

    @Find
    List<CountryEntity> findAllCountries();

    @Persist
    void add(AddressEntity address);
}
