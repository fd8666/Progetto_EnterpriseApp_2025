package com.example.eventra

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventra.screens.EventraColors
import com.example.eventra.screens.HomeScreen
import com.example.eventra.screens.LoginScreen
import com.example.eventra.screens.SearchScreen
import com.example.eventra.screens.WishlistScreen
import com.example.eventra.screens.ProfileScreen
import com.example.eventra.ui.theme.EventraTheme
import com.example.eventra.viewmodels.LoginViewModel
import com.example.eventra.untils.SessionManager

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EventraTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    HomePage()
                }
            }
        }
    }
}

@Composable
fun HomePage() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val loginViewModel: LoginViewModel = viewModel {
        LoginViewModel(context.applicationContext as Application)
    }

    val selectedIndex = remember { mutableIntStateOf(0) }
    var isUserLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

    // Observer per lo stato del login
    val loginState by loginViewModel.loginState.collectAsState()

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                isUserLoggedIn = true
            }
            else -> {}
        }
    }

    Scaffold(
        bottomBar = {
            EventraBottomBar(selectedIndex = selectedIndex)
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex.intValue) {
                0 -> HomeScreen()
                1 -> SearchScreen()
                2 -> WishlistScreen()
                3 -> {
                    if (isUserLoggedIn) {
                        ProfileScreen(
                            onLogout = {
                                sessionManager.clearSession()
                                isUserLoggedIn = false
                            }
                        )
                    } else {
                        LoginScreen(
                            onLoginSuccess = {
                                isUserLoggedIn = true
                            },
                            viewModel = loginViewModel
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventraBottomBar(selectedIndex: MutableState<Int>) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = EventraColors.TextDark,
        tonalElevation = 8.dp
    ) {
        // Home Tab
        NavigationBarItem(
            selected = selectedIndex.value == 0,
            onClick = { selectedIndex.value = 0 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EventraColors.PrimaryOrange,
                selectedTextColor = EventraColors.PrimaryOrange,
                unselectedIconColor = EventraColors.TextGray,
                unselectedTextColor = EventraColors.TextGray,
                indicatorColor = EventraColors.LightOrange
            )
        )

        // Search Tab
        NavigationBarItem(
            selected = selectedIndex.value == 1,
            onClick = { selectedIndex.value = 1 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 1) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = "Cerca",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Cerca",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EventraColors.PrimaryOrange,
                selectedTextColor = EventraColors.PrimaryOrange,
                unselectedIconColor = EventraColors.TextGray,
                unselectedTextColor = EventraColors.TextGray,
                indicatorColor = EventraColors.LightOrange
            )
        )

        // Wishlist Tab
        NavigationBarItem(
            selected = selectedIndex.value == 2,
            onClick = { selectedIndex.value = 2 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 2) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Favorite",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Favorite",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EventraColors.PrimaryOrange,
                selectedTextColor = EventraColors.PrimaryOrange,
                unselectedIconColor = EventraColors.TextGray,
                unselectedTextColor = EventraColors.TextGray,
                indicatorColor = EventraColors.LightOrange
            )
        )

        // Account Tab
        NavigationBarItem(
            selected = selectedIndex.value == 3,
            onClick = { selectedIndex.value = 3 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 3) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
                    contentDescription = "Account",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Account",
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = EventraColors.PrimaryOrange,
                selectedTextColor = EventraColors.PrimaryOrange,
                unselectedIconColor = EventraColors.TextGray,
                unselectedTextColor = EventraColors.TextGray,
                indicatorColor = EventraColors.LightOrange
            )
        )
    }
}