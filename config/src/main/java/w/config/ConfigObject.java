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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.io.File;
import java.io.OutputStream;
import java.io.Writer;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.Set;

/**
 * @author whilein
 */
public interface ConfigObject {

    @NotNull Map<@NotNull String, @NotNull Object> asMap();

    void writeTo(@NotNull Writer writer);

    void writeTo(@NotNull OutputStream os);

    void writeTo(@NotNull File file);

    void writeTo(@NotNull Path path);

    void set(@NotNull String key, @Nullable Object object);

    @NotNull ConfigObject createObject(@NotNull String key);

    void remove(@NotNull String key);

    boolean contains(@NotNull String key);

    boolean isEmpty();

    int size();

    @NotNull Set<@NotNull String> getKeys();

    @Nullable String getString(@NotNull String key);

    @Contract("_, !null -> !null")
    @Nullable String getString(@NotNull String key, @Nullable String defaultValue);

    boolean getBoolean(@NotNull String key, boolean defaultValue);

    boolean getBoolean(@NotNull String key);

    int getInt(@NotNull String key);

    double getDouble(@NotNull String key);

    long getLong(@NotNull String key);

    int getInt(@NotNull String key, int defaultValue);

    double getDouble(@NotNull String key, double defaultValue);

    long getLong(@NotNull String key, long defaultValue);

    @Nullable ConfigObject getObject(@NotNull String key);

    @Unmodifiable @NotNull List<@NotNull String> getStringList(@NotNull String key);

    @Unmodifiable @NotNull List<@NotNull ConfigObject> getObjectList(@NotNull String key);

    @NotNull OptionalInt findInt(@NotNull String key);

    @NotNull OptionalDouble findDouble(@NotNull String key);

    @NotNull OptionalLong findLong(@NotNull String key);

    @NotNull Optional<@NotNull String> findString(@NotNull String key);

    @NotNull Optional<@NotNull ConfigObject> findObject(@NotNull String key);

}
