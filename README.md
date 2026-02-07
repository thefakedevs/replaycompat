# Replay Compat

Минимальный шаблон мода для Minecraft Forge 1.20.1 с поддержкой Kotlin.

## Описание

Базовый мод-шаблон, который выводит логи о загрузке. Может использоваться как основа для разработки собственных модов.

## Требования

- Minecraft 1.20.1
- Forge 47.1.0+
- Java 17

## Сборка

```bash
./gradlew build
```

Собранный мод будет находиться в `build/libs/`

## Структура проекта

- `src/main/kotlin/ru/lavafrai/svogame/replaycompat/` - исходный код мода
- `src/main/resources/` - ресурсы мода (конфигурация, локализация)
- `build.gradle.kts` - конфигурация сборки
- `gradle.properties` - свойства проекта

## Лицензия

All Rights Reserved
