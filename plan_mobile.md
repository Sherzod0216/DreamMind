# DreamMind Mobile App Architecture Plan

## 1. Goal

Turn the current Compose UI prototype into a real Android application with clear architecture, backend integration, state management, local persistence, and testable business logic.

The current app is useful as a visual prototype, but most logic is inside large composable files. The next phase should separate UI, navigation, state, data, and backend communication while preserving the design quality.

## 2. Current State

Current Android code is very small and flat:

```txt
app/src/main/java/com/example/dreammind/
  MainActivity.kt
  DreamMindApp.kt
  AuthOnboardingFlow.kt
  data/
    MockDreamMindRepository.kt
  ui/theme/
    Color.kt
    Theme.kt
    Type.kt
```

Problems:

- `DreamMindApp.kt` contains too many screens and components.
- UI state is stored directly inside composables.
- Mock data is hardcoded in one repository object.
- There is no real navigation graph.
- There are no ViewModels.
- There is no API layer.
- There is no local persistence.
- There are no loading, error, or empty states connected to real data.

Target:

- Keep the UI style.
- Move each feature into its own package.
- Use ViewModels for screen state.
- Use repositories for data access.
- Use Retrofit for backend communication.
- Use DataStore for auth tokens and preferences.
- Use Room for local sleep sessions and coach messages if offline support is needed.

## 3. Recommended Android Stack

- Language: Kotlin
- UI: Jetpack Compose
- Architecture: MVVM with repositories
- Navigation: Navigation Compose
- Async: Kotlin Coroutines and Flow
- Networking: Retrofit + OkHttp
- JSON: Kotlinx Serialization or Moshi
- DI: Hilt
- Token storage: DataStore
- Local database: Room
- Image loading: Coil
- Testing: JUnit, Turbine, MockWebServer, Compose UI tests

Recommended dependency additions:

- `androidx.navigation:navigation-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.hilt:hilt-navigation-compose`
- `com.google.dagger:hilt-android`
- `com.squareup.retrofit2:retrofit`
- `com.squareup.okhttp3:okhttp`
- `com.squareup.okhttp3:logging-interceptor`
- `org.jetbrains.kotlinx:kotlinx-serialization-json`
- `androidx.datastore:datastore-preferences`
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`
- `io.coil-kt:coil-compose`

For final-year scope, Room is optional but recommended. DataStore is required.

## 4. Target Package Structure

Use one Android module for now. Multi-module architecture is not necessary for this project.

```txt
com.example.dreammind/
  MainActivity.kt
  DreamMindApp.kt

  core/
    common/
      Result.kt
      UiText.kt
      DateTimeUtils.kt
      DurationUtils.kt
    designsystem/
      component/
      theme/
      icon/
    navigation/
      DreamMindNavGraph.kt
      DreamMindRoute.kt
      BottomNavItem.kt
    network/
      DreamMindApi.kt
      AuthInterceptor.kt
      TokenAuthenticator.kt
      NetworkModule.kt
      dto/
    datastore/
      AuthTokenStore.kt
      UserPreferenceStore.kt
    database/
      DreamMindDatabase.kt
      dao/
      entity/
    model/
      User.kt
      Profile.kt
      OnboardingPreference.kt
      SleepSession.kt
      AlarmSetting.kt
      Analytics.kt
      CoachMessage.kt
    data/
      repository/
      mapper/
      fake/
    di/
      AppModule.kt
      RepositoryModule.kt
      NetworkModule.kt
      DatabaseModule.kt

  feature/
    auth/
      AuthScreen.kt
      AuthViewModel.kt
      AuthUiState.kt
      AuthRoute.kt
      component/
    onboarding/
      OnboardingRoute.kt
      OnboardingViewModel.kt
      OnboardingUiState.kt
      screens/
      component/
    home/
      HomeRoute.kt
      HomeScreen.kt
      HomeViewModel.kt
      HomeUiState.kt
      component/
    alarm/
      AlarmRoute.kt
      AlarmScreen.kt
      AlarmViewModel.kt
      AlarmUiState.kt
      component/
    sleep_log/
      SleepLogRoute.kt
      SleepLogScreen.kt
      SleepLogViewModel.kt
      SleepLogUiState.kt
      component/
    analytics/
      AnalyticsRoute.kt
      AnalyticsScreen.kt
      AnalyticsViewModel.kt
      AnalyticsUiState.kt
      component/
    session_detail/
      SessionDetailRoute.kt
      SessionDetailScreen.kt
      SessionDetailViewModel.kt
      SessionDetailUiState.kt
      component/
    coach/
      CoachRoute.kt
      CoachScreen.kt
      CoachViewModel.kt
      CoachUiState.kt
      component/
    profile/
      ProfileRoute.kt
      ProfileScreen.kt
      ProfileViewModel.kt
      ProfileUiState.kt
      component/
```

## 5. High-Level Architecture

Use this flow:

```txt
Composable Screen
  -> ViewModel
  -> Repository
  -> Remote API / Local Database / DataStore
```

Responsibilities:

- Composables render state and send user events.
- ViewModels own UI state and call repositories.
- Repositories decide whether to use remote, local, or fake data.
- API DTOs stay in the network layer.
- UI models stay in feature or core model packages.
- Mappers convert DTO/entity objects into app models.

## 6. App Startup Flow

On app launch:

1. Read tokens from DataStore.
2. If no access token, show auth flow.
3. If token exists, call `GET /auth/me`.
4. If auth is valid and onboarding is incomplete, show onboarding.
5. If auth is valid and onboarding is complete, show main app.
6. Home screen calls `GET /sleep-sessions/latest`.
7. If latest session is null, show empty dashboard.
8. If latest session exists, show populated dashboard.

This replaces the current local `EntryStage` state with a real app session state.

## 7. Navigation Plan

Use Navigation Compose.

Top-level graph:

```txt
auth_graph
  sign_in
  create_account

onboarding_graph
  welcome
  sleep_goal
  schedule
  sync

main_graph
  home
  alarm
  analytics
  coach
  profile
  sleep_log
  session_detail/{sessionId}
```

Bottom tabs:

- `Home`
- `Alarm`
- `Stats`
- `Coach`
- `Profile`

Non-tab screens:

- `SleepLog`
- `SessionDetail`
- Auth screens
- Onboarding screens

Important:

- Keep `Alarm` as its own tab.
- Keep sleep entry logging as a separate screen, opened from Home.
- `SessionDetail` should open from Home and Stats.

## 8. UI State Pattern

Each screen should expose a single UI state.

Example:

```kotlin
data class HomeUiState(
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val userName: String = "",
    val latestSession: SleepSessionSummary? = null,
    val insight: SleepInsight? = null,
    val routineItems: List<RoutineItem> = emptyList()
)
```

Composable:

```kotlin
@Composable
fun HomeRoute(
    viewModel: HomeViewModel = hiltViewModel(),
    onOpenSleepLog: () -> Unit,
    onOpenSessionDetail: (String) -> Unit,
    onOpenCoach: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreen(
        uiState = uiState,
        onOpenSleepLog = onOpenSleepLog,
        onOpenSessionDetail = onOpenSessionDetail,
        onOpenCoach = onOpenCoach,
        onRetry = viewModel::refresh
    )
}
```

Rule:

- `Route` composables talk to ViewModels.
- `Screen` composables are mostly stateless and receive data/events.

## 9. Data Layer

Create repository interfaces:

```kotlin
interface AuthRepository {
    suspend fun register(email: String, password: String, displayName: String): AuthSession
    suspend fun login(email: String, password: String): AuthSession
    suspend fun logout()
    fun observeAuthState(): Flow<AuthState>
}

interface UserRepository {
    suspend fun getCurrentUser(): User
    suspend fun getProfile(): Profile
    suspend fun updateProfile(profile: ProfileUpdate): Profile
}

interface OnboardingRepository {
    suspend fun getPreferences(): OnboardingPreference
    suspend fun updatePreferences(input: OnboardingInput): OnboardingPreference
}

interface SleepSessionRepository {
    suspend fun createSession(input: CreateSleepSessionInput): SleepSessionSummary
    suspend fun getLatestSession(): SleepSessionSummary?
    suspend fun getSessionDetail(id: String): SleepSessionDetail
    suspend fun getSessions(limit: Int, offset: Int): List<SleepSessionSummary>
}

interface AlarmRepository {
    suspend fun getAlarm(): AlarmSetting
    suspend fun updateAlarm(input: AlarmSettingInput): AlarmSetting
}

interface AnalyticsRepository {
    suspend fun getSummary(range: AnalyticsRange): AnalyticsSummary
    suspend fun getSleepHours(range: AnalyticsRange): List<SleepHoursPoint>
    suspend fun getQuality(range: AnalyticsRange): QualityTrend
}

interface CoachRepository {
    suspend fun getMessages(): List<CoachMessage>
    suspend fun sendMessage(content: String): CoachExchange
    suspend fun analyzeLastNight(): CoachMessage
}
```

Repository implementations:

- `RemoteAuthRepository`
- `RemoteSleepSessionRepository`
- `RemoteAnalyticsRepository`
- etc.

During backend development, keep fake repositories:

- `FakeAuthRepository`
- `FakeSleepSessionRepository`
- `FakeAnalyticsRepository`

This lets the mobile developer continue without backend blockers.

## 10. Network Layer

Create one Retrofit API interface:

```kotlin
interface DreamMindApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    @PUT("onboarding/me")
    suspend fun updateOnboarding(@Body body: UpdateOnboardingRequest): OnboardingResponse

    @POST("sleep-sessions")
    suspend fun createSleepSession(@Body body: CreateSleepSessionRequest): SleepSessionSummaryResponse

    @GET("sleep-sessions/latest")
    suspend fun latestSleepSession(): LatestSleepSessionResponse

    @GET("sleep-sessions/{id}")
    suspend fun sleepSessionDetail(@Path("id") id: String): SleepSessionDetailResponse

    @GET("analytics/summary")
    suspend fun analyticsSummary(@Query("range") range: String): AnalyticsSummaryResponse

    @GET("analytics/sleep-hours")
    suspend fun sleepHours(@Query("range") range: String): SleepHoursResponse

    @GET("analytics/quality")
    suspend fun quality(@Query("range") range: String): QualityResponse

    @GET("alarm/me")
    suspend fun getAlarm(): AlarmResponse

    @PUT("alarm/me")
    suspend fun updateAlarm(@Body body: UpdateAlarmRequest): AlarmResponse

    @GET("coach/messages")
    suspend fun coachMessages(): CoachMessagesResponse

    @POST("coach/messages")
    suspend fun sendCoachMessage(@Body body: SendCoachMessageRequest): CoachExchangeResponse
}
```

Token handling:

- Access token stored in DataStore.
- Refresh token stored in DataStore.
- OkHttp interceptor adds access token.
- Authenticator refreshes token on `401`.
- If refresh fails, clear tokens and navigate to auth.

## 11. Local Persistence

Use DataStore for:

- Access token
- Refresh token
- Last known user id
- Onboarding local progress if needed
- App settings

Use Room for:

- Cached sleep sessions
- Cached latest session
- Cached coach messages
- Cached analytics snapshots if desired

For final-year scope:

- DataStore is required.
- Room can be added after backend sync works.

## 12. Feature Plans

### Auth Feature

Screens:

- Sign in
- Create account

ViewModel:

- Holds email/password/display name.
- Validates input.
- Calls `AuthRepository`.
- Saves tokens through repository.
- Emits navigation event after success.

States:

- Idle
- Loading
- Error
- Success

### Onboarding Feature

Screens:

- Welcome
- Sleep goal
- Ideal schedule
- Sync vitals

ViewModel:

- Holds selected goal.
- Holds target bedtime and wake time.
- Holds sync preference.
- Calls `OnboardingRepository.updatePreferences`.

After completion:

- Navigate to main graph.

### Home Feature

Screen states:

- Loading
- Empty dashboard
- Populated dashboard
- Error

Data:

- Current user
- Latest sleep session
- AI insight
- Routine items

Actions:

- Open sleep log
- Open alarm tab
- Open coach tab
- Open session detail

### Sleep Log Feature

Inputs:

- Bedtime
- Wake time
- Quality score
- Activities
- Notes optional

Behavior:

- Create sleep session through backend.
- On success, navigate back to home.
- Home refreshes latest session.

### Alarm Feature

Data:

- Alarm time
- Smart wake enabled
- Sound
- Vibration level

Behavior:

- Load alarm setting.
- Save changes to backend.
- Later, optionally schedule Android local notification/alarm.

### Analytics Feature

Data:

- Summary
- Sleep hours chart
- Quality trend

Behavior:

- Period switch: week, month, year.
- Pull backend analytics for selected range.
- Open latest or selected session detail.

### Session Detail Feature

Data:

- Sleep session detail
- Sleep stage segments
- Coach insight
- Metrics

Behavior:

- Load by session id.
- Show loading and error states.

### Coach Feature

Data:

- Chat history
- Suggestion chips

Behavior:

- Send message.
- Append user message immediately.
- Show loading assistant bubble.
- Append assistant response.
- Analyze last night action.

### Profile Feature

Data:

- Profile
- Streak
- Average sleep
- Consistency
- Settings rows

Behavior:

- Update profile later.
- Logout clears tokens and returns to auth.

## 13. Model Mapping

Do not use backend DTOs directly in UI.

Use mapping:

```txt
Network DTO -> Domain Model -> UI State
```

Example:

```kotlin
fun SleepSessionSummaryResponse.toDomain(): SleepSessionSummary {
    return SleepSessionSummary(
        id = id,
        date = sessionDate,
        durationLabel = durationLabel,
        qualityScore = qualityScore,
        qualityLabel = qualityLabel
    )
}
```

Reason:

- Backend fields can change.
- UI should stay stable.
- Tests become easier.

## 14. Error Handling

Use one result type:

```kotlin
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val message: UiText, val cause: Throwable? = null) : AppResult<Nothing>
}
```

Common errors:

- No internet
- Unauthorized
- Server error
- Validation error
- Unknown error

UI behavior:

- Loading indicator for first load.
- Inline error card for recoverable failures.
- Retry button where useful.
- Auth failure returns to sign in.

## 15. Design System Extraction

Move reusable components out of `DreamMindApp.kt`.

Target components:

```txt
core/designsystem/component/
  DreamBackground.kt
  DreamBottomBar.kt
  DreamButton.kt
  DreamCard.kt
  DreamHeader.kt
  DreamHeroIcon.kt
  DreamInputField.kt
  DreamSegmentedControl.kt
  DreamSwitchCard.kt
  DreamTag.kt
  DreamChart.kt
  DreamEmptyState.kt
  DreamChatBubble.kt
```

Theme files:

```txt
core/designsystem/theme/
  Color.kt
  Theme.kt
  Type.kt
  Shape.kt
  Spacing.kt
```

Important:

- Keep the current beauty.
- Reduce copy-paste.
- Every screen should reuse the same card, button, header, and bottom nav components.

## 16. Migration Plan From Current Code

### Step 1: Extract Design System

Move reusable UI pieces from `DreamMindApp.kt` and `AuthOnboardingFlow.kt`:

- `GlassCard`
- `PrimaryButton`
- `HeroIcon`
- `DreamBackdrop`
- `DreamBottomBar`
- `ScreenHeader`
- `ScorePill`
- `SegmentedPicker`

Do this before adding backend calls.

### Step 2: Split Screens By Feature

Move screens into feature packages:

- `HomeScreen`
- `AlarmScreen`
- `SleepLogScreen`
- `StatsScreen`
- `SessionDetailScreen`
- `CoachScreen`
- `ProfileScreen`
- Auth screens
- Onboarding screens

Keep current UI behavior unchanged during this step.

### Step 3: Add Navigation Compose

Replace manual stage and overlay state with navigation routes.

Current:

```kotlin
var entryStageName by rememberSaveable { mutableStateOf(...) }
var overlayName by rememberSaveable { mutableStateOf(...) }
```

Target:

```kotlin
NavHost(
    navController = navController,
    startDestination = startDestination
)
```

### Step 4: Add ViewModels

Add ViewModels feature by feature.

Start with:

1. `HomeViewModel`
2. `SleepLogViewModel`
3. `AuthViewModel`
4. `OnboardingViewModel`

### Step 5: Add Fake Repositories

Replace `MockDreamMindRepository` with repository interfaces and fake implementations.

This keeps the UI working while architecture improves.

### Step 6: Add Retrofit and Backend DTOs

Create network layer and call backend endpoints.

Start with:

1. Auth
2. Current user
3. Latest sleep session
4. Create sleep session

### Step 7: Add DataStore Token Storage

Persist login session.

App should survive restart and stay logged in.

### Step 8: Connect Remaining Features

Connect:

- Onboarding
- Alarm
- Analytics
- Session detail
- Coach
- Profile

### Step 9: Add Loading and Error States

Every screen that calls backend should have:

- Loading state
- Content state
- Empty state where applicable
- Error state

### Step 10: Optional Room Cache

Add Room only after backend integration is stable.

## 17. Suggested Mobile Milestones

### Milestone 1: Refactor UI Into Features

Deliverables:

- Feature packages created.
- Design system components extracted.
- Current UI still works exactly as before.

### Milestone 2: Navigation Compose

Deliverables:

- Auth graph.
- Onboarding graph.
- Main graph.
- Bottom navigation.
- Sleep log and session detail as real routes.

### Milestone 3: ViewModels and Fake Repositories

Deliverables:

- ViewModels for all main features.
- Repository interfaces.
- Fake repositories replacing hardcoded data.

### Milestone 4: Auth Backend Integration

Deliverables:

- Register.
- Login.
- Token storage.
- Auth startup check.
- Logout.

### Milestone 5: Sleep Session Backend Integration

Deliverables:

- Create sleep session.
- Latest session.
- Empty/populated home from real backend.
- Session detail from backend.

### Milestone 6: Analytics and Alarm Backend Integration

Deliverables:

- Analytics range switch calls backend.
- Alarm setting load/save.

### Milestone 7: Coach Backend Integration

Deliverables:

- Message history.
- Send message.
- Analyze last night.
- Loading assistant response.

### Milestone 8: Polish and Testing

Deliverables:

- Loading states polished.
- Error states polished.
- Compose previews.
- Basic ViewModel tests.
- Final demo flow verified.

## 18. Testing Plan

Unit tests:

- AuthViewModel validation.
- HomeViewModel empty/populated state.
- SleepLogViewModel create session success/error.
- AnalyticsViewModel period switch.
- CoachViewModel send message flow.

Repository tests:

- Use MockWebServer for Retrofit.
- Verify DTO mapping.
- Verify token interceptor behavior.

Compose tests:

- Auth form renders.
- Empty home dashboard renders when no session.
- Populated home renders when latest session exists.
- Sleep log save button triggers event.
- Bottom nav switches tabs.

Manual demo test:

1. Install app fresh.
2. Register new user.
3. Complete onboarding.
4. See empty home.
5. Create sleep session.
6. See populated home.
7. Open stats.
8. Open session detail.
9. Ask coach to analyze last night.
10. Update alarm.
11. Logout and sign in again.

## 19. Backend-Mobile Collaboration

Backend developer should provide:

- Swagger URL.
- Base URL.
- Demo account credentials.
- Seeded demo data.
- Exact enum values.
- Response examples.

Mobile developer should provide:

- Required endpoint list.
- Screens blocked by missing endpoints.
- Field mismatch reports.
- Device/emulator testing feedback.

Shared rule:

- Any API contract change should be updated in Swagger and communicated before mobile changes.

## 20. API Integration Order

Best order for Android:

1. `POST /auth/register`
2. `POST /auth/login`
3. `GET /auth/me`
4. `PUT /onboarding/me`
5. `GET /sleep-sessions/latest`
6. `POST /sleep-sessions`
7. `GET /sleep-sessions/:id`
8. `GET /analytics/summary`
9. `GET /analytics/sleep-hours`
10. `GET /analytics/quality`
11. `GET /alarm/me`
12. `PUT /alarm/me`
13. `GET /coach/messages`
14. `POST /coach/messages`
15. `POST /coach/analyze-last-night`

This order supports the demo flow early.

## 21. Recommended Branch Strategy

If using Git:

- `main`: stable demo-ready app.
- `mobile-architecture`: refactor work.
- `backend-integration`: API integration work.
- `ui-polish`: visual improvements.

Merge only when app builds and basic flow works.

## 22. Scope Control

Do not add these before backend integration works:

- Health Connect.
- Push notifications.
- Social login.
- Complex local sync engine.
- Offline conflict resolution.
- Multi-module Gradle setup.
- Complex animations everywhere.

Future-work features are fine for the report, but they should not block the final demo.

## 23. Definition Of Done

The mobile app is no longer just copied design when:

- Screens are split into feature packages.
- Navigation is real.
- ViewModels own state.
- Repositories own data access.
- Auth persists across app restart.
- Sleep logs are sent to backend.
- Dashboard is based on backend data.
- Analytics is based on backend data.
- Session detail is loaded by id.
- Coach messages are persisted.
- Loading and error states exist.
- The final demo flow works without editing code.

