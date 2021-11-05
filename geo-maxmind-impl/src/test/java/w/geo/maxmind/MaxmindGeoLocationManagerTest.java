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

import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import w.geo.api.GeoLocationManager;

import java.net.InetAddress;

/**
 * @author whilein
 */
final class MaxmindGeoLocationManagerTest {

    static GeoLocationManager geoLocationManager;

    @BeforeAll
    static void setup() {
        geoLocationManager = MaxmindGeoLocationManager.create("URrbSKcE2wlyv7bR");
    }

    @Test
    @SneakyThrows
    void localHostLookup() {
        val result = geoLocationManager.lookup(InetAddress.getByName("mc.lastcraft.net"));
        System.out.println(result);

    }

}
