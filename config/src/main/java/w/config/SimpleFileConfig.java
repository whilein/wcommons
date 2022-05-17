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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class SimpleFileConfig implements FileConfig {
    private static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    private interface Src {

        boolean exists();

        void makeParentDirectory() throws IOException;

        @NotNull OutputStream openOutput() throws IOException;

        @NotNull InputStream openInput() throws IOException;
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class NioSrc implements Src {
        Path path;
        Path parentPath;

        @Override
        public boolean exists() {
            return Files.exists(path);
        }

        @Override
        public void makeParentDirectory() throws IOException {
            Files.createDirectories(parentPath);
        }

        @Override
        public @NotNull OutputStream openOutput() throws IOException {
            return Files.newOutputStream(path);
        }

        @Override
        public @NotNull InputStream openInput() throws IOException {
            return Files.newInputStream(path);
        }
    }

    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    private static final class IoSrc implements Src {
        File file;
        File parentFile;

        @Override
        public boolean exists() {
            return file.exists();
        }

        @Override
        public void makeParentDirectory() throws IOException {
            if (!parentFile.exists() && !parentFile.mkdirs()) {
                throw new IOException("Cannot mkdir: " + parentFile);
            }
        }

        @Override
        public @NotNull OutputStream openOutput() throws IOException {
            return new FileOutputStream(file);
        }

        @Override
        public @NotNull InputStream openInput() throws IOException {
            return new FileInputStream(file);
        }
    }

    Src src;

    ConfigProvider provider;

    @NonFinal
    @Delegate(types = Config.class)
    Config delegate;

    private static ConfigProvider _findProvider(final String fileName) {
        val extensionSeparator = fileName.lastIndexOf('.');

        if (extensionSeparator == -1) {
            throw new IllegalStateException("Cannot get an extension of file " + fileName);
        }

        val extension = fileName.substring(extensionSeparator + 1);

        final ConfigProvider provider = switch (extension) {
            case "yml", "yaml" -> YamlConfigProvider.INSTANCE;
            case "json" -> JsonConfigProvider.INSTANCE;
            default -> throw new IllegalStateException("Cannot find config provider for " + fileName);
        };

        return provider;
    }

    @SneakyThrows
    private static FileConfig _create(
            final Path path,
            final ConfigProvider provider
    ) {
        val absolutePath = path.toAbsolutePath();

        val config = new SimpleFileConfig(new NioSrc(absolutePath, absolutePath.getParent()), provider);
        config.reload();

        return config;
    }

    @SneakyThrows
    private static FileConfig _create(
            final File file,
            final ConfigProvider provider
    ) {
        val canonicalFile = file.getCanonicalFile();

        val config = new SimpleFileConfig(new IoSrc(canonicalFile, canonicalFile.getParentFile()), provider);
        config.reload();

        return config;
    }

    private static FileConfig _create(
            final Path path
    ) {
        return _create(path, _findProvider(path.getFileName().toString()));
    }

    private static FileConfig _create(
            final File file
    ) {
        return _create(file, _findProvider(file.getName()));
    }

    public static @NotNull FileConfig create(
            final @NotNull Path path,
            final @NotNull ConfigProvider provider
    ) {
        return _create(path, provider);
    }

    public static @NotNull FileConfig create(final @NotNull Path path) {
        return _create(path);
    }

    public static @NotNull FileConfig create(
            final @NotNull File file,
            final @NotNull ConfigProvider provider
    ) {
        return _create(file, provider);
    }

    public static @NotNull FileConfig create(
            final @NotNull String name,
            final @NotNull ConfigProvider provider
    ) {
        return _create(new File(name), provider);
    }

    public static @NotNull FileConfig create(
            final @NotNull File parent,
            final @NotNull String name,
            final @NotNull ConfigProvider provider
    ) {
        return _create(new File(parent, name), provider);
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

    public static @NotNull FileConfig create(final @NotNull String parent, final @NotNull String name) {
        return _create(new File(parent, name));
    }

    public static @NotNull FileConfig create(
            final @NotNull String parent,
            final @NotNull String name,
            final @NotNull ConfigProvider provider
    ) {
        return _create(new File(parent, name), provider);
    }

    @Override
    public void save() {
        try {
            src.makeParentDirectory();

            try (val os = src.openOutput()) {
                delegate.writeTo(os);
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveDefaults(final @NotNull String resource) {
        if (!src.exists()) {
            val cl = STACK_WALKER.getCallerClass().getClassLoader();

            try (val resourceStream = cl.getResourceAsStream(resource.startsWith("/")
                    ? resource.substring(1)
                    : resource)) {
                if (resourceStream == null) {
                    throw new IllegalStateException("Cannot save defaults, because default config "
                                                    + resource + " not found");
                }

                delegate = provider.parse(resourceStream);

                src.makeParentDirectory();

                try (val os = src.openOutput()) {
                    delegate.writeTo(os);
                }
            } catch (final IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void reload() {
        if (src.exists()) {
            try (val is = src.openInput()) {
                delegate = provider.parse(is);
            } catch (final Exception e) {
                delegate = provider.newObject();
            }
        } else {
            delegate = provider.newObject();
        }
    }
}
