CREATE TABLE `USERS`(
    `ID` INT AUTO_INCREMENT PRIMARY KEY,
    `NAME` VARCHAR(16) NOT NULL,
    `BALANCE` INT NOT NULL
);

INSERT INTO `USERS`(`NAME`, `BALANCE`) VALUES
    ('User1', 0),
    ('User2', 50),
    ('User3', 100),
    ('User4', 500),
    ('User5', 1000);

CREATE TABLE `USER_TAGS`(
    `ID` INT AUTO_INCREMENT,
    `USER_ID` INT NOT NULL,
    `TAG` VARCHAR(255) NOT NULL,
    FOREIGN KEY (`USER_ID`) REFERENCES `USERS`(`ID`)
);

INSERT INTO `USER_TAGS`(`USER_ID`, `TAG`) VALUES
    (1, 'Первый'),
    (1, 'Гей'),
    (2, 'Второй'),
    (2, 'Пидор'),
    (3, 'Третий'),
    (3, 'Долбоёб'),
    (4, 'Четвертый'),
    (4, 'Чмо'),
    (5, 'Пятый'),
    (5, 'Камбет');