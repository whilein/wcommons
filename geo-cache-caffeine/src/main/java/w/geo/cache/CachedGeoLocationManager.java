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

package w.geo.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.geo.api.GeoLocation;
import w.geo.api.GeoLocationManager;
import w.geo.api.UnknownGeoLocation;

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
                Caffeine.newBuilder()
                        .softValues()
                        .expireAfterAccess(24, TimeUnit.HOURS)
                        .build(delegate::lookup)
        );
    }

    public static @NotNull GeoLocationManager create(
            final @NotNull GeoLocationManager delegate,
            final @NotNull Consumer<@NotNull Caffeine<?, ?>> builderInitializer
    ) {
        val builder = Caffeine.newBuilder();
        builderInitializer.accept(builder);

        return new CachedGeoLocationManager(builder.build(delegate::lookup));
    }

    @Override
    public @NotNull GeoLocation lookup(final @NotNull InetAddress address) {
        val location = cache.get(address);

        return location == null
                ? UnknownGeoLocation.INSTANCE
                : location;
    }
}
