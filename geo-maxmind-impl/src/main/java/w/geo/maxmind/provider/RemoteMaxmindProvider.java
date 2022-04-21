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

package w.geo.maxmind.provider;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author whilein
 */
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public final class RemoteMaxmindProvider extends AbstractMaxmindProvider {

    private static final String DATABASE_URL = "https://download.maxmind.com/app/geoip_download" +
                                               "?edition_id=GeoLite2-City&license_key=%s&suffix=tar.gz";

    String key;

    public static @NotNull MaxmindProvider create(final @NotNull String key) {
        return new RemoteMaxmindProvider(key);
    }


    @Override
    protected InputStream openInputStream() throws IOException {
        val url = new URL(String.format(DATABASE_URL, key));
        val urlConnection = url.openConnection();

        return urlConnection.getInputStream();
    }

}
