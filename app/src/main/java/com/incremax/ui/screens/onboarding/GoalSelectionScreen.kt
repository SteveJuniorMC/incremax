package com.incremax.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.incremax.domain.model.FitnessGoal
import com.incremax.ui.screens.onboarding.components.GoalCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSelectionScreen(
    selectedGoal: FitnessGoal?,
    onGoalSelected: (FitnessGoal) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "What's your fitness goal?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "We'll recommend plans tailored to you",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 2x2 Grid of Goals
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoalCard(
                        goal = FitnessGoal.STRENGTH,
                        isSelected = selectedGoal == FitnessGoal.STRENGTH,
                        onClick = { onGoalSelected(FitnessGoal.STRENGTH) },
                        modifier = Modifier.weight(1f)
                    )
                    GoalCard(
                        goal = FitnessGoal.ENDURANCE,
                        isSelected = selectedGoal == FitnessGoal.ENDURANCE,
                        onClick = { onGoalSelected(FitnessGoal.ENDURANCE) },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    GoalCard(
                        goal = FitnessGoal.FLEXIBILITY,
                        isSelected = selectedGoal == FitnessGoal.FLEXIBILITY,
                        onClick = { onGoalSelected(FitnessGoal.FLEXIBILITY) },
                        modifier = Modifier.weight(1f)
                    )
                    GoalCard(
                        goal = FitnessGoal.GENERAL_FITNESS,
                        isSelected = selectedGoal == FitnessGoal.GENERAL_FITNESS,
                        onClick = { onGoalSelected(FitnessGoal.GENERAL_FITNESS) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Continue Button
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                enabled = selectedGoal != null
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
