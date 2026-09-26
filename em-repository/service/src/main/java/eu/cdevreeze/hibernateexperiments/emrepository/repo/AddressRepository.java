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

    String BASE_QUERY = "select ad from Address ad join fetch ad.city ct join fetch ct.country co";
    String BASE_CITY_QUERY = "select ci from City ci join fetch ci.country co";

    // This makes the repository a stateful one
    @Refresh
    void refresh(AddressEntity address);

    @Query(BASE_QUERY + " where ad.id = :id")
    Optional<AddressEntity> findById(Integer id);

    @Query(BASE_QUERY + " where ct.id = :cityId")
    List<AddressEntity> findByCityId(Integer cityId);

    @Query(BASE_QUERY + " where co.id = :countryId")
    List<AddressEntity> findByCountryId(Integer countryId);

    // In reality this would return too many results
    @Query(BASE_QUERY)
    List<AddressEntity> findAllAddresses();

    @Query(BASE_CITY_QUERY + " where co.id = :countryId")
    List<CityEntity> findCitiesByCountryId(Integer countryId);

    @Query(BASE_CITY_QUERY + " where ci.id = :id")
    Optional<CityEntity> findCityById(Integer id);

    @Find
    List<CountryEntity> findAllCountries();

    @Persist
    void add(AddressEntity address);
}
