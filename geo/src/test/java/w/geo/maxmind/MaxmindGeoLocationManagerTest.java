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

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import w.geo.GeoLocationManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * @author whilein
 */
final class MaxmindGeoLocationManagerTest {

    private static final String TEST_LICENSE_KEY = "P5g0fVdAQIq8yQau";

    static GeoLocationManager geoLocationManager;

    @BeforeAll
    static void setup() throws IOException {
        geoLocationManager = MaxmindGeoLocationManager.create(DatabaseProviders.cache(DatabaseProviders.download(TEST_LICENSE_KEY),
                Path.of("cache.mmdb"), Duration.ofMinutes(15)));
    }

    @Test
    public void lookupGoogle() throws UnknownHostException {
        val response = geoLocationManager.lookup(InetAddress.getByName("google.com"));
        assertNotNull(response.country());

        assertEquals("US", response.country().isoCode());
    }

}
