package com.example.eventra.screens

import android.annotation.SuppressLint
import android.app.Application
import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
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
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BigliettoScreen(
    eventoId: Long = 1L,
    onNavigateBack: () -> Unit = {},
    onNavigateToPayment: (List<BigliettoData>) -> Unit = {}
) {
    val context = LocalContext.current

    // ViewModels
    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as Application)
    }
    val tipoPostoViewModel: TipoPostoViewModel = viewModel {
        TipoPostoViewModel(context.applicationContext as Application)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as Application)
    }

    // States
    val evento by eventiViewModel.eventoDetail.collectAsState()
    val tipiPosto by tipoPostoViewModel.tipiPosto.collectAsState()
    val userData by profileViewModel.userData.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()
    val isLoadingTipi by tipoPostoViewModel.isLoading.collectAsState()

    // Biglietti da creare (usando BigliettoData semplificata)
    var biglietti by remember { mutableStateOf<List<BigliettoData>>(emptyList()) }
    var nextId by remember { mutableStateOf(1L) }

    // Calcoli totali
    val totalePrezzi = remember(biglietti, tipiPosto) {
        biglietti.sumOf { biglietto ->
            tipiPosto?.find { it.id == biglietto.tipoPostoId }?.prezzo ?: 0.0
        }
    }

    LaunchedEffect(eventoId) {
        eventiViewModel.getEventoById(1L)
        tipoPostoViewModel.getTipiPostoByEvento(1L)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EventraColors.BackgroundGray)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            // Header
            item {
                BigliettoHeader(
                    evento = evento,
                    onNavigateBack = onNavigateBack,
                    isLoading = isLoading
                )
            }

            // Tipi posto disponibili
            item {
                if (!tipiPosto.isNullOrEmpty()) {
                    TipiPostoSection(
                        tipiPosto = tipiPosto!!,
                        biglietti = biglietti,
                        onAddBiglietto = { tipoPosto ->
                            val nuovoBiglietto = BigliettoData(
                                id = nextId,
                                eventoId = 1L,
                                tipoPostoId = tipoPosto.id ?: 0L,
                                isExpanded = true
                            )
                            biglietti = biglietti + nuovoBiglietto
                            nextId++
                        },
                        onRemoveBiglietto = { tipoPostoId ->
                            biglietti = biglietti.filterNot {
                                it.tipoPostoId == tipoPostoId
                            }.let { filtered ->
                                val lastIndex = filtered.indexOfLast { it.tipoPostoId == tipoPostoId }
                                if (lastIndex >= 0) {
                                    filtered.toMutableList().apply { removeAt(lastIndex) }
                                } else filtered
                            }
                        }
                    )
                } else {
                    LoadingOrEmptySection(isLoadingTipi)
                }
            }

            // Form biglietti
            items(biglietti) { biglietto ->
                val tipoPosto = tipiPosto?.find { it.id == biglietto.tipoPostoId }
                if (tipoPosto != null) {
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

            // Riepilogo
            if (biglietti.isNotEmpty()) {
                item {
                    RiepilogoSection(
                        biglietti = biglietti,
                        tipiPosto = tipiPosto ?: emptyList(),
                        totalePrezzi = totalePrezzi
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(120.dp)) }
        }

        // FAB per procedere al pagamento
        if (biglietti.isNotEmpty() && areAllFormsValid(biglietti)) {
            ExtendedFloatingActionButton(
                onClick = { onNavigateToPayment(biglietti) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = EventraColors.PrimaryOrange,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.ShoppingCart, "Procedi al pagamento")
                Spacer(Modifier.width(8.dp))
                Text(
                    "Paga €${DecimalFormat("#.##").format(totalePrezzi)}",
                    fontWeight = FontWeight.Bold
                )
            }
        }

        if (isLoading || isLoadingTipi) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EventraColors.PrimaryOrange)
            }
        }
    }
}

@Composable
fun BigliettoHeader(
    evento: EventoData?,
    onNavigateBack: () -> Unit,
    isLoading: Boolean = false
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        EventraColors.PrimaryOrange,
                        EventraColors.DarkOrange
                    )
                )
            )
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                "Torna indietro",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.ConfirmationNumber,
                null,
                tint = Color.White,
                modifier = Modifier.size(48.dp)
            )

            Spacer(Modifier.height(16.dp))

            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                Spacer(Modifier.height(8.dp))
                Text("Caricamento evento...", color = Color.White.copy(0.9f))
            } else {
                Text(
                    evento?.nome ?: "Caricamento...",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "Compila i dati dei biglietti",
                    fontSize = 14.sp,
                    color = Color.White.copy(0.9f)
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
        Text(
            "Tipi di Posto Disponibili",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = EventraColors.TextDark,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        tipiPosto.forEach { tipoPosto ->
            val quantita = biglietti.count { it.tipoPostoId == tipoPosto.id }

            TipoPostoCard(
                tipoPosto = tipoPosto,
                quantitaSelezionata = quantita,
                onAdd = { onAddBiglietto(tipoPosto) },
                onRemove = { tipoPosto.id?.let { onRemoveBiglietto(it) } }
            )
            Spacer(Modifier.height(12.dp))
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    tipoPosto.nome,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
                Text(
                    "€${DecimalFormat("#.##").format(tipoPosto.prezzo)}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = EventraColors.PrimaryOrange
                )
                Text(
                    "${tipoPosto.postiDisponibili} posti disponibili",
                    fontSize = 12.sp,
                    color = EventraColors.TextGray
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onRemove,
                    enabled = quantitaSelezionata > 0
                ) {
                    Icon(
                        Icons.Default.Remove,
                        "Rimuovi",
                        tint = if (quantitaSelezionata > 0)
                            EventraColors.PrimaryOrange else EventraColors.TextGray
                    )
                }

                Box(
                    modifier = Modifier
                        .background(
                            EventraColors.LightOrange.copy(0.2f),
                            RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        quantitaSelezionata.toString(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.PrimaryOrange
                    )
                }

                IconButton(
                    onClick = onAdd,
                    enabled = quantitaSelezionata < tipoPosto.postiDisponibili
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Aggiungi",
                        tint = if (quantitaSelezionata < tipoPosto.postiDisponibili)
                            EventraColors.PrimaryOrange else EventraColors.TextGray
                    )
                }
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

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Biglietto ${tipoPosto.nome}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark
                    )
                    Text(
                        "€${DecimalFormat("#.##").format(tipoPosto.prezzo)}",
                        fontSize = 14.sp,
                        color = EventraColors.PrimaryOrange,
                        fontWeight = FontWeight.Medium
                    )

                    // Status indicator
                    val isComplete = biglietto.nomeSpettatore.isNotBlank() &&
                            biglietto.cognomeSpettatore.isNotBlank() &&
                            biglietto.emailSpettatore.isNotBlank() &&
                            Patterns.EMAIL_ADDRESS.matcher(biglietto.emailSpettatore).matches()

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (isComplete) Icons.Default.CheckCircle else Icons.Default.Warning,
                            null,
                            tint = if (isComplete) Color.Green else Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            if (isComplete) "Dati compilati" else "Compila i dati",
                            fontSize = 12.sp,
                            color = if (isComplete) Color.Green else Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onRemove) {
                        Icon(Icons.Default.Delete, "Rimuovi", tint = EventraColors.TextGray)
                    }
                    Icon(
                        if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        if (isExpanded) "Chiudi" else "Espandi",
                        tint = EventraColors.TextGray
                    )
                }
            }

            // Form espandibile
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = biglietto.nomeSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(nomeSpettatore = it))
                        },
                        label = { Text("Nome Spettatore") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = EventraColors.PrimaryOrange)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange
                        )
                    )

                    OutlinedTextField(
                        value = biglietto.cognomeSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(cognomeSpettatore = it))
                        },
                        label = { Text("Cognome Spettatore") },
                        leadingIcon = {
                            Icon(Icons.Default.Person, null, tint = EventraColors.PrimaryOrange)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange
                        )
                    )

                    OutlinedTextField(
                        value = biglietto.emailSpettatore,
                        onValueChange = {
                            onBigliettoChanged(biglietto.copy(emailSpettatore = it))
                        },
                        label = { Text("Email Spettatore") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = EventraColors.PrimaryOrange)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EventraColors.PrimaryOrange,
                            focusedLabelColor = EventraColors.PrimaryOrange
                        )
                    )

                    if (userData != null) {
                        OutlinedButton(
                            onClick = {
                                onBigliettoChanged(
                                    biglietto.copy(
                                        nomeSpettatore = userData.nome,
                                        cognomeSpettatore = userData.cognome,
                                        emailSpettatore = userData.email
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = EventraColors.PrimaryOrange
                            ),
                            border = BorderStroke(1.dp, EventraColors.PrimaryOrange)
                        ) {
                            Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Usa i miei dati")
                        }
                    }
                }
            }
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
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Riepilogo Biglietti",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Raggruppa per tipo posto
            val bigliettiRaggruppati = biglietti.groupBy { it.tipoPostoId }

            bigliettiRaggruppati.forEach { (tipoPostoId, bigliettiGruppo) ->
                val tipoPosto = tipiPosto.find { it.id == tipoPostoId }
                if (tipoPosto != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${bigliettiGruppo.size}x ${tipoPosto.nome}",
                            fontSize = 14.sp,
                            color = EventraColors.TextDark
                        )
                        Text(
                            "€${DecimalFormat("#.##").format(tipoPosto.prezzo * bigliettiGruppo.size)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = EventraColors.TextDark
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = EventraColors.DividerGray
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Totale (${biglietti.size} biglietti)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
                Text(
                    "€${DecimalFormat("#.##").format(totalePrezzi)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.PrimaryOrange
                )
            }

            Spacer(Modifier.height(16.dp))

            val completedForms = biglietti.count {
                it.nomeSpettatore.isNotBlank() &&
                        it.cognomeSpettatore.isNotBlank() &&
                        it.emailSpettatore.isNotBlank() &&
                        Patterns.EMAIL_ADDRESS.matcher(it.emailSpettatore).matches()
            }
            val allValid = completedForms == biglietti.size

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    if (allValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                    null,
                    tint = if (allValid) Color.Green else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    if (allValid) "Tutti i biglietti sono pronti"
                    else "Completa i dati ($completedForms/${biglietti.size})",
                    fontSize = 14.sp,
                    color = if (allValid) Color.Green else Color.Gray,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun LoadingOrEmptySection(isLoading: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            CircularProgressIndicator(color = EventraColors.PrimaryOrange)
        } else {
            Text(
                "Nessun tipo di posto disponibile",
                style = MaterialTheme.typography.bodyLarge,
                color = EventraColors.TextGray
            )
        }
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