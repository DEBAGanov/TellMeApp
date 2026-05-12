# Task Tracker — TellMeApp

---

## Этап 1: Базовая инфраструктура (MVP)

### Задача 1.1: Настройка архитектуры проекта
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Подключение Hilt, Compose Navigation, настройка структуры пакетов, добавление зависимостей
- **Шаги выполнения**:
  - [x] Добавить Hilt dependency и плагин в build.gradle.kts
  - [x] Добавить Compose Navigation
  - [x] Добавить OkHttp + Retrofit
  - [x] Добавить DataStore Preferences
  - [x] Добавить Kotlinx Serialization
  - [x] Создать структуру пакетов (di, data, domain, service, ui, util)
  - [x] Настроить Application класс с @HiltAndroidApp
- **Зависимости**: Нет

### Задача 1.2: Главный экран (UI)
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Создание главного экрана в стиле Happ — тёмный фон, центральная круглая кнопка статуса, текст статуса
- **Шаги выполнения**:
  - [x] Определить цветовую палитру (тёмная тема по умолчанию)
  - [x] Создать компонент PowerButton (круглая кнопка с glow-эффектом)
  - [x] Создать StatusIndicator (текстовый индикатор статуса)
  - [x] Реализовать MainScreen с компоновкой
  - [x] Реализовать MainViewModel с состоянием VoiceState
- **Зависимости**: Задача 1.1

### Задача 1.3: ForegroundService с уведомлением
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Создание фонового сервиса, который держит приложение активным и показывает постоянное уведомление
- **Шаги выполнения**:
  - [x] Создать VoiceForegroundService
  - [x] Реализовать создание notification channel
  - [x] Реализовать старт/стоп сервиса из UI
  - [x] Добавить разрешение FOREGROUND_SERVICE в манифест
  - [x] Добавить кнопку выключения в уведомление
- **Зависимости**: Задача 1.1

### Задача 1.4: AudioRecorder
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Модуль записи аудио через AudioRecord для последующей отправки в API
- **Шаги выполнения**:
  - [x] Создать AudioRecorder с настройками (16kHz, 16bit, mono)
  - [x] Реализовать start() / stop() / getAudioFile()
  - [x] Сохранение в WAV-формат
  - [x] Добавить разрешение RECORD_AUDIO
  - [x] Обработка ошибок (микрофон занят и т.д.)
- **Зависимости**: Задача 1.1

---

## Этап 2: Голосовое распознавание

### Задача 2.1: Интеграция AquaVoice API
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Настройка OkHttp для работы с AquaVoice API (OpenAI Whisper-compatible, SSE streaming)
- **Шаги выполнения**:
  - [x] Создать AquaVoiceApi клиент (OkHttp + multipart)
  - [x] Создать DTO: TranscriptionResponse, StreamChunk, ApiError
  - [x] Создать NetworkModule (Hilt) с OkHttpClient
  - [x] Реализовать SpeechRepository и SpeechRepositoryImpl
  - [x] Создать RecognizeSpeechUseCase
  - [x] SSE streaming метод (callbackFlow)
- **Зависимости**: Задача 1.1

### Задача 2.2: Детектор двойного нажатия Volume Up
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Перехват двойного нажатия кнопки увеличения громкости через AccessibilityService
- **Шаги выполнения**:
  - [x] Создать VolumeButtonDetector с логикой двойного нажатия (300мс таймаут)
  - [x] Создать VoiceAccessibilityService с FLAG_REQUEST_FILTER_KEY_EVENTS
  - [x] Интегрировать с ForegroundService (broadcast триггер)
  - [x] Зарегистрировать сервис в манифесте + XML конфигурация
- **Зависимости**: Задача 1.3

### Задача 2.3: Полный цикл запись → распознавание → текст
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Связка AudioRecorder + AquaVoice API + AccessibilityService через ViewModel
- **Шаги выполнения**:
  - [x] VoiceState (IDLE, RECORDING, PROCESSING) в MainViewModel
  - [x] Связать VolumeButton → AudioRecorder → API → insertText
  - [x] Виброотклик при старте/стопе записи
  - [x] Toast с кодом ошибки при неудаче
  - [x] Обработка ошибок (нет сети, API error, пустая запись)
- **Зависимости**: Задача 2.1, Задача 2.2

---

## Этап 3: Вставка текста

### Задача 3.1: AccessibilityService для вставки текста
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Создание AccessibilityService, который вставляет распознанный текст в активное поле ввода
- **Шаги выполнения**:
  - [x] Создать VoiceAccessibilityService
  - [x] Реализовать поиск фокусированного editable узла
  - [x] Реализовать ACTION_SET_TEXT для вставки
  - [x] Fallback: clipboard + ACTION_PASTE
  - [x] Зарегистрировать сервис в манифесте
- **Зависимости**: Задача 2.3

---

## Этап 4: Подписка и статистика

### Задача 4.1: Экран подписки
- **Приоритет**: Средний
- **Статус**: Завершена
- **Описание**: Экран активации подписки по ссылке и отображение информации
- **Шаги выполнения**:
  - [x] Создать SubscriptionScreen (поле ввода ссылки, кнопка активации)
  - [x] Создать SubscriptionCard (статус, срок, тариф)
  - [x] Реализовать SubscriptionViewModel
  - [x] Mock-данные для демонстрации (API будет позже)
- **Зависимости**: Задача 1.2

### Задача 4.2: Локальное хранение данных подписки
- **Приоритет**: Средний
- **Статус**: Завершена
- **Описание**: Сохранение данных подписки и статистики в DataStore
- **Шаги выполнения**:
  - [x] Создать PreferencesStore (DataStore)
  - [x] Хранение: ключ активации, срок действия, тариф, API-ключ
  - [x] Реализовать SubscriptionRepositoryImpl
  - [x] Создать ActivateSubscriptionUseCase, GetSubscriptionStatusUseCase
- **Зависимости**: Задача 1.1

---

## Этап 5: Полировка и релиз

### Задача 5.1: Экран настроек
- **Приоритет**: Средний
- **Статус**: Завершена
- **Описание**: Экран настроек приложения
- **Шаги выполнения**:
  - [x] Поле ввода API-ключа (тестовый режим)
  - [x] Переключатель виброотклика
  - [x] Переключатель визуального уведомления
  - [x] Выбор темы (тёмная/светлая)
- **Зависимости**: Задача 1.2

### Задача 5.2: Навигация и финальная сборка
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Настройка навигации между экранами и финальная сборка
- **Шаги выполнения**:
  - [x] Настроить Compose Navigation (Main → Subscription → Settings)
  - [x] Bottom Navigation с 3 табами (Голос, Подписка, Настройки)
  - [x] Edge-to-edge отображение
  - [x] Финальная сборка BUILD SUCCESSFUL
- **Зависимости**: Все предыдущие задачи

---

## Этап 6: Digital Assistant (кнопка питания)

### Задача 6.1: Регистрация как цифровой ассистент
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Регистрация TellMeApp как цифрового ассистента для запуска по долгому нажатию кнопки питания
- **Шаги выполнения**:
  - [x] Добавить прозрачную тему Theme.TellMeApp.Assistant в themes.xml
  - [x] Зарегистрировать AssistantActivity в манифесте с ACTION_ASSIST / ACTION_VOICE_ASSIST
  - [x] Создать AssistantViewModel (делегирует VoiceForegroundService)
  - [x] Создать AssistantOverlay (прозрачный Compose-оверлей с автостартом)
  - [x] Создать AssistantActivity (точка входа, старт ForegroundService)
  - [x] BUILD SUCCESSFUL
- **Зависимости**: Этапы 1-5

### Задача 6.2: Фоновая запись через физические кнопки
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: Перенос записи из ViewModel в VoiceForegroundService для работы в фоне (Telegram и др. приложения)
- **Шаги выполнения**:
  - [x] Рефакторинг VoiceForegroundService — запись + распознавание + вставка текста
  - [x] Добавлен AudioRecorder, RecognizeSpeechUseCase, SpeechRepository в сервис
  - [x] Broadcast receiver в сервисе для команд от AccessibilityService
  - [x] MainViewModel делегирует запись сервису (убрана дублирующая логика)
  - [x] AssistantViewModel делегирует запись сервису
  - [x] Поддержка Volume Down в дополнение к Volume Up
  - [x] BUILD SUCCESSFUL
- **Зависимости**: Задача 6.1

---

## Резюме прогресса

| Этап | Задач | Завершено | В процессе | Не начато |
|------|-------|-----------|------------|-----------|
| 1. Базовая инфраструктура | 4 | 4 | 0 | 0 |
| 2. Голосовое распознавание | 3 | 3 | 0 | 0 |
| 3. Вставка текста | 1 | 1 | 0 | 0 |
| 4. Подписка и статистика | 2 | 2 | 0 | 0 |
| 5. Полировка и релиз | 2 | 2 | 0 | 0 |
| 6. Digital Assistant | 2 | 2 | 0 | 0 |
| 7. Исправление триггера | 2 | 2 | 0 | 0 |
| 8. AI ассистент (z.ai) | 1 | 1 | 0 | 0 |
| 9. AI провайдеры (z.ai + Claude) | 1 | 1 | 0 | 0 |
| **Итого** | **18** | **18** | **0** | **0** |

---

## Этап 7: Исправление триггера Volume Up

### Задача 7.1: Не работают физические кнопки громкости
- **Приоритет**: Критический
- **Статус**: Завершена (переключено на кнопку питания)
- **Описание**: Нажатие Volume Up увеличивает громкость вместо старта записи. AccessibilityService не перехватывает key events на устройстве.
- **Шаги выполнения**:
  - [x] Диагностика: громкость меняется → `onKeyEvent` не вызывается
  - [x] Замена implicit broadcast на прямые вызовы через singleton
  - [x] Добавлено логирование в VoiceAccessibilityService и VoiceForegroundService
  - [x] Вкладка «Логи» с AppLogger для отладки на устройстве
  - [x] MediaSession как резервный механизм перехвата Volume Up/Down
  - [x] Диагностика: логирование ВСЕХ onKeyEvent (не только volume)
  - [x] Переключено на Digital Assistant (кнопка питания) — работает
- **Зависимости**: Этапы 1-6

### Задача 7.2: API распознавания возвращает success=false
- **Приоритет**: Критический
- **Статус**: Завершена
- **Описание**: AquaVoice API вызывается, но распознавание возвращает пустой текст. Исправлен URL на api.aquavoice.com/api/v1, модель на avalon-v1.5, добавлен SSL bypass.
- **Шаги выполнения**:
  - [x] Добавлено логирование HTTP-статуса, тела ответа и ошибки в AquaVoiceApi
  - [x] Добавлено логирование errorCode и errorMessage в VoiceForegroundService
  - [x] Добавлено поле errorMessage в Transcription
  - [x] Загрузка API-ключа из DataStore при старте сервиса
  - [x] Исправлен BASE_URL на api.aquavoice.com/api/v1 (был /v1 без /api)
  - [x] Исправлена модель на avalon-v1.5 (была avalon-1)
  - [x] SSL bypass в OkHttpClient для DNS-перехвата провайдера
  - [x] Распознавание работает (HTTP 200, текст возвращается)
- **Зависимости**: Задача 7.1

---

## Этап 8: AI ассистент (z.ai)

### Задача 8.1: Интеграция z.ai Chat Completions API
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Добавлена возможность обработки распознанного текста через AI (z.ai) с вставкой ответа AI вместо оригинального текста.
- **Шаги выполнения**:
  - [x] Создан ChatCompletionDto (request/response DTO)
  - [x] Создан AiChatApi (OkHttp, JSON POST, Bearer auth, модель glm-5.1)
  - [x] Создан AiChatRepository + AiChatRepositoryImpl
  - [x] Создан SendAiMessageUseCase
  - [x] DI привязка в RepositoryModule
  - [x] PreferencesStore: aiEnabled, aiApiKey (DataStore)
  - [x] SettingsScreen: поле ввода API ключа z.ai
  - [x] SettingsViewModel: управление AI API ключом
  - [x] MainScreen: переключатель AI ассистент
  - [x] MainViewModel: toggleAiMode с сохранением в DataStore
  - [x] VoiceForegroundService: AI обработка (processWithAi)
  - [x] VoiceState: добавлен AI_PROCESSING
  - [x] AssistantOverlay: отображение AI обработки
- **Зависимости**: Этапы 1-7

---

## Этап 9: Поддержка нескольких AI провайдеров (z.ai + Claude)

### Задача 9.1: Интеграция Claude Messages API
- **Приоритет**: Высокий
- **Статус**: Завершена
- **Описание**: Добавлена поддержка Claude API как второго AI провайдера с выбором между z.ai и Claude на главном экране.
- **Шаги выполнения**:
  - [x] Создан ClaudeMessageDto (request/response DTO для Claude Messages API)
  - [x] Создан ClaudeApi (OkHttp, x-api-key + anthropic-version headers, модель claude-sonnet-4-20250514)
  - [x] Создан AiProvider enum (ZAI / CLAUDE)
  - [x] Создан ClaudeRepository + ClaudeRepositoryImpl
  - [x] Создан SendClaudeMessageUseCase
  - [x] DI привязка ClaudeRepository в RepositoryModule
  - [x] PreferencesStore: aiProvider, claudeApiKey (DataStore)
  - [x] SettingsScreen: поле ввода API ключа Claude
  - [x] SettingsViewModel: управление Claude API ключом
  - [x] MainScreen: селектор провайдера (z.ai / Claude) при включённом AI
  - [x] MainViewModel: selectProvider с сохранением в DataStore
  - [x] VoiceForegroundService: маршрутизация к провайдеру (processWithZai / processWithClaude)
- **Зависимости**: Этап 8
