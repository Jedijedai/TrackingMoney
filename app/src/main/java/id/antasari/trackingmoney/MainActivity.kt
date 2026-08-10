package id.antasari.trackingmoney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import id.antasari.trackingmoney.ui.screens.DashboardScreen
import id.antasari.trackingmoney.ui.theme.TrackingMoneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TrackingMoneyTheme {
                DashboardScreen()
            }
        }
    }
}