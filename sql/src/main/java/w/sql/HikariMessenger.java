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

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.flow.FlowItems;
import w.flow.Flows;
import w.flow.IntFlow;
import w.flow.IntFlows;
import w.flow.function.FlowConsumer;
import w.flow.function.FlowSink;
import w.flow.function.IntFlowSupplier;
import w.util.ClassLoaderUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static java.sql.PreparedStatement.RETURN_GENERATED_KEYS;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class HikariMessenger implements Messenger {

    @Getter
    HikariDataSource dataSource;

    public static @NotNull Messenger create(
            final @NonNull HikariConfig config
    ) {
        return new HikariMessenger(new HikariDataSource(config));
    }

    public static @NotNull Messenger create(
            final @NonNull String jdbc,
            final @NonNull String user,
            final @NonNull String password
    ) {
        val dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(jdbc);
        dataSource.setUsername(user);
        dataSource.setPassword(password);

        return new HikariMessenger(dataSource);
    }

    @Override
    public void close() {
        dataSource.close();
    }

    @Override
    public @NotNull ScriptRunner scriptRunner() {
        return new ScriptRunnerImpl(getClass().getClassLoader(), null);
    }

    @Override
    public @NotNull Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    @Override
    public @NotNull IntFlow update(
            final @NonNull String query,
            final @Nullable Object @NonNull ... parameters
    ) {
        return IntFlows.ofSupplier(query, new Update(query, parameters));
    }

    @Override
    public @NotNull IntFlow update(final @NonNull String query) {
        return IntFlows.ofSupplier(query, new Update(query, null));
    }

    @Override
    public @NotNull IntFlow updateWithKeys(
            final @NonNull String query,
            final @Nullable Object @NonNull ... parameters
    ) {
        return IntFlows.ofSupplier(query, new UpdateWithKeys(query, parameters));
    }

    @Override
    public @NotNull IntFlow updateWithKeys(final @NonNull String query) {
        return IntFlows.ofSupplier(query, new UpdateWithKeys(query, null));
    }

    @Override
    public @NotNull FlowItems<@NotNull ResultSet> fetch(
            final @NonNull String query,
            final @Nullable Object @NonNull ... parameters
    ) {
        return Flows.ofEmitter(query, new Fetch(query, parameters));
    }

    @Override
    public @NotNull FlowItems<@NotNull ResultSet> fetch(final @NonNull String query) {
        return Flows.ofEmitter(query, new Fetch(query, null));
    }

    @FieldDefaults(level = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    private final class ScriptRunnerImpl implements ScriptRunner {

        ClassLoader classLoader;
        String schema;

        @Override
        public @NotNull ScriptRunner classLoader(final @NonNull ClassLoader cl) {
            this.classLoader = cl;

            return this;
        }

        @Override
        public @NotNull ScriptRunner schema(final @NonNull String schema) {
            this.schema = schema;
            return this;
        }

        @Override
        @SneakyThrows
        public @NotNull ScriptRunner run(final @NonNull String resource) {
            val resourceStream = ClassLoaderUtils.getResource(classLoader, resource);

            if (resourceStream == null) {
                throw new FileNotFoundException("No resource with name " + resource + " found");
            }

            try (val reader = new BufferedReader(new InputStreamReader(resourceStream))) {
                return _run(reader);
            }
        }

        @Override
        @SneakyThrows
        public @NotNull ScriptRunner run(final @NonNull File file) {
            try (val reader = new BufferedReader(new FileReader(file))) {
                return _run(reader);
            }
        }

        @Override
        @SneakyThrows
        public @NotNull ScriptRunner run(final @NonNull Path path) {
            try (val reader = Files.newBufferedReader(path)) {
                return _run(reader);
            }
        }

        @Override
        @SneakyThrows
        public @NotNull ScriptRunner run(final @NonNull InputStream is) {
            return _run(new BufferedReader(new InputStreamReader(is)));
        }

        @Override
        @SneakyThrows
        public @NotNull ScriptRunner run(final @NonNull Reader reader) {
            return !(reader instanceof BufferedReader)
                    ? _run(new BufferedReader(reader))
                    : _run((BufferedReader) reader);
        }

        private ScriptRunner _run(final BufferedReader reader) throws IOException, SQLException {
            val query = new StringBuilder();

            try (val connection = dataSource.getConnection()) {
                val oldAutoCommit = connection.getAutoCommit();

                if (oldAutoCommit) {
                    connection.setAutoCommit(false);
                }

                val oldSchema = connection.getSchema();

                if (schema != null) {
                    connection.setSchema(schema);
                }

                String line;

                while ((line = reader.readLine()) != null) {
                    val trimmedLine = line.trim();

                    if (trimmedLine.isEmpty()
                            || trimmedLine.startsWith("--")
                            || trimmedLine.startsWith("//")) {
                        continue;
                    }

                    if (trimmedLine.endsWith(";")) {
                        query.append(trimmedLine);

                        try (val statement = connection.createStatement()) {
                            statement.execute(query.toString());
                        }

                        query.setLength(0);

                        continue;
                    }

                    query.append(trimmedLine);
                    query.append(" ");
                }

                connection.commit();

                if (schema != null) {
                    connection.setSchema(oldSchema);
                }

                if (oldAutoCommit) {
                    connection.setAutoCommit(true);
                }
            }

            return this;
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static abstract class Request {
        String query;
        Object[] parameters;

        protected final PreparedStatement makeWithoutKeys(
                final Connection connection
        ) throws SQLException {
            val statement = connection.prepareStatement(query);
            fillStatement(statement);

            return statement;
        }

        protected final PreparedStatement makeWithKeys(
                final Connection connection
        ) throws SQLException {
            val statement = connection.prepareStatement(query, RETURN_GENERATED_KEYS);
            fillStatement(statement);

            return statement;
        }

        private void fillStatement(final PreparedStatement statement) throws SQLException {
            if (parameters != null) {
                for (int i = 0, j = parameters.length; i < j; i++) {
                    statement.setObject(1 + i, parameters[i]);
                }
            }
        }

    }

    private final class Update extends Request implements IntFlowSupplier {
        private Update(final String query, final Object[] parameters) {
            super(query, parameters);
        }

        @Override
        @SneakyThrows
        public int get() {
            try (val connection = dataSource.getConnection();
                 val statement = makeWithoutKeys(connection)) {
                return statement.executeUpdate();
            }
        }
    }

    private final class UpdateWithKeys extends Request implements IntFlowSupplier {
        private UpdateWithKeys(final String query, final Object[] parameters) {
            super(query, parameters);
        }

        @Override
        @SneakyThrows
        public int get() {
            try (val connection = dataSource.getConnection();
                 val statement = makeWithKeys(connection)) {
                statement.executeUpdate();

                try (val result = statement.getGeneratedKeys()) {
                    return result.next() ? result.getInt(1) : 0;
                }
            }
        }
    }

    private final class Fetch extends Request implements FlowConsumer<FlowSink<ResultSet>> {
        private Fetch(final String query, final Object[] parameters) {
            super(query, parameters);
        }

        @Override
        @SneakyThrows
        public void accept(final FlowSink<ResultSet> sink) {
            try (val connection = dataSource.getConnection();
                 val statement = makeWithoutKeys(connection);
                 val query = statement.executeQuery()) {

                while (query.next()) {
                    sink.next(query);
                }
            }
        }
    }

}
