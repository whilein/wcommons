/*
 *    Copyright 2024 Whilein
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
import w.config.mapper.Mapper;

import java.util.List;
import java.util.Optional;

/**
 * @author whilein
 */
public interface MutableConfig extends Config {
    @Unmodifiable @NotNull List<? extends @NotNull MutableConfig> getObjectList(@NotNull String key);

    @NotNull Optional<? extends @NotNull MutableConfig> findObject(@NotNull String key);

    @NotNull Mapper<? extends MutableConfig> configMapper();

    @NotNull MutableConfig getObject(@NotNull String key) throws ConfigMissingKeyException;

    @Contract("_, !null -> !null")
    @Nullable MutableConfig getObject(@NotNull String key, @Nullable MutableConfig defaultValue);

    @NotNull MutableConfig createObject(@NotNull String key);

    void set(@NotNull String key, @Nullable Object object);

    void setAll(@NotNull Config config);

    void remove(@NotNull String key);

}
