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

package w.config;

import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PROTECTED, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class AbstractJacksonConfigParser implements JacksonConfigParser {

    @Getter
    ObjectWriter objectWriter;

    @Getter
    ObjectReader objectReader;

    private ConfigObject loadObject(final Map<?, ?> map) {
        val object = new ConfigObjectImpl(new LinkedHashMap<>());
        loadObject(map, object);

        return object;
    }

    private void loadObject(final Map<?, ?> map, final ConfigObject object) {
        for (val entry : map.entrySet()) {
            val key = entry.getKey().toString();
            val value = entry.getValue();

            if (value instanceof Map) {
                loadObject((Map<?, ?>) value, object.createObject(key));
            } else {
                object.set(key, value);
            }
        }
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final @NotNull File file, final @NotNull Class<E> type) {
        try (val fis = new FileInputStream(file)) {
            return _parse(fis, type);
        }
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final @NotNull Path path, final @NotNull Class<E> type) {
        try (val is = Files.newInputStream(path)) {
            return _parse(is, type);
        }
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final @NotNull Reader reader, final @NotNull Class<E> type) {
        return objectReader.readValue(reader, type);
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final @NotNull InputStream stream, final @NotNull Class<E> type) {
        return objectReader.readValue(stream, type);
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final @NotNull String input, final @NotNull Class<E> type) {
        return objectReader.readValue(input, type);
    }

    @Override
    @SneakyThrows
    public <E> @NotNull E parse(final byte @NotNull [] input, final @NotNull Class<E> type) {
        return objectReader.readValue(input, type);
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull Path path) {
        try (val is = Files.newInputStream(path)) {
            return _parse(is);
        }
    }

    @Override
    public @NotNull ConfigObject newObject() {
        return new ConfigObjectImpl(new LinkedHashMap<>());
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull File file) {
        try (val is = new FileInputStream(file)) {
            return _parse(is);
        }
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull String input) {
        return loadObject(objectReader.readValue(input, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final byte @NotNull [] input) {
        return loadObject(objectReader.readValue(input, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull Reader reader) {
        return loadObject(objectReader.readValue(reader, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull InputStream is) {
        return _parse(is);
    }

    private ConfigObject _parse(final InputStream is) throws IOException {
        return loadObject(objectReader.readValue(is, Map.class));
    }

    private <E> E _parse(final InputStream is, final Class<E> type) throws IOException {
        return objectReader.readValue(is, type);
    }

    private final class ConfigObjectImpl extends AbstractMapConfigObject {

        private ConfigObjectImpl(final Map<String, Object> map) {
            super(map);
        }

        @Override
        @SneakyThrows
        public String toString() {
            return objectWriter.writeValueAsString(map);
        }

        @Override
        protected ConfigObject createObject(final Map<String, Object> map) {
            return new ConfigObjectImpl(map);
        }

        @Override
        @SneakyThrows
        public void writeTo(final @NotNull Writer writer) {
            objectWriter.writeValue(writer, map);
        }

        @Override
        @SneakyThrows
        public void writeTo(final @NotNull OutputStream os) {
            objectWriter.writeValue(os, map);
        }
    }
}
