package com.example.eventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.eventra.screens.HomeScreen
import com.example.eventra.screens.SearchScreen
import com.example.eventra.ui.theme.EventraTheme

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
    val selectedIndex = remember { mutableIntStateOf(0) }

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
            }
        }
    }
}

@Composable
fun EventraBottomBar(selectedIndex: MutableState<Int>) {
    NavigationBar(
        containerColor = Color.Black.copy(alpha = 0.9f),
        contentColor = Color.White
    ) {
        // Home Tab
        NavigationBarItem(
            selected = selectedIndex.value == 0,
            onClick = { selectedIndex.value = 0 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 0) Icons.Filled.Home else Icons.Outlined.Home,
                    contentDescription = stringResource(R.string.nav_home),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_home),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A00FF),
                selectedTextColor = Color(0xFF6A00FF),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF6A00FF).copy(alpha = 0.2f)
            )
        )

        // Search Tab
        NavigationBarItem(
            selected = selectedIndex.value == 1,
            onClick = { selectedIndex.value = 1 },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 1) Icons.Filled.Search else Icons.Outlined.Search,
                    contentDescription = stringResource(R.string.nav_search),
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = stringResource(R.string.nav_search),
                    style = MaterialTheme.typography.labelSmall
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF6A00FF),
                selectedTextColor = Color(0xFF6A00FF),
                unselectedIconColor = Color.White.copy(alpha = 0.7f),
                unselectedTextColor = Color.White.copy(alpha = 0.7f),
                indicatorColor = Color(0xFF6A00FF).copy(alpha = 0.2f)
            )
        )
    }
}
