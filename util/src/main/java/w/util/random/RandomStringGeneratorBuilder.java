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

import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.function.Supplier;

/**
 * @author whilein
 */
public interface RandomStringGeneratorBuilder {

    /**
     * Установить рандом, по умолчанию используется {@link Random} из {@link w.util.RandomUtils#getRandom()}.
     *
     * @param randomFactory Фабрика {@link Random}
     * @return {@code this}
     */
    @NotNull RandomStringGeneratorBuilder randomFactory(@NotNull Supplier<@NotNull Random> randomFactory);

    /**
     * Установить рандом, по умолчанию используется {@link Random} из {@link w.util.RandomUtils#getRandom()}.
     *
     * @param random {@link Random}
     * @return {@code this}
     */
    @NotNull RandomStringGeneratorBuilder random(@NotNull Random random);

    /**
     * Установить {@link w.util.RandomUtils#getSecureRandom()} как рандом, по умолчанию
     * используется {@link Random} из {@link w.util.RandomUtils#getRandom()}.
     *
     * @return {@code this}
     */
    @NotNull RandomStringGeneratorBuilder randomSecure();

    /**
     * Установить {@link java.util.concurrent.ThreadLocalRandom} как рандом, по умолчанию
     * используется {@link Random} из {@link w.util.RandomUtils#getRandom()}.
     *
     * @return {@code this}
     */
    @NotNull RandomStringGeneratorBuilder randomThreadLocal();

    @NotNull RandomStringGeneratorBuilder addLetters();

    @NotNull RandomStringGeneratorBuilder addUpperLetters();

    @NotNull RandomStringGeneratorBuilder addLowerLetters();

    @NotNull RandomStringGeneratorBuilder addNumbers();

    @NotNull RandomStringGeneratorBuilder setDictionary(@NotNull String dictionary);

    @NotNull RandomStringGeneratorBuilder addDictionary(@NotNull String dictionary);

    @NotNull RandomStringGenerator build();


}
