package com.example.eventra.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.OrderHistoryViewModel
import com.example.eventra.viewmodels.data.AppTheme
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun ProfileScreen(
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel(context.applicationContext as android.app.Application) }
    val orderHistoryViewModel: OrderHistoryViewModel = viewModel { OrderHistoryViewModel(context.applicationContext as android.app.Application) }

    val userData by profileViewModel.userData.collectAsState()
    val isLoading by profileViewModel.isLoading.collectAsState()
    val updateState by profileViewModel.updateState.collectAsState()
    val currentTheme by profileViewModel.currentTheme.collectAsState()

    var selectedSection by remember { mutableStateOf(ProfileSection.PROFILE) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        if (selectedSection == ProfileSection.ORDERS) {
            orderHistoryViewModel.loadUserTickets()
        }
    }

    LaunchedEffect(updateState) {
        if (updateState is ProfileViewModel.UpdateState.Success) {
            showEditDialog = false
            showPasswordDialog = false
            profileViewModel.clearUpdateState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                if (currentTheme == AppTheme.DARK)
                    EventraColors.BackgroundGray
                else
                    EventraColors.BackgroundGray
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header con gradiente
            EventraProfileHeader(
                userData = userData,
                selectedSection = selectedSection,
                onSectionSelected = {
                    selectedSection = it
                    if (it == ProfileSection.ORDERS) {
                        orderHistoryViewModel.loadUserTickets()
                    }
                }
            )

            // Content
            AnimatedContent(
                targetState = selectedSection,
                transitionSpec = {
                    slideInHorizontally { if (targetState.ordinal > initialState.ordinal) it else -it } + fadeIn() with
                            slideOutHorizontally { if (targetState.ordinal > initialState.ordinal) -it else it } + fadeOut()
                }
            ) { section ->
                when (section) {
                    ProfileSection.PROFILE -> {
                        ProfileInfoSection(
                            userData = userData,
                            onEditProfile = { showEditDialog = true },
                            onChangePassword = { showPasswordDialog = true },
                            onChangeTheme = { showThemeDialog = true },
                            currentTheme = currentTheme,
                            onLogout = onLogout
                        )
                    }
                    ProfileSection.ORDERS -> {
                        OrderHistorySection(viewModel = orderHistoryViewModel)
                    }
                    ProfileSection.SETTINGS -> {
                        SettingsSection(
                            currentTheme = currentTheme,
                            onThemeChange = { profileViewModel.setTheme(it) },
                            onLogout = onLogout
                        )
                    }
                }
            }
        }

        // Dialogs
        if (showEditDialog) {
            EditProfileDialog(
                userData = userData,
                isLoading = isLoading,
                updateState = updateState,
                onDismiss = { showEditDialog = false },
                onSave = { nome, cognome, telefono ->
                    profileViewModel.updateUserProfile(nome, cognome, telefono)
                }
            )
        }

        if (showPasswordDialog) {
            ChangePasswordDialog(
                isLoading = isLoading,
                updateState = updateState,
                onDismiss = { showPasswordDialog = false },
                onSave = { oldPassword, newPassword ->
                    profileViewModel.updatePassword(oldPassword, newPassword)
                }
            )
        }

        if (showThemeDialog) {
            ThemeSelectionDialog(
                currentTheme = currentTheme,
                onDismiss = { showThemeDialog = false },
                onThemeSelected = { theme ->
                    profileViewModel.setTheme(theme)
                    showThemeDialog = false
                }
            )
        }

        // Loading overlay
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EventraLoadingIndicatorProfile()
            }
        }
    }
}

enum class ProfileSection(val title: String, val icon: ImageVector) {
    PROFILE("Profilo", Icons.Default.Person),
    ORDERS("Ordini", Icons.Default.Receipt),
    SETTINGS("Impostazioni", Icons.Default.Settings)
}

@Composable
fun EventraProfileHeader(
    userData: com.example.eventra.viewmodels.data.UtenteData?,
    selectedSection: ProfileSection,
    onSectionSelected: (ProfileSection) -> Unit
) {
    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
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
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (userData != null) {
                            "${userData.nome.firstOrNull()?.uppercase() ?: ""}${userData.cognome.firstOrNull()?.uppercase() ?: ""}"
                        } else "?",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = userData?.let { "${it.nome} ${it.cognome}" } ?: "Caricamento...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Text(
                    text = userData?.email ?: "",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }

        // Section Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EventraColors.CardWhite)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ProfileSection.values().forEach { section ->
                ProfileTab(
                    section = section,
                    isSelected = selectedSection == section,
                    onClick = { onSectionSelected(section) }
                )
            }
        }
    }
}

@Composable
fun ProfileTab(
    section: ProfileSection,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.1f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Column(
        modifier = Modifier
            .clickable { onClick() }
            .padding(vertical = 12.dp)
            .scale(scale),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = section.icon,
            contentDescription = section.title,
            tint = if (isSelected) EventraColors.PrimaryOrange else EventraColors.TextGray,
            modifier = Modifier.size(24.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = section.title,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) EventraColors.PrimaryOrange else EventraColors.TextGray
        )
    }
}

@Composable
fun ProfileInfoSection(
    userData: com.example.eventra.viewmodels.data.UtenteData?,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onChangeTheme: () -> Unit,
    currentTheme: AppTheme,
    onLogout: () -> Unit
) {
    val backgroundColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.BackgroundGray
    } else {
        EventraColors.BackgroundGray
    }

    val cardColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.CardWhite
    } else {
        EventraColors.CardWhite
    }

    val textColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.TextDark
    } else {
        EventraColors.TextDark
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ProfileMenuItem(
            icon = Icons.Default.Edit,
            title = "Modifica Profilo",
            subtitle = "Aggiorna i tuoi dati personali",
            onClick = onEditProfile,
            currentTheme = currentTheme
        )

        ProfileMenuItem(
            icon = Icons.Default.Lock,
            title = "Cambia Password",
            subtitle = "Aggiorna la tua password",
            onClick = onChangePassword,
            currentTheme = currentTheme
        )

        ProfileMenuItem(
            icon = Icons.Default.Palette,
            title = "Tema Applicazione",
            subtitle = "Tema attuale: ${getThemeDisplayName(currentTheme)}",
            onClick = onChangeTheme,
            currentTheme = currentTheme
        )

        ProfileMenuItem(
            icon = Icons.Default.Help,
            title = "Supporto",
            subtitle = "Aiuto e FAQ",
            onClick = { /* TODO */ },
            currentTheme = currentTheme
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Logout Button
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Esci",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFFD32F2F)
                    )
                    Text(
                        text = "Disconnetti dal tuo account",
                        fontSize = 12.sp,
                        color = Color(0xFFD32F2F).copy(alpha = 0.7f)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun OrderHistorySection(
    viewModel: OrderHistoryViewModel
) {
    val biglietti by viewModel.biglietti.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorState by viewModel.errorState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadUserTickets()
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                EventraLoadingIndicatorProfile()
            }
        } else if (errorState != null) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    // CORRETTO: Usa Alignment.CenterHorizontally invece di as Alignment.Horizontal
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = EventraColors.TextGray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = errorState!!,
                        fontSize = 16.sp,
                        color = EventraColors.TextGray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            viewModel.clearError()
                            viewModel.loadUserTickets()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EventraColors.PrimaryOrange
                        )
                    ) {
                        Text("Riprova")
                    }
                }
            }
        } else if (biglietti.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    // CORRETTO: Usa Alignment.CenterHorizontally invece di as Alignment.Horizontal
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = EventraColors.TextGray,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Nessun biglietto acquistato",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = EventraColors.TextDark
                    )

                    Text(
                        text = "I tuoi biglietti acquistati appariranno qui",
                        fontSize = 14.sp,
                        color = EventraColors.TextGray,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "I Miei Biglietti (${biglietti.size})",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }

                items(biglietti) { biglietto ->
//                    TicketCard(biglietto = biglietto)
                }
            }
        }
    }
}
//
//@Composable
//fun TicketCard(
//    biglietto: com.example.eventra.viewmodels.data.BigliettoData
//) {
//    var isExpanded by remember { mutableStateOf(false) }
//    val rotationAngle by animateFloatAsState(
//        targetValue = if (isExpanded) 180f else 0f,
//        animationSpec = tween(300)
//    )
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .clickable { isExpanded = !isExpanded },
//        shape = RoundedCornerShape(12.dp),
//        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
//        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
//    ) {
//        Column(
//            modifier = Modifier.padding(16.dp)
//        ) {
//            // Header del biglietto
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween,
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Column(modifier = Modifier.weight(1f)) {
//                    Text(
//                        text = biglietto.eventoId,
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = EventraColors.TextDark
//                    )
//
//                    Text(
//                        text = biglietto.tipoPostoNome,
//                        fontSize = 14.sp,
//                        color = EventraColors.TextGray
//                    )
//                }
//
//                Column(
//                    horizontalAlignment = Alignment.End
//                ) {
//                    Text(
//                        text = "€${String.format("%.2f", biglietto.prezzo)}",
//                        fontSize = 16.sp,
//                        fontWeight = FontWeight.Bold,
//                        color = EventraColors.PrimaryOrange
//                    )
//
//                    Icon(
//                        imageVector = Icons.Default.ExpandMore,
//                        contentDescription = null,
//                        tint = EventraColors.TextGray,
//                        modifier = Modifier
//                            .size(20.dp)
//                            .graphicsLayer { rotationZ = rotationAngle }
//                    )
//                }
//            }
//
//            // Dettagli espandibili
//            AnimatedVisibility(
//                visible = isExpanded,
//                enter = expandVertically() + fadeIn(),
//                exit = shrinkVertically() + fadeOut()
//            ) {
//                Column(
//                    modifier = Modifier.padding(top = 12.dp)
//                ) {
//                    Divider(color = EventraColors.DividerGray)
//
//                    Spacer(modifier = Modifier.height(12.dp))
//
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "ID Biglietto:",
//                            fontSize = 12.sp,
//                            color = EventraColors.TextGray
//                        )
//                        Text(
//                            text = "#${biglietto.id}",
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = EventraColors.TextDark
//                        )
//                    }
//
//                    if (!biglietto.nomeSpettatore.isNullOrEmpty()) {
//                        Spacer(modifier = Modifier.height(4.dp))
//                        Row(
//                            modifier = Modifier.fillMaxWidth(),
//                            horizontalArrangement = Arrangement.SpaceBetween
//                        ) {
//                            Text(
//                                text = "Spettatore:",
//                                fontSize = 12.sp,
//                                color = EventraColors.TextGray
//                            )
//                            Text(
//                                text = "${biglietto.nomeSpettatore} ${biglietto.cognomeSpettatore ?: ""}",
//                                fontSize = 12.sp,
//                                fontWeight = FontWeight.Medium,
//                                color = EventraColors.TextDark
//                            )
//                        }
//                    }
//
//                    Spacer(modifier = Modifier.height(4.dp))
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                        horizontalArrangement = Arrangement.SpaceBetween
//                    ) {
//                        Text(
//                            text = "Data Evento:",
//                            fontSize = 12.sp,
//                            color = EventraColors.TextGray
//                        )
//                        Text(
//                            text = biglietto.dataEvento,
//                            fontSize = 12.sp,
//                            fontWeight = FontWeight.Medium,
//                            color = EventraColors.TextDark
//                        )
//                    }
//                }
//            }
//        }
//    }
//}

@Composable
fun SettingsSection(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Impostazioni",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = EventraColors.TextDark,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Theme Setting
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Tema Applicazione",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = EventraColors.TextDark
                            )
                            Text(
                                text = getThemeDisplayName(currentTheme),
                                fontSize = 12.sp,
                                color = EventraColors.TextGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTheme.values().forEach { theme ->
                        ThemeChip(
                            theme = theme,
                            isSelected = currentTheme == theme,
                            onClick = { onThemeChange(theme) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // Other settings
        ProfileMenuItem(
            icon = Icons.Default.Notifications,
            title = "Notifiche",
            subtitle = "Gestisci le notifiche push",
            onClick = { /* TODO */ }
        )

        ProfileMenuItem(
            icon = Icons.Default.Language,
            title = "Lingua",
            subtitle = "Italiano",
            onClick = { /* TODO */ }
        )

        ProfileMenuItem(
            icon = Icons.Default.Info,
            title = "Informazioni App",
            subtitle = "Versione 1.0.0",
            onClick = { /* TODO */ }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Logout
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogout() }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Logout,
                    contentDescription = null,
                    tint = Color(0xFFD32F2F),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = "Esci dall'account",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFFD32F2F)
                )
            }
        }
    }
}

@Composable
fun ThemeChip(
    theme: AppTheme,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) EventraColors.PrimaryOrange else EventraColors.BackgroundGray
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Text(
            text = getThemeDisplayName(theme),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isSelected) Color.White else EventraColors.TextDark,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileDialog(
    userData: com.example.eventra.viewmodels.data.UtenteData?,
    isLoading: Boolean,
    updateState: ProfileViewModel.UpdateState,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    var nome by remember { mutableStateOf(userData?.nome ?: "") }
    var cognome by remember { mutableStateOf(userData?.cognome ?: "") }
    var telefono by remember { mutableStateOf(userData?.numerotelefono ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Modifica Profilo",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    label = { Text("Nome") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = cognome,
                    onValueChange = { cognome = it },
                    label = { Text("Cognome") },
                    leadingIcon = {
                        Icon(Icons.Default.Person, contentDescription = null)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = telefono,
                    onValueChange = { telefono = it },
                    label = { Text("Telefono") },
                    leadingIcon = {
                        Icon(Icons.Default.Phone, contentDescription = null)
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (updateState is ProfileViewModel.UpdateState.Error) {
                    Text(
                        text = updateState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(nome, cognome, telefono) },
                enabled = !isLoading && nome.isNotBlank() && cognome.isNotBlank(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Salva")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordDialog(
    isLoading: Boolean,
    updateState: ProfileViewModel.UpdateState,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var showPasswords by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cambia Password",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Password Attuale") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    trailingIcon = {
                        IconButton(onClick = { showPasswords = !showPasswords }) {
                            Icon(
                                imageVector = if (showPasswords) Icons.Default.Visibility
                                else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    },
                    visualTransformation = if (showPasswords) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Nuova Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    visualTransformation = if (showPasswords) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Conferma Password") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null)
                    },
                    visualTransformation = if (showPasswords) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = confirmPassword.isNotEmpty() && newPassword != confirmPassword
                )

                if (confirmPassword.isNotEmpty() && newPassword != confirmPassword) {
                    Text(
                        text = "Le password non coincidono",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }

                if (updateState is ProfileViewModel.UpdateState.Error) {
                    Text(
                        text = updateState.message,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(oldPassword, newPassword) },
                enabled = !isLoading &&
                        oldPassword.isNotBlank() &&
                        newPassword.isNotBlank() &&
                        newPassword == confirmPassword &&
                        newPassword.length >= 6,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Cambia")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annulla")
            }
        }
    )
}

@Composable
fun ThemeSelectionDialog(
    currentTheme: AppTheme,
    onDismiss: () -> Unit,
    onThemeSelected: (AppTheme) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Seleziona Tema",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                AppTheme.values().forEach { theme ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onThemeSelected(theme) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentTheme == theme,
                            onClick = { onThemeSelected(theme) },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = EventraColors.PrimaryOrange
                            )
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = getThemeDisplayName(theme),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = getThemeDescription(theme),
                                fontSize = 12.sp,
                                color = EventraColors.TextGray
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Chiudi")
            }
        }
    )
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    currentTheme: AppTheme = AppTheme.SYSTEM
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val cardColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.CardWhite
    } else {
        EventraColors.CardWhite
    }

    val textColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.TextDark
    } else {
        EventraColors.TextDark
    }

    val subtitleColor = if (currentTheme == AppTheme.DARK) {
        EventraColors.TextGray
    } else {
        EventraColors.TextGray
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        EventraColors.LightOrange,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = textColor
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = subtitleColor
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = subtitleColor,
                modifier = Modifier.size(20.dp)
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
fun EventraLoadingIndicatorProfile() {
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
                    text = "Caricamento...",
                    fontSize = 12.sp,
                    color = EventraColors.TextGray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Utility functions
fun getThemeDisplayName(theme: AppTheme): String {
    return when (theme) {
        AppTheme.LIGHT -> "Chiaro"
        AppTheme.DARK -> "Scuro"
        AppTheme.SYSTEM -> "Sistema"
    }
}

fun getThemeDescription(theme: AppTheme): String {
    return when (theme) {
        AppTheme.LIGHT -> "Tema sempre chiaro"
        AppTheme.DARK -> "Tema sempre scuro"
        AppTheme.SYSTEM -> "Segue le impostazioni del sistema"
    }
}