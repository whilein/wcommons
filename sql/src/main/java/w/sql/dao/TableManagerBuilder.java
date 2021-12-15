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

package w.sql.dao;

import org.jetbrains.annotations.NotNull;
import w.sql.dao.column.Column;
import w.sql.dao.foreignkey.ForeignKey;

/**
 * @author whilein
 */
public interface TableManagerBuilder<T extends Enum<T> & Column> {

    @NotNull TableManagerBuilder<T> database(@NotNull String database);

    @NotNull TableManagerBuilder<T> id(@NotNull T column);

    @NotNull TableManagerBuilder<T> primaryKey(@NotNull T @NotNull ... columns);

    @NotNull TableManagerBuilder<T> uniqueKey(@NotNull T @NotNull ... columns);

    @NotNull TableManagerBuilder<T> foreignKey(@NotNull ForeignKey foreignKey);

    @NotNull TableManager<T> build();

}
