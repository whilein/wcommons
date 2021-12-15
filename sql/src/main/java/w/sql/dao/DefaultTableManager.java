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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.flow.Flow;
import w.flow.FlowItems;
import w.flow.IntFlow;
import w.sql.Messenger;
import w.sql.dao.column.Column;
import w.sql.dao.foreignkey.ForeignKey;
import w.sql.dao.property.Properties;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultTableManager<T extends Enum<T> & Column> implements TableManager<T> {

    Messenger messenger;

    T[] columns;
    T id;

    String getAll;
    String[] getColumn;
    String[] getAllBy;
    String[][] getColumnBy;

    String[] update;
    String insert;
    String delete;

    public static <T extends Enum<T> & Column> @NotNull TableManagerBuilder<T> builder(
            final @NotNull Messenger messenger,
            final @NotNull Class<T> type,
            final @NotNull String name
    ) {
        return new TableManagerBuilderImpl<>(
                messenger,
                type,
                type.getEnumConstants(),
                name,
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );
    }

    private Properties<T> mapToProperties(
            final ResultSet rs,
            final T lookupColumn,
            final Object lookupValue
    ) throws SQLException {
        Object[] values = new Object[columns.length];

        for (int i = 0, j = values.length; i < j; i++) {
            val column = columns[i];

            if (column == lookupColumn) {
                values[i] = lookupValue;
                continue;
            }

            values[i] = column.getDefinition().getReader().read(rs, i + 1);
        }

        return new PropertiesImpl(values);
    }

    @Override
    public @NotNull Flow<@NotNull Properties<T>> getAll(final @NotNull Object id) {
        return messenger.fetch(getAll, id).mapFirst(rs -> mapToProperties(rs, this.id, id));
    }

    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <V> Flow<V> get(final @NotNull Object id, final @NotNull T column) {
        return messenger.fetch(getColumn[column.ordinal()], id)
                .mapFirst(rs -> (V) column.getDefinition().getReader().read(rs, 1));
    }

    @Override
    public @NotNull FlowItems<@NotNull Properties<T>> getAllBy(
            final @NotNull T column,
            final @Nullable Object value
    ) {
        return messenger.fetch(getAllBy[column.ordinal()], value).map(rs -> mapToProperties(rs, column, value));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> @NotNull FlowItems<@NotNull V> getBy(
            final @NotNull T by,
            final @Nullable Object value,
            final @NotNull T column
    ) {
        return messenger.fetch(getColumnBy[column.ordinal()][by.ordinal()], value)
                .map(rs -> (V) column.getDefinition().getReader().read(rs, 1));
    }

    @Override
    public @NotNull IntFlow insert(final @NotNull Consumer<@NotNull Properties<T>> consumer) {
        val properties = new PropertiesImpl(new Object[columns.length]);
        consumer.accept(properties);

        return messenger.updateWithKeys(insert, properties.toArray());
    }

    @Override
    public @NotNull IntFlow delete(final @NotNull Object id) {
        return messenger.update(delete, id);
    }

    @Override
    public @NotNull IntFlow update(final @NotNull Object id, final @NotNull T column, final @Nullable Object value) {
        return messenger.update(update[column.ordinal()], value, id);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private final class PropertiesImpl implements Properties<T> {

        Object[] values;

        @Override
        public @NotNull Properties<T> set(final @NotNull T key, final @Nullable Object value) {
            values[key.ordinal()] = value;
            return this;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <V> V get(final @NotNull T key) {
            return (V) values[key.ordinal()];
        }

        @Override
        public String toString() {
            val joiner = new StringJoiner(", ", "[", "]");

            for (val column : columns) {
                joiner.add(column.name() + " = " + values[column.ordinal()]);
            }

            return joiner.toString();
        }

        @Override
        public Object[] toArray() {
            return values;
        }

    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class TableManagerBuilderImpl<T extends Enum<T> & Column>
            implements TableManagerBuilder<T> {
        Messenger messenger;

        Class<T> columnType;
        T[] columns;

        String name;

        @NonFinal
        String database;

        @NonFinal
        T id;

        List<T[]> primaryKeys;
        List<T[]> uniqueKeys;

        List<ForeignKey> foreignKeys;

        @Override
        public @NotNull TableManagerBuilder<T> database(final @NotNull String database) {
            this.database = database;

            return this;
        }

        @Override
        public @NotNull TableManagerBuilder<T> id(final @NotNull T column) {
            this.id = column;

            return this;
        }

        @Override
        @SafeVarargs
        public final @NotNull TableManagerBuilder<T> primaryKey(final @NotNull T @NotNull ... columns) {
            this.primaryKeys.add(columns);
            return this;
        }

        @Override
        @SafeVarargs
        public final @NotNull TableManagerBuilder<T> uniqueKey(final @NotNull T @NotNull ... columns) {
            this.uniqueKeys.add(columns);
            return this;
        }

        @Override
        public @NotNull TableManagerBuilder<T> foreignKey(final @NotNull ForeignKey foreignKey) {
            this.foreignKeys.add(foreignKey);
            return this;
        }

        private StringBuilder appendTable(final StringBuilder builder) {
            if (database != null) {
                builder.append('`').append(database).append('`').append('.');
            }

            return builder.append('`').append(name).append('`');
        }


        private String getDelete() {
            val updateColumn = new StringBuilder("DELETE FROM ");
            appendTable(updateColumn).append(" WHERE ");

            if (id == null) {
                updateColumn.append("`ID`");
            } else {
                updateColumn.append('`').append(id.name()).append('`');
            }

            return updateColumn.append("=?").toString();
        }

        private String getUpdate(final T column) {
            val updateColumn = new StringBuilder("UPDATE ");
            appendTable(updateColumn).append(" SET ").append('`').append(column.name()).append("`=? WHERE ");

            if (id == null) {
                updateColumn.append("`ID`");
            } else {
                updateColumn.append('`').append(id.name()).append('`');
            }

            return updateColumn.append("=?").toString();
        }

        private String getInsert() {
            val insert = new StringBuilder("INSERT INTO ");
            appendTable(insert).append('(');

            int counter = 0;

            for (val column : columns) {
                if (column == id) {
                    continue;
                }

                if (counter++ != 0) {
                    insert.append(',');
                }

                insert.append('`').append(column.name()).append('`');
            }

            insert.append(") VALUES (");

            for (int i = 0; i < counter; i++) {
                if (i != 0) insert.append(',');
                insert.append('?');
            }

            return insert.append(")").toString();
        }

        private String[] getUpdate() {
            return Stream.of(columns)
                    .map(this::getUpdate)
                    .toArray(String[]::new);
        }

        private String getColumn(final T column) {
            val getColumn = new StringBuilder("SELECT `")
                    .append(column.name())
                    .append("` FROM ");

            appendTable(getColumn).append(" WHERE ");

            if (id == null) {
                getColumn.append("`ID`");
            } else {
                getColumn.append('`').append(id.name()).append('`');
            }

            return getColumn.append("=?").toString();
        }

        private String[] getColumns() {
            return Stream.of(columns)
                    .map(this::getColumn)
                    .toArray(String[]::new);
        }

        private String getColumnBy(final T column, final T by) {
            val getColumnBy = new StringBuilder("SELECT `")
                    .append(column.name())
                    .append("` FROM ");

            appendTable(getColumnBy).append(" WHERE ");

            return getColumnBy.append('`').append(by.name()).append('`').append("=?").toString();
        }

        private String[][] getColumnBy() {
            val columnCount = columns.length;

            val columnBy = new String[columnCount][columnCount];

            for (int i = 0; i < columnCount; i++) {
                for (int j = 0; j < columnCount; j++) {
                    if (i == j) continue;

                    columnBy[i][j] = getColumnBy(columns[i], columns[j]);
                }
            }

            return columnBy;
        }

        private String getAllBy(final T by) {
            val getAllBy = new StringBuilder("SELECT ");

            boolean hasAnyColumns = false;

            for (val column : columns) {
                if (column == by) {
                    continue;
                }

                if (hasAnyColumns) {
                    getAllBy.append(',');
                }

                getAllBy.append('`').append(column.name()).append('`');
                hasAnyColumns = true;
            }

            getAllBy.append(" FROM ");
            appendTable(getAllBy).append(" WHERE ");

            getAllBy.append('`').append(by.name()).append('`');

            return getAllBy.append("=?").toString();
        }

        private String[] getAllBy() {
            return Stream.of(columns)
                    .map(this::getAllBy)
                    .toArray(String[]::new);
        }

        private String getAll() {
            val getAll = new StringBuilder("SELECT ");

            boolean hasAnyColumns = false;

            for (val column : columns) {
                if (column == id) {
                    continue;
                }

                if (hasAnyColumns) {
                    getAll.append(',');
                }

                getAll.append('`').append(column.name()).append('`');
                hasAnyColumns = true;
            }

            getAll.append(" FROM ");
            appendTable(getAll).append(" WHERE ");

            if (id == null) {
                getAll.append("`ID`");
            } else {
                getAll.append('`').append(id.name()).append('`');
            }

            return getAll.append("=?").toString();
        }

        @Override
        public @NotNull TableManager<T> build() {

            val manager = new DefaultTableManager<>(
                    messenger,
                    columns,
                    id,
                    getAll(),
                    getColumns(),
                    getAllBy(),
                    getColumnBy(),
                    getUpdate(),
                    getInsert(),
                    getDelete()
            );

            {
                val createTable = new StringBuilder("CREATE TABLE IF NOT EXISTS ");
                appendTable(createTable).append('(');

                boolean hasAnyColumns = false;

                if (id == null) {
                    createTable.append("`ID` INT NOT NULL AUTO_INCREMENT PRIMARY KEY,");
                }

                for (val column : columns) {
                    if (hasAnyColumns) {
                        createTable.append(',');
                    }

                    val definition = column.getDefinition();

                    createTable.append('`').append(column.name()).append("` ").append(definition.getType());

                    if (definition.isNotNull()) {
                        createTable.append(" NOT NULL");
                    }

                    if (definition.isAutoIncrement()) {
                        createTable.append(" AUTO_INCREMENT");
                    }

                    hasAnyColumns = true;
                }

                for (val key : uniqueKeys) {
                    if (hasAnyColumns) {
                        createTable.append(',');
                    }

                    createTable.append("UNIQUE KEY (").append(Arrays.stream(key)
                            .map(column -> "`" + column.name() + "`").
                            collect(Collectors.joining(", "))).append(")");

                    hasAnyColumns = true;
                }

                for (val key : primaryKeys) {
                    if (hasAnyColumns) {
                        createTable.append(',');
                    }

                    createTable.append("PRIMARY KEY (").append(Arrays.stream(key)
                            .map(column -> "`" + column.name() + "`").
                            collect(Collectors.joining(", "))).append(")");

                    hasAnyColumns = true;
                }

                for (val key : foreignKeys) {
                    if (hasAnyColumns) {
                        createTable.append(',');
                    }

                    createTable.append("FOREIGN KEY (`").append(key.getColumn().name())
                            .append("`) REFERENCES ");

                    if (key.getReferenceDatabase() != null) {
                        createTable.append("`").append(key.getReferenceDatabase()).append("`.");
                    }

                    createTable
                            .append("`").append(key.getReferenceTable())
                            .append("`(`").append(key.getReferenceColumn()).append("`) ON DELETE ")
                            .append(key.getOnDelete().getName()).append(" ON UPDATE ")
                            .append(key.getOnUpdate().getName());

                    hasAnyColumns = true;
                }

                createTable.append(")");

                messenger.update(createTable.toString()).call();
            }

            return manager;
        }
    }


}
