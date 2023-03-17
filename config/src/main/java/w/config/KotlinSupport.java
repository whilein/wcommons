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

package w.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.kotlin.KotlinModule;
import lombok.experimental.UtilityClass;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
@UtilityClass
public class KotlinSupport {

    private final Support SUPPORT;

    static {
        Support support;

        try {
            Class.forName("com.fasterxml.jackson.module.kotlin.KotlinModule");

            support = new Support();
        } catch (final ClassNotFoundException e) {
            support = null;
        }

        SUPPORT = support;
    }

    public void tryEnable(final ObjectMapper objectMapper) {
        if (SUPPORT != null) {
            SUPPORT.enable(objectMapper);
        }
    }

    public boolean isAvailable() {
        return SUPPORT != null;
    }

    private static final class Support {

        public void enable(final @NotNull ObjectMapper objectMapper) {
            objectMapper.registerModule(new KotlinModule.Builder()
                    .build());
        }

    }
}
