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
 * Transactional service layer, as purely abstract API. This abstract API is entirely technology-agnostic,
 * thus allowing for quite different implementations. The methods in this API take and return immutable
 * objects, in particular immutable model objects. Being abstract interfaces, these service layer
 * contracts are also very easy to mock.
 *
 * @author Chris de Vreeze
 */
@NullMarked
package eu.cdevreeze.hibernateexperiments.plainsql.service;

import org.jspecify.annotations.NullMarked;