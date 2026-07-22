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

package eu.cdevreeze.hibernateexperiments.emcriteria.service.impl;

import module eu.cdevreeze.hibernateexperiments.emcriteria.model;
import module jakarta.persistence;
import module java.base;
import com.google.common.collect.ImmutableList;
import eu.cdevreeze.hibernateexperiments.emcriteria.entity.*;
import eu.cdevreeze.hibernateexperiments.emcriteria.service.AddressService;
import org.hibernate.jpa.SpecHints;

/**
 * The same as {@link ConcreteAddressService}, except for the absence of {@link EntityGraph}'s.
 * This minor code change alone makes the number of generated SQL queries explode!
 *
 * @author Chris de Vreeze
 */
public final class InefficientAddressService implements AddressService {

    // Yet it is not in the (current) JPA 4.0 API documentation.
    // Also, what does it mean with "returning only one result"? What I did below still seems to work in avoiding the 1 + N problem.

    private final EntityManagerFactory emf;

    public InefficientAddressService(EntityManagerFactory emf) {
        this.emf = emf;
    }

    @Override
    public Optional<Address> findById(long id) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.id), id));
            cq.select(address);

            return entityManager.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .findFirst();
        });
    }

    @Override
    public ImmutableList<Address> findByCityId(long cityId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.id), cityId));
            cq.select(address);

            return entityManager.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.where(cb.equal(address.get(AddressEntity_.city).get(CityEntity_.country).get(CountryEntity_.id), countryId));
            cq.select(address);

            return entityManager.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Address> findAll() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<AddressEntity> cq = cb.createQuery(AddressEntity.class);

            Root<AddressEntity> address = cq.from(AddressEntity.class);
            cq.select(address);

            return entityManager.createQuery(cq)
                    .getResultStream()
                    .map(AddressEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<City> findCitiesByCountryId(long countryId) {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CityEntity> cq = cb.createQuery(CityEntity.class);

            Root<CityEntity> city = cq.from(CityEntity.class);
            cq.where(cb.equal(city.get(CityEntity_.country).get(CountryEntity_.id), countryId));
            cq.select(city);

            return entityManager.createQuery(cq)
                    .getResultStream()
                    .map(CityEntity::toModelObject)
                    .collect(ImmutableList.toImmutableList());
        });
    }

    @Override
    public ImmutableList<Country> findAllCountries() {
        // This starts a new transaction in our case of resource-local transactions
        return emf.callInTransaction(entityManager -> {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<CountryEntity> cq = cb.createQuery(CountryEntity.class);

            Root<CountryEntity> country = cq.from(CountryEntity.class);
            cq.select(country);

            return entityManager.createQuery(cq)
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
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CityEntity> cq = cb.createQuery(CityEntity.class);

        Root<CityEntity> city = cq.from(CityEntity.class);
        cq.where(cb.equal(city.get(CityEntity_.id), cityId));
        cq.select(city);

        // Here we do set the load graph
        EntityGraph<CityEntity> entityGraph = CityEntity_.class_.createEntityGraph();
        entityGraph.addAttributeNode(CityEntity_.country);

        // This sets the load graph, not the fetch graph
        // Yet that makes no difference here since we configured lazy fetching for all entity associations
        return entityManager.createQuery(cq)
                .setHint(SpecHints.HINT_SPEC_LOAD_GRAPH, entityGraph)
                .getSingleResult();
    }
}
