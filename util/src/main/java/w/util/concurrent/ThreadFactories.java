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

package w.util.concurrent;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author whilein
 */
@UtilityClass
public class ThreadFactories {

    public static @NotNull ThreadFactory named(final @NotNull String format) {
        return new Named(new AtomicInteger(), format);
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Named implements ThreadFactory {
        AtomicInteger counter;
        String format;

        @Override
        public Thread newThread(final @NotNull Runnable r) {
            return new Thread(r, String.format(format, counter.getAndIncrement()));
        }
    }

}
