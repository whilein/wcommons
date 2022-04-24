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

package w.sql;

import com.zaxxer.hikari.HikariConfig;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import w.flow.FlowCollectors;
import w.sql.dao.DefaultTableManager;
import w.sql.dao.foreignkey.ForeignKey;
import w.sql.dao.foreignkey.ForeignKeyOption;

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
    void testTableManager() {
        val ranksTable = DefaultTableManager.builder(messenger, RankColumn.class, "RANKS")
                .build();

        val rankId = ranksTable.insert(properties -> properties.set(RankColumn.NAME, "Гей"))
                .call();

        val table = DefaultTableManager.builder(messenger, UserColumn.class, "USERS")
                .foreignKey(ForeignKey.builder(UserColumn.RANK_ID, "RANKS", "ID")
                        .onDelete(ForeignKeyOption.CASCADE)
                        .onUpdate(ForeignKeyOption.NO_ACTION)
                        .build())
                .build();

        val userId = table.insert(properties -> properties
                        .set(UserColumn.NAME, "Камбет")
                        .set(UserColumn.RANK_ID, rankId))
                .call();

        val userRank = table.<Integer>get(userId, UserColumn.RANK_ID)
                .call();

        assertEquals(rankId, userRank.intValue());

        val userData = table.getAll(userId)
                .call();

        assertEquals("[NAME = Камбет, RANK_ID = 1]", userData.toString());

        val byRank = table.<String>getBy(UserColumn.RANK_ID, rankId, UserColumn.NAME)
                .findFirst()
                .call();

        assertEquals("Камбет", byRank);
    }

    @Test
    void collect() {
        val selectUsers = "SELECT `ID`, `NAME` FROM `_`.`USERS`";

        val result = messenger.fetch(selectUsers)
                .collect(FlowCollectors.toUnmodifiableMap(
                        rs -> rs.getInt("ID"),
                        rs -> rs.getString("NAME")
                ))
                .call();

        assertEquals("{1=User1, 2=User2, 3=User3, 4=User4, 5=User5}", result.toString());
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

        Assertions.assertEquals(firstResult + 1, secondResult);
    }
}
