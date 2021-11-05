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
import w.commons.flow.FlowCollectors;

import java.util.Random;

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
        config.setMaximumPoolSize(8);
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
    void flatMap() {
        val selectUsers = "SELECT `ID`, `NAME` FROM `_`.`USERS`";
        val selectTagByUserId = "SELECT `TAG` FROM `_`.`USER_TAGS` WHERE `USER_ID`=?";

        val result = messenger.fetch(selectUsers)
                .flatMap(user -> messenger.fetch(selectTagByUserId, user.getInt("ID"))
                        .map(tag -> tag.getString("TAG")))
                .collect(FlowCollectors.joining(", "))
                .call();

        assertEquals("Первый, Гей, Второй, Пидор, Третий, Долбоёб, Четвертый, Чмо, Пятый, Камбет", result);
    }

    @Test
    void fetch() {
        val result = messenger.fetch("SELECT * FROM `_`.`USERS`")
                .map(rs -> rs.getString("NAME") + ':' + rs.getString("BALANCE"))
                .collect(FlowCollectors.joining(";"))
                .call();

        assertEquals("User1:0;User2:50;User3:100;User4:500;User5:1000", result);
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
