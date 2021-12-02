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
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import org.jetbrains.annotations.NotNull;

/**
 * @author whilein
 */
public final class YamlConfigParser extends AbstractJacksonConfigParser {

    private YamlConfigParser(final ObjectMapper objectMapper) {
        super(objectMapper);
    }

    public static @NotNull YamlConfigParser create() {
        return new YamlConfigParser(new ObjectMapper(new YAMLFactory()
                .configure(YAMLParser.Feature.EMPTY_STRING_AS_NULL, false)
                .configure(YAMLGenerator.Feature.WRITE_DOC_START_MARKER, false)));
    }

}
