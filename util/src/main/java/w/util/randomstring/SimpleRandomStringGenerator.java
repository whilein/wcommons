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

package w.util.randomstring;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import w.util.RandomUtils;
import w.util.buffering.Buffering;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleRandomStringGenerator implements RandomStringGenerator {

    Supplier<Random> factory;
    char[] dictionary;

    public static @NotNull RandomStringGeneratorBuilder builder() {
        return new SimpleRandomStringGenerator.Builder(new StringBuilder());
    }

    @Override
    public @NotNull String nextString(final int length) {
        try (val buffered = Buffering.getStringBuilder()) {
            val result = buffered.get();
            val random = factory.get();

            for (int i = 0; i < length; i++) {
                result.append(dictionary[random.nextInt(dictionary.length)]);
            }

            return result.toString();
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Builder implements RandomStringGeneratorBuilder {

        StringBuilder dictionary;

        @NonFinal
        Supplier<Random> factory;

        private RandomStringGeneratorBuilder add(final String text) {
            dictionary.append(text);
            return this;
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder randomFactory(
                final @NotNull Supplier<@NotNull Random> randomFactory
        ) {
            this.factory = randomFactory;

            return this;
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder random(final @NotNull Random random) {
            return randomFactory(() -> random);
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder randomSecure() {
            return random(RandomUtils.getSecureRandom());
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder randomThreadLocal() {
            return randomFactory(ThreadLocalRandom::current);
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder addLetters() {
            return add("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz");
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder addUpperLetters() {
            return add("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder addLowerLetters() {
            return add("abcdefghijklmnopqrstuvwxyz");
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder addNumbers() {
            return add("0123456789");
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder setDictionary(final @NotNull String dictionary) {
            this.dictionary.setLength(dictionary.length());
            this.dictionary.replace(0, dictionary.length(), dictionary);

            return this;
        }

        @Override
        public @NotNull RandomStringGeneratorBuilder addDictionary(final @NotNull String dictionary) {
            return add(dictionary);
        }

        private static final Supplier<Random> DEFAULT_RANDOM_FACTORY;

        static {
            val random = RandomUtils.getRandom();
            DEFAULT_RANDOM_FACTORY = () -> random;
        }

        @Override
        public @NotNull RandomStringGenerator build() {
            val randomFactory = factory == null
                    ? DEFAULT_RANDOM_FACTORY
                    : factory;

            return new SimpleRandomStringGenerator(randomFactory, dictionary.toString().toCharArray());
        }
    }
}
