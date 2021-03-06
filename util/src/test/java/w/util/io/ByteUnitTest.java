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

package w.util.io;

import org.junit.jupiter.api.Test;
import w.util.ByteUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class ByteUnitTest {

    @Test
    void convert() {
        assertEquals(0L, ByteUnit.UNO.toKibi(1L));
        assertEquals(0.5, ByteUnit.UNO.toKibi(512.0));

        assertEquals(1024L, ByteUnit.KIBI.toUno(1L));
        assertEquals(1024L, ByteUnit.MEBI.toKibi(1L));
        assertEquals(1024L, ByteUnit.GIBI.toMebi(1L));
    }

}