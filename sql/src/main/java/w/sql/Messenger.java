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

package w.sql;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.flow.FlowItems;
import w.flow.IntFlow;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author whilein
 */
public interface Messenger {

    @NotNull ScriptRunner scriptRunner();

    @NotNull DataSource getDataSource();

    @NotNull Connection getConnection() throws SQLException;

    @NotNull IntFlow update(@NotNull String query, @Nullable Object @NotNull ... parameters);

    @NotNull IntFlow update(@NotNull String query);

    @NotNull IntFlow updateWithKeys(@NotNull String query, @Nullable Object @NotNull ... parameters);

    @NotNull IntFlow updateWithKeys(@NotNull String query);

    @NotNull FlowItems<@NotNull ResultSet> fetch(@NotNull String query, @Nullable Object @NotNull ... parameters);

    @NotNull FlowItems<@NotNull ResultSet> fetch(@NotNull String query);

    void close();

}
