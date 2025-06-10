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

    // Validazione password in tempo reale
    var passwordError by remember { mutableStateOf("") }

    // Google Sign In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
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

    // Validazione password per registrazione
// Validazione password per registrazione
    LaunchedEffect(password, isLoginMode) {
        if (!isLoginMode && password.isNotEmpty()) {
            passwordError = validatePassword(password)
        } else {
            passwordError = ""
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        EventraColors.PrimaryOrange,
                        EventraColors.DarkOrange
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
                passwordError = passwordError,
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
                onRegisterClick = {
                    if (passwordError.isEmpty()) {
                        viewModel.register(nome, cognome, email, password, telefono)
                    }
                },
                onGoogleSignIn = {
                    try {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.web_client_id))
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context as Activity, gso)
                        val signInIntent = googleSignInClient.signInIntent
                        googleSignInLauncher.launch(signInIntent)
                    } catch (e: Exception) {
                        // Fallback se non è disponibile
                    }
                },
                onModeSwitch = {
                    isLoginMode = !isLoginMode
                    viewModel.clearError()
                    // Reset campi
                    if (isLoginMode) {
                        nome = ""
                        cognome = ""
                        telefono = ""
                    }
                    password = ""
                    passwordError = ""
                }
            )
        }

        // Loading Overlay
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
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
            colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
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
    passwordError: String,
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
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
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
                        passwordError = passwordError,
                        isRegistration = !loginMode,
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
                canProceed = if (isLoginMode) true else passwordError.isEmpty() && password.isNotEmpty(),
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
    passwordError: String,
    isRegistration: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onPasswordVisibilityToggle: () -> Unit
) {EventraTextField(
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
        onPasswordVisibilityToggle = onPasswordVisibilityToggle,
        error = if (isRegistration) passwordError else "",
        isRegistration = isRegistration
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
    keyboardType: KeyboardType = KeyboardType.Text,
    error: String = ""
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            isError = error.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error.isNotEmpty()) Color.Red else EventraColors.PrimaryOrange,
                focusedLabelColor = if (error.isNotEmpty()) Color.Red else EventraColors.PrimaryOrange,
                unfocusedBorderColor = if (error.isNotEmpty()) Color.Red else EventraColors.DividerGray,
                focusedLeadingIconColor = EventraColors.PrimaryOrange,
                errorBorderColor = Color.Red,
                errorLabelColor = Color.Red
            )
        )

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventraPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityToggle: () -> Unit,
    error: String = "",
    isRegistration: Boolean = false
) {
    Column {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange
                )
            },
            trailingIcon = {
                IconButton(onClick = onPasswordVisibilityToggle) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility
                        else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Nascondi password" else "Mostra password",
                        tint = EventraColors.TextGray
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
            else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            isError = error.isNotEmpty(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = if (error.isNotEmpty()) Color.Red else EventraColors.PrimaryOrange,
                focusedLabelColor = if (error.isNotEmpty()) Color.Red else EventraColors.PrimaryOrange,
                unfocusedBorderColor = if (error.isNotEmpty()) Color.Red else EventraColors.DividerGray,
                focusedLeadingIconColor = EventraColors.PrimaryOrange,
                errorBorderColor = Color.Red,
                errorLabelColor = Color.Red
            )
        )

        if (isRegistration && value.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF7FAFC)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Requisiti password:",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    PasswordRequirement(
                        text = "Almeno 6 caratteri",
                        isValid = value.length >= 6
                    )
                    PasswordRequirement(
                        text = "Una lettera minuscola",
                        isValid = value.any { it.isLowerCase() }
                    )
                    PasswordRequirement(
                        text = "Una lettera maiuscola",
                        isValid = value.any { it.isUpperCase() }
                    )
                    PasswordRequirement(
                        text = "Un numero",
                        isValid = value.any { it.isDigit() }
                    )
                    PasswordRequirement(
                        text = "Un carattere speciale (@\$!%*?&)",
                        isValid = value.any { it in "@\$!%*?&" }
                    )
                    PasswordRequirement(
                        text = "Solo caratteri consentiti",
                        isValid = value.all { it.isLetterOrDigit() || it in "@\$!%*?&" }
                    )
                }
            }
        }

        if (error.isNotEmpty()) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
fun PasswordRequirement(
    text: String,
    isValid: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            imageVector = if (isValid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isValid) Color.Green else EventraColors.TextGray,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 11.sp,
            color = if (isValid) Color.Green else EventraColors.TextGray
        )
    }
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
                    containerColor = Color(0xFFFFEBEE)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = loginState.message,
                        color = Color(0xFFD32F2F),
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
    canProceed: Boolean,
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
        enabled = !isLoading && canProceed,
        colors = ButtonDefaults.buttonColors(
            containerColor = EventraColors.PrimaryOrange,
            disabledContainerColor = EventraColors.DividerGray
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
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = EventraColors.DividerGray
        )
        Text(
            text = "oppure",
            modifier = Modifier.padding(horizontal = 16.dp),
            color = EventraColors.TextGray,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = EventraColors.DividerGray
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
            contentColor = EventraColors.TextDark,
            containerColor = Color.Transparent
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            EventraColors.DividerGray
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Login,
                contentDescription = "Google",
                modifier = Modifier.size(24.dp),
                tint = EventraColors.PrimaryOrange
            )
            Text(
                text = "Continua con Google",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = EventraColors.TextDark
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
fun validatePassword(password: String): String {
    return when {
        password.length < 6 -> "Minimo 6 caratteri"
        !password.any { it.isLowerCase() } -> "Serve almeno una lettera minuscola"
        !password.any { it.isUpperCase() } -> "Serve almeno una lettera maiuscola"
        !password.any { it.isDigit() } -> "Serve almeno un numero"
        !password.any { it in "@\$!%*?&" } -> "Serve un carattere speciale (@\$!%*?&)"
        !password.all { it.isLetterOrDigit() || it in "@\$!%*?&" } -> "Caratteri non consentiti. Usa solo lettere, numeri e @\$!%*?&"
        else -> ""
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
            color = EventraColors.PrimaryOrange,
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
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
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
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier
                        .size(32.dp)
                        .graphicsLayer { rotationZ = rotation }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Attendere...",
                    fontSize = 12.sp,
                    color = EventraColors.TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}