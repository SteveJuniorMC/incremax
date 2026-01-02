package com.incremax.ui.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Plans : NavRoutes("plans")
    object PlanDetail : NavRoutes("plan/{planId}") {
        fun createRoute(planId: String) = "plan/$planId"
    }
    object CreatePlan : NavRoutes("create_plan")
    object Workout : NavRoutes("workout/{planId}") {
        fun createRoute(planId: String) = "workout/$planId"
    }
    object Progress : NavRoutes("progress")
    object Achievements : NavRoutes("achievements")
    object Profile : NavRoutes("profile")
    object NotificationSettings : NavRoutes("notification_settings")
    object Onboarding : NavRoutes("onboarding")
    object SignIn : NavRoutes("sign_in")
}

enum class BottomNavItem(
    val route: String,
    val title: String,
    val icon: String
) {
    HOME(NavRoutes.Home.route, "Home", "home"),
    PLANS(NavRoutes.Plans.route, "Plans", "list"),
    PROGRESS(NavRoutes.Progress.route, "Progress", "bar_chart"),
    ACHIEVEMENTS(NavRoutes.Achievements.route, "Awards", "emoji_events"),
    PROFILE(NavRoutes.Profile.route, "Profile", "person")
}
