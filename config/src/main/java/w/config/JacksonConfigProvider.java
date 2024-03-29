/*
 *    Copyright 2023 Whilein
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
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.config.deserializer.ConfigDeserializer;
import w.config.mapper.AbstractMapper;

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
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class JacksonConfigProvider implements ConfigProvider {

    ObjectMapper objectMapper;

    public static @NotNull ConfigProvider create(@NotNull ObjectMapper objectMapper) {
        val provider = new JacksonConfigProvider(objectMapper = objectMapper.copy());

        val module = new SimpleModule();
        module.addDeserializer(Config.class, new ConfigDeserializer(provider));
        objectMapper.registerModule(module);

        return provider;
    }

    private void loadObject(Map<?, ?> map, MutableConfig object) {
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
    public @NotNull MutableConfig parse(final @NotNull Path path) {
        try (val is = Files.newInputStream(path)) {
            return _parse(is);
        }
    }

    @Override
    public @NotNull MutableConfig newObject() {
        return new JacksonConfig(new LinkedHashMap<>());
    }

    @Override
    @SneakyThrows
    public @NotNull MutableConfig parse(final @NotNull File file) {
        try (val is = new FileInputStream(file)) {
            return _parse(is);
        }
    }

    @Override
    @SneakyThrows
    public @NotNull MutableConfig parse(final @NotNull String input) {
        return convert(objectMapper.readValue(input, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull MutableConfig parse(final byte @NotNull [] input) {
        return convert(objectMapper.readValue(input, Map.class));
    }

    @Override
    public @NotNull MutableConfig convert(@NotNull Map<?, ?> map) {
        val object = new JacksonConfig(new LinkedHashMap<>());
        loadObject(map, object);

        return object;
    }

    @Override
    @SneakyThrows
    public @NotNull MutableConfig parse(final @NotNull Reader reader) {
        return convert(objectMapper.readValue(reader, Map.class));
    }

    @Override
    @SneakyThrows
    public @NotNull MutableConfig parse(final @NotNull InputStream is) {
        return _parse(is);
    }

    private MutableConfig _parse(final InputStream is) throws IOException {
        return convert(objectMapper.readValue(is, Map.class));
    }

    private final class JacksonMapper<T> extends AbstractMapper<T> {

        private JacksonMapper(Class<T> type) {
            super(type);
        }

        @Override
        protected T doMap(final Object o) {
            try {
                return objectMapper.convertValue(o, type);
            } catch (final Exception e) {
                return null;
            }
        }
    }

    private final class JacksonConfig extends MapBasedMutableConfig {

        private JacksonConfig(final Map<String, Object> map) {
            super(map);
        }

        @Override
        @SneakyThrows
        public String toString() {
            return objectMapper.writeValueAsString(map);
        }

        @Override
        protected MutableConfig createObject(final Map<String, Object> map) {
            return new JacksonConfig(map);
        }

        @Override
        public @NotNull <T> AbstractMapper<T> mapAs(final @NotNull Class<T> type) {
            return new JacksonMapper<>(type);
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

        @Override
        public <T> T asType(final @NotNull Class<T> type) {
            return objectMapper.convertValue(map, type);
        }
    }
}
