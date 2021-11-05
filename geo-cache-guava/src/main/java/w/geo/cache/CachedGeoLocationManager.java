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

package w.geo.cache;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.geo.api.GeoLocation;
import w.geo.api.GeoLocationManager;

import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class CachedGeoLocationManager implements GeoLocationManager {

    LoadingCache<InetAddress, GeoLocation> cache;

    public static @NotNull GeoLocationManager create(
            final @NotNull GeoLocationManager delegate
    ) {
        return new CachedGeoLocationManager(
                CacheBuilder.newBuilder()
                        .softValues()
                        .expireAfterAccess(24, TimeUnit.HOURS)
                        .build(new CacheLoaderImpl(delegate))
        );
    }

    public static @NotNull GeoLocationManager create(
            final @NotNull GeoLocationManager delegate,
            final @NotNull Consumer<@NotNull CacheBuilder<?, ?>> builderInitializer
    ) {
        val builder = CacheBuilder.newBuilder();
        builderInitializer.accept(builder);

        return new CachedGeoLocationManager(builder.build(new CacheLoaderImpl(delegate)));
    }

    @Override
    @SneakyThrows
    public @NotNull GeoLocation lookup(final @NotNull InetAddress address) {
        return cache.get(address);
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class CacheLoaderImpl extends CacheLoader<InetAddress, GeoLocation> {
        GeoLocationManager delegate;

        @Override
        public @NotNull GeoLocation load(final @NotNull InetAddress key) {
            return delegate.lookup(key);
        }
    }
}
