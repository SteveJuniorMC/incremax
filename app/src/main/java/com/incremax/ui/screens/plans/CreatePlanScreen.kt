package com.incremax.ui.screens.plans

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.incremax.domain.model.*
import com.incremax.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

data class CreatePlanUiState(
    val exercises: List<Exercise> = emptyList(),
    val selectedExercise: Exercise? = null,
    val planName: String = "",
    val description: String = "",
    val startingAmount: String = "",
    val targetAmount: String = "",
    val incrementAmount: String = "",
    val incrementFrequency: IncrementFrequency = IncrementFrequency.WEEKLY,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CreatePlanViewModel @Inject constructor(
    private val workoutPlanRepository: WorkoutPlanRepository,
    private val exerciseRepository: ExerciseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CreatePlanUiState())
    val uiState: StateFlow<CreatePlanUiState> = _uiState.asStateFlow()

    private val _planCreated = MutableSharedFlow<Unit>()
    val planCreated: SharedFlow<Unit> = _planCreated.asSharedFlow()

    init {
        loadExercises()
    }

    private fun loadExercises() {
        viewModelScope.launch {
            exerciseRepository.getAllExercises().collect { exercises ->
                _uiState.update {
                    it.copy(
                        exercises = exercises,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun setExercise(exercise: Exercise) {
        _uiState.update { it.copy(selectedExercise = exercise) }
    }

    fun setPlanName(name: String) {
        _uiState.update { it.copy(planName = name) }
    }

    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun setStartingAmount(amount: String) {
        _uiState.update { it.copy(startingAmount = amount) }
    }

    fun setTargetAmount(amount: String) {
        _uiState.update { it.copy(targetAmount = amount) }
    }

    fun setIncrementAmount(amount: String) {
        _uiState.update { it.copy(incrementAmount = amount) }
    }

    fun setIncrementFrequency(frequency: IncrementFrequency) {
        _uiState.update { it.copy(incrementFrequency = frequency) }
    }

    fun createPlan() {
        val state = _uiState.value

        // Validation
        if (state.selectedExercise == null) {
            _uiState.update { it.copy(error = "Please select an exercise") }
            return
        }
        if (state.planName.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a plan name") }
            return
        }
        val startingAmount = state.startingAmount.toIntOrNull()
        if (startingAmount == null || startingAmount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid starting amount") }
            return
        }
        val targetAmount = state.targetAmount.toIntOrNull()
        if (targetAmount == null || targetAmount <= startingAmount) {
            _uiState.update { it.copy(error = "Target must be greater than starting amount") }
            return
        }
        val incrementAmount = state.incrementAmount.toIntOrNull()
        if (incrementAmount == null || incrementAmount <= 0) {
            _uiState.update { it.copy(error = "Please enter a valid increment amount") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }

            val plan = WorkoutPlan(
                id = UUID.randomUUID().toString(),
                name = state.planName,
                description = state.description,
                exerciseId = state.selectedExercise.id,
                startingAmount = startingAmount,
                targetAmount = targetAmount,
                incrementAmount = incrementAmount,
                incrementFrequency = state.incrementFrequency,
                startDate = LocalDate.now(),
                isActive = true,
                isPreset = false
            )

            workoutPlanRepository.insertPlan(plan)
            _uiState.update { it.copy(isSaving = false) }
            _planCreated.emit(Unit)
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatePlanScreen(
    onPlanCreated: () -> Unit,
    onBack: () -> Unit,
    viewModel: CreatePlanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showExerciseDropdown by remember { mutableStateOf(false) }
    var showFrequencyDropdown by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.planCreated.collect {
            onPlanCreated()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Plan") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.createPlan() },
                        enabled = !uiState.isSaving
                    ) {
                        if (uiState.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("Create")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Error message
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(onClick = { viewModel.clearError() }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Dismiss",
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                }
            }

            // Exercise Selector
            item {
                Text(
                    text = "Exercise",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showExerciseDropdown,
                    onExpandedChange = { showExerciseDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.selectedExercise?.name ?: "",
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Select an exercise") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showExerciseDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showExerciseDropdown,
                        onDismissRequest = { showExerciseDropdown = false }
                    ) {
                        uiState.exercises.forEach { exercise ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(exercise.name)
                                        Text(
                                            text = exercise.category.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.setExercise(exercise)
                                    showExerciseDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Plan Name
            item {
                OutlinedTextField(
                    value = uiState.planName,
                    onValueChange = { viewModel.setPlanName(it) },
                    label = { Text("Plan Name") },
                    placeholder = { Text("e.g., My Push-up Challenge") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Description
            item {
                OutlinedTextField(
                    value = uiState.description,
                    onValueChange = { viewModel.setDescription(it) },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("Describe your goal...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }

            // Starting Amount
            item {
                OutlinedTextField(
                    value = uiState.startingAmount,
                    onValueChange = { viewModel.setStartingAmount(it) },
                    label = { Text("Starting Amount") },
                    placeholder = { Text("e.g., 10") },
                    suffix = { Text(uiState.selectedExercise?.unit ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Target Amount
            item {
                OutlinedTextField(
                    value = uiState.targetAmount,
                    onValueChange = { viewModel.setTargetAmount(it) },
                    label = { Text("Target Amount") },
                    placeholder = { Text("e.g., 100") },
                    suffix = { Text(uiState.selectedExercise?.unit ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Increment Amount
            item {
                OutlinedTextField(
                    value = uiState.incrementAmount,
                    onValueChange = { viewModel.setIncrementAmount(it) },
                    label = { Text("Increment Amount") },
                    placeholder = { Text("e.g., 2") },
                    prefix = { Text("+") },
                    suffix = { Text(uiState.selectedExercise?.unit ?: "") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }

            // Increment Frequency
            item {
                Text(
                    text = "Increase Every",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                ExposedDropdownMenuBox(
                    expanded = showFrequencyDropdown,
                    onExpandedChange = { showFrequencyDropdown = it }
                ) {
                    OutlinedTextField(
                        value = uiState.incrementFrequency.name.lowercase().replaceFirstChar { it.uppercase() },
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showFrequencyDropdown)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showFrequencyDropdown,
                        onDismissRequest = { showFrequencyDropdown = false }
                    ) {
                        IncrementFrequency.entries.forEach { frequency ->
                            DropdownMenuItem(
                                text = { Text(frequency.name.lowercase().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    viewModel.setIncrementFrequency(frequency)
                                    showFrequencyDropdown = false
                                }
                            )
                        }
                    }
                }
            }

            // Preview
            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (uiState.startingAmount.isNotBlank() && uiState.targetAmount.isNotBlank() && uiState.incrementAmount.isNotBlank()) {
                    val start = uiState.startingAmount.toIntOrNull() ?: 0
                    val target = uiState.targetAmount.toIntOrNull() ?: 0
                    val increment = uiState.incrementAmount.toIntOrNull() ?: 0

                    if (start > 0 && target > start && increment > 0) {
                        val periods = ((target - start) + increment - 1) / increment
                        val days = when (uiState.incrementFrequency) {
                            IncrementFrequency.DAILY -> periods
                            IncrementFrequency.WEEKLY -> periods * 7
                            IncrementFrequency.BIWEEKLY -> periods * 14
                            IncrementFrequency.MONTHLY -> periods * 30
                        }

                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = "Plan Preview",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "You'll reach your goal of $target ${uiState.selectedExercise?.unit ?: "units"} in approximately $days days",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }

            // Bottom spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}
