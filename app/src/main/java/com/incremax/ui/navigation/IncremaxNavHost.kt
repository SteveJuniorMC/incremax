package com.incremax.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.incremax.ui.screens.achievements.AchievementsScreen
import com.incremax.ui.screens.home.HomeScreen
import com.incremax.ui.screens.plans.CreatePlanScreen
import com.incremax.ui.screens.plans.PlanDetailScreen
import com.incremax.ui.screens.plans.PlansScreen
import com.incremax.ui.screens.profile.ProfileScreen
import com.incremax.ui.screens.progress.ProgressScreen
import com.incremax.ui.screens.workout.WorkoutScreen

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
fun IncremaxNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

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
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
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
            startDestination = NavRoutes.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
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
                ProfileScreen()
            }
        }
    }
}
