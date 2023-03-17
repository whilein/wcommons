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

package w.util.mutable;

import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface MutableOptionalReference<T> extends MutableReference<T> {

    void clear();

    <X extends Throwable> T orElseThrow(@NotNull X cause) throws X;

    <X extends Throwable> T orElseThrow(@NotNull Supplier<X> supplier) throws X;

    T orElse(T value);

    T orElseGet(@NotNull Supplier<T> value);

    boolean isEmpty();

    boolean isPresent();

}
