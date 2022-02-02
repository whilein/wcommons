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

package w.util.hash;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.jetbrains.annotations.NotNull;
import w.util.Hex;

import java.nio.charset.StandardCharsets;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum Hash {

    SHA_256(DefaultDigestFactory.create("SHA-256")),
    KECCAK_224(KeccakDigestFactory.create(224)),
    KECCAK_256(KeccakDigestFactory.create(256)),
    KECCAK_384(KeccakDigestFactory.create(384)),
    KECCAK_512(KeccakDigestFactory.create(512));

    DigestFactory digestFactory;

    public @NotNull Digest getDigest() {
        return digestFactory.create();
    }

    public @NotNull String digest(final @NotNull String input) {
        return Hex.toHex(digest(input.getBytes(StandardCharsets.UTF_8)));
    }

    public byte @NotNull [] digest(final byte @NotNull [] bytes) {
        return digest(bytes, 0, bytes.length);
    }

    public byte @NotNull [] digest(final byte @NotNull [] bytes, final int off, final int len) {
        return getDigest().process(bytes, off, len);
    }

}
