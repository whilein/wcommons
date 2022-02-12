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

package w.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
final class NumberUtilsTest {

    @Test
    void formatNumber_long() {
        assertEquals("-1", NumberUtils.formatNumber(-1));
        assertEquals("0", NumberUtils.formatNumber(0));
        assertEquals("1", NumberUtils.formatNumber(1L));
        assertEquals("1,000", NumberUtils.formatNumber(1000L));
        assertEquals("1,000,000", NumberUtils.formatNumber(1000000L));
        assertEquals("1,000,000,000", NumberUtils.formatNumber(1000000000L));
        assertEquals("1,000,000,000,000", NumberUtils.formatNumber(1000000000000L));
    }

    @Test
    void formatNumber_double() {
        assertEquals("-0.12", NumberUtils.formatNumber(-0.123));
        assertEquals("0.12", NumberUtils.formatNumber(0.123));
        assertEquals("1.12", NumberUtils.formatNumber(1.123));
        assertEquals("1,000.12", NumberUtils.formatNumber(1000.123));
        assertEquals("1,000,000.12", NumberUtils.formatNumber(1000000.123));
        assertEquals("1,000,000,000.12", NumberUtils.formatNumber(1000000000.123));
        assertEquals("1,000,000,000,000.12", NumberUtils.formatNumber(1000000000000.123));
    }
}