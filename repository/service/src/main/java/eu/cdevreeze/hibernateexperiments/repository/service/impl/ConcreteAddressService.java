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

package eu.cdevreeze.hibernateexperiments.repository.service.impl;

import module eu.cdevreeze.hibernateexperiments.repository.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.repository.entity.AddressEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CityEntity;
import eu.cdevreeze.hibernateexperiments.repository.entity.CountryEntity;
import eu.cdevreeze.hibernateexperiments.repository.repo.AddressRepository;
import eu.cdevreeze.hibernateexperiments.repository.repo._AddressRepository;
import eu.cdevreeze.hibernateexperiments.repository.service.AddressService;

/**
 * Concrete {@link AddressService} implementation.
 *
 * @author Chris de Vreeze
 */
public final class ConcreteAddressService implements AddressService {

    private final EntityManagerFactory emf;

    public ConcreteAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findById((int) id).map(AddressEntity::toModelObject);
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findByCityId((int) cityId)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findByCountryId((int) countryId)
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findAllAddresses()
                    .stream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findCitiesByCountryId((int) countryId)
                    .stream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            return addressRepository.findAllCountries()
                    .stream()
                    .map(CountryEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public Address add(Address.NewAddress address) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(EntityAgent.class, entityAgent -> {
            AddressRepository addressRepository = new _AddressRepository(entityAgent);
            CityEntity cityEntity = addressRepository.findCityById((int) address.cityId()).orElseThrow();

            AddressEntity addressEntity = new AddressEntity();
            addressEntity.setAddress(address.address1());
            addressEntity.setAddress2(address.address2());
            addressEntity.setDistrict(address.district());
            addressEntity.setCity(cityEntity);
            addressEntity.setPostalCode(address.postalCode());
            addressEntity.setPhone(address.phone());
            addressEntity.setLastUpdate(address.lastUpdate());

            addressEntity = addressRepository.add(addressEntity);
            return addressEntity.toModelObject();
        });
    }
}
