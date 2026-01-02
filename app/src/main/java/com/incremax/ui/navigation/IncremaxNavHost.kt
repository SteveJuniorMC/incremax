package com.incremax.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.incremax.domain.repository.OnboardingRepository
import com.incremax.ui.screens.achievements.AchievementsScreen
import com.incremax.ui.screens.home.HomeScreen
import com.incremax.ui.screens.onboarding.OnboardingFlow
import com.incremax.ui.screens.plans.CreatePlanScreen
import com.incremax.ui.screens.plans.PlanDetailScreen
import com.incremax.ui.screens.plans.PlansScreen
import com.incremax.ui.screens.profile.ProfileScreen
import com.incremax.ui.screens.progress.ProgressScreen
import com.incremax.ui.screens.settings.NotificationSettingsScreen
import com.incremax.ui.screens.workout.WorkoutScreen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import javax.inject.Inject

@HiltViewModel
class NavHostViewModel @Inject constructor(
    onboardingRepository: OnboardingRepository
) : ViewModel() {
    val hasCompletedOnboarding = onboardingRepository.hasCompletedOnboarding()
}

data class BottomNavItemData(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItemData(NavRoutes.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    BottomNavItemData(NavRoutes.Plans.route, "Plans", Icons.Filled.FitnessCenter, Icons.Outlined.FitnessCenter),
    BottomNavItemData(NavRoutes.Progress.route, "Progress", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    BottomNavItemData(NavRoutes.Achievements.route, "Awards", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents),
    BottomNavItemData(NavRoutes.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.Person)
)

@Composable
fun IncremaxNavHost(
    viewModel: NavHostViewModel = hiltViewModel()
) {
    val hasCompletedOnboarding by viewModel.hasCompletedOnboarding.collectAsState(initial = null)

    // Show loading while checking onboarding status
    if (hasCompletedOnboarding == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val startDestination = if (hasCompletedOnboarding == true) {
        NavRoutes.Home.route
    } else {
        NavRoutes.Onboarding.route
    }

    val showBottomBar = currentDestination?.route in bottomNavItems.map { it.route }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.title
                                )
                            },
                            label = { Text(item.title) },
                            selected = selected,
                            onClick = {
                                if (item.route != currentDestination?.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = item.route != NavRoutes.Home.route
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Onboarding Flow
            composable(NavRoutes.Onboarding.route) {
                OnboardingFlow(
                    onComplete = {
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(NavRoutes.Home.route) {
                HomeScreen(
                    onStartWorkout = { planId ->
                        navController.navigate(NavRoutes.Workout.createRoute(planId))
                    },
                    onNavigateToPlans = {
                        navController.navigate(NavRoutes.Plans.route)
                    }
                )
            }

            composable(NavRoutes.Plans.route) {
                PlansScreen(
                    onPlanClick = { planId ->
                        navController.navigate(NavRoutes.PlanDetail.createRoute(planId))
                    },
                    onCreatePlan = {
                        navController.navigate(NavRoutes.CreatePlan.route)
                    }
                )
            }

            composable(
                route = NavRoutes.PlanDetail.route,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                PlanDetailScreen(
                    planId = planId,
                    onStartWorkout = {
                        navController.navigate(NavRoutes.Workout.createRoute(planId))
                    },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.CreatePlan.route) {
                CreatePlanScreen(
                    onPlanCreated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(
                route = NavRoutes.Workout.route,
                arguments = listOf(navArgument("planId") { type = NavType.StringType })
            ) { backStackEntry ->
                val planId = backStackEntry.arguments?.getString("planId") ?: return@composable
                WorkoutScreen(
                    planId = planId,
                    onWorkoutComplete = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Progress.route) {
                ProgressScreen()
            }

            composable(NavRoutes.Achievements.route) {
                AchievementsScreen()
            }

            composable(NavRoutes.Profile.route) {
                ProfileScreen(
                    onNotificationSettingsClick = {
                        navController.navigate(NavRoutes.NotificationSettings.route)
                    }
                )
            }

            composable(NavRoutes.NotificationSettings.route) {
                NotificationSettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
