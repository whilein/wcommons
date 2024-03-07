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

package w.geo.maxmind;

import com.maxmind.db.NoCache;
import com.maxmind.geoip2.DatabaseReader;
import com.maxmind.geoip2.exception.GeoIp2Exception;
import com.maxmind.geoip2.model.CityResponse;
import com.maxmind.geoip2.record.AbstractNamedRecord;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.geo.GeoLocationLookupException;
import w.geo.GeoLocationManager;
import w.geo.model.Country;
import w.geo.model.GeoLocation;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collections;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class MaxmindGeoLocationManager implements GeoLocationManager {

    private static final String DEFAULT_LOCALE = "en";

    String locale;
    DatabaseReader reader;

    public static @NotNull GeoLocationManager create(@NotNull DatabaseProvider provider)
            throws IOException {
        return create(DEFAULT_LOCALE, provider);
    }

    public static @NotNull GeoLocationManager create(@NotNull String locale, @NotNull DatabaseProvider provider)
            throws IOException {
        DatabaseReader reader;

        try (val is = provider.openStream()) {
            reader = new DatabaseReader.Builder(is)
                    .locales(Collections.singletonList(locale))
                    .withCache(NoCache.getInstance())
                    .build();
        }

        return new MaxmindGeoLocationManager(locale, reader);
    }

    private static String resolveNamed(AbstractNamedRecord record, String locale) {
        val names = record.getNames();
        if (names.isEmpty()) return null;

        String name = names.get(locale);
        if (name == null && !locale.equals(DEFAULT_LOCALE)) {
            name = names.get(DEFAULT_LOCALE);
        }

        return name;
    }

    private GeoLocation mapResponse(CityResponse response) {
        val mmCountry = response.getCountry();
        val mmCity = response.getCity();

        val countryName = resolveNamed(mmCountry, locale);
        val country = countryName == null ? null : new Country(countryName, mmCountry.getIsoCode());

        val city = resolveNamed(mmCity, locale);

        return new GeoLocation(city, country);
    }

    @Override
    public @NotNull GeoLocation lookup(@NotNull InetAddress address) throws GeoLocationLookupException {
        try {
            return reader.tryCity(address)
                    .map(this::mapResponse)
                    .orElse(GeoLocation.unknown());
        } catch (IOException | GeoIp2Exception e) {
            throw new GeoLocationLookupException("Failed lookup: " + address, e);
        }
    }

}
