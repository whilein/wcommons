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
import w.sql.dao.column.Column;

/**
 * @author whilein
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultForeignKey<T extends Enum<T> & Column> implements ForeignKey<T> {

    T column;
    String referenceDatabase, referenceTable, referenceColumn;
    ForeignKeyOption onDelete, onUpdate;

    public static <T extends Enum<T> & Column> @NotNull ForeignKeyBuilder<T> builder(
            final @NotNull T column,
            final @NotNull String referenceTable,
            final @NotNull String referenceColumn
    ) {
        return new ForeignKeyBuilderImpl<>(column, null, referenceTable, referenceColumn,
                ForeignKeyOption.NO_ACTION, ForeignKeyOption.NO_ACTION);
    }

    public static <T extends Enum<T> & Column> @NotNull ForeignKey<T> create(
            final @NotNull T column,
            final @NotNull String referenceTable,
            final @NotNull String referenceColumn
    ) {
        return new DefaultForeignKey<>(column, null, referenceTable, referenceColumn,
                ForeignKeyOption.NO_ACTION, ForeignKeyOption.NO_ACTION);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ForeignKeyBuilderImpl<T extends Enum<T> & Column> implements ForeignKeyBuilder<T> {

        T column;

        @NonFinal
        String referenceDatabase;

        String referenceTable;

        String referenceColumn;

        @NonFinal
        ForeignKeyOption onDelete, onUpdate;

        @Override
        public @NotNull ForeignKeyBuilder<T> referenceDatabase(final @NotNull String database) {
            this.referenceDatabase = database;

            return this;
        }

        @Override
        public @NotNull ForeignKeyBuilder<T> onUpdate(final @NotNull ForeignKeyOption option) {
            this.onUpdate = option;
            return this;
        }

        @Override
        public @NotNull ForeignKeyBuilder<T> onDelete(final @NotNull ForeignKeyOption option) {
            this.onDelete = option;
            return this;
        }

        @Override
        public @NotNull ForeignKey<T> build() {
            return new DefaultForeignKey<>(column, referenceDatabase, referenceTable, referenceColumn,
                    onDelete, onUpdate);
        }
    }
}
