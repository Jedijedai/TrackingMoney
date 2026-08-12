package id.antasari.trackingmoney.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import id.antasari.trackingmoney.ui.screens.AddTransactionScreen
import id.antasari.trackingmoney.ui.screens.DashboardScreen
import id.antasari.trackingmoney.ui.screens.SettingsScreen
import id.antasari.trackingmoney.ui.screens.CategoryManageScreen
import id.antasari.trackingmoney.ui.screens.SearchScreen
import id.antasari.trackingmoney.ui.screens.ProfileScreen
import id.antasari.trackingmoney.ui.screens.RecurringManageScreen
import id.antasari.trackingmoney.ui.screens.ReportsScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = "dashboard") {
        composable("dashboard") {
            DashboardScreen(
                onNavigateToAddTransaction = {
                    navController.navigate("add_transaction")
                },
                onNavigateToEditTransaction = { id ->
                    navController.navigate("add_transaction?id=$id")
                },
                onNavigateToSettings = {
                    navController.navigate("settings")
                },
                onNavigateToSearch = {
                    navController.navigate("search_transactions")
                },
                onNavigateToReports = {
                    navController.navigate("reports")
                }
            )
        }
        composable(
            route = "add_transaction?id={id}",
            arguments = listOf(navArgument("id") { type = NavType.StringType; nullable = true })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id")?.toIntOrNull()
            AddTransactionScreen(
                transactionId = id,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("settings") {
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToCategoryManage = { navController.navigate("categories_manage") },
                onNavigateToRecurringManage = { navController.navigate("recurring_manage") },
                onNavigateToProfile = { navController.navigate("profile") }
            )
        }
        composable("profile") {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("recurring_manage") {
            RecurringManageScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable("categories_manage") {
            CategoryManageScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
        composable("search_transactions") {
            SearchScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToEditTransaction = { id ->
                    navController.navigate("add_transaction?id=$id")
                }
            )
        }
        composable("reports") {
            ReportsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}
