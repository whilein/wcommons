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

import com.fasterxml.jackson.databind.ObjectMapper;
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
    ObjectMapper objectMapper;

    private ConfigObject loadObject(final Map<?, ?> map) {
        val object = new ConfigObjectImplAbstract(new LinkedHashMap<>());
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
    public @NotNull ConfigObject parse(final @NotNull Path path) {
        try (val is = Files.newInputStream(path)) {
            return _parse(is);
        }
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
        return loadObject(objectMapper.readValue(input, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final byte @NotNull [] input) {
        return loadObject(objectMapper.readValue(input, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull Reader reader) {
        return loadObject(objectMapper.readValue(reader, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull ConfigObject parse(final @NotNull InputStream is) {
        return _parse(is);
    }

    private ConfigObject _parse(final InputStream is) throws IOException {
        return loadObject(objectMapper.readValue(is, Map.class));
    }

    private final class ConfigObjectImplAbstract extends AbstractMapConfigObject {

        private ConfigObjectImplAbstract(final Map<String, Object> map) {
            super(map);
        }

        @Override
        @SneakyThrows
        public String toString() {
            return objectMapper.writeValueAsString(map);
        }

        @Override
        protected ConfigObject createObject(final Map<String, Object> map) {
            return new ConfigObjectImplAbstract(map);
        }

        @Override
        @SneakyThrows
        public void writeTo(final @NotNull Writer writer) {
            objectMapper.writeValue(writer, map);
        }

        @Override
        @SneakyThrows
        public void writeTo(final @NotNull OutputStream os) {
            objectMapper.writeValue(os, map);
        }
    }
}
