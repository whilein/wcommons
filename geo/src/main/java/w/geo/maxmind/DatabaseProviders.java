/*
 *    Copyright 2024 Whilein
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

package w.geo.maxmind;

import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kamranzafar.jtar.TarEntry;
import org.kamranzafar.jtar.TarInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author whilein
 */
@UtilityClass
public class DatabaseProviders {

    private static final String DEFAULT_MAXMIND_DATABASE_URL
            = "https://download.maxmind.com/app/geoip_download?edition_id=GeoLite2-City&license_key=%s&suffix=tar.gz";

    public static @NotNull DatabaseProvider download(@NotNull String licenseKey) {
        try {
            return unpackTarGz(fromURL(String.format(DEFAULT_MAXMIND_DATABASE_URL,
                    URLEncoder.encode(licenseKey, StandardCharsets.UTF_8))));
        } catch (MalformedURLException e) { // unreachable
            throw new IllegalStateException("Default download URL is malformed", e);
        }
    }

    public @NotNull DatabaseProvider unpackZip(@NotNull DatabaseProvider delegate) {
        return new Zip(delegate);
    }

    public @NotNull DatabaseProvider unpackTar(@NotNull DatabaseProvider delegate) {
        return new Tar(delegate);
    }

    public @NotNull DatabaseProvider unpackTarGz(@NotNull DatabaseProvider delegate) {
        return unpackTar(decompressGZIP(delegate));
    }

    public @NotNull DatabaseProvider decompressGZIP(@NotNull DatabaseProvider delegate) {
        return new GZIP(delegate);
    }

    public @NotNull DatabaseProvider fromURL(@NotNull String url) throws MalformedURLException {
        return fromURL(new URL(url));
    }

    public @NotNull DatabaseProvider fromURL(@NotNull URL url) {
        return new FromURL(url);
    }

    public @NotNull DatabaseProvider fromFile(@NotNull Path path) {
        return new FromFile(path);
    }

    public @NotNull DatabaseProvider cache(
            @NotNull DatabaseProvider delegate,
            @NotNull Path destination,
            @Nullable Duration ttl
    ) {
        return new Cache(delegate, destination, ttl);
    }

    public @NotNull DatabaseProvider cache(@NotNull DatabaseProvider delegate, @NotNull Path destination) {
        return cache(delegate, destination, null);
    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Cache implements DatabaseProvider {

        DatabaseProvider delegate;

        Path destination;
        Duration ttl;

        Lock lock = new ReentrantLock();

        private InputStream openStream0() throws IOException {
            if (!Files.exists(destination) || !checkTTL()) {
                try (val is = delegate.openStream();
                     val os = Files.newOutputStream(destination)) {
                    is.transferTo(os);
                }
            }

            return Files.newInputStream(destination);
        }

        @Override
        public @NotNull InputStream openStream() throws IOException {
            lock.lock();

            try {
                return openStream0();
            } finally {
                lock.unlock();
            }
        }

        private boolean checkTTL() throws IOException {
            if (ttl == null) return true;

            val timestamp = Files.getLastModifiedTime(destination)
                    .toInstant();

            return Duration.between(timestamp, Instant.now()).compareTo(ttl) < 0;
        }

    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Zip implements DatabaseProvider {

        DatabaseProvider delegate;

        @Override
        public @NotNull InputStream openStream() throws IOException {
            val is = delegate.openStream();
            val zis = new ZipInputStream(is);

            ZipEntry entry;

            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".mmdb")) {
                    return zis;
                }
            }

            throw new IllegalStateException("No .mmdb file in ZIP archive from " + delegate);
        }

    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class GZIP implements DatabaseProvider {

        DatabaseProvider delegate;

        @Override
        public @NotNull InputStream openStream() throws IOException {
            return new GZIPInputStream(delegate.openStream());
        }

    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class Tar implements DatabaseProvider {

        DatabaseProvider delegate;

        @Override
        public @NotNull InputStream openStream() throws IOException {
            val is = delegate.openStream();
            val tis = new TarInputStream(is);

            TarEntry entry;

            while ((entry = tis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".mmdb")) {
                    return tis;
                }
            }

            throw new IllegalStateException("No .mmdb file in TAR archive from " + delegate);
        }

    }

    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class FromFile implements DatabaseProvider {
        Path source;

        @Override
        public @NotNull InputStream openStream() throws IOException {
            return Files.newInputStream(source);
        }

        @Override
        public String toString() {
            return source.toString();
        }
    }


    @FieldDefaults(makeFinal = true)
    @RequiredArgsConstructor
    private static final class FromURL implements DatabaseProvider {

        URL source;

        @Override
        public @NotNull InputStream openStream() throws IOException {
            return source.openStream();
        }

        @Override
        public String toString() {
            return source.toString();
        }
    }

}
