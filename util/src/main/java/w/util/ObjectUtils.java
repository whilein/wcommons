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

package w.util;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;
import w.util.lookup.FullAccessLookup;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;

/**
 * @author whilein
 */
@UtilityClass
public class ObjectUtils {

    private final MethodHandle CLONE;

    static {
        try {
            CLONE = FullAccessLookup.getLookup().findVirtual(Object.class, "clone",
                    MethodType.methodType(Object.class));
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SneakyThrows
    public Object clone(final @NotNull Object object) {
        return CLONE.invokeExact(object);
    }

}