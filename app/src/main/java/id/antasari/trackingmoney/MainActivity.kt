package id.antasari.trackingmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import id.antasari.trackingmoney.data.preferences.ThemeMode
import id.antasari.trackingmoney.ui.navigation.AppNavigation
import id.antasari.trackingmoney.ui.theme.TrackingMoneyTheme
import id.antasari.trackingmoney.ui.viewmodel.SettingsViewModel
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import id.antasari.trackingmoney.worker.RecurringWorker
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        setupRecurringWorker()

        setContent {
            val settingsViewModel: SettingsViewModel = viewModel()
            val themeMode by settingsViewModel.themeMode.collectAsState()
            
            val darkTheme = when (themeMode) {
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
            }

            TrackingMoneyTheme(darkTheme = darkTheme) {
                AppNavigation()
            }
        }
    }

    private fun setupRecurringWorker() {
        val workRequest = PeriodicWorkRequestBuilder<RecurringWorker>(24, TimeUnit.HOURS)
            .build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "recurring_transactions_worker",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }
}