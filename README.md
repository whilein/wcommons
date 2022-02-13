Актуальная версия:
**0.1.47**

В данном проекте находится:

- [База данных](sql)
- ["Шина событий"](eventbus)
- [Индексирование аннотаций](annotation-index)
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
