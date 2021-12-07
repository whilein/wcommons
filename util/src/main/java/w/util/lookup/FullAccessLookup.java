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

package w.util.lookup;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.unsafe.UnsafeInternals;

import java.lang.invoke.MethodHandles;

/**
 * @author whilein
 */
@UtilityClass
public class FullAccessLookup {

    private volatile MethodHandles.Lookup IMPL_LOOKUP;

    public @NotNull MethodHandles.Lookup getLookup() {
        if (IMPL_LOOKUP == null) {
            synchronized (FullAccessLookup.class) {
                if (IMPL_LOOKUP == null) {
                    initLookup();
                }
            }
        }

        return IMPL_LOOKUP;
    }

    @SneakyThrows
    private void initLookup() {
        val field = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");

        try {
            field.setAccessible(true);
        } catch (final RuntimeException e) {
            if (!e.getClass().getName().equals("java.lang.reflect.InaccessibleObjectException"))
                throw e;
        }

        try {
            IMPL_LOOKUP = (MethodHandles.Lookup) field.get(null);
        } catch (final IllegalAccessException e) {
            IMPL_LOOKUP = (MethodHandles.Lookup) UnsafeInternals.staticFieldValue(field);
        }
    }


}
