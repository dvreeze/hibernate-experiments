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
 * Note the {@link org.jspecify.annotations.NullUnmarked} annotation, since nullability is everywhere in JPA entities.
 * In JPA entities, optionality of entity fields is expressed using JPA annotations.
 *
 * @author Chris de Vreeze
 */
@NullUnmarked
package eu.cdevreeze.hibernateexperiments.repository.entity;

import org.jspecify.annotations.NullUnmarked;