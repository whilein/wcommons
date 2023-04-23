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

package w.util.random;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
public class WeightedRandomTests {

    @Test
    public void test() {
        val values = Map.of(
                "123", 0.2,
                "321", 0.4,
                "333", 0.1,
                "666", 0.3
        );

        val counts = new HashMap<String, Integer>();

        val weightedRandom = SimpleWeightedRandom.create(values);

        final int iterations = 100_000;

        for (int i = 0; i < iterations; i++) {
            val object = weightedRandom.nextObject();
            counts.merge(object, 1, Integer::sum);
        }

        for (val value : values.entrySet()) {
            assertEquals(value.getValue(), counts.get(value.getKey()) / (double) iterations, 0.1);
        }
    }

}
