package com.example.eventra.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.eventra.viewmodels.data.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = EventraColors.PrimaryOrange,
    secondary = EventraColors.DarkOrange,
    tertiary = EventraColors.LightOrange,
    background = EventraColors.BackgroundGrayDark,
    surface = EventraColors.CardWhiteDark,
    onBackground = EventraColors.TextDarkDark,
    onSurface = EventraColors.TextDarkDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EventraColors.PrimaryOrange,
    secondary = EventraColors.DarkOrange,
    tertiary = EventraColors.LightOrange,
    background = EventraColors.BackgroundGray,
    surface = EventraColors.CardWhite,
    onBackground = EventraColors.TextDark,
    onSurface = EventraColors.TextDark,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onSecondary = androidx.compose.ui.graphics.Color.White
)

@Composable
fun EventraTheme(
    theme: AppTheme = AppTheme.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val systemInDarkTheme = isSystemInDarkTheme()

    val darkTheme = when (theme) {
        AppTheme.LIGHT -> false
        AppTheme.DARK -> true
        AppTheme.SYSTEM -> systemInDarkTheme
    }

    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val dynamicColorScheme = if (darkTheme) {
                dynamicDarkColorScheme(context)
            } else {
                dynamicLightColorScheme(context)
            }
            // Mantieni i colori primari custom
            dynamicColorScheme.copy(
                primary = EventraColors.PrimaryOrange,
                secondary = EventraColors.DarkOrange,
                tertiary = EventraColors.LightOrange
            )
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
