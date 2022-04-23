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

package w.crypto;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class DefaultDigestFactory extends AbstractDigestFactory {

    String algorithm;

    public static @NotNull DigestFactory create(final @NotNull String algorithm) {
        try {
            MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            return UnsupportedDigestFactory.getInstance();
        }

        return new DefaultDigestFactory(algorithm);
    }

    @Override
    @SneakyThrows
    protected MessageDigest getDigest() {
        return MessageDigest.getInstance(algorithm);
    }
}
