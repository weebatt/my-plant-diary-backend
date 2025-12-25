# Frontend (Vue 3) — План реализации и архитектура

Документ описывает, как собрать фронтенд для My Plant Diary на Vue 3. Цели: быстрое MVP, согласованность с уже реализованным бэкендом, удобная разработка и масштабируемость.

## Стек и принципы
- Фреймворк: Vue 3 (Composition API) + TypeScript
- Сборка: Vite
- Маршрутизация: Vue Router 4
- Состояние: Pinia
- HTTP‑клиент: Axios (инстанс со встроенным baseURL и JWT)
- UI: Element Plus (формы/таблицы/диалоги) + иконки (unplugin‑icons / iconify)
- Стили: SCSS или Tailwind CSS (по вкусу; ниже — с Element Plus, без Tailwind)
- Линтинг/форматтер: ESLint + Prettier
- Тесты: Vitest + Vue Test Utils (юнит); Playwright (e2e) — опционально

Переменные окружения (Vite):
- `VITE_API_BASE_URL` — базовый URL бэкенда (например, `http://localhost:8080`)

## Архитектура приложения
- Pages (views):
  - Публичные
    - PlantsListView — `/plants` (поиск, пагинация, сортировка) → GET `/plants`
    - PlantDetailView — `/plants/:id` → GET `/plants/{id}`
    - RecommendationsView — `/plants/recommendations` → GET `/plants/recommendations`
  - Аутентификация
    - RegisterView — `/auth/register` → POST `/auth/register`
    - LoginView — `/auth/login` → POST `/auth/login` (получение JWT)
  - Профиль (JWT)
    - ProfileView — `/profile` → GET/PATCH `/profile`; смена пароля → POST `/profile/password`
  - Админ (JWT, роль ADMIN)
    - AdminPlantsListView — `/admin/plants` → GET `/admin/plants`
    - AdminPlantEditView — `/admin/plants/:id` → GET/PATCH/DELETE `/admin/plants/{id}`
    - AdminPlantCreateView — `/admin/plants/new` → POST `/admin/plants`
  - Дневник (JWT)
    - DiaryMyPlantsView — `/diary/plants` → GET/POST `/diary/plants`
    - DiaryPlantCareView — `/diary/plants/:id/care` → GET/POST `/diary/care/{userPlantId}`
    - RemindersView — `/diary/reminders` → GET `/diary/reminders/due?before=...`; создание → POST `/diary/reminders/{userPlantId}`; завершение → POST `/diary/reminders/complete/{reminderId}`
  - Календарь (JWT)
    - CalendarDayView — `/calendar/day` → GET `/calendar/day?date=YYYY-MM-DD`
    - CalendarWeekView — `/calendar/week` → GET `/calendar/week?start=YYYY-MM-DD`
    - CalendarMonthView — `/calendar/month` → GET `/calendar/month?year=YYYY&month=MM`

- Компоненты (components):
  - AppHeader / AppSidebar / AppLayout
  - PaginationControls (page/size, сортировка)
  - SearchBar (q)
  - PlantCard / PlantForm (admin)
  - CareEntryForm, ReminderForm
  - CalendarToolbar (переключение day/week/month)

- Состояние (Pinia stores):
  - `useAuthStore` — JWT, профиль, логин/логаут, guard для приватных маршрутов
  - `usePlantsStore` — публичные растения, admin CRUD, поиск, пагинация
  - `useDiaryStore` — мои растения, уход, напоминания
  - `useCalendarStore` — day/week/month представления

- API слой (Axios):
  - Единый инстанс `api` (baseURL из `import.meta.env.VITE_API_BASE_URL`)
  - Interceptor добавляет `Authorization: Bearer <token>` при наличии JWT
  - Обработка ошибок ProblemDetails (от бэка) → единый хэндлер в UI (toast/notification)

## Структура проекта (предложение)
```
frontend/
  package.json
  index.html
  vite.config.ts
  src/
    main.ts
    App.vue
    router/
      index.ts
      guards.ts
    stores/
      auth.ts
      plants.ts
      diary.ts
      calendar.ts
    api/
      client.ts        # axios инстанс
      auth.ts          # /auth/*
      plants.ts        # /plants, /admin/plants
      diary.ts         # /diary/*
      calendar.ts      # /calendar/*
      types.ts         # DTO/типы
    components/
      layout/
        AppHeader.vue
        AppLayout.vue
      common/
        PaginationControls.vue
        SearchBar.vue
      plants/
        PlantCard.vue
        PlantForm.vue
      diary/
        CareEntryForm.vue
        ReminderForm.vue
      calendar/
        CalendarToolbar.vue
    views/
      public/
        PlantsListView.vue
        PlantDetailView.vue
        RecommendationsView.vue
      auth/
        LoginView.vue
        RegisterView.vue
      profile/
        ProfileView.vue
      admin/
        AdminPlantsListView.vue
        AdminPlantEditView.vue
        AdminPlantCreateView.vue
      diary/
        DiaryMyPlantsView.vue
        DiaryPlantCareView.vue
        RemindersView.vue
      calendar/
        CalendarDayView.vue
        CalendarWeekView.vue
        CalendarMonthView.vue
```

## Навигация и доступ
- Публичные маршруты: `/plants`, `/plants/:id`, `/plants/recommendations`, `/auth/*`
- Приватные (JWT): `/profile`, `/diary/*`, `/calendar/*`, `/admin/*`
- Guard:
  - Если нет JWT → редирект на `/auth/login` при попытке доступа к приватным страницам
  - Для `/admin/*` дополнительно проверять `role === 'ADMIN'` (поле приходит в JWT)

## API соответствия (бэкенд → фронт)
- Auth
  - POST `/auth/register` (email, password) → 200 {id,email}
  - POST `/auth/login` (email, password) → 200 {token}
  - GET `/profile` → 200 {id,email,role}; PATCH `/profile` (email?); POST `/profile/password` (currentPassword,newPassword)
- Публичные растения
  - GET `/plants?q=&page=&size=&sort=` → PageResponse
  - GET `/plants/{id}` → Plant
  - GET `/plants/recommendations?light=&water=&minTempC=&maxTempC=&limit=` → Plant[]
- Админ растения (JWT ADMIN)
  - GET `/admin/plants` (можно использовать публичный список)
  - GET `/admin/plants/{id}`, PATCH `/admin/plants/{id}`, DELETE `/admin/plants/{id}`
  - POST `/admin/plants`
- Дневник (JWT)
  - User plants: GET/POST `/diary/plants`; PATCH/DELETE `/diary/plants/{id}`
  - Care entries: GET/POST `/diary/care/{userPlantId}?page=&size=&sort=`
  - Reminders: GET `/diary/reminders/due?before=&page=&size=&sort=`; POST `/diary/reminders/{userPlantId}`; POST `/diary/reminders/complete/{reminderId}`
- Календарь (JWT)
  - GET `/calendar/day?date=&page=&size=&sort=`
  - GET `/calendar/week?start=&page=&size=&sort=`
  - GET `/calendar/month?year=&month=&page=&size=&sort=`

## Состояние и кэширование
- Pinia хранит JWT в `localStorage` (восстановление при старте)
- Запросы с пагинацией/поиском — кэшировать последний запрос/ответ для возврата мгновенно при возврате на экран
- Ошибки ProblemDetails отображать toast/alert из единого хэндлера

## Примеры реализаций (схематично)
```ts
// src/api/client.ts
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

export const api = axios.create({ baseURL: import.meta.env.VITE_API_BASE_URL })

api.interceptors.request.use((config) => {
  const auth = useAuthStore()
  if (auth.token) config.headers.Authorization = `Bearer ${auth.token}`
  return config
})

api.interceptors.response.use(
  (r) => r,
  (error) => {
    // Здесь можно распарсить ProblemDetails и показывать уведомление
    throw error
  }
)
```

```ts
// src/stores/auth.ts
import { defineStore } from 'pinia'
import { api } from '@/api/client'

export const useAuthStore = defineStore('auth', {
  state: () => ({ token: localStorage.getItem('token') || '' }),
  actions: {
    async login(email: string, password: string) {
      const { data } = await api.post('/auth/login', { email, password })
      this.token = data.token
      localStorage.setItem('token', this.token)
    },
    logout() {
      this.token = ''
      localStorage.removeItem('token')
    },
  },
})
```

## Мильстоны фронтенда
- F0 — Скелет
  - Vite + Vue3 + TS + Pinia + Router + Element Plus; базовая тема/лейаут
  - API‑клиент, конфиг окружения
  - Маршруты и заглушки страниц

- F1 — Аутентификация и профиль
  - Регистрация/логин, хранение JWT, guards
  - Профиль: просмотр/изменение email, смена пароля

- F2 — Публичный словарь
  - Список (поиск, пагинация, сортировка), карточки, детали
  - Рекомендации

- F3 — Админ словарь
  - CRUD растений (таблица, форма, валидация)

- F4 — Дневник
  - Мои растения (список/создание/редактирование/удаление)
  - Уход (список с пагинацией, добавление)
  - Напоминания (список, создание, завершение)

- F5 — Календарь
  - Day/Week/Month; навигация, отображение метаданных

- F6 — Качество
  - Тесты (Vitest), e2e (Playwright — опционально)
  - Линт/форматирование, GitHub Actions для фронта (по желанию)

## Локальная разработка
- Предпосылки: Node 20+, pnpm/npm/yarn
- Создание проекта (пример):
  - `pnpm create vite my-plant-diary-frontend --template vue-ts`
  - `cd my-plant-diary-frontend && pnpm add vue-router pinia axios element-plus`
  - Настроить alias `@` на `src`, ESLint/Prettier, добавить Pinia и Router в `main.ts`
- Запуск: `pnpm dev`, сборка: `pnpm build`, превью: `pnpm preview`

## Docker (опционально)
- Dev/Prod Dockerfile + nginx конфигурация для SPA (перенаправление на `/index.html`)
- ENV `VITE_API_BASE_URL` пробрасывать через build args или `.env`

## UX/дизайн (минимальные ожидания)
- Чистый светлый UI, адаптивный
- Toast/notification для ошибок ProblemDetails
- Подтверждения действий (удаление, завершение напоминаний)
- Инпуты дат/времени с контролем формата

## Риски и улучшения
- Нет refresh‑токенов в бэкенде — при истечении JWT потребуется повторный логин
- Для больших датасетов — рассмотреть серверную фильтрацию/сортировку везде
- i18n — добавить при росте аудитории (vue‑i18n)

---

Этого плана достаточно, чтобы стартовать фронтенд на Vue 3 и быстро покрыть все реализованные серверные сценарии. При необходимости расширю документ примерами для каждой страницы и добавлю CI для фронта.

