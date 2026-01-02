package com.incremax.ui.screens.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.WorkoutPlan
import com.incremax.ui.screens.onboarding.components.PlanSelectionCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlanRecommendationScreen(
    recommendedPlans: List<WorkoutPlan>,
    otherPlans: List<WorkoutPlan>,
    selectedPlanIds: Set<String>,
    goalName: String,
    onTogglePlan: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    var showOtherPlans by remember { mutableStateOf(false) }
    val selectedCount = selectedPlanIds.size

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Top Bar
        TopAppBar(
            title = { },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            contentPadding = PaddingValues(horizontal = 24.dp)
        ) {
            item {
                Text(
                    text = "Recommended for you",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Based on your goal: $goalName",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Recommended Plans
            items(recommendedPlans) { plan ->
                PlanSelectionCard(
                    plan = plan,
                    isSelected = plan.id in selectedPlanIds,
                    onToggle = { onTogglePlan(plan.id) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Other Plans Section
            if (otherPlans.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))

                    TextButton(
                        onClick = { showOtherPlans = !showOtherPlans },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (showOtherPlans) "Hide other plans" else "Show other plans",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            if (showOtherPlans) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                item {
                    AnimatedVisibility(visible = showOtherPlans) {
                        Column {
                            otherPlans.forEach { plan ->
                                PlanSelectionCard(
                                    plan = plan,
                                    isSelected = plan.id in selectedPlanIds,
                                    onToggle = { onTogglePlan(plan.id) }
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // Bottom Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Button(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = selectedCount > 0
                ) {
                    Text(
                        text = if (selectedCount == 0) {
                            "Select at least one plan"
                        } else if (selectedCount == 1) {
                            "Continue with 1 plan"
                        } else {
                            "Continue with $selectedCount plans"
                        },
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
