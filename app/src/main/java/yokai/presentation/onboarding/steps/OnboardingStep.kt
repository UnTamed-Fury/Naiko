package yokai.presentation.onboarding.steps

import androidx.compose.runtime.Composable

internal interface OnboardingStep {

    val isComplete: Boolean

    @Composable
    fun Content()
}
