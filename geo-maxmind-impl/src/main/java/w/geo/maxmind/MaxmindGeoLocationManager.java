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

package w.geo.maxmind;

import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.record.AbstractNamedRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.geo.api.*;
import w.geo.maxmind.provider.MaxmindProvider;

import java.net.InetAddress;
import java.util.Optional;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaxmindGeoLocationManager implements GeoLocationManager {

    DatabaseReader database;

    @SneakyThrows
    public static @NotNull GeoLocationManager create(
            final @NotNull MaxmindProvider provider
    ) {
        return new MaxmindGeoLocationManager(provider.openReader());
    }

    private Optional<String> getName(final AbstractNamedRecord record) {
        val names = record.getNames();

        return Optional.ofNullable(names.get("ru"))
                .or(() -> Optional.ofNullable(names.get("en")));
    }

    @Override
    public @NotNull GeoLocation lookup(final @NotNull InetAddress address) {
        try {
            return database.tryCity(address)
                    .map(result -> {
                        val country = getName(result.getCountry())
                                .map(countryName -> ImmutableCountry.create(
                                        countryName,
                                        result.getCountry().getIsoCode()
                                ))
                                .orElse(null);

                        val city = getName(result.getCity())
                                .orElse(null);

                        return country != null || city != null
                                ? ImmutableGeoLocation.create(country, city)
                                : UnknownGeoLocation.INSTANCE;
                    })
                    .orElse(UnknownGeoLocation.INSTANCE);
        } catch (final Exception e) {
            return UnknownGeoLocation.INSTANCE;
        }
    }

}
