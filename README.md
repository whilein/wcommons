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
            <artifactId>wcommons-eventbus</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-geo</artifactId>
        </dependency>

        <dependency>
            <groupId>io.github.whilein.wcommons</groupId>
            <artifactId>wcommons-util</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Содержание

- ["Шина событий"](eventbus)
- [Изменение байткода в рантайме](asm-patcher)
- [Java агент](agent)
- [ASM](asm)
    - MagicAccessorImpl bridge
- [Утилиты](util)
    - Lazy
    - Pair, MutablePair, UnorderedPair
    - MutableInt, MutableLong, MutableReference, MutableOptionalInt, MutableOptionalLong, MutableOptionalReference
    - Message
    - RandomUtils, RandomStringGenerator, WeightedRandom
    - ClassLoaderUtils
    - Async (Упрощенный вариант Future)
    - Buffering
    - Bytes, ByteSlice
    - Hex
    - Root
- [Конфигурация](config)
- [Геолокация](geo)
