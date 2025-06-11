package com.example.eventra.screens

import android.app.Application
import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventra.viewmodels.*
import com.example.eventra.viewmodels.data.*
import java.text.DecimalFormat

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BigliettoScreen(
    eventoId: Long = 1L,
    onNavigateToEventDetail: () -> Unit = {},
    onNavigateToPayment: (List<BigliettoData>) -> Unit = {}
) {
    val context = LocalContext.current

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as Application)
    }
    val tipoPostoViewModel: TipoPostoViewModel = viewModel {
        TipoPostoViewModel(context.applicationContext as Application)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as Application)
    }

    val evento by eventiViewModel.eventoDetail.collectAsState()
    val tipiPosto by tipoPostoViewModel.tipiPosto.collectAsState()
    val userData by profileViewModel.userData.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()
    val isLoadingTipi by tipoPostoViewModel.isLoading.collectAsState()

    var biglietti by remember { mutableStateOf<List<BigliettoData>>(emptyList()) }
    var nextId by remember { mutableStateOf(1L) }
    var showAlert by remember { mutableStateOf(false) }
    var alertMessage by remember { mutableStateOf("") }

    val totalePrezzi = remember(biglietti, tipiPosto) {
        biglietti.sumOf { biglietto ->
            tipiPosto?.find { it.id == biglietto.tipoPostoId }?.prezzo ?: 0.0
        }
    }

    LaunchedEffect(eventoId) {
        eventiViewModel.getEventoById(eventoId)
        tipoPostoViewModel.getTipiPostoByEvento(eventoId)
        profileViewModel.loadUserProfile()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EventraColors.BackgroundGray,
                        EventraColors.BackgroundGray.copy(0.8f)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            item {
                BigliettoHeader(
                    evento = evento,
                    onNavigateToEventDetail = onNavigateToEventDetail,
                    isLoading = isLoading
                )
            }

            item {
                if (!tipiPosto.isNullOrEmpty()) {
                    TipiPostoSection(
                        tipiPosto = tipiPosto!!,
                        biglietti = biglietti,
                        onAddBiglietto = { tipoPosto ->
                            val currentCount = biglietti.count { it.tipoPostoId == tipoPosto.id }
                            if (currentCount < tipoPosto.postiDisponibili) {
                                val nuovoBiglietto = BigliettoData(
                                    id = nextId,
                                    nomeSpettatore = "",
                                    cognomeSpettatore = "",
                                    emailSpettatore = "",
                                    eventoId = eventoId,
                                    tipoPostoId = tipoPosto.id ?: 0L,
                                    pagamentoId = null,
                                    isExpanded = true,
                                    dataCreazione = java.time.LocalDateTime.now().toString()
                                )
                                biglietti = biglietti + nuovoBiglietto
                                nextId++
                            } else {
                                alertMessage = "Non è possibile aggiungere più biglietti per ${tipoPosto.nome}. Posti disponibili: ${tipoPosto.postiDisponibili}"
                                showAlert = true
                            }
                        },
                        onRemoveBiglietto = { tipoPostoId ->
                            val bigliettiFiltered = biglietti.toMutableList()
                            val indexToRemove = bigliettiFiltered.indexOfLast { it.tipoPostoId == tipoPostoId }
                            if (indexToRemove >= 0) {
                                bigliettiFiltered.removeAt(indexToRemove)
                            }
                            biglietti = bigliettiFiltered
                        }
                    )
                } else {
                    LoadingOrEmptySection(isLoadingTipi)
                }
            }

            items(biglietti) { biglietto ->
                val tipoPosto = tipiPosto?.find { it.id == biglietto.tipoPostoId }
                if (tipoPosto != null) {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(300)
                        ) + fadeIn(animationSpec = tween(300))
                    ) {
                        BigliettoFormCard(
                            biglietto = biglietto,
                            tipoPosto = tipoPosto,
                            userData = userData,
                            onBigliettoChanged = { updatedBiglietto ->
                                biglietti = biglietti.map {
                                    if (it.id == updatedBiglietto.id) updatedBiglietto else it
                                }
                            },
                            onRemove = {
                                biglietti = biglietti.filter { it.id != biglietto.id }
                            }
                        )
                    }
                }
            }

            if (biglietti.isNotEmpty()) {
                item {
                    AnimatedVisibility(
                        visible = true,
                        enter = slideInVertically(
                            initialOffsetY = { it },
                            animationSpec = tween(400)
                        ) + fadeIn(animationSpec = tween(400))
                    ) {
                        RiepilogoSection(
                            biglietti = biglietti,
                            tipiPosto = tipiPosto ?: emptyList(),
                            totalePrezzi = totalePrezzi
                        )
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }


        AnimatedVisibility(
            visible = biglietti.isNotEmpty() && areAllFormsValid(biglietti),
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ) + fadeIn(),
            exit = scaleOut() + fadeOut(),
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            ExtendedFloatingActionButton(
                onClick = {
                    val bigliettiAggiornati = biglietti.map { biglietto ->
                        biglietto.copy(
                            dataCreazione = java.time.LocalDateTime.now().toString(),
                            emailSpettatore = userData?.email ?: ""
                        )
                    }
                    onNavigateToPayment(bigliettiAggiornati)
                },
                modifier = Modifier
                    .padding(16.dp)
                    .shadow(
                        elevation = 12.dp,
                        shape = RoundedCornerShape(28.dp),
                        clip = false
                    ),
                containerColor = EventraColors.PrimaryOrange,
                contentColor = Color.White,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 16.dp
                )
            ) {
                Icon(
                    Icons.Default.ShoppingCart,
                    "Procedi al pagamento",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Paga €${DecimalFormat("#.##").format(totalePrezzi)}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
        if (isLoading || isLoadingTipi) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier.padding(32.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(EventraColors.CardWhite)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = EventraColors.PrimaryOrange,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Caricamento in corso...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = EventraColors.TextDark
                        )
                    }
                }
            }
        }
        if (showAlert) {
            AlertDialog(
                onDismissRequest = { showAlert = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "Attenzione",
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.TextDark
                        )
                    }
                },
                text = {
                    Text(
                        text = alertMessage,
                        color = EventraColors.TextDark,
                        lineHeight = 20.sp
                    )
                },
                confirmButton = {
                    FilledTonalButton(
                        onClick = { showAlert = false },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = EventraColors.PrimaryOrange.copy(0.1f),
                            contentColor = EventraColors.PrimaryOrange
                        )
                    ) {
                        Text(
                            text = "Ho capito",
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                containerColor = EventraColors.CardWhite,
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

@Composable
fun TipoPostoCard(
    tipoPosto: TipoPostoData,
    quantitaSelezionata: Int,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        border = if (quantitaSelezionata > 0) {
            BorderStroke(2.dp, EventraColors.PrimaryOrange.copy(0.3f))
        } else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        tipoPosto.nome,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Euro,
                            null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            DecimalFormat("#.##").format(tipoPosto.prezzo),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.PrimaryOrange
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FilledIconButton(
                        onClick = onRemove,
                        enabled = quantitaSelezionata > 0,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (quantitaSelezionata > 0) {
                                EventraColors.PrimaryOrange.copy(0.1f)
                            } else {
                                EventraColors.DividerGray
                            },
                            contentColor = if (quantitaSelezionata > 0) {
                                EventraColors.PrimaryOrange
                            } else {
                                EventraColors.TextGray
                            }
                        )
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            "Rimuovi",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Card(
                        modifier = Modifier.wrapContentSize(),
                        colors = CardDefaults.cardColors(
                            EventraColors.PrimaryOrange.copy(0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            quantitaSelezionata.toString(),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.PrimaryOrange,
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }

                    FilledIconButton(
                        onClick = onAdd,
                        enabled = quantitaSelezionata < tipoPosto.postiDisponibili,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (quantitaSelezionata < tipoPosto.postiDisponibili) {
                                EventraColors.PrimaryOrange.copy(0.1f)
                            } else {
                                EventraColors.DividerGray
                            },
                            contentColor = if (quantitaSelezionata < tipoPosto.postiDisponibili) {
                                EventraColors.PrimaryOrange
                            } else {
                                EventraColors.TextGray
                            }
                        )
                    ) {
                        Icon(
                            Icons.Default.Add,
                            "Aggiungi",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.EventSeat,
                        null,
                        tint = EventraColors.TextGray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${tipoPosto.postiDisponibili} disponibili",
                        fontSize = 13.sp,
                        color = EventraColors.TextGray,
                        fontWeight = FontWeight.Medium
                    )
                }

                if (quantitaSelezionata > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                EventraColors.PrimaryOrange.copy(0.1f),
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.CheckCircle,
                            null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$quantitaSelezionata selezionati",
                            fontSize = 12.sp,
                            color = EventraColors.PrimaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            if (tipoPosto.postiDisponibili > 0) {
                Spacer(modifier = Modifier.height(12.dp))

                val progress = (tipoPosto.postiDisponibili - quantitaSelezionata).toFloat() / tipoPosto.postiDisponibili.toFloat()

                LinearProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = when {
                        progress > 0.7f -> Color.Green
                        progress > 0.3f -> EventraColors.PrimaryOrange
                        else -> Color.Red
                    },
                    trackColor = EventraColors.DividerGray.copy(0.5f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BigliettoFormCard(
    biglietto: BigliettoData,
    tipoPosto: TipoPostoData,
    userData: UtenteData?,
    onBigliettoChanged: (BigliettoData) -> Unit,
    onRemove: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(biglietto.isExpanded) }
    var hasUserDataApplied by remember { mutableStateOf(false) }

    val isComplete = biglietto.nomeSpettatore.isNotBlank() &&
            biglietto.cognomeSpettatore.isNotBlank() &&
            biglietto.emailSpettatore.isNotBlank() &&
            Patterns.EMAIL_ADDRESS.matcher(biglietto.emailSpettatore).matches()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .shadow(
                elevation = if (isExpanded) 8.dp else 4.dp,
                shape = RoundedCornerShape(16.dp),
                clip = false
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        border = if (isComplete) {
            BorderStroke(2.dp, Color.Green.copy(0.3f))
        } else if (isExpanded) {
            BorderStroke(2.dp, EventraColors.PrimaryOrange.copy(0.3f))
        } else null
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.ConfirmationNumber,
                            null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Biglietto ${tipoPosto.nome}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.TextDark
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Icon(
                            Icons.Default.Euro,
                            null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            DecimalFormat("#.##").format(tipoPosto.prezzo),
                            fontSize = 16.sp,
                            color = EventraColors.PrimaryOrange,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            if (isComplete) Color.Green.copy(0.1f) else MaterialTheme.colorScheme.error.copy(0.1f)
                        ),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                if (isComplete) Icons.Default.CheckCircle else Icons.Default.Warning,
                                null,
                                tint = if (isComplete) Color.Green else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                if (isComplete) "Dati completi" else "Dati mancanti",
                                fontSize = 12.sp,
                                color = if (isComplete) Color.Green else MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.error.copy(0.1f),
                            contentColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            "Rimuovi",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    FilledIconButton(
                        onClick = { isExpanded = !isExpanded },
                        modifier = Modifier.size(44.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = EventraColors.PrimaryOrange.copy(0.1f),
                            contentColor = EventraColors.PrimaryOrange
                        )
                    ) {
                        Icon(
                            if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            if (isExpanded) "Chiudi" else "Espandi",
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                ) + fadeIn(animationSpec = tween(300)),
                exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider(
                        color = EventraColors.DividerGray.copy(0.5f),
                        thickness = 1.dp
                    )


                    OutlinedTextField(
                        value = biglietto.nomeSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(nomeSpettatore = it))
                        },
                        label = { Text("Nome Spettatore *") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = EventraColors.PrimaryOrange)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange,
                            cursorColor = EventraColors.PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = biglietto.nomeSpettatore.isBlank(),
                        supportingText = if (biglietto.nomeSpettatore.isBlank()) {
                            { Text("Campo obbligatorio", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    OutlinedTextField(
                        value = biglietto.cognomeSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(cognomeSpettatore = it))
                        },
                        label = { Text("Cognome Spettatore *") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = EventraColors.PrimaryOrange)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange,
                            cursorColor = EventraColors.PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = biglietto.cognomeSpettatore.isBlank(),
                        supportingText = if (biglietto.cognomeSpettatore.isBlank()) {
                            { Text("Campo obbligatorio", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )

                    val isEmailValid = Patterns.EMAIL_ADDRESS.matcher(biglietto.emailSpettatore).matches() || biglietto.emailSpettatore.isBlank()

                    OutlinedTextField(
                        value = biglietto.emailSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(emailSpettatore = it))
                        },
                        label = { Text("Email Spettatore *") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = EventraColors.PrimaryOrange)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange,
                            cursorColor = EventraColors.PrimaryOrange
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = !isEmailValid,
                        supportingText = if (!isEmailValid) {
                            { Text("Inserisci un'email valida", color = MaterialTheme.colorScheme.error) }
                        } else null
                    )


                    if (userData != null && !hasUserDataApplied) {
                        FilledTonalButton(
                            onClick = {
                                onBigliettoChanged(
                                    biglietto.copy(
                                        nomeSpettatore = userData.nome,
                                        cognomeSpettatore = userData.cognome,
                                        emailSpettatore = userData.email
                                    )
                                )
                                hasUserDataApplied = true
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = EventraColors.PrimaryOrange.copy(0.1f),
                                contentColor = EventraColors.PrimaryOrange
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Usa i miei dati (${userData.nome} ${userData.cognome})",
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }// Info card migliorata
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            EventraColors.PrimaryOrange.copy(0.05f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, EventraColors.PrimaryOrange.copy(0.2f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    null,
                                    tint = EventraColors.PrimaryOrange,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    "Informazioni Biglietto",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = EventraColors.TextDark
                                )
                            }

                            InfoRow(
                                icon = Icons.Default.Email,
                                text = "Il biglietto sarà inviato all'email dello spettatore"
                            )
                            InfoRow(
                                icon = Icons.Default.QrCode,
                                text = "Conserva il codice QR per l'ingresso"
                            )
                            InfoRow(
                                icon = Icons.Default.EventSeat,
                                text = "Tipo posto: ${tipoPosto.nome}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Icon(
            icon,
            null,
            tint = EventraColors.PrimaryOrange.copy(0.7f),
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text,
            fontSize = 12.sp,
            color = EventraColors.TextGray,
            lineHeight = 16.sp
        )
    }
}


fun areAllFormsValid(biglietti: List<BigliettoData>): Boolean {
    return biglietti.isNotEmpty() && biglietti.all { biglietto ->
        biglietto.nomeSpettatore.isNotBlank() &&
                biglietto.cognomeSpettatore.isNotBlank() &&
                biglietto.emailSpettatore.isNotBlank() &&
                Patterns.EMAIL_ADDRESS.matcher(biglietto.emailSpettatore).matches()
    }
}

@Composable
fun BigliettoHeader(
    evento: EventoData?,
    onNavigateToEventDetail: () -> Unit,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EventraColors.PrimaryOrange,
                        EventraColors.DarkOrange,
                        EventraColors.DarkOrange.copy(0.8f)
                    )
                )
            )
    ) {
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(x = (-50).dp, y = (-50).dp)
                .background(
                    Color.White.copy(0.1f),
                    CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(150.dp)
                .align(Alignment.TopEnd)
                .offset(x = 75.dp, y = (-75).dp)
                .background(
                    Color.White.copy(0.05f),
                    CircleShape
                )
        )

        FilledIconButton(
            onClick = onNavigateToEventDetail,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .shadow(4.dp, CircleShape),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = Color.White.copy(0.2f),
                contentColor = Color.White
            )
        ) {
            Icon(
                Icons.Default.ArrowBack,
                "Torna indietro",
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.padding(8.dp),
                colors = CardDefaults.cardColors(Color.White.copy(0.2f)),
                shape = CircleShape
            ) {
                Icon(
                    Icons.Default.ConfirmationNumber,
                    null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(56.dp)
                        .padding(12.dp)
                )
            }

            Spacer(Modifier.height(20.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    "Caricamento evento...",
                    color = Color.White.copy(0.9f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            } else {
                Text(
                    evento?.nome ?: "Caricamento...",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 26.sp
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Compila i dati dei biglietti",
                    fontSize = 15.sp,
                    color = Color.White.copy(0.9f),
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun TipiPostoSection(
    tipiPosto: List<TipoPostoData>,
    biglietti: List<BigliettoData>,
    onAddBiglietto: (TipoPostoData) -> Unit,
    onRemoveBiglietto: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(EventraColors.CardWhite),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EventSeat,
                    null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Tipi di Posto Disponibili",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }
        }

        tipiPosto.forEachIndexed { index, tipoPosto ->
            val quantita = biglietti.count { it.tipoPostoId == tipoPosto.id }

            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(300, delayMillis = index * 100)
                ) + fadeIn(animationSpec = tween(300, delayMillis = index * 100))
            ) {
                TipoPostoCard(
                    tipoPosto = tipoPosto,
                    quantitaSelezionata = quantita,
                    onAdd = { onAddBiglietto(tipoPosto) },
                    onRemove = { tipoPosto.id?.let { onRemoveBiglietto(it) } }
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
fun RiepilogoSection(
    biglietti: List<BigliettoData>,
    tipiPosto: List<TipoPostoData>,
    totalePrezzi: Double
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                clip = false
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        border = BorderStroke(2.dp, EventraColors.PrimaryOrange.copy(0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 20.dp)
            ) {
                Icon(
                    Icons.Default.Receipt,
                    null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Riepilogo Biglietti",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }


            val bigliettiRaggruppati = biglietti.groupBy { it.tipoPostoId }

            bigliettiRaggruppati.forEach { (tipoPostoId, bigliettiGruppo) ->
                val tipoPosto = tipiPosto.find { it.id == tipoPostoId }
                if (tipoPosto != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        colors = CardDefaults.cardColors(
                            EventraColors.BackgroundGray.copy(0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        EventraColors.PrimaryOrange.copy(0.1f)
                                    ),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        "${bigliettiGruppo.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EventraColors.PrimaryOrange,
                                        modifier = Modifier.padding(8.dp)
                                    )
                                }
                                Spacer(Modifier.width(12.dp))
                                Text(
                                    tipoPosto.nome,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EventraColors.TextDark
                                )
                            }

                            Text(
                                "€${DecimalFormat("#.##").format(tipoPosto.prezzo * bigliettiGruppo.size)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = EventraColors.PrimaryOrange
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 16.dp),
                color = EventraColors.DividerGray,
                thickness = 2.dp
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    EventraColors.PrimaryOrange.copy(0.1f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Totale",
                            fontSize = 16.sp,
                            color = EventraColors.TextGray,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            "${biglietti.size} biglietti",
                            fontSize = 14.sp,
                            color = EventraColors.TextGray
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Euro,
                            null,
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            DecimalFormat("#.##").format(totalePrezzi),
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.PrimaryOrange
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            val completedForms = biglietti.count {
                it.nomeSpettatore.isNotBlank() &&
                        it.cognomeSpettatore.isNotBlank() &&
                        it.emailSpettatore.isNotBlank() &&
                        Patterns.EMAIL_ADDRESS.matcher(it.emailSpettatore).matches()
            }
            val allValid = completedForms == biglietti.size

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    if (allValid) Color.Green.copy(0.1f) else MaterialTheme.colorScheme.error.copy(0.1f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        if (allValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        tint = if (allValid) Color.Green else MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            if (allValid) "Tutti i biglietti sono pronti"
                            else "Completa i dati mancanti",
                            fontSize = 16.sp,
                            color = if (allValid) Color.Green else MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        if (!allValid) {
                            Text(
                                "Completati: $completedForms/${biglietti.size}",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.error.copy(0.8f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingOrEmptySection(isLoading: Boolean) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = EventraColors.PrimaryOrange,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Caricamento tipi di posto...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = EventraColors.TextGray
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.EventSeat,
                        null,
                        tint = EventraColors.TextGray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Nessun tipo di posto disponibile",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = EventraColors.TextGray,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        "Riprova più tardi o contatta l'organizzatore",
                        fontSize = 14.sp,
                        color = EventraColors.TextGray.copy(0.8f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }
}