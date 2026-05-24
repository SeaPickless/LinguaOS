package com.linguaos.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.linguaos.app.ui.auth.*
import com.linguaos.app.ui.dashboard.DashboardScreen
import com.linguaos.app.ui.flashcard.FlashCardScreen
import com.linguaos.app.ui.gamification.AchievementsScreen
import com.linguaos.app.ui.gamification.LeaderboardScreen
import com.linguaos.app.ui.onboarding.OnboardingScreen
import com.linguaos.app.ui.practice.PracticeScreen
import com.linguaos.app.ui.profile.ProfileScreen
import com.linguaos.app.ui.quiz.QuizScreen
import com.linguaos.app.ui.units.UnitListScreen

@Composable
fun LinguaNavHost(navController: NavHostController, startDestination: String) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            OnboardingScreen(onFinished = {
                navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
            })
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess       = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.LOGIN) { inclusive = true } } },
                onNavigateToRegister = { navController.navigate(Routes.REGISTER) },
                onNavigateToForgotPassword = { navController.navigate(Routes.FORGOT_PW) },
                onSwitchUser         = { navController.navigate(Routes.USER_SELECT) }
            )
        }

        composable(Routes.REGISTER) {
            RegisterScreen(
                onRegistered       = { navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.REGISTER) { inclusive = true } } },
                onNavigateToLogin  = { navController.popBackStack() }
            )
        }

        composable(Routes.FORGOT_PW) {
            ForgotPasswordScreen(onSuccess = { navController.popBackStack() }, onBack = { navController.popBackStack() })
        }

        composable(Routes.USER_SELECT) {
            UserSelectScreen(
                onUserSelected = { navController.navigate(Routes.DASHBOARD) { popUpTo(Routes.USER_SELECT) { inclusive = true } } },
                onBack         = { navController.popBackStack() }
            )
        }

        composable(Routes.DASHBOARD) {
            DashboardScreen(
                onNavigateToProfile    = { navController.navigate(Routes.PROFILE) },
                onNavigateToUnitList   = { courseId -> navController.navigate(Routes.unitList(courseId)) },
                onNavigateToLeaderboard= { navController.navigate(Routes.LEADERBOARD) }
            )
        }

        composable(Routes.PROFILE) {
            ProfileScreen(
                onBack    = { navController.popBackStack() },
                onLogout  = { navController.navigate(Routes.LOGIN) { popUpTo(0) { inclusive = true } } },
                onAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
            )
        }

        composable(
            route     = Routes.UNIT_LIST,
            arguments = listOf(navArgument("courseId") { type = NavType.StringType })
        ) {
            UnitListScreen(
                onBack      = { navController.popBackStack() },
                onFlashCards= { cid, unit -> navController.navigate(Routes.flashcards(cid, unit)) },
                onPractice  = { cid, unit -> navController.navigate(Routes.practice(cid, unit)) },
                onQuiz      = { cid, unit, mode -> navController.navigate(Routes.quiz(cid, unit, mode)) }
            )
        }

        composable(
            route     = Routes.FLASHCARDS,
            arguments = listOf(
                navArgument("courseId")   { type = NavType.StringType },
                navArgument("unitNumber") { type = NavType.IntType }
            )
        ) { FlashCardScreen(onBack = { navController.popBackStack() }) }

        composable(
            route     = Routes.PRACTICE,
            arguments = listOf(
                navArgument("courseId")   { type = NavType.StringType },
                navArgument("unitNumber") { type = NavType.IntType }
            )
        ) { PracticeScreen(onBack = { navController.popBackStack() }) }

        composable(
            route     = Routes.QUIZ,
            arguments = listOf(
                navArgument("courseId")   { type = NavType.StringType },
                navArgument("unitNumber") { type = NavType.IntType },
                navArgument("quizMode")   { type = NavType.StringType }
            )
        ) { QuizScreen(onBack = { navController.popBackStack() }) }

        composable(Routes.LEADERBOARD) {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.ACHIEVEMENTS) {
            AchievementsScreen(onBack = { navController.popBackStack() })
        }
    }
}
