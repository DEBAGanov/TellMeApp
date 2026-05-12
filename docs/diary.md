# Дневник наблюдений проекта — TellMeApp

---

## [2026-05-12] — Добавление Claude API как второго AI провайдера

### Наблюдения

- Пользователь хочет выбор между z.ai (GLM Coding Plan) и Claude (Anthropic) для AI обработки распознанного текста
- Claude Messages API использует другой формат аутентификации (`x-api-key` + `anthropic-version`) и другую структуру ответа (content blocks с type/text)
- z.ai API совместим с OpenAI Chat Completions, Claude — нет. Нужны отдельные API клиенты
- Выбор провайдера сохраняется в DataStore и используется VoiceForegroundService при обработке

### Решения

- **AiProvider enum** — `ZAI` / `CLAUDE` с `key` для DataStore и `displayName` для UI
- **Отдельные DTO** — `ChatCompletionDto` для z.ai, `ClaudeMessageDto` для Claude (разные форматы запросов/ответов)
- **Отдельные API клиенты** — `AiChatApi` (z.ai) и `ClaudeApi` (Claude) с разными заголовками и URL
- **Маршрутизация в сервисе** — `processWithAi()` читает `aiProvider` из DataStore, вызывает `processWithZai()` или `processWithClaude()`
- **UI** — селектор провайдера виден только при включённом AI, две кнопки (z.ai / Claude)

### Проблемы

- Документация Claude API не загрузилась через webReader (JS-рендеринг) — использовано знание формата API

---

## [2026-05-11] — Интеграция AI (z.ai)

### Наблюдения

- z.ai API возвращает HTTP 429 "Insufficient balance" при использовании `/api/paas/v4` — нужен `/api/coding/paas/v4` для GLM Coding Plan подписки
- System prompt: "Respond concisely in the same language the user writes" — важно для русскоязычных пользователей
- Non-streaming подход проще и достаточен для вставки текста (не нужен потоковый вывод)

### Решения

- URL: `https://api.z.ai/api/coding/paas/v4/chat/completions` (Coding Plan)
- Модель: `glm-5.1`
- AI обработка опциональна — toggle на главном экране
- При ошибке AI — fallback на оригинальный распознанный текст

---

## [2026-05-09] — Digital Assistant и фоновая запись

### Наблюдения

- AccessibilityService не перехватывает key events на Xiaomi/MIUI — `onKeyEvent` не вызывается
- MediaSession частично работает для Volume Up/Down, но нестабильно на Xiaomi
- Digital Assistant (кнопка питания) — самый надёжный триггер, работает на всех устройствах
- AccessibilityService на Xiaomi показывает "срабатывает некорректно" даже после включения — возможно MIUI-специфичные ограничения

### Решения

- Основной триггер: Digital Assistant через `ACTION_ASSIST` / `ACTION_VOICE_ASSIST`
- Резервный: MediaSession для Volume Up/Down (долгое нажатие 400мс)
- Запись перенесена из ViewModel в VoiceForegroundService для работы в фоне
- Виброотклик при старте (зажатие) и стопе (отпускание) кнопки

### Проблемы

- AccessibilityService для вставки текста нестабилен на Xiaomi — fallback на clipboard
- SSL-перехват провайдера требует bypass в OkHttpClient

---

## [2026-05-07] — Проектирование архитектуры

### Наблюдения

- Проект представляет собой фоновое приложение для голосового ввода текста в любые поля ввода на устройстве
- AquaVoice API (Avalon) полностью совместим с OpenAI Whisper API — это упрощает интеграцию
- Приложение Happ использует минималистичный дизайн с центральной кнопкой — хороший ориентир для UI
- Основные технические сложности: (1) глобальный перехват нажатий, (2) вставка текста, (3) стабильная фоновая работа

### Решения

- **Архитектура**: MVVM + Clean Architecture
- **Перехват кнопки**: Digital Assistant (кнопка питания) как основной, MediaSession как резервный
- **Фоновая работа**: ForegroundService с persistent notification
- **API-клиент**: OkHttp для HTTP-запросов
- **Хранение**: DataStore Preferences
- **DI**: Hilt

### Проблемы

- API подписок ещё не готов — mock-реализации
- На Xiaomi AccessibilityService может быть агрессивно убит системой
