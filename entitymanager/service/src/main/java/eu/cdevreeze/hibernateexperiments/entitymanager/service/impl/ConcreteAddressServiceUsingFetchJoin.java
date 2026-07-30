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

package eu.cdevreeze.hibernateexperiments.entitymanager.service.impl;

import module eu.cdevreeze.hibernateexperiments.entitymanager.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.entity.CountryEntity;
import eu.cdevreeze.hibernateexperiments.entitymanager.service.AddressService;

/**
 * Concrete {@link AddressService} implementation, using fetch joins.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressServiceUsingFetchJoin implements AddressService {

    private final EntityManagerFactory emf;

    public ConcreteAddressServiceUsingFetchJoin(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co where ad.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, id)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co where ct.id = ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, cityId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co where co.id =  ?1";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString =
                    "select ad from Address ad join fetch ad.city ct join fetch ct.country co";

            return entityManager.createQuery(qlString, AddressEntity.class)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString =
                    "select ci from City ci join fetch ci.country co where co.id = ?1";

            return entityManager.createQuery(qlString, CityEntity.class)
                    .setParameter(1, countryId)
                    .getResultStream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            String qlString = "select c from Country c";

            return entityManager.createQuery(qlString, CountryEntity.class)
                    .getResultStream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Address add(Address.NewAddress address) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CityEntity cityEntity = findCityEntityById((int) address.cityId(), entityManager);

            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setAddress(address.address1());
            addressEntity.setAddress2(address.address2());
            addressEntity.setDistrict(address.district());
            addressEntity.setCity(cityEntity);
            addressEntity.setPostalCode(address.postalCode());
            addressEntity.setPhone(address.phone());
            addressEntity.setLastUpdate(address.lastUpdate());

            entityManager.persist(addressEntity);
            return addressEntity.toModelObject();
        });
    }

    private CityEntity findCityEntityById(int cityId, EntityManager entityManager) {
        String qlString = "select ci from City ci join fetch ci.country co where ci.id = :id";

        return entityManager.createQuery(qlString, CityEntity.class)
                .setParameter("id", cityId)
                .getSingleResult();
    }
}
