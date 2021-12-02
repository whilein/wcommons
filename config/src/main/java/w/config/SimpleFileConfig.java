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

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.experimental.Delegate;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleFileConfig implements FileConfig {

    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private static final ConfigParser JSON = JsonConfigParser.create(), YAML = YamlConfigParser.create();

    File file;
    File parentFile;

    ConfigParser parser;

    @NonFinal
    @Delegate(types = ConfigObject.class)
    ConfigObject delegate;

    @SneakyThrows
    private static FileConfig _create(
            final File file,
            final ConfigParser parser
    ) {
        val canonicalFile = file.getCanonicalFile();

        val config = new SimpleFileConfig(canonicalFile, canonicalFile.getParentFile(), parser);
        config.reload();

        return config;
    }

    private static FileConfig _create(
            final File file
    ) {
        val fileName = file.getName();
        val extensionSeparator = fileName.lastIndexOf('.');

        if (extensionSeparator == -1) {
            throw new IllegalStateException("Cannot get an extension of file " + file);
        }

        val extension = fileName.substring(extensionSeparator + 1);

        final ConfigParser parser;

        switch (extension) {
            case "yml":
            case "yaml":
                parser = YAML;
                break;
            case "json":
                parser = JSON;
                break;
            default:
                throw new IllegalStateException("Cannot find config parser for " + file);
        }

        return _create(file, parser);
    }

    public static @NotNull FileConfig create(
            final @NotNull File file,
            final @NotNull ConfigParser parser
    ) {
        return _create(file, parser);
    }

    public static @NotNull FileConfig create(
            final @NotNull String name,
            final @NotNull ConfigParser parser
    ) {
        return _create(new File(name), parser);
    }

    public static @NotNull FileConfig create(
            final @NotNull File parent,
            final @NotNull String name,
            final @NotNull ConfigParser parser
    ) {
        return _create(new File(parent, name), parser);
    }

    public static @NotNull FileConfig create(final @NotNull File file) {
        return _create(file);
    }

    public static @NotNull FileConfig create(final @NotNull String name) {
        return _create(new File(name));
    }

    public static @NotNull FileConfig create(final @NotNull File parent, final @NotNull String name) {
        return _create(new File(parent, name));
    }

    @Override
    public void save() {
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            throw new IllegalStateException("Unable to save config at " + file
                    + ": can't create parent directory");
        }

        delegate.writeTo(file);
    }

    @Override
    public void saveDefaults(final @NotNull String resource) {
        if (!file.exists()) {
            val caller = STACK_WALKER.getCallerClass();

            try (val resourceStream = caller.getResourceAsStream(resource)) {
                if (resourceStream == null) {
                    throw new IllegalStateException("Cannot save defaults: no " + resource + " found");
                }

                delegate = parser.parse(resourceStream);
                delegate.writeTo(file);
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload() {
        delegate = file.exists()
                ? parser.parse(file)
                : parser.newObject();
    }
}
