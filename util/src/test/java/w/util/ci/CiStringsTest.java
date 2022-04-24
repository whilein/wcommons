/*
 *    Copyright 2022 Whilein
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

package w.util.ci;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
class CiStringsTest {

    @Test
    void testInMap() {
        final String[] array = {
                "ХУЙ член залупа",
                "хуй ЧЛЕН залупа"
        };

        val x = CiStrings.from(array[0]);
        val y = CiStrings.from(array[1]);

        assertEquals(x, y);

        val map = new HashMap<Object, Integer>();
        map.put(x, 123);
        map.put(y, 321);

        assertEquals(map.get(x), Integer.valueOf(321));
        assertEquals(map.get(y), Integer.valueOf(321));
    }

}