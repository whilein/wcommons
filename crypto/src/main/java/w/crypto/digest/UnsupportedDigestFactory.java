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

package w.crypto.digest;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UnsupportedDigestFactory implements DigestFactory {

    private static final DigestFactory INSTANCE = new UnsupportedDigestFactory();

    public static @NotNull DigestFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public @NotNull DigestAlgorithm create() {
        throw new UnsupportedOperationException();
    }
}
