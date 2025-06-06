package com.example.eventra

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge

import com.example.eventra.screens.HomeScreen
import com.example.eventra.screens.WishlistScreen
import com.example.eventra.ui.theme.EventraTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            EventraTheme {
                WishlistScreen()
            }
        }
    }
}


