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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.sql.dao.column.Column;

/**
 * @author whilein
 */
public interface ForeignKey<T extends Enum<T> & Column> {

    static <T extends Enum<T> & Column> @NotNull ForeignKeyBuilder<T> builder(
            final @NotNull T column,
            final @NotNull String referenceTable,
            final @NotNull String referenceColumn
    ) {
        return DefaultForeignKey.builder(column, referenceTable, referenceColumn);
    }

    static <T extends Enum<T> & Column> @NotNull ForeignKey<T> create(
            final @NotNull T column,
            final @NotNull String referenceTable,
            final @NotNull String referenceColumn
    ) {
        return DefaultForeignKey.create(column, referenceTable, referenceColumn);
    }

    @NotNull T getColumn();

    @Nullable String getReferenceDatabase();

    @NotNull String getReferenceTable();

    @NotNull String getReferenceColumn();

    @NotNull ForeignKeyOption getOnDelete();

    @NotNull ForeignKeyOption getOnUpdate();


}
