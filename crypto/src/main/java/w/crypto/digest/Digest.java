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

package w.crypto.digest;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import w.crypto.digest.bouncycastle.Sha3DigestFactory;
import w.util.Hex;

import java.nio.charset.StandardCharsets;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Digest {

    MD2(DefaultDigestFactory.create("MD2")),
    MD5(DefaultDigestFactory.create("MD5")),
    SHA_1(DefaultDigestFactory.create("SHA-1")),
    SHA_224(DefaultDigestFactory.create("SHA-224")),
    SHA_256(DefaultDigestFactory.create("SHA-256")),
    SHA5_224(DefaultDigestFactory.create("SHA-512/224")),
    SHA5_256(DefaultDigestFactory.create("SHA-512/256")),
    SHA5_512(DefaultDigestFactory.create("SHA-512")),
    SHA3_224(Sha3DigestFactory.create(224)),
    SHA3_256(Sha3DigestFactory.create(256)),
    SHA3_384(Sha3DigestFactory.create(384)),
    SHA3_512(Sha3DigestFactory.create(512));

    DigestFactory digestFactory;

    public @NotNull DigestAlgorithm createAlgorithm() {
        return digestFactory.create();
    }

    public @NotNull String digest(final @NotNull String input) {
        return Hex.toHex(digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    public byte @NotNull [] digest(final byte @NotNull [] bytes) {
        return digest(bytes, 0, bytes.length);
    }

    public byte @NotNull [] digest(final byte @NotNull [] bytes, final int off, final int len) {
        return createAlgorithm().digest(bytes, off, len);
    }

}
