package id.antasari.trackingmoney.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = BluePrimary,
    secondary = BlueLight,
    tertiary = BluePrimaryDark,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = LightText,
    onSecondary = LightText,
    onTertiary = LightSurface,
    onBackground = DarkText,
    onSurface = DarkText,
    surfaceVariant = Color(0xFF27272A), // Zinc 800
    onSurfaceVariant = Color(0xFFA1A1AA) // Zinc 400
)

private val LightColorScheme = lightColorScheme(
    primary = BluePrimary,
    secondary = BluePrimaryDark,
    tertiary = BlueLight,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = LightSurface,
    onSecondary = LightSurface,
    onTertiary = LightText,
    onBackground = LightText,
    onSurface = LightText,
    surfaceVariant = Color(0xFFF1F5F9), // Slate 100
    onSurfaceVariant = Color(0xFF334155) // Slate 700
)

@Composable
fun TrackingMoneyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}