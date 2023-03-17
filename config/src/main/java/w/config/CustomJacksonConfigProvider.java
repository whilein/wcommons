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

package w.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
public final class CustomJacksonConfigProvider extends AbstractJacksonConfigProvider {

    private CustomJacksonConfigProvider(
            final ObjectWriter objectWriter,
            final ObjectReader objectReader,
            final ObjectConverter objectConverter
    ) {
        super(objectWriter, objectReader, objectConverter);
    }

    public static @NotNull ConfigProvider create(final @NotNull ObjectMapper objectMapper) {
        return new CustomJacksonConfigProvider(objectMapper.writer(), objectMapper.reader(),
                objectMapper::convertValue);
    }

    public static @NotNull ConfigProvider create(final @NotNull ObjectWriter objectWriter,
                                                              final @NotNull ObjectReader objectReader,
                                                              final @NotNull ObjectConverter objectConverter) {
        return new CustomJacksonConfigProvider(objectWriter, objectReader, objectConverter);
    }

}
