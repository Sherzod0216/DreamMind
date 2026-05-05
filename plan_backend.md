# DreamMind Backend Plan

## 1. Goal

Build a production-style backend for DreamMind using NestJS. The backend should support the Android mobile app with real authentication, persisted sleep data, user onboarding, alarm settings, analytics, and AI coach chat.

This backend is designed for a final-year project, so the priority is a clear, demonstrable, reliable system rather than unnecessary enterprise complexity.

## 2. Recommended Stack

- Runtime: Node.js 20 LTS
- Framework: NestJS
- Database: PostgreSQL
- ORM: Prisma
- Authentication: JWT access tokens and refresh tokens
- Password hashing: Argon2
- Validation: class-validator and class-transformer
- API documentation: Swagger/OpenAPI
- Local development database: Docker Compose
- Testing: Jest with unit tests and basic e2e tests
- Deployment target: Render, Railway, Fly.io, or VPS

## 3. Main Backend Responsibilities

- Register and authenticate users.
- Store user profile data.
- Store onboarding preferences.
- Store sleep sessions created from the mobile app.
- Generate sleep-stage timeline data for session detail screens.
- Store alarm settings.
- Provide weekly, monthly, and yearly analytics.
- Store coach chat history.
- Generate mock or real AI coach responses.
- Provide a clean API contract for the Android developer.

## 4. Architecture

Use a modular NestJS architecture:

```txt
src/
  app.module.ts
  main.ts
  common/
    decorators/
    filters/
    guards/
    interceptors/
    pipes/
    types/
  config/
    env.schema.ts
    configuration.ts
  prisma/
    prisma.module.ts
    prisma.service.ts
  auth/
    auth.module.ts
    auth.controller.ts
    auth.service.ts
    dto/
    guards/
    strategies/
  users/
    users.module.ts
    users.controller.ts
    users.service.ts
    dto/
  onboarding/
    onboarding.module.ts
    onboarding.controller.ts
    onboarding.service.ts
    dto/
  sleep-sessions/
    sleep-sessions.module.ts
    sleep-sessions.controller.ts
    sleep-sessions.service.ts
    dto/
  alarms/
    alarms.module.ts
    alarms.controller.ts
    alarms.service.ts
    dto/
  analytics/
    analytics.module.ts
    analytics.controller.ts
    analytics.service.ts
    dto/
  coach/
    coach.module.ts
    coach.controller.ts
    coach.service.ts
    dto/
  health/
    health.module.ts
    health.controller.ts
```

Keep modules simple. Each module should have a controller, service, DTOs, and tests where useful.

## 5. Database Schema

Use Prisma migrations. The first version should include these models.

### User

Stores account credentials.

Fields:

- `id`
- `email`
- `passwordHash`
- `createdAt`
- `updatedAt`

Rules:

- Email must be unique.
- Password must never be stored directly.

### Profile

Stores user-facing profile information.

Fields:

- `id`
- `userId`
- `displayName`
- `age`
- `heightCm`
- `weightKg`
- `avatarUrl`
- `createdAt`
- `updatedAt`

Rules:

- One profile per user.
- Create default profile during registration.

### OnboardingPreference

Stores onboarding answers.

Fields:

- `id`
- `userId`
- `goal`
- `targetBedtime`
- `targetWakeTime`
- `syncEnabled`
- `completedAt`
- `createdAt`
- `updatedAt`

Goal enum:

- `FALL_ASLEEP_FASTER`
- `WAKE_UP_REFRESHED`
- `TRACK_SLEEP_HEALTH`

### SleepSession

Stores one logged sleep session.

Fields:

- `id`
- `userId`
- `sessionDate`
- `bedtime`
- `wakeTime`
- `durationMinutes`
- `qualityScore`
- `qualityLabel`
- `notes`
- `createdAt`
- `updatedAt`

Rules:

- `durationMinutes` can be calculated server-side from `bedtime` and `wakeTime`.
- `qualityScore` can initially come from the mobile app or be calculated from quality level.

### SleepSessionActivity

Stores pre-sleep activities selected by the user.

Fields:

- `id`
- `sessionId`
- `activity`

Activity enum:

- `CAFFEINE`
- `READING`
- `SCREEN_TIME`
- `EXERCISE`
- `MEDITATION`
- `OTHER`

### SleepStageSegment

Stores generated timeline data for the detail screen.

Fields:

- `id`
- `sessionId`
- `stage`
- `startOffsetMinutes`
- `durationMinutes`
- `heightFraction`

Stage enum:

- `WAKE`
- `REM`
- `LIGHT`
- `DEEP`

Rules:

- Generate mock but realistic stage data when a sleep session is created.
- This gives the mobile detail screen realistic data without requiring wearable integration.

### AlarmSetting

Stores the user's alarm configuration.

Fields:

- `id`
- `userId`
- `alarmTime`
- `smartWakeEnabled`
- `smartWakeWindowMinutes`
- `sound`
- `vibrationLevel`
- `createdAt`
- `updatedAt`

Rules:

- One alarm setting per user.
- Create default alarm settings during registration or first request.

### CoachMessage

Stores chat history.

Fields:

- `id`
- `userId`
- `role`
- `content`
- `createdAt`

Role enum:

- `USER`
- `ASSISTANT`
- `SYSTEM`

## 6. Prisma Schema Draft

```prisma
model User {
  id           String   @id @default(uuid())
  email        String   @unique
  passwordHash String
  createdAt    DateTime @default(now())
  updatedAt    DateTime @updatedAt

  profile       Profile?
  onboarding    OnboardingPreference?
  sessions      SleepSession[]
  alarmSetting  AlarmSetting?
  coachMessages CoachMessage[]
}

model Profile {
  id          String   @id @default(uuid())
  userId      String   @unique
  displayName String
  age         Int?
  heightCm    Int?
  weightKg    Int?
  avatarUrl   String?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model OnboardingPreference {
  id             String    @id @default(uuid())
  userId         String    @unique
  goal           SleepGoal?
  targetBedtime  String?
  targetWakeTime String?
  syncEnabled    Boolean   @default(false)
  completedAt    DateTime?
  createdAt      DateTime  @default(now())
  updatedAt      DateTime  @updatedAt

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model SleepSession {
  id              String   @id @default(uuid())
  userId          String
  sessionDate     DateTime
  bedtime         DateTime
  wakeTime        DateTime
  durationMinutes Int
  qualityScore    Int
  qualityLabel    String
  notes           String?
  createdAt       DateTime @default(now())
  updatedAt       DateTime @updatedAt

  user       User                   @relation(fields: [userId], references: [id], onDelete: Cascade)
  activities SleepSessionActivity[]
  stages     SleepStageSegment[]
}

model SleepSessionActivity {
  id        String        @id @default(uuid())
  sessionId String
  activity  SleepActivity

  session SleepSession @relation(fields: [sessionId], references: [id], onDelete: Cascade)
}

model SleepStageSegment {
  id                 String     @id @default(uuid())
  sessionId           String
  stage              SleepStage
  startOffsetMinutes Int
  durationMinutes    Int
  heightFraction     Float

  session SleepSession @relation(fields: [sessionId], references: [id], onDelete: Cascade)
}

model AlarmSetting {
  id                     String   @id @default(uuid())
  userId                 String   @unique
  alarmTime              String
  smartWakeEnabled       Boolean  @default(true)
  smartWakeWindowMinutes Int      @default(30)
  sound                  String   @default("Forest Morning")
  vibrationLevel         Float    @default(0.65)
  createdAt              DateTime @default(now())
  updatedAt              DateTime @updatedAt

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

model CoachMessage {
  id        String           @id @default(uuid())
  userId    String
  role      CoachMessageRole
  content   String
  createdAt DateTime         @default(now())

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)
}

enum SleepGoal {
  FALL_ASLEEP_FASTER
  WAKE_UP_REFRESHED
  TRACK_SLEEP_HEALTH
}

enum SleepActivity {
  CAFFEINE
  READING
  SCREEN_TIME
  EXERCISE
  MEDITATION
  OTHER
}

enum SleepStage {
  WAKE
  REM
  LIGHT
  DEEP
}

enum CoachMessageRole {
  USER
  ASSISTANT
  SYSTEM
}
```

## 7. API Contract

All protected routes require:

```http
Authorization: Bearer <access_token>
```

All responses should use normal JSON.

Recommended error format:

```json
{
  "statusCode": 400,
  "message": "Validation failed",
  "error": "Bad Request"
}
```

## 8. Auth API

### Register

```http
POST /auth/register
```

Request:

```json
{
  "email": "alex@example.com",
  "password": "password123",
  "displayName": "Alex Johnson"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "user": {
    "id": "uuid",
    "email": "alex@example.com",
    "displayName": "Alex Johnson"
  }
}
```

### Login

```http
POST /auth/login
```

Request:

```json
{
  "email": "alex@example.com",
  "password": "password123"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt",
  "user": {
    "id": "uuid",
    "email": "alex@example.com",
    "displayName": "Alex Johnson"
  }
}
```

### Get Current User

```http
GET /auth/me
```

Response:

```json
{
  "id": "uuid",
  "email": "alex@example.com",
  "profile": {
    "displayName": "Alex Johnson",
    "age": 22,
    "heightCm": 178,
    "weightKg": 72,
    "avatarUrl": null
  },
  "onboardingCompleted": true
}
```

### Refresh Token

```http
POST /auth/refresh
```

Request:

```json
{
  "refreshToken": "jwt"
}
```

Response:

```json
{
  "accessToken": "jwt",
  "refreshToken": "jwt"
}
```

## 9. Profile API

### Get Profile

```http
GET /profile/me
```

Response:

```json
{
  "displayName": "Alex Johnson",
  "age": 22,
  "heightCm": 178,
  "weightKg": 72,
  "avatarUrl": null,
  "memberSince": "2026-04-26T10:00:00.000Z"
}
```

### Update Profile

```http
PUT /profile/me
```

Request:

```json
{
  "displayName": "Alex Johnson",
  "age": 22,
  "heightCm": 178,
  "weightKg": 72
}
```

## 10. Onboarding API

### Get Onboarding Preferences

```http
GET /onboarding/me
```

Response:

```json
{
  "goal": "FALL_ASLEEP_FASTER",
  "targetBedtime": "22:30",
  "targetWakeTime": "06:45",
  "syncEnabled": false,
  "completedAt": "2026-04-26T10:30:00.000Z"
}
```

### Update Onboarding Preferences

```http
PUT /onboarding/me
```

Request:

```json
{
  "goal": "FALL_ASLEEP_FASTER",
  "targetBedtime": "22:30",
  "targetWakeTime": "06:45",
  "syncEnabled": false,
  "completed": true
}
```

## 11. Sleep Sessions API

### Create Sleep Session

```http
POST /sleep-sessions
```

Request:

```json
{
  "sessionDate": "2026-04-26",
  "bedtime": "2026-04-25T22:45:00.000Z",
  "wakeTime": "2026-04-26T07:30:00.000Z",
  "qualityScore": 65,
  "qualityLabel": "Fair",
  "activities": ["CAFFEINE", "EXERCISE"],
  "notes": "Felt okay in the morning"
}
```

Response:

```json
{
  "id": "uuid",
  "sessionDate": "2026-04-26",
  "durationMinutes": 525,
  "durationLabel": "8h 45m",
  "qualityScore": 65,
  "qualityLabel": "Fair",
  "activities": ["CAFFEINE", "EXERCISE"]
}
```

### List Sleep Sessions

```http
GET /sleep-sessions
```

Query params:

- `limit`
- `offset`
- `from`
- `to`

Response:

```json
{
  "items": [
    {
      "id": "uuid",
      "sessionDate": "2026-04-26",
      "durationMinutes": 525,
      "durationLabel": "8h 45m",
      "qualityScore": 65,
      "qualityLabel": "Fair"
    }
  ],
  "total": 1
}
```

### Get Latest Sleep Session

```http
GET /sleep-sessions/latest
```

Response:

```json
{
  "id": "uuid",
  "sessionDate": "2026-04-26",
  "durationMinutes": 444,
  "durationLabel": "7h 24m",
  "qualityScore": 82,
  "qualityLabel": "Good"
}
```

If there is no session:

```json
{
  "session": null
}
```

### Get Sleep Session Detail

```http
GET /sleep-sessions/:id
```

Response:

```json
{
  "id": "uuid",
  "sessionDate": "2026-04-26",
  "dateLabel": "Oct 24",
  "durationMinutes": 444,
  "durationLabel": "7h 24m",
  "qualityScore": 82,
  "qualityLabel": "Good",
  "activities": ["SCREEN_TIME", "READING"],
  "stages": [
    {
      "stage": "WAKE",
      "startOffsetMinutes": 0,
      "durationMinutes": 18,
      "heightFraction": 1
    },
    {
      "stage": "REM",
      "startOffsetMinutes": 18,
      "durationMinutes": 52,
      "heightFraction": 0.66
    }
  ],
  "metrics": [
    {
      "label": "Deep Sleep",
      "value": "1h 12m",
      "trend": "+5m",
      "progress": 0.45
    },
    {
      "label": "Efficiency",
      "value": "94%",
      "trend": "Optimal",
      "progress": 0.94
    }
  ],
  "coachInsight": "Your REM sleep was 18% higher than usual after yesterday's low screen time."
}
```

### Update Sleep Session

```http
PATCH /sleep-sessions/:id
```

### Delete Sleep Session

```http
DELETE /sleep-sessions/:id
```

## 12. Alarm API

### Get Alarm Setting

```http
GET /alarm/me
```

Response:

```json
{
  "alarmTime": "06:45",
  "smartWakeEnabled": true,
  "smartWakeWindowMinutes": 30,
  "sound": "Forest Morning",
  "vibrationLevel": 0.65
}
```

### Update Alarm Setting

```http
PUT /alarm/me
```

Request:

```json
{
  "alarmTime": "06:45",
  "smartWakeEnabled": true,
  "smartWakeWindowMinutes": 30,
  "sound": "Forest Morning",
  "vibrationLevel": 0.65
}
```

## 13. Analytics API

Analytics should be calculated server-side from sleep sessions.

Supported ranges:

- `week`
- `month`
- `year`

### Summary

```http
GET /analytics/summary?range=week
```

Response:

```json
{
  "range": "week",
  "averageSleepMinutes": 468,
  "averageSleepLabel": "7h 48m",
  "qualityPercent": 85,
  "sleepDebtMinutes": -75,
  "sleepDebtLabel": "-1h 15m",
  "consistencyPercent": 92,
  "averageBedtime": "23:24",
  "averageWakeTime": "07:12"
}
```

### Sleep Hours Chart

```http
GET /analytics/sleep-hours?range=week
```

Response:

```json
{
  "items": [
    {
      "label": "MON",
      "durationMinutes": 420,
      "progress": 0.75
    },
    {
      "label": "TUE",
      "durationMinutes": 500,
      "progress": 0.85
    }
  ]
}
```

### Quality Trend

```http
GET /analytics/quality?range=week
```

Response:

```json
{
  "qualityPercent": 85,
  "trendText": "Your sleep quality improved by 4% compared to last week."
}
```

## 14. Coach API

The first version can use mock responses. Keep the service interface ready for real AI later.

### Get Coach Messages

```http
GET /coach/messages
```

Response:

```json
{
  "items": [
    {
      "id": "uuid",
      "role": "ASSISTANT",
      "content": "Good morning! Your deep sleep was higher than usual.",
      "createdAt": "2026-04-26T08:32:00.000Z"
    }
  ]
}
```

### Send Coach Message

```http
POST /coach/messages
```

Request:

```json
{
  "content": "Why did I sleep better last night?"
}
```

Response:

```json
{
  "userMessage": {
    "id": "uuid",
    "role": "USER",
    "content": "Why did I sleep better last night?"
  },
  "assistantMessage": {
    "id": "uuid",
    "role": "ASSISTANT",
    "content": "You slept earlier and had lower screen time, which likely improved recovery."
  }
}
```

### Analyze Last Night

```http
POST /coach/analyze-last-night
```

Response:

```json
{
  "message": {
    "id": "uuid",
    "role": "ASSISTANT",
    "content": "Last night you slept 7h 24m with an 82/100 score. Deep sleep improved by 5 minutes."
  }
}
```

## 15. AI Coach Strategy

Start with a mock implementation.

The `CoachService` should have methods like:

- `sendMessage(userId, content)`
- `analyzeLastNight(userId)`
- `generateSessionInsight(userId, sessionId)`

Version 1 behavior:

- Store the user's message.
- Look up latest sleep session.
- Return a polished rule-based assistant response.
- Store the assistant response.

Version 2 optional behavior:

- Add OpenAI or another AI provider behind the same service methods.
- Keep the controller and Android contract unchanged.

Example rule-based response logic:

- If quality score is over 80: mention positive recovery.
- If duration is under 6 hours: mention sleep debt.
- If screen time is selected: suggest reducing screens.
- If exercise is selected: mention possible recovery benefits.
- If caffeine is selected: suggest earlier caffeine cutoff.

## 16. Sleep Stage Generation

Because Health Connect is not required, generate realistic sleep-stage data on the backend.

When a session is created:

1. Calculate total duration.
2. Generate 6 to 10 stage segments.
3. Make total segment duration equal session duration.
4. Include a mix of wake, REM, light, and deep stages.
5. Store the result in `SleepStageSegment`.

Simple stage pattern:

```txt
WAKE -> LIGHT -> DEEP -> LIGHT -> REM -> LIGHT -> DEEP -> REM -> WAKE
```

This lets the mobile app render the detail screen from real backend data.

## 17. Security Requirements

- Hash passwords with Argon2.
- Use JWT access tokens.
- Use refresh tokens.
- Store refresh token hashes if implementing logout properly.
- Validate all request DTOs.
- Never return `passwordHash`.
- Protect all user data routes with JWT guard.
- Always scope database queries by current `userId`.
- Enable CORS for the Android app and Swagger testing.

## 18. DTO Validation Rules

Examples:

- Email must be valid.
- Password minimum length: 8.
- Display name maximum length: 80.
- Quality score must be between 0 and 100.
- Vibration level must be between 0 and 1.
- Range query must be one of `week`, `month`, `year`.
- Activity and goal fields must use enums.

Use DTOs for every request body.

## 19. Mobile Integration Contract

The backend developer should keep Swagger updated from day one.

Mobile developer needs:

- Base API URL
- Auth endpoints
- Exact request and response models
- JWT storage rules
- Empty-state behavior
- Error response shape

Recommended Android behavior:

- On launch, check saved access token.
- If token exists, call `GET /auth/me`.
- If user has no sleep sessions, show empty dashboard.
- If user has a latest session, show populated dashboard.
- After creating sleep session, refresh latest session and analytics.
- If token expires, use refresh token.
- If refresh fails, return to sign-in.

## 20. Development Milestones

### Milestone 1: Project Setup

Deliverables:

- NestJS project created.
- PostgreSQL Docker Compose added.
- Prisma configured.
- Environment validation added.
- Swagger enabled.
- Health endpoint added.

Endpoints:

```http
GET /health
```

### Milestone 2: Authentication

Deliverables:

- Register.
- Login.
- JWT auth guard.
- Current user endpoint.
- Password hashing.
- Default profile creation.

Endpoints:

```http
POST /auth/register
POST /auth/login
GET /auth/me
POST /auth/refresh
```

### Milestone 3: Profile and Onboarding

Deliverables:

- Profile get/update.
- Onboarding get/update.
- Mobile app can complete onboarding through backend.

Endpoints:

```http
GET /profile/me
PUT /profile/me
GET /onboarding/me
PUT /onboarding/me
```

### Milestone 4: Sleep Sessions

Deliverables:

- Create sleep session.
- List sleep sessions.
- Latest sleep session.
- Sleep session detail.
- Generated sleep stages.

Endpoints:

```http
POST /sleep-sessions
GET /sleep-sessions
GET /sleep-sessions/latest
GET /sleep-sessions/:id
PATCH /sleep-sessions/:id
DELETE /sleep-sessions/:id
```

### Milestone 5: Alarm Settings

Deliverables:

- Get alarm setting.
- Update alarm setting.
- Default alarm setting.

Endpoints:

```http
GET /alarm/me
PUT /alarm/me
```

### Milestone 6: Analytics

Deliverables:

- Weekly/monthly/yearly summary.
- Sleep-hours chart data.
- Quality ring data.
- Empty analytics behavior.

Endpoints:

```http
GET /analytics/summary?range=week
GET /analytics/sleep-hours?range=week
GET /analytics/quality?range=week
```

### Milestone 7: Coach

Deliverables:

- Coach message history.
- Send message.
- Mock assistant response.
- Analyze last night.

Endpoints:

```http
GET /coach/messages
POST /coach/messages
POST /coach/analyze-last-night
```

### Milestone 8: Polish, Testing, Deployment

Deliverables:

- Swagger documentation complete.
- Seed script added.
- Basic tests added.
- Backend deployed.
- Android app connected to backend.

## 21. Suggested Work Split

Backend developer:

- NestJS setup.
- Database schema.
- Auth.
- All REST endpoints.
- Swagger.
- Deployment.

Mobile developer:

- Replace mock repositories with API repositories.
- Token storage.
- API models.
- Loading and error states.
- Refresh UI after writes.

Shared work:

- API contract review.
- Field naming.
- Demo data.
- Final presentation flow.

## 22. Testing Plan

Unit tests:

- Auth service password hashing and login.
- Sleep duration calculation.
- Sleep stage generation.
- Analytics summary calculation.
- Coach mock response generation.

E2E tests:

- Register -> login -> create sleep session -> get latest session.
- Complete onboarding.
- Update alarm settings.
- Get analytics after session creation.
- Send coach message.

Manual Swagger tests:

- Test all endpoints with one demo user.
- Export screenshots for documentation if needed.

## 23. Seed Data

Add a seed script:

```bash
npx prisma db seed
```

Seed should create:

- One demo user: `alex@example.com`
- Profile: Alex Johnson
- Completed onboarding
- 7 to 14 sleep sessions
- Alarm settings
- Coach messages

This lets the mobile app show the populated dashboard immediately during demos.

## 24. Environment Variables

```env
NODE_ENV=development
PORT=3000
DATABASE_URL=postgresql://postgres:postgres@localhost:5432/dreammind
JWT_ACCESS_SECRET=replace_me
JWT_REFRESH_SECRET=replace_me
JWT_ACCESS_EXPIRES_IN=15m
JWT_REFRESH_EXPIRES_IN=7d
CORS_ORIGIN=*
```

Optional later:

```env
OPENAI_API_KEY=
AI_PROVIDER=openai
```

## 25. Deployment Plan

Simple deployment options:

- Railway: easiest for PostgreSQL and Node backend.
- Render: good free/cheap option.
- Fly.io: solid but needs more setup.
- VPS: most control, more maintenance.

Deployment checklist:

- PostgreSQL database created.
- Environment variables configured.
- Prisma migrations run.
- Seed data inserted.
- Swagger URL available.
- Android app base URL updated.

## 26. Swagger Requirements

Enable Swagger at:

```http
/docs
```

Swagger should include:

- Auth bearer token support.
- Request DTO examples.
- Response examples where possible.
- Tags by module.

This is important because it lets the mobile developer integrate without reading backend code.

## 27. Final Demo Flow

The backend should support this presentation flow:

1. Register a new user.
2. Complete onboarding.
3. Show empty dashboard because there are no sessions.
4. Create first sleep session.
5. Show populated dashboard.
6. Open analytics.
7. Open sleep session detail.
8. Ask the coach to analyze last night.
9. Update alarm setting.
10. Show profile/settings.

## 28. Scope Control

Do not build these in version 1:

- Health Connect integration.
- Real wearable sync.
- Social login.
- Email verification.
- Password reset email.
- Payment/subscription.
- Complex AI memory.
- Admin dashboard.

They can be mentioned as future work in the final report.

## 29. Backend Quality Checklist

- App starts with `npm run start:dev`.
- Swagger works at `/docs`.
- Prisma migrations are committed.
- Seed data works.
- All protected endpoints reject missing JWT.
- Users can only access their own data.
- DTO validation rejects bad input.
- Sleep session creation generates stage data.
- Analytics handles empty data.
- Coach works without a real AI key.
- Android can complete the full demo flow.

## 30. Recommended First Commands

```bash
npm i -g @nestjs/cli
nest new dreammind-backend
cd dreammind-backend
npm install @nestjs/config @nestjs/swagger swagger-ui-express
npm install @prisma/client prisma
npm install class-validator class-transformer
npm install @nestjs/passport passport passport-jwt
npm install argon2
npm install cookie-parser
npm install -D @types/passport-jwt @types/cookie-parser
npx prisma init
```

Then add Docker Compose for PostgreSQL and start implementing Milestone 1.

