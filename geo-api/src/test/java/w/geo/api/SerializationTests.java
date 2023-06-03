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

package w.geo.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
class SerializationTests {

    ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @SneakyThrows
    void testGeoLocation() {
        val country = ImmutableGeoLocation.builder()
                .country(ImmutableCountry.builder()
                        .name("Russia")
                        .isoCode("RU")
                        .build())
                .city("Moscow")
                .build();

        testSerialization("{\"city\":\"Moscow\",\"country\":{\"name\":\"Russia\",\"isoCode\":\"RU\"}}",
                GeoLocation.class, country);
    }


    @Test
    @SneakyThrows
    void testCountry() {
        val country = ImmutableCountry.builder()
                .name("Russia")
                .isoCode("RU")
                .build();

        testSerialization("{\"name\":\"Russia\",\"isoCode\":\"RU\"}",
                Country.class, country);
    }

    @SneakyThrows
    private void testSerialization(String expectJson, Class<?> type, Object object) {
        val json = objectMapper.writeValueAsString(object);
        assertEquals(expectJson, json);

        val deserialized = objectMapper.readValue(json, type);
        assertEquals(deserialized, object);
    }


}