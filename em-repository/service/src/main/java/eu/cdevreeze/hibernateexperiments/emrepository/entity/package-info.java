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

/**
 * JPA {@link jakarta.persistence.Entity} classes. They are internal implementation details of
 * the service layer.
 * <p>
 * All entity associations have been configured to use lazy fetching. Fetching behavior is chosen per
 * query on an ad-hoc basis.
 * <p>
 * The owning side of associations is the side that contains the foreign key. So that would be the
 * ManyToOne side rather than the OneToMany side (if not OneToOne or ManyToMany). As a consequence,
 * the owning side is often the side opposite to the relationship as "aggregation". For example,
 * an order has line items, not the other way around, but a line item would have a foreign key to the order containing it.
 * Again, this shows the importance of lazy fetching by default. Also note that the immutable model
 * record classes are closer to "aggregations".
 * <p>
 * Note the {@link NullUnmarked} annotation, since nullability is everywhere in JPA entities.
 * In JPA entities, optionality of entity fields is expressed using JPA annotations.
 *
 * @author Chris de Vreeze
 */
@NullUnmarked
package eu.cdevreeze.hibernateexperiments.emrepository.entity;

import org.jspecify.annotations.NullUnmarked;