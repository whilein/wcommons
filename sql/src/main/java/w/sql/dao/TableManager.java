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
import org.jetbrains.annotations.Nullable;
import w.flow.Flow;
import w.flow.FlowItems;
import w.flow.IntFlow;
import w.sql.dao.column.Column;
import w.sql.dao.property.Properties;

import java.util.function.Consumer;

/**
 * @author whilein
 */
public interface TableManager<T extends Enum<T> & Column> {

    @NotNull Flow<@NotNull Properties<T>> getAll(@NotNull Object id);

    <V> @NotNull Flow<V> get(@NotNull Object id, @NotNull T column);

    @NotNull FlowItems<@NotNull Properties<T>> getAllBy(@NotNull T column, @Nullable Object value);

    <V> @NotNull FlowItems<@NotNull V> getBy(@NotNull T column, @Nullable Object value, @NotNull T another);

    @NotNull IntFlow insert(@NotNull Consumer<@NotNull Properties<T>> properties);

    @NotNull IntFlow delete(@NotNull Object id);

    @NotNull IntFlow update(@NotNull Object id, @NotNull T column, @Nullable Object value);


}
