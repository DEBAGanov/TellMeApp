# Changelog — TellMeApp

Все значительные изменения проекта документируются в этом файле.

---

## [2026-05-13] - Этап 10: Промпты для AI провайдеров
### Добавлено
- `AiProviderScreen` — экран настроек AI провайдера с полем ввода промпта
- `AiProviderViewModel` — ViewModel для управления промптом провайдера
- `PreferencesStore`: добавлены `zaiPrompt`, `claudePrompt` и соответствующие save-методы
- Навигация: маршрут `ai_provider/{providerKey}` для перехода к экрану провайдера
### Изменено
- `MainScreen` — кнопки провайдеров теперь навигируют на экран настроек провайдера
- `AppNavigation` — добавлен маршрут к AiProviderScreen, bottom bar скрывается на экране провайдера
- `VoiceForegroundService` — при AI обработке читает промпт провайдера из DataStore, склеивает с распознанным текстом (`"промпт\n\nтекст"`)
- `tasktracker.md` — добавлен Этап 10

---

## [2026-05-12] - Этап 9: Поддержка нескольких AI провайдеров (z.ai + Claude)
### Добавлено
- `ClaudeMessageDto` — DTO для Claude Messages API (request/response с content blocks)
- `ClaudeApi` — HTTP клиент для Claude (`POST /v1/messages`, `x-api-key` + `anthropic-version`, модель `claude-sonnet-4-20250514`)
- `AiProvider` enum — выбор между `ZAI` и `CLAUDE`
- `ClaudeRepository` / `ClaudeRepositoryImpl` — репозиторий для Claude API
- `SendClaudeMessageUseCase` — domain use case для Claude
- `PreferencesStore`: добавлены `aiProvider` и `claudeApiKey`
- `RepositoryModule`: привязка `ClaudeRepository`
### Изменено
- `SettingsScreen` — добавлено поле ввода API ключа Claude
- `SettingsViewModel` — управление Claude API ключом (inject `ClaudeRepository`)
- `MainScreen` — добавлен селектор провайдера (z.ai / Claude), описание AI обновляется динамически
- `MainViewModel` — добавлен `aiProvider` в state, метод `selectProvider()` с сохранением в DataStore
- `VoiceForegroundService` — маршрутизация AI запросов: `processWithZai()` / `processWithClaude()` на основе `aiProvider`
- `tasktracker.md` — добавлен Этап 9

---

## [2026-05-11] - Этап 8: AI ассистент (z.ai)
### Добавлено
- `ChatCompletionDto` — DTO для z.ai Chat Completions API (request/response)
- `AiChatApi` — HTTP клиент для z.ai (`POST /chat/completions`, Bearer auth, модель `glm-5.1`)
- `AiChatRepository` / `AiChatRepositoryImpl` — репозиторий для AI чата
- `SendAiMessageUseCase` — domain use case
- `VoiceState.AI_PROCESSING` — новое состояние для AI обработки
- `PreferencesStore`: добавлены `aiEnabled` и `aiApiKey`
### Изменено
- `SettingsScreen` — добавлено поле ввода API ключа z.ai
- `SettingsViewModel` — управление AI API ключом
- `MainScreen` — добавлен переключатель AI ассистент
- `MainViewModel` — добавлен `isAiModeEnabled`, метод `toggleAiMode()`
- `VoiceForegroundService` — AI обработка (processWithAi), вставка ответа AI
- `StatusIndicator`, `AssistantOverlay`, `AssistantViewModel` — поддержка `AI_PROCESSING`
### Исправлено
- URL z.ai изменён с `/api/paas/v4` на `/api/coding/paas/v4` (GLM Coding Plan)

---

## [2026-05-09] - Этап 6: Digital Assistant + Этап 7: Исправления
### Добавлено
- `AssistantActivity` — точка входа через ACTION_ASSIST/ACTION_VOICE_ASSIST
- `AssistantOverlay` — прозрачный Compose-оверлей с автозаписью
- `AssistantViewModel` — делегирует VoiceForegroundService
- Прозрачная тема `Theme.TellMeApp.Assistant` в `themes.xml`
- `AppLogger` — ring-buffer логгер с вкладкой «Логи» в приложении
- Кнопка «Copy» на экране логов
### Изменено
- `VoiceForegroundService` — перенос записи из ViewModel в сервис, MediaSession для Volume Up/Down
- `MainViewModel` — делегирует запись сервису
- `VoiceAccessibilityService` — вставка текста в позицию курсора (textSelectionStart/End)
- `accessibility_service_config.xml` — обновлены флаги и типы событий
### Исправлено
- AquaVoice API URL: `/v1` → `/api/v1`, модель: `avalon-1` → `avalon-v1.5`
- SSL bypass в OkHttpClient для DNS-перехвата провайдера

---

## [2026-05-08] - Этап 5: Полировка и релиз
### Добавлено
- `SettingsScreen` — экран настроек: API-ключ AquaVoice, виброотклик, визуальное уведомление, тёмная тема
- `SettingsViewModel` — управление настройками через PreferencesStore + SpeechRepository
- `AppNavigation` — Bottom Navigation Bar с 3 табами (Голос / Подписка / Настройки)
- Compose Navigation с NavHost, saveState/restoreState
### Изменено
- `MainActivity` — использует `AppNavigation()` вместо `MainScreen()` напрямую
### Исправлено
- Замена `material-icons-extended` (OOM) на базовые Material иконки (`Home`, `Star`, `Settings`)

---

## [2026-05-08] - Этап 4: Подписка и статистика
### Добавлено
- `PreferencesStore` — DataStore хранилище (activation_link, subscription_status, expiry_date, tariff_type, api_key)
- `SubscriptionRepository` / `SubscriptionRepositoryImpl` — управление подпиской с mock-активацией (30 дней Pro)
- `ActivateSubscriptionUseCase`, `GetSubscriptionStatusUseCase` — domain use cases
- `SubscriptionViewModel` — управление состоянием экрана подписки
- `SubscriptionScreen` — экран с полем ввода ссылки, кнопкой активации, карточкой статуса и детальной информацией (тариф, срок, дни)
- `RepositoryModule` обновлён — добавлена привязка SubscriptionRepository

---

## [2026-05-08] - Этап 2: Голосовое распознавание + Этап 3: Вставка текста
### Добавлено
- `AquaVoiceApi` — HTTP клиент (OkHttp multipart) с batch и SSE streaming, язык=ru, модель=avalon-1
- `TranscriptionResponse`, `ApiError` — DTO для ответов API
- `NetworkModule` — Hilt модуль (OkHttpClient с logging, Json)
- `RepositoryModule` — Hilt привязка SpeechRepository → SpeechRepositoryImpl
- `SpeechRepository` / `SpeechRepositoryImpl` — распознавание с обработкой ошибок и кодами
- `RecognizeSpeechUseCase` — domain use case
- `VolumeButtonDetector` — детектор двойного нажатия (300мс окно)
- `VoiceAccessibilityService` — перехват Volume Up + вставка текста (ACTION_SET_TEXT / clipboard fallback)
- XML конфигурация AccessibilityService (`accessibility_service_config.xml`)
- Полный цикл в `MainViewModel`: VoiceTrigger → Recording → API → InsertText
- Виброотклик при старте/стопе записи
- Toast с кодом ошибки при неудачном распознавании
### Изменено
- `MainViewModel` — инжектирован `RecognizeSpeechUseCase`, добавлены voiceTriggerReceiver, управление AudioRecorder
- `AndroidManifest` — добавлен VoiceAccessibilityService с BIND_ACCESSIBILITY_SERVICE
- `strings.xml` — добавлено описание AccessibilityService

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
