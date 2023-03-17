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

package w.geo.ipinfo;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import w.geo.api.*;

import java.net.InetAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class IpInfoGeoLocationManager implements GeoLocationManager {

    private static final Map<String, String> ISO2COUNTRY = Collections.unmodifiableMap(
            new HashMap<>() {
                {
                    for (val locale : Locale.getAvailableLocales()) {
                        put(locale.getCountry(), locale.getDisplayCountry());
                    }
                }
            }
    );

    HttpClient client;
    ObjectMapper objectMapper;

    @SneakyThrows
    public static @NotNull GeoLocationManager create() {
        val objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return new IpInfoGeoLocationManager(
                HttpClient.newHttpClient(),
                objectMapper
        );
    }

    @Override
    public @NotNull GeoLocation lookup(final @NotNull InetAddress address) {
        try {
            val json = client.send(
                    HttpRequest.newBuilder()
                            .uri(URI.create("https://ipinfo.io/" + address.getHostAddress()))
                            .header("Accept", "application/json")
                            .build(),
                    HttpResponse.BodyHandlers.ofString()
            );

            val response = objectMapper.readValue(json.body(), Resp.class);

            val city = response.city;
            val country = response.country;

            if (city == null && country == null) {
                return UnknownGeoLocation.INSTANCE;
            }

            return ImmutableGeoLocation.create(
                    ImmutableCountry.create(
                            ISO2COUNTRY.getOrDefault(country, country),
                            country
                    ),
                    city
            );
        } catch (final Exception e) {
            e.printStackTrace();

            return UnknownGeoLocation.INSTANCE;
        }
    }

    @Setter
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE)
    private static final class Resp {
        String city;
        String country;
    }
}
