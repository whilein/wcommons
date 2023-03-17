/*
 *    Copyright 2023 Whilein
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package w.eventbus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
public interface NamespaceValidator {

    boolean isValid(@NotNull Object object);

    static @NotNull NamespaceValidator permitAll() {
        return PermitAll.INSTANCE;
    }

    static @NotNull NamespaceValidator permitInstanceOf(final @NotNull Class<?> type) {
        return new PermitInstanceOf(type);
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    final class PermitAll implements NamespaceValidator {

        private static final NamespaceValidator INSTANCE = new PermitAll();

        @Override
        public boolean isValid(final @NotNull Object object) {
            return true;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class PermitInstanceOf implements NamespaceValidator {
        Class<?> type;

        @Override
        public boolean isValid(final @NotNull Object object) {
            return type.isAssignableFrom(object.getClass());
        }

    }
}
