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

package eu.cdevreeze.hibernateexperiments.entitymanager.model;

import module java.base;
import com.google.common.collect.ImmutableList;
import org.jspecify.annotations.Nullable;

/**
 * Immutable film {@link Record}.
 *
 * @author Chris de Vreeze
 */
public record Film(
        long id,
        String title,
        @Nullable String description,
        @Nullable Year releaseYear,
        Language language,
        @Nullable Language originalLanguage,
        int rentalDuration,
        BigDecimal rentalRate,
        @Nullable Integer length,
        BigDecimal replacementCost,
        @Nullable String rating,
        Instant lastUpdate,
        @Nullable ImmutableList<String> specialFeatures,
        String fullText,
        ImmutableList<Actor> actors,
        ImmutableList<Category> categories
) {

    public Optional<String> descriptionOption() {
        return Optional.ofNullable(description);
    }

    public Optional<Year> releaseYearOption() {
        return Optional.ofNullable(releaseYear);
    }

    public Optional<Language> originalLanguageOption() {
        return Optional.ofNullable(originalLanguage);
    }

    public OptionalInt lengthOption() {
        return Optional.ofNullable(length).stream().mapToInt(i -> i).findFirst();
    }

    public Optional<String> ratingOption() {
        return Optional.ofNullable(rating);
    }

    public Optional<ImmutableList<String>> specialFeaturesOption() {
        return Optional.ofNullable(specialFeatures);
    }
}
