package com.example.dreammind

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bedtime
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Timeline
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WbTwilight
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dreammind.core.designsystem.component.HeroIcon
import com.example.dreammind.feature.auth.AuthOnboardingViewModel
import com.example.dreammind.ui.theme.DeepNight
import com.example.dreammind.ui.theme.DreamAccent
import com.example.dreammind.ui.theme.DreamBorder
import com.example.dreammind.ui.theme.DreamCard
import com.example.dreammind.ui.theme.DreamCardAlt
import com.example.dreammind.ui.theme.DreamMuted
import com.example.dreammind.ui.theme.DreamPrimary
import com.example.dreammind.ui.theme.DreamPrimarySoft
import com.example.dreammind.ui.theme.DreamSurface
import com.example.dreammind.ui.theme.DreamText

enum class EntryStage {
    Welcome,
    CreateAccount,
    SignIn,
    Goal,
    Schedule,
    Sync,
    Main
}

private enum class SleepGoal(
    val title: String,
    val icon: ImageVector
) {
    FallAsleepFaster("Fall Asleep Faster", Icons.Rounded.Speed),
    WakeRefreshed("Wake Up Refreshed", Icons.Rounded.WbSunny),
    TrackHealth("Track Sleep Health", Icons.Rounded.Timeline)
}

@Composable
fun AuthOnboardingFlow(
    currentStage: EntryStage,
    onStageChange: (EntryStage) -> Unit,
    onFinish: (Boolean) -> Unit,
    viewModel: AuthOnboardingViewModel
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val selectedGoal = SleepGoal.valueOf(uiState.selectedGoalName)
    val bedtimeOptions = remember { listOf("10:00", "10:30", "11:00", "11:30") }
    val wakeOptions = remember { listOf("06:15", "06:45", "07:00", "07:30") }

    when (currentStage) {
        EntryStage.Welcome -> WelcomeScreen(
            onGetStarted = { onStageChange(EntryStage.CreateAccount) }
        )

        EntryStage.CreateAccount -> CreateAccountScreen(
            email = uiState.createEmail,
            password = uiState.createPassword,
            onEmailChange = viewModel::updateCreateEmail,
            onPasswordChange = viewModel::updateCreatePassword,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onGetStarted = { viewModel.register { onStageChange(EntryStage.Goal) } },
            onSignIn = { onStageChange(EntryStage.SignIn) },
            onGoogleClick = {
                viewModel.showUnavailable("Google sign-up is not configured for this final-year demo. Use email and password.")
            }
        )

        EntryStage.SignIn -> SignInScreen(
            email = uiState.signInEmail,
            password = uiState.signInPassword,
            onEmailChange = viewModel::updateSignInEmail,
            onPasswordChange = viewModel::updateSignInPassword,
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onSignIn = {
                viewModel.login { onboardingCompleted ->
                    if (onboardingCompleted) {
                        onFinish(true)
                    } else {
                        onStageChange(EntryStage.Goal)
                    }
                }
            },
            onCreateAccount = { onStageChange(EntryStage.CreateAccount) },
            onForgotPassword = {
                viewModel.showUnavailable("Password reset is not configured yet. Use the seeded demo account or create a new one.")
            },
            onGoogleClick = {
                viewModel.showUnavailable("Google sign-in is not configured for this final-year demo. Use email and password.")
            }
        )

        EntryStage.Goal -> GoalScreen(
            selectedGoal = selectedGoal,
            onGoalSelected = { viewModel.selectGoal(it.name) },
            onContinue = { onStageChange(EntryStage.Schedule) }
        )

        EntryStage.Schedule -> ScheduleScreen(
            bedtime = bedtimeOptions[uiState.bedtimeIndex],
            wakeTime = wakeOptions[uiState.wakeTimeIndex],
            onBedtimeTap = { viewModel.cycleBedtime(bedtimeOptions.size) },
            onWakeTimeTap = { viewModel.cycleWakeTime(wakeOptions.size) },
            onContinue = { onStageChange(EntryStage.Sync) }
        )

        EntryStage.Sync -> SyncScreen(
            isLoading = uiState.isLoading,
            errorMessage = uiState.errorMessage,
            onEnableSync = {
                viewModel.completeOnboarding(
                    bedtime = bedtimeOptions[uiState.bedtimeIndex],
                    wakeTime = wakeOptions[uiState.wakeTimeIndex],
                    syncEnabled = true
                ) {
                    onFinish(false)
                }
            },
            onSkip = {
                viewModel.completeOnboarding(
                    bedtime = bedtimeOptions[uiState.bedtimeIndex],
                    wakeTime = wakeOptions[uiState.wakeTimeIndex],
                    syncEnabled = false
                ) {
                    onFinish(false)
                }
            }
        )

        EntryStage.Main -> Unit
    }
}

@Composable
private fun WelcomeScreen(
    onGetStarted: () -> Unit
) {
    AuthStageLayout(
        bottomContent = {
            OnboardingPageDots(activeIndex = 0)
            Spacer(modifier = Modifier.height(32.dp))
            AuthPrimaryButton(
                label = "Get Started",
                onClick = onGetStarted
            )
        }
    ) {
        HeroIcon(icon = Icons.Rounded.DarkMode, accentIcon = Icons.Rounded.AutoAwesome, haloSize = 160.dp)
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Restorative Sleep",
            style = MaterialTheme.typography.headlineSmall,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Discover soothing nighttime routines and gentle metrics designed to guide you into deeper, more restful slumber.",
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun CreateAccountScreen(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onGetStarted: () -> Unit,
    onSignIn: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AuthStageLayout(
        topContent = {
            InfoTray(
                icon = Icons.Rounded.PersonAdd,
                text = "Join 10k+ users sleeping better",
                iconTint = DreamPrimarySoft
            )
        },
        bottomContent = {
            AuthTextLinkRow(
                prompt = "Already have an account?",
                action = "Sign In",
                onAction = onSignIn
            )
        }
    ) {
        AuthFormCard(
            heroIcon = Icons.Rounded.AutoAwesome,
            title = "Create Account",
            subtitle = "Start your journey to better rest"
        ) {
            AuthInputField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email Address",
                leadingIcon = Icons.Rounded.Email
            )
            Spacer(modifier = Modifier.height(14.dp))
            AuthInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Create Password",
                leadingIcon = Icons.Rounded.Key,
                trailingIcon = Icons.Rounded.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                }
            )
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = "By signing up, you agree to our Terms of Service and Privacy Policy.",
                style = MaterialTheme.typography.bodySmall,
                color = DreamMuted,
                textAlign = TextAlign.Start
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(14.dp))
                AuthErrorMessage(message = errorMessage)
            }
            Spacer(modifier = Modifier.height(24.dp))
            AuthPrimaryButton(
                label = if (isLoading) "Creating Account..." else "Get Started",
                enabled = !isLoading,
                onClick = onGetStarted
            )
            Spacer(modifier = Modifier.height(18.dp))
            DividerLabel(label = "or")
            Spacer(modifier = Modifier.height(18.dp))
            AuthSecondaryButton(
                label = "Sign up with Google",
                onClick = onGoogleClick
            )
        }
    }
}

@Composable
private fun SignInScreen(
    email: String,
    password: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    onSignIn: () -> Unit,
    onCreateAccount: () -> Unit,
    onForgotPassword: () -> Unit,
    onGoogleClick: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }

    AuthStageLayout(
        topContent = {
            InfoTray(
                icon = Icons.Rounded.Lock,
                text = "Your sleep data stays private and on-device",
                iconTint = DreamAccent
            )
        },
        bottomContent = {
            AuthTextLinkRow(
                prompt = "Don't have an account?",
                action = "Create Account",
                onAction = onCreateAccount
            )
        }
    ) {
        AuthFormCard(
            heroIcon = Icons.Rounded.Shield,
            title = "Welcome Back",
            subtitle = "Sign in to sync your sleep insights"
        ) {
            AuthInputField(
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Email Address",
                leadingIcon = Icons.Rounded.Email
            )
            Spacer(modifier = Modifier.height(14.dp))
            AuthInputField(
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Password",
                leadingIcon = Icons.Rounded.Key,
                trailingIcon = Icons.Rounded.VisibilityOff,
                onTrailingIconClick = { passwordVisible = !passwordVisible },
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                }
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "Forgot Password?",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onForgotPassword),
                style = MaterialTheme.typography.bodyMedium,
                color = DreamPrimarySoft,
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(18.dp))
            if (errorMessage != null) {
                AuthErrorMessage(message = errorMessage)
                Spacer(modifier = Modifier.height(18.dp))
            }
            AuthPrimaryButton(
                label = if (isLoading) "Signing In..." else "Sign In",
                enabled = !isLoading,
                onClick = onSignIn
            )
            Spacer(modifier = Modifier.height(18.dp))
            DividerLabel(label = "or")
            Spacer(modifier = Modifier.height(18.dp))
            AuthSecondaryButton(
                label = "Continue with Google",
                onClick = onGoogleClick
            )
        }
    }
}

@Composable
private fun GoalScreen(
    selectedGoal: SleepGoal,
    onGoalSelected: (SleepGoal) -> Unit,
    onContinue: () -> Unit
) {
    AuthStageLayout(
        bottomContent = {
            OnboardingPageDots(activeIndex = 1)
            Spacer(modifier = Modifier.height(32.dp))
            AuthPrimaryButton(
                label = "Continue",
                onClick = onContinue
            )
        }
    ) {
        HeroIcon(icon = Icons.Rounded.Timeline, accentIcon = Icons.Rounded.AutoAwesome, haloSize = 160.dp)
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "What is your goal?",
            style = MaterialTheme.typography.headlineSmall,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Select the primary reason for your restorative journey.",
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        SleepGoal.entries.forEach { goal ->
            GoalOptionCard(
                goal = goal,
                selected = goal == selectedGoal,
                onClick = { onGoalSelected(goal) }
            )
            Spacer(modifier = Modifier.height(14.dp))
        }
    }
}

@Composable
private fun ScheduleScreen(
    bedtime: String,
    wakeTime: String,
    onBedtimeTap: () -> Unit,
    onWakeTimeTap: () -> Unit,
    onContinue: () -> Unit
) {
    AuthStageLayout(
        bottomContent = {
            OnboardingPageDots(activeIndex = 2)
            Spacer(modifier = Modifier.height(32.dp))
            AuthPrimaryButton(
                label = "Continue",
                onClick = onContinue
            )
        }
    ) {
        HeroIcon(icon = Icons.Rounded.Alarm, accentIcon = Icons.Rounded.AutoAwesome, haloSize = 144.dp)
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Ideal Schedule",
            style = MaterialTheme.typography.headlineSmall,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Set your target sleep window to calibrate your smart alarm metrics.",
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(28.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            TimeCard(
                modifier = Modifier.weight(1f),
                title = "Bedtime",
                value = bedtime,
                meridiem = "PM",
                icon = Icons.Rounded.Bedtime,
                accent = DreamPrimarySoft,
                onTap = onBedtimeTap
            )
            TimeCard(
                modifier = Modifier.weight(1f),
                title = "Wake Up",
                value = wakeTime,
                meridiem = "AM",
                icon = Icons.Rounded.WbTwilight,
                accent = DreamAccent,
                onTap = onWakeTimeTap
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        InfoCapsule(text = "8h 15m total rest")
    }
}

@Composable
private fun SyncScreen(
    isLoading: Boolean,
    errorMessage: String?,
    onEnableSync: () -> Unit,
    onSkip: () -> Unit
) {
    AuthStageLayout(
        bottomContent = {
            OnboardingPageDots(activeIndex = 3)
            Spacer(modifier = Modifier.height(32.dp))
            AuthPrimaryButton(
                label = if (isLoading) "Saving..." else "Enable Sync",
                trailingIcon = Icons.Rounded.Sync,
                enabled = !isLoading,
                onClick = onEnableSync
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Skip for now",
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { if (!isLoading) onSkip() }
                    ),
                style = MaterialTheme.typography.bodyLarge,
                color = DreamMuted,
                textAlign = TextAlign.Center
            )
        }
    ) {
        HeroIcon(icon = Icons.Rounded.Shield, accentIcon = Icons.Rounded.AutoAwesome, haloSize = 160.dp)
        Spacer(modifier = Modifier.height(26.dp))
        Text(
            text = "Sync your vitals",
            style = MaterialTheme.typography.headlineSmall,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Connect your health data to allow our AI Coach to provide personalized insights and automate your sleep tracking.",
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(18.dp))
        InfoTray(
            icon = Icons.Rounded.Lock,
            text = "Privacy is our priority",
            iconTint = DreamAccent
        )
        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(18.dp))
            AuthErrorMessage(message = errorMessage)
        }
    }
}

@Composable
private fun AuthStageLayout(
    topContent: (@Composable () -> Unit)? = null,
    bottomContent: @Composable ColumnScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(top = 14.dp, bottom = 220.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (topContent != null) {
                topContent()
                Spacer(modifier = Modifier.height(22.dp))
            } else {
                Spacer(modifier = Modifier.height(34.dp))
            }

            content()
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color.Transparent, DeepNight.copy(alpha = 0.82f), DeepNight)
                    )
                )
                .navigationBarsPadding()
                .padding(bottom = 18.dp, top = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            content = bottomContent
        )
    }
}

@Composable
private fun AuthFormCard(
    heroIcon: ImageVector,
    title: String,
    subtitle: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        HeroIcon(icon = heroIcon, accentIcon = null, haloSize = 108.dp)
        Spacer(modifier = Modifier.height(22.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = DreamText,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(26.dp))
        content()
    }
}

@Composable
private fun InfoTray(
    icon: ImageVector,
    text: String,
    iconTint: Color
) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = DreamCardAlt.copy(alpha = 0.56f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamBorder.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = DreamText.copy(alpha = 0.92f)
            )
        }
    }
}

@Composable
private fun AuthInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    onTrailingIconClick: (() -> Unit)? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = DreamCardAlt.copy(alpha = 0.78f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamBorder.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = DreamMuted,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                visualTransformation = visualTransformation,
                textStyle = MaterialTheme.typography.bodyLarge.merge(TextStyle(color = DreamText)),
                modifier = Modifier.weight(1f),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = DreamMuted.copy(alpha = 0.6f)
                        )
                    }
                    innerTextField()
                }
            )
            if (trailingIcon != null) {
                Spacer(modifier = Modifier.width(12.dp))
                androidx.compose.material3.Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = DreamMuted.copy(alpha = 0.55f),
                    modifier = Modifier
                        .size(20.dp)
                        .then(
                            if (onTrailingIconClick != null) {
                                Modifier.clickable(onClick = onTrailingIconClick)
                            } else {
                                Modifier
                            }
                        )
                )
            }
        }
    }
}

@Composable
private fun DividerLabel(label: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DreamBorder.copy(alpha = 0.5f))
        )
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            color = DreamMuted
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .height(1.dp)
                .background(DreamBorder.copy(alpha = 0.5f))
        )
    }
}

@Composable
private fun AuthErrorMessage(message: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = DreamAccent.copy(alpha = 0.12f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamAccent.copy(alpha = 0.28f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = DreamText
        )
    }
}

@Composable
private fun AuthPrimaryButton(
    label: String,
    trailingIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowForward,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(enabled = enabled, onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            DreamPrimary.copy(alpha = if (enabled) 1f else 0.45f),
                            DreamPrimarySoft.copy(alpha = if (enabled) 1f else 0.45f)
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(vertical = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = DeepNight
                )
                androidx.compose.material3.Icon(
                    imageVector = trailingIcon,
                    contentDescription = null,
                    tint = DeepNight,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
private fun AuthSecondaryButton(
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(28.dp),
        color = Color.Transparent,
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamBorder.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .background(DreamPrimary.copy(alpha = 0.14f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = DreamPrimarySoft,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = DreamText
            )
        }
    }
}

@Composable
private fun AuthTextLinkRow(
    prompt: String,
    action: String,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prompt,
            style = MaterialTheme.typography.bodyLarge,
            color = DreamMuted
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = action,
            modifier = Modifier.clickable(onClick = onAction),
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
            color = DreamPrimarySoft
        )
    }
}

@Composable
private fun GoalOptionCard(
    goal: SleepGoal,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        color = if (selected) DreamCardAlt.copy(alpha = 0.94f) else DreamCard.copy(alpha = 0.82f),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (selected) DreamPrimary else DreamBorder.copy(alpha = 0.45f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        if (selected) DreamPrimary.copy(alpha = 0.16f) else DreamSurface.copy(alpha = 0.92f),
                        RoundedCornerShape(18.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.Icon(
                    imageVector = goal.icon,
                    contentDescription = null,
                    tint = if (selected) DreamPrimarySoft else DreamMuted
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = goal.title,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleMedium,
                color = DreamText
            )
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .border(
                        width = 2.dp,
                        color = if (selected) DreamPrimary else DreamBorder,
                        shape = CircleShape
                    )
                    .background(
                        color = if (selected) DreamPrimary else Color.Transparent,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = DeepNight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    meridiem: String,
    icon: ImageVector,
    accent: Color,
    onTap: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(28.dp))
            .clickable(onClick = onTap),
        shape = RoundedCornerShape(28.dp),
        color = DreamCard.copy(alpha = 0.9f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamBorder.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                androidx.compose.material3.Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DreamMuted,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelLarge,
                    color = DreamMuted
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )
            Text(
                text = meridiem,
                style = MaterialTheme.typography.labelLarge,
                color = DreamMuted
            )
            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .width(36.dp)
                    .height(3.dp)
                    .background(DreamBorder.copy(alpha = 0.7f), RoundedCornerShape(999.dp))
            )
        }
    }
}

@Composable
private fun InfoCapsule(text: String) {
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = DreamCardAlt.copy(alpha = 0.65f),
        border = androidx.compose.foundation.BorderStroke(1.dp, DreamBorder.copy(alpha = 0.32f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Icon(
                imageVector = Icons.Rounded.Alarm,
                contentDescription = null,
                tint = DreamPrimarySoft,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = DreamText
            )
        }
    }
}

@Composable
private fun OnboardingPageDots(activeIndex: Int) {
    val total = 4
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            Box(
                modifier = Modifier
                    .width(if (index == activeIndex) 28.dp else 8.dp)
                    .height(8.dp)
                    .background(
                        color = if (index == activeIndex) DreamPrimary else DreamBorder,
                        shape = RoundedCornerShape(999.dp)
                    )
            )
        }
    }
}
