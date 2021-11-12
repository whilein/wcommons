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

package w.commons.sql;

import com.zaxxer.hikari.HikariConfig;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.UtilityClass;
import lombok.val;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@UtilityClass
public final class MessengerFactory {

    private final Map<MessengerPoolKey, Messenger> POOL = new HashMap<>();

    public @NotNull Messenger getOrCreate(
            final @NonNull Dialect dialect
    ) {
        synchronized (POOL) {
            val host = System.getenv(dialect + "_HOST");
            val port = Integer.parseInt(System.getenv(dialect + "_PORT"));
            val user = System.getenv(dialect + "_USER");
            val password = System.getenv(dialect + "_PASSWORD");
            val database = System.getenv(dialect + "_DATABASE");

            return POOL.computeIfAbsent(
                    new MessengerPoolKey(dialect, host, port, user, password, database),
                    MessengerFactory::init
            );
        }
    }

    private Messenger init(
            final MessengerPoolKey key
    ) {
        val hikariProperties = new Properties();

        // Русские символы
        hikariProperties.put("dataSource.useUnicode", "true");
        hikariProperties.put("dataSource.characterEncoding", "utf8");

        hikariProperties.put("dataSource.cachePrepStmts", "true");
        hikariProperties.put("dataSource.prepStmtCacheSize", "250");
        hikariProperties.put("dataSource.prepStmtCacheSqlLimit", "2048");

        val config = new HikariConfig(hikariProperties);

        config.setLeakDetectionThreshold(30000);
        config.setConnectionTimeout(30000);
        config.setMaxLifetime(60000);

        config.setDriverClassName(key.getDialect().getDriver());

        val dialect = key.getDialect();

        val url = new StringBuilder();
        url.append("jdbc:");
        url.append(dialect.getName());
        url.append("://");
        url.append(key.getHost());
        url.append(':');
        url.append(key.getPort());

        if (key.getDatabase() != null) {
            url.append('/');
            url.append(key.getDatabase());
        }

        config.setJdbcUrl(url.toString());

        config.setUsername(key.getUser());
        config.setPassword(key.getPassword());

        final int pool = Integer.getInteger(dialect + "_POOL", 8);

        config.setMaximumPoolSize(pool);
        config.setMinimumIdle(pool);

        return HikariMessenger.create(dialect, config);
    }

    @Getter
    @EqualsAndHashCode
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    @FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
    private static final class MessengerPoolKey {
        Dialect dialect;
        String host;
        int port;
        String user;
        String password;
        String database;
    }

}
