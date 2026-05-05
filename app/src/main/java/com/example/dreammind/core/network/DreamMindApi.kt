package com.example.dreammind.core.network

import com.example.dreammind.core.network.dto.AuthResponse
import com.example.dreammind.core.network.dto.AlarmResponse
import com.example.dreammind.core.network.dto.AnalyzeLastNightResponse
import com.example.dreammind.core.network.dto.AnalyticsSummaryResponse
import com.example.dreammind.core.network.dto.CoachExchangeResponse
import com.example.dreammind.core.network.dto.CoachMessagesResponse
import com.example.dreammind.core.network.dto.CreateSleepSessionRequest
import com.example.dreammind.core.network.dto.LoginRequest
import com.example.dreammind.core.network.dto.MeResponse
import com.example.dreammind.core.network.dto.OnboardingResponse
import com.example.dreammind.core.network.dto.QualityResponse
import com.example.dreammind.core.network.dto.RefreshTokenRequest
import com.example.dreammind.core.network.dto.RefreshTokenResponse
import com.example.dreammind.core.network.dto.RegisterRequest
import com.example.dreammind.core.network.dto.SleepHoursResponse
import com.example.dreammind.core.network.dto.SleepSessionDetailResponse
import com.example.dreammind.core.network.dto.SleepSessionSummaryResponse
import com.example.dreammind.core.network.dto.SendCoachMessageRequest
import com.example.dreammind.core.network.dto.UpdateAlarmRequest
import com.example.dreammind.core.network.dto.UpdateOnboardingRequest
import com.example.dreammind.core.network.dto.UpdateProfileRequest
import com.example.dreammind.core.network.dto.ProfileResponse
import com.google.gson.JsonObject
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Query

interface DreamMindApi {
    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): MeResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshTokenRequest): RefreshTokenResponse

    @GET("profile/me")
    suspend fun profile(): ProfileResponse

    @PUT("profile/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): ProfileResponse

    @GET("onboarding/me")
    suspend fun onboarding(): OnboardingResponse

    @PUT("onboarding/me")
    suspend fun updateOnboarding(@Body body: UpdateOnboardingRequest): OnboardingResponse

    @POST("sleep-sessions")
    suspend fun createSleepSession(@Body body: CreateSleepSessionRequest): SleepSessionSummaryResponse

    @GET("sleep-sessions/latest")
    suspend fun latestSleepSession(): JsonObject

    @GET("sleep-sessions/{id}")
    suspend fun sleepSessionDetail(@Path("id") id: String): SleepSessionDetailResponse

    @GET("alarm/me")
    suspend fun getAlarm(): AlarmResponse

    @PUT("alarm/me")
    suspend fun updateAlarm(@Body body: UpdateAlarmRequest): AlarmResponse

    @GET("analytics/summary")
    suspend fun analyticsSummary(@Query("range") range: String): AnalyticsSummaryResponse

    @GET("analytics/sleep-hours")
    suspend fun sleepHours(@Query("range") range: String): SleepHoursResponse

    @GET("analytics/quality")
    suspend fun quality(@Query("range") range: String): QualityResponse

    @GET("coach/messages")
    suspend fun coachMessages(): CoachMessagesResponse

    @POST("coach/messages")
    suspend fun sendCoachMessage(@Body body: SendCoachMessageRequest): CoachExchangeResponse

    @POST("coach/analyze-last-night")
    suspend fun analyzeLastNight(): AnalyzeLastNightResponse
}
