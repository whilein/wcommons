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

  <a href="https://search.maven.org/artifact/io.github.whilein.wcommons/wcommons">
    <img src="https://img.shields.io/maven-central/v/io.github.whilein.wcommons/wcommons">
  </a>
</div>

## Содержание

- [База данных](sql)
- ["Шина событий"](eventbus)
- [Изменение байткода в рантайме](asm-patcher)
- [Java агент](agent)
- [Flow](flow)
- [Загрузка реализаций](impl-loader)
- [ASM](asm)
  - MagicAccessorImpl bridge
- [Утилиты](util)
  - Lazy (Simple, Concurrent & ThreadLocal)
  - Pair, MutablePair
  - MutableInt, MutableLong, MutableReference, MutableOptionalInt, MutableOptionalLong, MutableOptionalReference
  - Message
  - RandomUtils & RandomStringGenerator
  - ClassLoaderUtils
  - Async (Упрощенный вариант Future)
  - Buffering
  - Bytes, ByteSlice
  - Hash, Hex
  - Root
- [Конфигурация](config)
  - SimpleFileConfig
  - YamlConfigProvider
  - JsonConfigProvider
- Геолокация
  - [Апи](geo-api)
  - [Реализация maxmind](geo-maxmind-impl)
  - [Реализация ipinfo](geo-ipinfo-impl)
  - [Кэширование на основе Google Guava](geo-cache-guava)
  - [Кэширование на основе Caffeine](geo-cache-caffeine)
