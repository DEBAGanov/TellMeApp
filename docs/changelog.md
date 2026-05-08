# Changelog — TellMeApp

Все значительные изменения проекта документируются в этом файле.

---

## [2026-05-08] - Задача 1.4: AudioRecorder
### Добавлено
- `AudioRecorder` — запись через `AudioRecord` (16kHz, 16-bit, mono), источник `VOICE_RECOGNITION`
- Автоматическая запись WAV-заголовка (44 байта) после остановки
- Обработка ошибок инициализации микрофона
- Поток записи в отдельном thread (`AudioRecorder`)

---

## [2026-05-08] - Задача 1.3: ForegroundService
### Добавлено
- `VoiceForegroundService` — фоновый сервис с `START_STICKY`, notification channel (low priority)
- `StopServiceReceiver` — BroadcastReceiver для кнопки "Выключить" в уведомлении
- Сервис и receiver зарегистрированы в AndroidManifest (`foregroundServiceType="specialUse"`)
### Изменено
- `MainViewModel` — инжектирован `ApplicationContext`, подключён старт/стоп сервиса через `toggleService()`
- `MainViewModel` — регистрация `BroadcastReceiver` для синхронизации состояния при остановке из уведомления

---

## [2026-05-08] - Задача 1.2: Главный экран UI
### Добавлено
- `PowerButton` — круглая кнопка с glow-эффектом, три состояния (idle/active/recording)
- `StatusIndicator` — текстовый индикатор с анимацией точек и прогрессом обработки
- `SubscriptionCard` — компактная карточка подписки (статус, дата окончания)
- `MainScreen` — главный экран с тёмным фоном, PowerButton по центру, подписка внизу
- `MainViewModel` — ViewModel с `MainUiState` и toggle логикой
### Изменено
- `MainActivity` — подключён `MainScreen`, убран шаблонный код, `darkTheme = true` по умолчанию

---

## [2026-05-08] - Этап 1: Базовая инфраструктура
### Добавлено
- Package переименован в `com.TellMeUp.tellmeapp`
- Зависимости: Hilt, Compose Navigation, Retrofit, OkHttp (SSE), DataStore, Kotlinx Serialization, KSP
- Application класс `TellMeApp` с `@HiltAndroidApp`
- `MainActivity` аннотирован `@AndroidEntryPoint`
- Структура пакетов: `di/`, `data/`, `domain/`, `service/`, `ui/`, `util/`
- Доменные модели: `VoiceState`, `Subscription`, `Transcription`
- Цветовая палитра в стиле Happ (тёмно-синий + голубые акценты)
- Разрешения в AndroidManifest: RECORD_AUDIO, FOREGROUND_SERVICE, INTERNET, VIBRATE, POST_NOTIFICATIONS
- Файлы ответов на вопросы `docs/qa.md` обновлены

### Изменено
- `libs.versions.toml` — добавлено 10+ новых зависимостей и 3 плагина
- `build.gradle.kts` (root + app) — подключены новые плагины
- `Color.kt` — палитра заменена на Happ-style
- `Theme.kt` — упрощён, убран dynamicColor

---

## [2026-05-07] - Проектирование архитектуры
### Добавлено
- Полная документация проекта в `docs/project.md` (архитектура, стек, экраны, API-интеграция, этапы разработки)
- Трекер задач `docs/tasktracker.md` — 12 задач в 5 этапах
- Технический дневник `docs/diary.md` — первое наблюдение о проектировании
- Файл вопросов `docs/qa.md` — 14 открытых вопросов (3 критических)
- Файл правил `Claude.md` — стандарты и процесс разработки
- Диаграммы архитектуры (Mermaid): компоненты системы, поток голосового ввода

---

## [2026-05-07] - Инициализация проекта
### Добавлено
- Создан проект Android Studio (Kotlin + Jetpack Compose + Material 3)
- Инициализирован git-репозиторий
- Создана структура документации (`docs/project.md`, `docs/changelog.md`, `docs/tasktracker.md`)
