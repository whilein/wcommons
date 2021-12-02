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


import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * @author whilein
 */
public final class JsonConfigParser extends AbstractJacksonConfigParser {

    private JsonConfigParser(final ObjectMapper objectMapper) {
        super(objectMapper);
    }

    private static final class ConfigPrettyPrinter extends DefaultPrettyPrinter {
        @Override
        public @NotNull DefaultPrettyPrinter createInstance() {
            return new ConfigPrettyPrinter();
        }

        @Override
        public void writeObjectFieldValueSeparator(final @NotNull JsonGenerator jg) throws IOException {
            jg.writeRaw(": ");
        }
    }

    public static @NotNull ConfigParser create() {
        return new JsonConfigParser(new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT)
                .setDefaultPrettyPrinter(new ConfigPrettyPrinter()));
    }


}
