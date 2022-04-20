/*
 *    Copyright 2022 Whilein
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

import java.io.OutputStream;
import java.io.Writer;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public final class SimpleConfig extends AbstractMapConfig {

    private SimpleConfig(final Map<String, Object> map) {
        super(map);
    }

    public static @NotNull Config create(final @NotNull Map<@NotNull String, @NotNull Object> map) {
        return new SimpleConfig(map);
    }

    @Override
    public int hashCode() {
        return map.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof SimpleConfig config && map.equals(config.map);
    }

    public static @NotNull Config create() {
        return new SimpleConfig(new LinkedHashMap<>());
    }

    @Override
    protected Config createObject(final Map<String, Object> map) {
        return new SimpleConfig(map);
    }

    @Override
    protected <T> T getAs(final Object value, final Class<T> type) {
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
