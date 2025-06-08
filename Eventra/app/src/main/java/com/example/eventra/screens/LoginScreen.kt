package com.example.eventra.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.delay
import com.example.eventra.R
import com.example.eventra.viewmodels.LoginViewModel

// Colori per il tema di login
object EventraLoginColors {
    val PrimaryBlue = Color(0xFF1E3A8A) // Blu principale
    val SecondaryBlue = Color(0xFF3B82F6) // Blu secondario
    val AccentBlue = Color(0xFF60A5FA) // Blu accento
    val LightBlue = Color(0xFFDBEAFE) // Blu chiaro
    val BackgroundLight = Color(0xFFF8FAFC) // Sfondo chiaro
    val CardWhite = Color(0xFFFFFFFF) // Bianco card
    val TextDark = Color(0xFF1F2937) // Testo scuro
    val TextGray = Color(0xFF6B7280) // Testo grigio
    val BorderGray = Color(0xFFD1D5DB) // Bordi grigi
    val SuccessGreen = Color(0xFF10B981) // Verde successo
    val ErrorRed = Color(0xFFEF4444) // Rosso errore
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val loginState by viewModel.loginState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }

    // Campi per il login
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Campi aggiuntivi per la registrazione
    var nome by remember { mutableStateOf("") }
    var cognome by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }

    // Animazioni
    var logoScale by remember { mutableStateOf(0.8f) }
    var cardAlpha by remember { mutableStateOf(0f) }

    // Google Sign In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            // Integrazione con backend OAuth
            onLoginSuccess()
        } catch (e: ApiException) {
            // Gestione errore
        }
    }

    // Animazioni di ingresso
    LaunchedEffect(Unit) {
        logoScale = 1f
        delay(300)
        cardAlpha = 1f
    }

    // Observer per lo stato del login
    LaunchedEffect(loginState) {
        when (loginState) {
            is LoginViewModel.LoginState.Success -> {
                onLoginSuccess()
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EventraLoginColors.PrimaryBlue,
                        EventraLoginColors.SecondaryBlue,
                        EventraLoginColors.AccentBlue
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            // Logo Header
            EventraLoginHeader(
                scale = logoScale,
                isLoginMode = isLoginMode
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Card di Login/Registrazione
            EventraLoginCard(
                alpha = cardAlpha,
                isLoginMode = isLoginMode,
                isLoading = isLoading,
                loginState = loginState,
                email = email,
                password = password,
                passwordVisible = passwordVisible,
                nome = nome,
                cognome = cognome,
                telefono = telefono,
                onEmailChange = { email = it },
                onPasswordChange = { password = it },
                onPasswordVisibilityToggle = { passwordVisible = !passwordVisible },
                onNomeChange = { nome = it },
                onCognomeChange = { cognome = it },
                onTelefonoChange = { telefono = it },
                onLoginClick = { viewModel.login(email, password) },
                onRegisterClick = { viewModel.register(nome, cognome, email, password, telefono) },
                onGoogleSignIn = {
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken(context.getString(R.string.web_client_id))
                        .requestEmail()
                        .build()

                    val googleSignInClient = GoogleSignIn.getClient(context as Activity, gso)
                    val signInIntent = googleSignInClient.signInIntent
                    googleSignInLauncher.launch(signInIntent)
                },
                onModeSwitch = {
                    isLoginMode = !isLoginMode
                    viewModel.clearError()
                }
            )
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EventraLoadingIndicator()
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EventraLoginHeader(
    scale: Float,
    isLoginMode: Boolean
) {
    val animatedScale by animateFloatAsState(
        targetValue = scale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.scale(animatedScale)
    ) {
        // Logo
        Card(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            colors = CardDefaults.cardColors(containerColor = EventraLoginColors.CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "Logo Eventra",
                    modifier = Modifier.size(60.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Titolo animato
        AnimatedContent(
            targetState = isLoginMode,
            transitionSpec = {
                slideInVertically { it } + fadeIn() with
                        slideOutVertically { -it } + fadeOut()
            }
        ) { loginMode ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (loginMode) "Bentornato!" else "Crea Account",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (loginMode) "Accedi al tuo account" else "Unisciti alla community",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Normal,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EventraLoginCard(
    alpha: Float,
    isLoginMode: Boolean,
    isLoading: Boolean,
    loginState: LoginViewModel.LoginState,
    email: String,
    password: String,
    passwordVisible: Boolean,
    nome: String,
    cognome: String,
    telefono: String,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit,
    onNomeChange: (String) -> Unit,
    onCognomeChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onGoogleSignIn: () -> Unit,
    onModeSwitch: () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = alpha,
        animationSpec = tween(600)
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { this.alpha = animatedAlpha },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = EventraLoginColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Campi Input animati
            AnimatedContent(
                targetState = isLoginMode,
                transitionSpec = {
                    slideInVertically { if (targetState) -it else it } + fadeIn() with
                            slideOutVertically { if (targetState) it else -it } + fadeOut()
                }
            ) { loginMode ->
                Column {
                    if (!loginMode) {
                        // Campi registrazione
                        EventraRegistrationFields(
                            nome = nome,
                            cognome = cognome,
                            telefono = telefono,
                            onNomeChange = onNomeChange,
                            onCognomeChange = onCognomeChange,
                            onTelefonoChange = onTelefonoChange
                        )
                    }

                    // Campi comuni
                    EventraLoginFields(
                        email = email,
                        password = password,
                        passwordVisible = passwordVisible,
                        onEmailChange = onEmailChange,
                        onPasswordChange = onPasswordChange,
                        onPasswordVisibilityToggle = onPasswordVisibilityToggle
                    )
                }
            }

            // Messaggio di errore
            EventraErrorMessage(loginState = loginState)

            Spacer(modifier = Modifier.height(32.dp))

            // Pulsante principale
            EventraMainButton(
                isLoginMode = isLoginMode,
                isLoading = isLoading,
                onLoginClick = onLoginClick,
                onRegisterClick = onRegisterClick
            )

            // Divider
            EventraDivider()

            // Google Sign In
            EventraGoogleButton(onGoogleSignIn = onGoogleSignIn)

            // Switch mode
            EventraSwitchModeButton(
                isLoginMode = isLoginMode,
                onModeSwitch = onModeSwitch
            )
        }
    }
}

@Composable
fun EventraRegistrationFields(
    nome: String,
    cognome: String,
    telefono: String,
    onNomeChange: (String) -> Unit,
    onCognomeChange: (String) -> Unit,
    onTelefonoChange: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EventraTextField(
            value = nome,
            onValueChange = onNomeChange,
            label = "Nome",
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )

        EventraTextField(
            value = cognome,
            onValueChange = onCognomeChange,
            label = "Cognome",
            icon = Icons.Default.Person,
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    EventraTextField(
        value = telefono,
        onValueChange = onTelefonoChange,
        label = "Telefono (opzionale)",
        icon = Icons.Default.Phone,
        keyboardType = KeyboardType.Phone
    )

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun EventraLoginFields(
    email: String,
    password: String,
    passwordVisible: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit
) {
    EventraTextField(
        value = email,
        onValueChange = onEmailChange,
        label = "Email",
        icon = Icons.Default.Email,
        keyboardType = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(16.dp))

    EventraPasswordField(
        value = password,
        onValueChange = onPasswordChange,
        passwordVisible = passwordVisible,
        onPasswordVisibilityToggle = onPasswordVisibilityToggle
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventraTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EventraLoginColors.PrimaryBlue
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EventraLoginColors.PrimaryBlue,
            focusedLabelColor = EventraLoginColors.PrimaryBlue,
            unfocusedBorderColor = EventraLoginColors.BorderGray,
            focusedLeadingIconColor = EventraLoginColors.PrimaryBlue
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventraPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text("Password") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = EventraLoginColors.PrimaryBlue
            )
        },
        trailingIcon = {
            IconButton(onClick = onPasswordVisibilityToggle) {
                Icon(
                    imageVector = if (passwordVisible) Icons.Default.Visibility
                    else Icons.Default.VisibilityOff,
                    contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                    tint = EventraLoginColors.TextGray
                )
            }
        },
        visualTransformation = if (passwordVisible) VisualTransformation.None
        else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EventraLoginColors.PrimaryBlue,
            focusedLabelColor = EventraLoginColors.PrimaryBlue,
            unfocusedBorderColor = EventraLoginColors.BorderGray,
            focusedLeadingIconColor = EventraLoginColors.PrimaryBlue
        )
    )
}

@Composable
fun EventraErrorMessage(loginState: LoginViewModel.LoginState) {
    AnimatedVisibility(
        visible = loginState is LoginViewModel.LoginState.Error,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut()
    ) {
        if (loginState is LoginViewModel.LoginState.Error) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = EventraLoginColors.ErrorRed.copy(alpha = 0.1f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = EventraLoginColors.ErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = loginState.message,
                        color = EventraLoginColors.ErrorRed,
                        fontSize = 14.sp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun EventraMainButton(
    isLoginMode: Boolean,
    isLoading: Boolean,
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Button(
        onClick = {
            isPressed = true
            if (isLoginMode) onLoginClick() else onRegisterClick()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = EventraLoginColors.PrimaryBlue,
            disabledContainerColor = EventraLoginColors.BorderGray
        ),
        shape = RoundedCornerShape(28.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn() with fadeOut() }
        ) { loading ->
            if (loading) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Text(
                        text = "Attendere...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            } else {
                Text(
                    text = if (isLoginMode) "ACCEDI" else "REGISTRATI",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EventraDivider() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier.weight(1f),
            color = EventraLoginColors.BorderGray
        )
        Text(
            text = "oppure",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = EventraLoginColors.TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Divider(
            modifier = Modifier.weight(1f),
            color = EventraLoginColors.BorderGray
        )
    }
}

@Composable
fun EventraGoogleButton(onGoogleSignIn: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    OutlinedButton(
        onClick = {
            isPressed = true
            onGoogleSignIn()
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .scale(scale),
        shape = RoundedCornerShape(28.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EventraLoginColors.TextDark,
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            EventraLoginColors.BorderGray
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_google),
                contentDescription = "Google",
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = "Continua con Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = EventraLoginColors.TextDark
            )
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(100)
            isPressed = false
        }
    }
}

@Composable
fun EventraSwitchModeButton(
    isLoginMode: Boolean,
    onModeSwitch: () -> Unit
) {
    TextButton(
        onClick = onModeSwitch,
        modifier = Modifier.padding(top = 20.dp)
    ) {
        Text(
            text = if (isLoginMode) "Non hai un account? Registrati" else "Hai già un account? Accedi",
            color = EventraLoginColors.PrimaryBlue,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun EventraLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier.size(100.dp),
        shape = RoundedCornerShape(50.dp),
        colors = CardDefaults.cardColors(containerColor = EventraLoginColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Loading",
                    tint = EventraLoginColors.PrimaryBlue,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Attendere...",
                    fontSize = 12.sp,
                    color = EventraLoginColors.TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
