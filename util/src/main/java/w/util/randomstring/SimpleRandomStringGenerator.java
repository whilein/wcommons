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
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Random;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleRandomStringGenerator implements RandomStringGenerator {

    private static final Random RANDOM = new SecureRandom();

    char[] dictionary;

    public static @NotNull RandomStringGeneratorBuilder builder() {
        return new SimpleRandomStringGenerator.Builder(new StringBuilder());
    }

    @Override
    public @NotNull String nextString(final int length) {
        val output = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            output.append(dictionary[RANDOM.nextInt(dictionary.length)]);
        }

        return output.toString();
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class Builder implements RandomStringGeneratorBuilder {

        StringBuilder dictionary;

        private RandomStringGeneratorBuilder add(final String text) {
            dictionary.append(text);
            return this;
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

        @Override
        public @NotNull RandomStringGenerator build() {
            return new SimpleRandomStringGenerator(dictionary.toString().toCharArray());
        }
    }
}
