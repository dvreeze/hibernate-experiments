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

package eu.cdevreeze.hibernateexperiments.plainsql.model;

import module java.base;
import org.jspecify.annotations.Nullable;

/**
 * Immutable address {@link Record}.
 *
 * @author Chris de Vreeze
 */
public record Address(
        long id,
        String address1,
        @Nullable String address2,
        String district,
        City city,
        @Nullable String postalCode,
        String phone,
        Instant lastUpdate
) {

    public Optional<String> address2Option() {
        return Optional.ofNullable(address2);
    }

    public Optional<String> postalCodeOption() {
        return Optional.ofNullable(postalCode);
    }
}
