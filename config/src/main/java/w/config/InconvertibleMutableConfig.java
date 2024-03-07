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

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import w.config.mapper.Mapper;

import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class InconvertibleMutableConfig extends MapBasedMutableConfig {

    private InconvertibleMutableConfig(Map<String, Object> map) {
        super(map);
    }

    public static @NotNull MutableConfig from(@NotNull Map<@NotNull String, @NotNull Object> map) {
        return new InconvertibleMutableConfig(map);
    }

    public static @NotNull MutableConfig create() {
        return new InconvertibleMutableConfig(new LinkedHashMap<>());
    }

    @Override
    protected MutableConfig createObject(final Map<String, Object> map) {
        return new InconvertibleMutableConfig(map);
    }

    @Override
    public  <T> T asType(final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull <T> Mapper<T> mapAs(final @NotNull Class<T> type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(final @NotNull Writer writer) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeTo(final @NotNull OutputStream os) {
        throw new UnsupportedOperationException();
    }

}
