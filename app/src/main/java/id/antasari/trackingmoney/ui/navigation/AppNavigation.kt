package id.antasari.trackingmoney.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import id.antasari.trackingmoney.ui.screens.AddTransactionScreen
import id.antasari.trackingmoney.ui.screens.DashboardScreen

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
    }
}
