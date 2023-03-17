# wcommons

<div align="center">
  <a href="https://github.com/whilein/wcommons/blob/master/LICENSE">
    <img src="https://img.shields.io/github/license/whilein/wcommons">
  </a>

  <a href="https://discord.gg/ANEHruraCc">
    <img src="https://img.shields.io/discord/819859288049844224?logo=discord">
  </a>

  <a href="https://github.com/whilein/wcommons/issues">
    <img src="https://img.shields.io/github/issues/whilein/wcommons">
  </a>

  <a href="https://github.com/whilein/wcommons/pulls">
    <img src="https://img.shields.io/github/issues-pr/whilein/wcommons">
  </a>

  <a href="https://central.sonatype.com/artifact/io.github.whilein.wcommons/wcommons-bom/1.0.3/versions">
    <img src="https://img.shields.io/maven-central/v/io.github.whilein.wcommons/wcommons-bom">
  </a>
</div>

## Использование

```xml

<project>
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.github.whilein.wcommons</groupId>
                <artifactId>wcommons-bom</artifactId>
                <version>${wcommons.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-agent</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-asm</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-asm-patcher</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-util</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-config</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-crypto</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-eventbus</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo-api</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo-maxmind-impl</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo-ipinfo-impl</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo-cache-caffeine</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo-cache-guava</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-impl-loader</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-flow</artifactId>
        </dependency>
      
        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-util</artifactId>
        </dependency>
      
        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-sql</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Содержание

- [Работа с базой данных](sql)
- ["Шина событий"](eventbus)
- [Изменение байткода в рантайме](asm-patcher)
- [Java агент](agent)
- [Flow](flow)
- [Загрузка реализаций](impl-loader)
- [ASM](asm)
    - MagicAccessorImpl bridge
- [Утилиты](util)
    - Lazy
    - Pair, MutablePair, UnorderedPair
    - MutableInt, MutableLong, MutableReference, MutableOptionalInt, MutableOptionalLong, MutableOptionalReference
    - Message
    - RandomUtils & RandomStringGenerator
    - ClassLoaderUtils
    - Async (Упрощенный вариант Future)
    - Buffering
    - Bytes, ByteSlice
    - Hex
    - Root
- [Конфигурация](config)
- Геолокация
    - [Апи](geo-api)
    - [Реализация maxmind](geo-maxmind-impl)
    - [Реализация ipinfo](geo-ipinfo-impl)
    - [Кэширование на основе Google Guava](geo-cache-guava)
    - [Кэширование на основе Caffeine](geo-cache-caffeine)
