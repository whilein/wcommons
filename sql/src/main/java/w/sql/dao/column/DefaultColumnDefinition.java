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

package w.sql.dao.column;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.jetbrains.annotations.NotNull;
import w.sql.dao.codec.ResultSetCodec;
import w.sql.dao.codec.ResultSetReader;

/**
 * @author whilein
 */
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultColumnDefinition implements ColumnDefinition {

    String type;
    Class<?> javaType;

    ResultSetReader<?> reader;

    boolean notNull;
    boolean autoIncrement;

    public static @NotNull ColumnDefinitionBuilder builder(
            final @NotNull String type,
            final @NotNull Class<?> javaType
    ) {
        return new ColumnDefinitionBuilderImpl(type, javaType);
    }


    @Getter
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ColumnDefinitionBuilderImpl implements ColumnDefinitionBuilder {

        String type;
        Class<?> javaType;

        @NonFinal
        String defaultValue;

        @NonFinal
        boolean notNull;

        @NonFinal
        boolean autoIncrement;

        @Override
        public @NotNull ColumnDefinitionBuilder defaults(final int number) {
            defaultValue = String.valueOf(number);
            return this;
        }

        @Override
        public @NotNull ColumnDefinitionBuilder defaults(final @NotNull String text) {
            defaultValue = '\'' + text + '\'';
            return this;
        }

        @Override
        public @NotNull ColumnDefinitionBuilder autoIncrement() {
            autoIncrement = true;
            return this;
        }

        @Override
        public @NotNull ColumnDefinitionBuilder notNull() {
            notNull = true;
            return this;
        }

        @Override
        public @NotNull ColumnDefinition build() {
            return new DefaultColumnDefinition(
                    type,
                    javaType,
                    ResultSetCodec.findReader(javaType),
                    notNull,
                    autoIncrement
            );
        }
    }
}
