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

package w.util.message;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author whilein
 */
@UtilityClass
public class Messages {

    private final int STYLE_UNKNOWN = 0;
    private final int STYLE_OLD = 1;
    private final int STYLE_NEW = 2;

    private Token parse(final String text) {
        Token head = null;
        Token tail = null;

        int style = STYLE_UNKNOWN;
        int start = 0;
        int counter = 0;

        for (int i = 0; i < text.length(); i++) {
            val ch = text.charAt(i);

            if (ch == '%') {
                val nextChIdx = i + 1;

                if (nextChIdx == text.length()) {
                    break;
                }

                val nextCh = text.charAt(nextChIdx);

                final int index;

                if ((nextCh >= '0' && nextCh <= '9')
                        || (nextCh >= 'A' && nextCh <= 'F')
                        || (nextCh >= 'a' && nextCh <= 'f')) {
                    if (style == STYLE_OLD) {
                        throw new IllegalStateException("Cannot use %<hex> in old style format!");
                    }

                    style = STYLE_NEW;

                    if (nextCh <= '9') {
                        index = nextCh - '0';
                    } else if (nextCh <= 'F') {
                        index = nextCh - 'A';
                    } else {
                        index = nextCh - 'a';
                    }
                } else if (nextCh == 's') {
                    if (style == STYLE_NEW) {
                        throw new IllegalStateException("Cannot use %s in new style format!");
                    }

                    style = STYLE_OLD;
                    index = counter++;
                } else if (nextCh == '%') {
                    i++;
                    continue;
                } else {
                    continue;
                }

                val textToken = new Text(unescape(text.substring(start, i)));
                val paramToken = new Param(index);

                textToken.next = paramToken;

                if (head == null) {
                    head = textToken;
                    tail = paramToken;
                } else {
                    tail = tail.next = paramToken;
                }

                start = nextChIdx + 1;
            }
        }

        if (start < text.length()) {
            val endText = new Text(unescape(text.substring(start)));

            if (head == null) {
                head = endText;
            } else {
                tail.next = endText;
            }
        }

        return head;
    }

    private String unescape(final String text) {
        return text.replace("%%", "%");
    }

    private Message _create(final String text) {
        val token = parse(text);
        return new Line(token.toString(), token);
    }

    private Message _create(final List<String> texts) {
        val text = String.join("\n", texts);

        val line = parse(text);
        val lines = new Token[texts.size()];

        val rawLine = new StringBuilder();
        val rawLines = new ArrayList<String>();

        int counter = 0;

        for (val element : texts) {
            val token = parse(element);
            lines[counter++] = token;

            val rawToken = token.toString();
            rawLine.append(rawToken).append('\n');
            rawLines.add(rawToken);
        }

        return new Lines(
                rawLine.toString(),
                Collections.unmodifiableList(rawLines),
                line,
                lines
        );
    }

    public static @NotNull Message create(final @NotNull String text) {
        val lines = text.split("\n", -1);

        return lines.length > 1
                ? _create(List.of(lines))
                : _create(text);
    }

    public static @NotNull Message create(final @NotNull List<@NotNull String> texts) {
        return texts.size() == 1
                ? _create(texts.get(0))
                : _create(texts);
    }

    private static List<String> join(final Token[] tokens, final Object[] parameters) {
        val list = new ArrayList<String>(tokens.length);

        for (val token : tokens) {
            val out = new StringBuilder();
            token.writeAll(out, parameters);

            list.add(out.toString());
        }

        return list;
    }

    private static String join(final Token token, final Object[] parameters) {
        val out = new StringBuilder();
        token.writeAll(out, parameters);

        return out.toString();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Lines implements Message {
        String raw;
        List<String> rawList;

        Token line;
        Token[] lines;

        @Override
        public @NotNull String format() {
            return raw;
        }

        @Override
        public @NotNull String format(final @Nullable Object @Nullable ... parameters) {
            if (parameters == null || parameters.length == 0)
                return raw;

            return join(line, parameters);
        }

        @Override
        public @NotNull List<@NotNull String> formatAsList() {
            return rawList;
        }

        @Override
        public @NotNull List<@NotNull String> formatAsList(final @Nullable Object @Nullable ... parameters) {
            if (parameters == null || parameters.length == 0)
                return rawList;

            return join(lines, parameters);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Line implements Message {
        String raw;
        Token token;

        @Override
        public @NotNull String format() {
            return raw;
        }

        @Override
        public @NotNull String format(final @Nullable Object @Nullable ... parameters) {
            if (parameters == null)
                return raw;

            return join(token, parameters);
        }

        @Override
        public @NotNull List<@NotNull String> formatAsList() {
            return Collections.singletonList(raw);
        }

        @Override
        public @NotNull List<@NotNull String> formatAsList(final @Nullable Object @Nullable ... parameters) {
            return Collections.singletonList(format(parameters));
        }
    }

    private static abstract class Token {

        Token next;

        abstract void write(StringBuilder output, Object[] params);

        void writeAll(final StringBuilder output, final Object[] params) {
            write(output, params);

            if (next != null) {
                next.writeAll(output, params);
            }
        }

        @Override
        public String toString() {
            val out = new StringBuilder();
            writeAll(out, new Object[0]);

            return out.toString();
        }
    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class Text extends Token {

        String text;

        @Override
        public void write(final StringBuilder output, final Object[] params) {
            output.append(text);
        }

    }

    @RequiredArgsConstructor
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class Param extends Token {

        int index;

        @Override
        public void write(final StringBuilder output, final Object[] params) {
            try {
                output.append(params[index]);
            } catch (final ArrayIndexOutOfBoundsException e) {
                output.append('%').append(index);
            }
        }

    }

}
