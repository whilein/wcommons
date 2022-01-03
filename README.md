Здесь собран весь код вайла, который работает вне зависимости от самого кода ласта

Актуальная версия:
**0.1.28**

В данном проекте находится:

- [База данных](sql)
- ["Шина событий"](eventbus)
- [Индексирование аннотаций](annotation-index)
- [Изменение байткода в рантайме](asm-patcher)
- [Flow](flow)
- [Загрузка реализаций](impl-loader)
- [ASM](asm)
  - MagicAccessorImpl bridge
- [Утилиты](util)
  - Lazy (Simple, Concurrent & ThreadLocal)
  - Pair, MutPair
  - Message
  - RandomUtils & RandomStringGenerator
  - ClassLoaderUtils
  - Root
- [Конфигурация](config)
  - SimpleFileConfig
  - YamlConfigParser
    - JsonConfigParser
- Геолокация
  - [Апи](geo-api)
  - [Реализация maxmind](geo-maxmind-impl)
  - [Реализация ipinfo](geo-ipinfo-impl)
  - [Кэширование на основе Google Guava](geo-cache-guava)
  - [Кэширование на основе Caffeine](geo-cache-caffeine)