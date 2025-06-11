package com.example.eventra

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
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
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.data.BigliettoData

enum class Screen {
    HOME,
    SEARCH,
    WISHLIST,
    PROFILE,
    LOGIN,
    BIGLIETTO,
    PAGAMENTO,
    PAYMENT_SUCCESS,
    EVENT_DETAIL
}

data class ScreenState(
    val screen: Screen,
    val eventoId: Long? = null,
    val biglietti: List<BigliettoData>? = null
)

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

    val wishlistViewModel: WishlistViewModel = viewModel {
        WishlistViewModel(context.applicationContext as Application)
    }

    val navigationStack = remember { mutableStateListOf<ScreenState>() }
    var currentBottomBarIndex by remember { mutableIntStateOf(0) }
    var isUserLoggedIn by remember { mutableStateOf(sessionManager.isLoggedIn()) }

    LaunchedEffect(Unit) {
        if (navigationStack.isEmpty()) {
            navigationStack.add(ScreenState(Screen.HOME))
        }
    }

    val loginState by loginViewModel.loginState.collectAsState()
    val eventi by eventiViewModel.eventi.collectAsState()

    LaunchedEffect(Unit) {
        eventiViewModel.getAllEventi()
    }

    fun navigateTo(screenState: ScreenState) {
        navigationStack.add(screenState)
    }

    fun navigateBack() {
        if (navigationStack.size > 1) {
            navigationStack.removeLastOrNull()
        }
    }

    fun navigateToHome() {
        navigationStack.clear()
        navigationStack.add(ScreenState(Screen.HOME))
        currentBottomBarIndex = 0
    }
    
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                isUserLoggedIn = true
                navigateToHome()
            }
            else -> {}
        }
    }
    fun navigateToBottomBarTab(index: Int) {
        val screen = when (index) {
            0 -> Screen.HOME
            1 -> Screen.SEARCH
            2 -> Screen.WISHLIST
            3 -> if (isUserLoggedIn) Screen.PROFILE else Screen.LOGIN
            else -> Screen.HOME
        }

        val currentScreen = navigationStack.lastOrNull()?.screen
        if (currentScreen in listOf(Screen.HOME, Screen.SEARCH, Screen.WISHLIST, Screen.PROFILE, Screen.LOGIN)) {
            navigationStack.removeLastOrNull()
        }

        navigationStack.add(ScreenState(screen))
        currentBottomBarIndex = index
    }

    fun navigateToEventDetail(eventoId: Long) {
        navigateTo(ScreenState(Screen.EVENT_DETAIL, eventoId = eventoId))
    }

    fun navigateToBiglietto(eventoId: Long) {
        navigateTo(ScreenState(Screen.BIGLIETTO, eventoId = eventoId))
    }

    fun navigateToPayment(biglietti: List<BigliettoData>) {
        navigateTo(ScreenState(Screen.PAGAMENTO, biglietti = biglietti))
    }

    fun navigateToPaymentSuccess() {
        navigateTo(ScreenState(Screen.PAYMENT_SUCCESS))
    }

    val currentScreenState = navigationStack.lastOrNull() ?: ScreenState(Screen.HOME)
    val currentScreen = currentScreenState.screen

    val showBottomBar = when (currentScreen) {
        Screen.HOME, Screen.SEARCH, Screen.WISHLIST, Screen.PROFILE, Screen.LOGIN -> true
        else -> false
    }

    LaunchedEffect(currentScreen) {
        when (currentScreen) {
            Screen.HOME -> currentBottomBarIndex = 0
            Screen.SEARCH -> currentBottomBarIndex = 1
            Screen.WISHLIST -> currentBottomBarIndex = 2
            Screen.PROFILE, Screen.LOGIN -> currentBottomBarIndex = 3
            else -> { }
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                EventraBottomBar(
                    selectedIndex = currentBottomBarIndex,
                    onTabSelected = { index ->
                        navigateToBottomBarTab(index)
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
                        onNavigateToEventDetail = { eventoId ->
                            navigateToEventDetail(eventoId)
                        }
                    )
                }

                Screen.EVENT_DETAIL -> {
                    EventDetailScreen(
                        eventoId = currentScreenState.eventoId ?: 0L,
                        onBackPressed = {
                            navigateBack()
                        },
                        onNavigateToBiglietto = { eventoId ->
                            navigateToBiglietto(eventoId)
                        },
                        wishlistViewModel = if (isUserLoggedIn) wishlistViewModel else null
                    )
                }

                Screen.SEARCH -> {
                    SearchScreen(
                        onNavigateToBiglietto = { eventoId ->
                            navigateToBiglietto(eventoId)
                        },
                        onNavigateToEventDetail = { eventoId ->
                            navigateToEventDetail(eventoId)
                        }
                    )
                }

                Screen.WISHLIST -> {
                    WishlistScreen(
                        onNavigateToEventDetail = { eventoId ->
                            navigateToEventDetail(eventoId)
                        }
                    )
                }

                Screen.PROFILE -> {
                    ProfileScreen(
                        onLogout = {
                            sessionManager.clearSession()
                            isUserLoggedIn = false
                            navigationStack.clear()
                            navigationStack.add(ScreenState(Screen.LOGIN))
                            currentBottomBarIndex = 3
                        }
                    )
                }

                Screen.LOGIN -> {
                    LoginScreen(
                        onLoginSuccess = {
                            isUserLoggedIn = true
                            navigateToHome()
                        },
                        viewModel = loginViewModel
                    )
                }

                Screen.BIGLIETTO -> {
                    BigliettoScreen(
                        eventoId = currentScreenState.eventoId ?: 0L,
                        onNavigateToEventDetail = {

                            navigateBack()
                        },
                        onNavigateToPayment = { biglietti ->
                            navigateToPayment(biglietti)
                        }
                    )
                }

                Screen.PAGAMENTO -> {
                    PagamentoScreen(
                        biglietti = currentScreenState.biglietti ?: emptyList(),
                        onPaymentSuccess = {
                            navigateToPaymentSuccess()
                        },
                        onBackPressed = {
                            navigateBack()
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
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        contentColor = EventraColors.TextDark,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = selectedIndex == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (selectedIndex == 0) Icons.Filled.Home else Icons.Outlined.Home,
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

        NavigationBarItem(
            selected = selectedIndex == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (selectedIndex == 1) Icons.Filled.Search else Icons.Outlined.Search,
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

        NavigationBarItem(
            selected = selectedIndex == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (selectedIndex == 2) Icons.Filled.Favorite else Icons.Outlined.Favorite,
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

        NavigationBarItem(
            selected = selectedIndex == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    imageVector = if (selectedIndex == 3) Icons.Filled.AccountCircle else Icons.Outlined.AccountCircle,
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