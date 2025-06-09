package com.example.eventra

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventra.screens.*
import com.example.eventra.ui.theme.EventraTheme
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.LoginViewModel
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.BigliettoData

// Enum per la navigazione
enum class Screen {
    HOME,
    SEARCH,
    WISHLIST,
    PROFILE,
    LOGIN,
    BIGLIETTO,
    PAGAMENTO,
    PAYMENT_SUCCESS
}

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

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as Application)
    }

    val selectedIndex = remember { mutableIntStateOf(0) }
    var isUserLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedEventoId by remember { mutableLongStateOf(0L) }
    var bigliettiForPayment by remember { mutableStateOf<List<BigliettoData>>(emptyList()) }

    // Observer per lo stato del login
    val loginState by loginViewModel.loginState.collectAsState()
    val eventi by eventiViewModel.eventi.collectAsState()

    // Carica eventi all'avvio
    LaunchedEffect(Unit) {
        eventiViewModel.getAllEventi()
    }

    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                isUserLoggedIn = true
                currentScreen = Screen.HOME
                selectedIndex.intValue = 0
            }
            else -> {}
        }
    }

    // Funzioni di navigazione
    fun navigateToHome() {
        currentScreen = Screen.HOME
        selectedIndex.intValue = 0
    }

    fun navigateToBiglietto(eventoId: Long = 1L) {
        selectedEventoId = eventoId
        currentScreen = Screen.BIGLIETTO
    }

    fun navigateToPayment(biglietti: List<BigliettoData>) {
        bigliettiForPayment = biglietti
        currentScreen = Screen.PAGAMENTO
    }

    fun navigateToPaymentSuccess() {
        currentScreen = Screen.PAYMENT_SUCCESS
    }

    fun navigateBackFromBiglietto() {
        currentScreen = Screen.HOME
        selectedIndex.intValue = 0
    }

    fun navigateBackFromPayment() {
        currentScreen = Screen.BIGLIETTO
    }

    // Mostra/nascondi bottom bar in base allo screen corrente
    val showBottomBar = when (currentScreen) {
        Screen.HOME, Screen.SEARCH, Screen.WISHLIST, Screen.PROFILE, Screen.LOGIN -> true
        else -> false
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                EventraBottomBar(
                    selectedIndex = selectedIndex,
                    onTabSelected = { index ->
                        selectedIndex.intValue = index
                        currentScreen = when (index) {
                            0 -> Screen.HOME
                            1 -> Screen.SEARCH
                            2 -> Screen.BIGLIETTO
                            3 -> if (isUserLoggedIn) Screen.PROFILE else Screen.LOGIN
                            else -> Screen.HOME
                        }
                    }
                )
            }
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (showBottomBar) paddingValues else PaddingValues(0.dp))
        ) {
            when (currentScreen) {
                Screen.HOME -> {
                    HomeScreen(
                        onNavigateToBiglietto = { eventoId ->
                            navigateToBiglietto(eventoId)
                        }
                    )
                }
                Screen.SEARCH -> {
                    SearchScreen(
                        onNavigateToBiglietto = { eventoId ->
                            navigateToBiglietto(eventoId)
                        }
                    )
                }
                Screen.WISHLIST -> {
                    WishlistScreen()
                }
                Screen.PROFILE -> {
                    ProfileScreen(
                        onLogout = {
                            sessionManager.clearSession()
                            isUserLoggedIn = false
                            currentScreen = Screen.LOGIN
                        }
                    )
                }
                Screen.LOGIN -> {
                    LoginScreen(
                        onLoginSuccess = {
                            isUserLoggedIn = true
                            currentScreen = Screen.HOME
                            selectedIndex.intValue = 0
                        },
                        viewModel = loginViewModel
                    )
                }
                Screen.BIGLIETTO -> {
                    BigliettoScreen(
                        eventoId = selectedEventoId,
                        onNavigateBack = {
                            navigateBackFromBiglietto()
                        },
                        onNavigateToPayment = { biglietti ->
                            navigateToPayment(biglietti)
                        }
                    )
                }
                Screen.PAGAMENTO -> {
                    PagamentoScreen(
                        biglietti = bigliettiForPayment,
                        onPaymentSuccess = {
                            navigateToPaymentSuccess()
                        },
                        onBackPressed = {
                            navigateBackFromPayment()
                        }
                    )
                }
                Screen.PAYMENT_SUCCESS -> {
                    PaymentSuccessScreen(
                        onNavigateToHome = {
                            navigateToHome()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun EventraBottomBar(
    selectedIndex: MutableState<Int>,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = EventraColors.TextDark,
        tonalElevation = 8.dp
    ) {
        // Home Tab
        NavigationBarItem(
            selected = selectedIndex.value == 0,
            onClick = { onTabSelected(0) },
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
            onClick = { onTabSelected(1) },
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
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (selectedIndex.value == 2) Icons.Filled.Favorite else Icons.Outlined.Favorite,
                    contentDescription = "Wishlist",
                    modifier = Modifier.size(24.dp)
                )
            },
            label = {
                Text(
                    text = "Wishlist",
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
            onClick = { onTabSelected(3) },
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

// Screen di successo pagamento
@Composable
fun PaymentSuccessScreen(
    onNavigateToHome: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Successo",
            tint = Color.Green,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Pagamento Completato!",
            style = MaterialTheme.typography.headlineMedium,
            color = EventraColors.TextDark
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "I tuoi biglietti sono stati acquistati con successo.\nRiceverai una conferma via email.",
            style = MaterialTheme.typography.bodyLarge,
            color = EventraColors.TextGray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onNavigateToHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = EventraColors.PrimaryOrange
            )
        ) {
            Text(
                text = "Torna alla Home",
                color = Color.White,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

