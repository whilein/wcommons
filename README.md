Здесь собран весь код вайла, который работает вне зависимости от самого кода ласта

Актуальная версия:
**0.1.27**

В данном проекте находится:

- [База данных](sql)
- [Загрузка реализаций](impl)
- [Индексирование аннотаций](annotation-index)
- [Изменение байткода в рантайме](asm-patcher)
- [Flow](flow)
- [Утилиты](util)
    - Lazy (Simple, Concurrent & ThreadLocal)
    - Pair, MutPair
    - Message
    - RandomUtils & RandomStringGenerator
    - ClassLoaderUtils
    - UnsafeInternals
    - FullAccessLookup
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