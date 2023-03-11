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

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.unsafe.Unsafe;

import java.lang.invoke.VarHandle;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * @author whilein
 */
class UnsafeTests {

    Unsafe unsafe;

    @BeforeEach
    void setup() {
        unsafe = Unsafe.getUnsafe();

        assertTrue(unsafe.isAvailable());
    }

    @Test
    @SneakyThrows
    void putStaticField() {
        assertEquals("Old Value", Dummy.STATIC_VALUE);

        unsafe.putStaticField(Dummy.class.getDeclaredField("STATIC_VALUE"), "New Value");

        assertEquals("New Value", Dummy.STATIC_VALUE);
    }

    @Test
    @SneakyThrows
    void allocateInstance() {
        val lookup = unsafe.trustedLookup();
        val vh = lookup.findVarHandle(Dummy.class, "objectValue", String.class);

        val dummy = (Dummy) unsafe.allocateInstance(Dummy.class);
        assertNull(dummy.objectValue);
        vh.set(dummy, "321");
        assertEquals("321", dummy.objectValue);
    }

    @Test
    @SneakyThrows
    void putField() {
        val dummy = new Dummy("Old Value");

        assertEquals("Old Value", dummy.objectValue);

        unsafe.putField(Dummy.class.getDeclaredField("objectValue"), dummy, "New Value");

        assertEquals("New Value", dummy.objectValue);
    }

    @RequiredArgsConstructor
    private static final class Dummy {
        private static final Object STATIC_VALUE;

        private final String objectValue;

        static {
            STATIC_VALUE = "Old Value";
        }
    }

}