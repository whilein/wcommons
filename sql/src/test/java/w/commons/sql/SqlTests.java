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
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Random;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author whilein
 */
class SqlTests {

    Messenger messenger;

    @BeforeEach
    void openConnection() {
        val config = new HikariConfig();
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("admin");
        config.setPassword("admin");
        config.setJdbcUrl("jdbc:h2:mem:" + Integer.toHexString(new Random().nextInt(Integer.MAX_VALUE)));
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(1);

        messenger = HikariMessenger.create(config);

        messenger.scriptRunner()
                .run("init.sql")
                .schema("_").run("init_schema.sql");
    }

    @AfterEach
    void closeConnection() {
        messenger.close();
    }

    @Test
    void fetch() {
        val result = messenger.fetch("SELECT * FROM `_`.`USERS`")
                .map(rs -> rs.getString("NAME") + ':' + rs.getString("BALANCE"))
                .collect(Collectors.joining(";"))
                .call();

        assertEquals("User0:0;User1:50;User2:100;User3:500;User4:1000", result);
    }

    @Test
    void update() {
        val query = "INSERT INTO `_`.`USERS`(`NAME`, `BALANCE`) VALUES (?, ?)";

        val firstResult = messenger.updateWithKeys(query, "User123", 123)
                .call();

        val secondResult = messenger.updateWithKeys(query, "User321", 321)
                .call();

        assertEquals(firstResult + 1, secondResult);
    }
}
