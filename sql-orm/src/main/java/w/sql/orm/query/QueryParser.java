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

package w.sql.orm.query;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import w.commons.sql.Dialect;
import w.sql.orm.definition.EntityColumnDefinition;
import w.sql.orm.definition.EntityDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author whilein
 */
@UtilityClass
public class QueryParser {

    // ${*}, ${:abc_def}, ${abc_def}
    private final Pattern PARAMETER_PATTERN = Pattern.compile("(\\$\\{(\\*|(:?[\\w_]+))})");

    public @NotNull ParsedQuery parse(
            final @NotNull String expression,
            final @Nullable EntityDefinition entity,
            final @NotNull Dialect dialect
    ) {
        val matcher = PARAMETER_PATTERN.matcher(expression);

        val in = new ArrayList<String>();
        val out = new ArrayList<String>();

        val quotes = getQuotes(dialect);

        String result = expression;

        while (matcher.find()) {
            val group = matcher.group();
            val parameter = group.substring(2, group.length() - 1);

            final String replacement;

            if (parameter.startsWith("@")) {
                val name = parameter.substring(1);

                replacement = quotes + name + quotes;

                in.add(name);
            } else if (parameter.equals("*")) {
                if (entity != null) {
                    Arrays.stream(entity.getColumns())
                            .map(EntityColumnDefinition::getName)
                            .forEach(out::add);
                }

                replacement = "*";
            } else {
                replacement = quotes + parameter + quotes;

                out.add(parameter);
            }

            result = matcher.replaceFirst(replacement);
        }

        return new ParsedQueryImpl(result, in, out);
    }

    private static char getQuotes(final Dialect dialect) {
        switch (dialect) {
            default:
            case H2:
            case MYSQL:
                return '`';
            case POSTGRES:
                return '"';
        }
    }

    @Getter
    @ToString
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class ParsedQueryImpl implements ParsedQuery {
        String query;
        List<String> inParameters, outParameters;
    }

}
