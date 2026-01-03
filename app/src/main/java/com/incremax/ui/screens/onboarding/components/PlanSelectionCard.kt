package com.incremax.ui.screens.onboarding.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.incremax.domain.model.WorkoutPlan
import com.incremax.ui.components.GamifiedPlanCard

@Composable
fun PlanSelectionCard(
    plan: WorkoutPlan,
    isSelected: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    GamifiedPlanCard(
        plan = plan,
        onClick = onToggle,
        modifier = modifier,
        isSelectable = true,
        isSelected = isSelected
    )
}
