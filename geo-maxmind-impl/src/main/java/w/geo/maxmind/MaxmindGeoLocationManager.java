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

package w.geo.maxmind;

import com.maxmind.db.NoCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.record.AbstractNamedRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;
import w.geo.api.GeoLocation;
import w.geo.api.GeoLocationManager;
import w.geo.api.ImmutableCountry;
import w.geo.api.ImmutableGeoLocation;
import w.geo.api.UnknownGeoLocation;

import java.net.InetAddress;
import java.net.URL;
import java.util.Optional;
import java.util.zip.GZIPInputStream;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaxmindGeoLocationManager implements GeoLocationManager {

    private static final String DATABASE_URL = "https://download.maxmind.com/app/geoip_download" +
            "?edition_id=GeoLite2-City&license_key=%s&suffix=tar.gz";

    DatabaseReader database;

    @SneakyThrows
    public static @NotNull GeoLocationManager create(
            final @NotNull String key
    ) {
        val url = new URL(String.format(DATABASE_URL, key));
        val urlConnection = url.openConnection();

        try (val is = urlConnection.getInputStream();
             val gis = new GZIPInputStream(is);
             val tis = new TarInputStream(gis)) {
            TarEntry entry;

            while ((entry = tis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".mmdb")) {
                    return new MaxmindGeoLocationManager(new DatabaseReader.Builder(tis)
                            .withCache(NoCache.getInstance())
                            .build());
                }
            }
        }

        throw new IllegalStateException("Cannot unpack maxmind database: no .mmdb file found");
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
                    .map(result -> ImmutableGeoLocation.create(
                            getName(result.getCountry())
                                    .map(country -> ImmutableCountry.create(country, result.getCountry().getIsoCode()))
                                    .orElse(null),
                            getName(result.getCity())
                                    .orElse(null)
                    ))
                    .orElse(UnknownGeoLocation.INSTANCE);
        } catch (final Exception e) {
            return UnknownGeoLocation.INSTANCE;
        }
    }

}
