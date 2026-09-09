# GradleRun

GradleRun — Android-приложение, которое превращает выбранную папку в базовый Android Gradle-проект.

## Что умеет

- выбрать папку через системный Android picker;
- указать название проекта;
- указать `applicationId` / package name;
- создать `settings.gradle.kts`, root `build.gradle.kts` и `gradle.properties`;
- создать модуль `app` с Android Manifest, ресурсами и `MainActivity.kt`;
- создать `.gitignore`;
- автоматически подготовить проект к открытию в Android Studio или другом Gradle-совместимом IDE;
- проверять сборку через GitHub Actions.

## Использование

1. Установи и запусти GradleRun.
2. Введи название проекта и package name.
3. Нажми **Выбрать папку**.
4. Выбери папку, в которую можно записывать файлы.
5. Нажми **Создать проект**.
6. Открой выбранную папку как Gradle-проект.

> В текущей версии GradleRun создаёт проект, но сам Gradle/Android SDK внутри APK не поставляется. Сборка выполняется внешним Gradle/Android Studio или CI.
