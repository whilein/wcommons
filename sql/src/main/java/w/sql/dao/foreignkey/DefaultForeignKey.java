/*
 *    Copyright 2021 Whilein
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

package w.sql.dao.foreignkey;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultForeignKey implements ForeignKey {

    String references;
    ForeignKeyOption onDelete, onUpdate;

    public static @NotNull ForeignKeyBuilder builder(final @NotNull String references) {
        return new ForeignKeyBuilderImpl(references, ForeignKeyOption.NO_ACTION, ForeignKeyOption.NO_ACTION);
    }

    public static @NotNull ForeignKey create(final @NotNull String references) {
        return new DefaultForeignKey(references, ForeignKeyOption.NO_ACTION, ForeignKeyOption.NO_ACTION);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ForeignKeyBuilderImpl implements ForeignKeyBuilder {

        String references;

        @NonFinal
        ForeignKeyOption onDelete, onUpdate;

        @Override
        public @NotNull ForeignKeyBuilder onUpdate(final @NotNull ForeignKeyOption option) {
            this.onUpdate = option;
            return this;
        }

        @Override
        public @NotNull ForeignKeyBuilder onDelete(final @NotNull ForeignKeyOption option) {
            this.onDelete = option;
            return this;
        }

        @Override
        public @NotNull ForeignKey build() {
            return new DefaultForeignKey(references, onDelete, onUpdate);
        }
    }
}
