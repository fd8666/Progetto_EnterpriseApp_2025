package com.example.eventra.screens

import android.app.Application
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
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
import com.example.eventra.viewmodels.BigliettoViewModel
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.OrdineViewModel
import com.example.eventra.viewmodels.PagamentoViewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.TipoPostoViewModel
import com.example.eventra.viewmodels.data.*
import kotlinx.coroutines.delay
import java.text.DecimalFormat

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun PagamentoScreen(
    biglietti: List<BigliettoData>,
    onPaymentSuccess: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current

    // ViewModels
    val pagamentoViewModel: PagamentoViewModel = viewModel {
        PagamentoViewModel(context.applicationContext as Application)
    }
    val ordineViewModel: OrdineViewModel = viewModel {
        OrdineViewModel(context.applicationContext as Application)
    }
    val bigliettoViewModel: BigliettoViewModel = viewModel {
        BigliettoViewModel(context.applicationContext as Application)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as Application)
    }
    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as Application)
    }
    val tipoPostoViewModel: TipoPostoViewModel = viewModel {
        TipoPostoViewModel(context.applicationContext as Application)
    }

    // Stati del form
    var nomeTitolare by remember { mutableStateOf("") }
    var cognomeTitolare by remember { mutableStateOf("") }
    var numeroCarta by remember { mutableStateOf("") }
    var meseScadenza by remember { mutableStateOf("") }
    var annoScadenza by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var showCvv by remember { mutableStateOf(false) }

    // Stati UI
    var currentStep by remember { mutableStateOf(1) }
    var isProcessingPayment by remember { mutableStateOf(false) }
    var paymentCompleted by remember { mutableStateOf(false) }

    // Stati da ViewModel
    val evento by eventiViewModel.eventoDetail.collectAsState()
    val tipiPosto by tipoPostoViewModel.tipiPosto.collectAsState()
    val userData by profileViewModel.userData.collectAsState()
    val isLoadingPagamento by pagamentoViewModel.isLoading.collectAsState()
    val isLoadingOrdine by ordineViewModel.isLoading.collectAsState()
    val paymentSuccess by pagamentoViewModel.paymentSuccess.collectAsState()
    val ordineCreated by ordineViewModel.ordineCreated.collectAsState()
    val errorPagamento by pagamentoViewModel.error.collectAsState()
    val errorOrdine by ordineViewModel.error.collectAsState()

    // Calcoli prezzo basati sui tipi posto
    val (prezzoTotale, commissioni, prezzoFinale) = remember(biglietti, tipiPosto) {
        val totale = biglietti.sumOf { biglietto ->
            tipiPosto?.find { it.id == biglietto.tipoPostoId }?.prezzo ?: 0.0
        }
        val comm = totale * 0.05 // 5% di commissioni
        val finale = totale + comm
        Triple(totale, comm, finale)
    }

    // Caricamento iniziale
    LaunchedEffect(biglietti) {
        if (biglietti.isNotEmpty()) {
            val eventoId = biglietti.first().eventoId
            eventiViewModel.getEventoById(eventoId)
            tipoPostoViewModel.getTipiPostoByEvento(eventoId)
        }
        profileViewModel.loadUserProfile()
    }

    // Gestione successo pagamento
    LaunchedEffect(paymentSuccess) {
        if (paymentSuccess && !paymentCompleted) {
            paymentCompleted = true
            delay(2000)
            onPaymentSuccess()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EventraColors.BackgroundGray)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            PagamentoHeader(
                evento = evento,
                onBackPressed = onBackPressed
            )

            // Indicatori di progresso
            PaymentProgressIndicator(
                currentStep = currentStep,
                totalSteps = 3
            )

            when (currentStep) {
                1 -> {
                    // Step 1: Riepilogo ordine
                    RiepilogoOrdineSection(
                        biglietti = biglietti,
                        tipiPosto = tipiPosto,
                        prezzoTotale = prezzoTotale,
                        commissioni = commissioni,
                        prezzoFinale = prezzoFinale,
                        onContinua = { currentStep = 2 }
                    )
                }
                2 -> {
                    // Step 2: Dati pagamento
                    DatiPagamentoSection(
                        nomeTitolare = nomeTitolare,
                        onNomeTitolareChange = { nomeTitolare = it },
                        cognomeTitolare = cognomeTitolare,
                        onCognomeTitolareChange = { cognomeTitolare = it },
                        numeroCarta = numeroCarta,
                        onNumeroCartaChange = { numeroCarta = it },
                        meseScadenza = meseScadenza,
                        onMeseScadenzaChange = { meseScadenza = it },
                        annoScadenza = annoScadenza,
                        onAnnoScadenzaChange = { annoScadenza = it },
                        cvv = cvv,
                        onCvvChange = { cvv = it },
                        showCvv = showCvv,
                        onShowCvvChange = { showCvv = it },
                        onIndietro = { currentStep = 1 },
                        onContinua = { currentStep = 3 },
                        isFormValid = isFormValid(nomeTitolare, cognomeTitolare, numeroCarta, meseScadenza, annoScadenza, cvv)
                    )
                }
                3 -> {
                    // Step 3: Conferma e pagamento
                    ConfermaePagamentoSection(
                        evento = evento,
                        nomeTitolare = nomeTitolare,
                        cognomeTitolare = cognomeTitolare,
                        numeroCarta = numeroCarta,
                        meseScadenza = meseScadenza,
                        annoScadenza = annoScadenza,
                        prezzoFinale = prezzoFinale,
                        isProcessing = isLoadingPagamento || isLoadingOrdine || isProcessingPayment,
                        onIndietro = { currentStep = 2 },
                        onPaga = {
                            processPaymentWithTickets(
                                pagamentoViewModel = pagamentoViewModel,
                                ordineViewModel = ordineViewModel,
                                bigliettoViewModel = bigliettoViewModel,
                                biglietti = biglietti,
                                nomeTitolare = nomeTitolare,
                                cognomeTitolare = cognomeTitolare,
                                numeroCarta = numeroCarta,
                                meseScadenza = meseScadenza,
                                annoScadenza = annoScadenza,
                                cvv = cvv,
                                prezzoFinale = prezzoFinale,
                                userData = userData,
                                onProcessingStart = { isProcessingPayment = true },
                                onProcessingEnd = { isProcessingPayment = false }
                            )
                        }
                    )
                }
            }

            // Messaggio di successo
            if (paymentCompleted) {
                PaymentSuccessSection()
            }

            Spacer(modifier = Modifier.height(100.dp))
        }

        // Loading overlay
        if (isLoadingPagamento || isLoadingOrdine || isProcessingPayment) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                PaymentLoadingIndicator()
            }
        }

        // Error handling
        errorPagamento?.let { error ->
            LaunchedEffect(error) {
                isProcessingPayment = false
            }
        }

        errorOrdine?.let { error ->
            LaunchedEffect(error) {
                isProcessingPayment = false
            }
        }
    }
}

@Composable
fun PagamentoHeader(
    evento: EventoData?,
    onBackPressed: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
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
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackPressed,
                    modifier = Modifier
                        .background(
                            Color.White.copy(alpha = 0.2f),
                            CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Indietro",
                        tint = Color.White
                    )
                }

                Text(
                    text = "Pagamento",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.width(48.dp))
            }

            if (evento != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = evento.nome ?: "Evento",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun PaymentProgressIndicator(
    currentStep: Int,
    totalSteps: Int
) {
    val steps = listOf("Riepilogo", "Pagamento", "Conferma")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            steps.forEachIndexed { index, stepName ->
                StepIndicator(
                    stepNumber = index + 1,
                    stepName = stepName,
                    isActive = currentStep == index + 1,
                    isCompleted = currentStep > index + 1,
                    modifier = Modifier.weight(1f)
                )

                if (index < steps.size - 1) {
                    StepConnector(
                        isCompleted = currentStep > index + 1,
                        modifier = Modifier.weight(0.5f)
                    )
                }
            }
        }
    }
}

@Composable
fun StepIndicator(
    stepNumber: Int,
    stepName: String,
    isActive: Boolean,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Cerchio con numero o check
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = when {
                        isCompleted -> EventraColors.PrimaryOrange
                        isActive -> EventraColors.PrimaryOrange
                        else -> EventraColors.DividerGray
                    },
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completato",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Text(
                    text = stepNumber.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else EventraColors.TextGray
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nome dello step
        Text(
            text = stepName,
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
            color = when {
                isCompleted -> EventraColors.PrimaryOrange
                isActive -> EventraColors.PrimaryOrange
                else -> EventraColors.TextGray
            },
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StepConnector(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(2.dp)
            .background(
                color = if (isCompleted) EventraColors.PrimaryOrange else EventraColors.DividerGray,
                shape = RoundedCornerShape(1.dp)
            )
    )
}

@Composable
fun RiepilogoOrdineSection(
    biglietti: List<BigliettoData>,
    tipiPosto: List<TipoPostoData>?,
    prezzoTotale: Double,
    commissioni: Double,
    prezzoFinale: Double,
    onContinua: () -> Unit
) {
    val decimalFormat = DecimalFormat("#.##")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Riepilogo Ordine",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Raggruppa i biglietti per tipo posto
            val bigliettiRaggruppati = biglietti.groupBy { it.tipoPostoId }

            bigliettiRaggruppati.forEach { (tipoPostoId, bigliettiGruppo) ->
                val tipoPosto = tipiPosto?.find { it.id == tipoPostoId }

                if (tipoPosto != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${bigliettiGruppo.size}x ${tipoPosto.nome}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = EventraColors.TextDark
                                )
                                Text(
                                    text = "€${decimalFormat.format(tipoPosto.prezzo)} cad.",
                                    fontSize = 14.sp,
                                    color = EventraColors.TextGray
                                )
                            }
                            Text(
                                text = "€${decimalFormat.format(tipoPosto.prezzo * bigliettiGruppo.size)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = EventraColors.TextDark
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Lista spettatori per questo tipo di posto
                        bigliettiGruppo.forEach { biglietto ->
                            Text(
                                text = "• ${biglietto.nomeSpettatore} ${biglietto.cognomeSpettatore}",
                                fontSize = 12.sp,
                                color = EventraColors.TextGray,
                                modifier = Modifier.padding(start = 16.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }
            }

            Divider(color = EventraColors.DividerGray)

            Spacer(modifier = Modifier.height(16.dp))

            // Calcoli prezzi
            PriceRow(
                label = "Subtotale biglietti",
                amount = prezzoTotale
            )

            Spacer(modifier = Modifier.height(8.dp))

            PriceRow(
                label = "Commissioni di servizio",
                amount = commissioni
            )

            Spacer(modifier = Modifier.height(12.dp))

            Divider(color = EventraColors.DividerGray)

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Totale",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
                Text(
                    text = "€${decimalFormat.format(prezzoFinale)}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.PrimaryOrange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onContinua,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(25.dp)
            ) {
                Text(
                    text = "Continua",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
fun PriceRow(
    label: String,
    amount: Double
) {
    val decimalFormat = DecimalFormat("#.##")

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = EventraColors.TextGray
        )
        Text(
            text = "€${decimalFormat.format(amount)}",
            fontSize = 14.sp,
            color = EventraColors.TextDark,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DatiPagamentoSection(
    nomeTitolare: String,
    onNomeTitolareChange: (String) -> Unit,
    cognomeTitolare: String,
    onCognomeTitolareChange: (String) -> Unit,
    numeroCarta: String,
    onNumeroCartaChange: (String) -> Unit,
    meseScadenza: String,
    onMeseScadenzaChange: (String) -> Unit,
    annoScadenza: String,
    onAnnoScadenzaChange: (String) -> Unit,
    cvv: String,
    onCvvChange: (String) -> Unit,
    showCvv: Boolean,
    onShowCvvChange: (Boolean) -> Unit,
    onIndietro: () -> Unit,
    onContinua: () -> Unit,
    isFormValid: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Dati di Pagamento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nome Titolare
            PaymentTextField(
                value = nomeTitolare,
                onValueChange = onNomeTitolareChange,
                label = "Nome Titolare",
                placeholder = "Inserisci il nome",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Cognome Titolare
            PaymentTextField(
                value = cognomeTitolare,
                onValueChange = onCognomeTitolareChange,
                label = "Cognome Titolare",
                placeholder = "Inserisci il cognome",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Numero Carta
            PaymentTextField(
                value = formatCardNumber(numeroCarta),
                onValueChange = { newValue ->
                    val digitsOnly = newValue.replace(Regex("[^\\d]"), "")
                    if (digitsOnly.length <= 16) {
                        onNumeroCartaChange(digitsOnly)
                    }
                },
                label = "Numero Carta",
                placeholder = "1234 5678 9012 3456",
                leadingIcon = Icons.Default.CreditCard,
                keyboardType = KeyboardType.Number
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Mese Scadenza
                PaymentTextField(
                    value = meseScadenza,
                    onValueChange = { newValue ->
                        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {

                            onMeseScadenzaChange(newValue)
                        }
                    }
                    ,
                    label = "Mese",
                    placeholder = "MM",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )

                // Anno Scadenza
                PaymentTextField(
                    value = annoScadenza,
                    onValueChange = { newValue ->
                        if (newValue.length <= 2 && newValue.all { it.isDigit() }) {
                            onAnnoScadenzaChange(newValue)
                        }
                    },
                    label = "Anno",
                    placeholder = "YY",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number
                )

                // CVV
                PaymentTextField(
                    value = cvv,
                    onValueChange = { newValue ->
                        if (newValue.length <= 4 && newValue.all { it.isDigit() }) {
                            onCvvChange(newValue)
                        }
                    },
                    label = "CVV",
                    placeholder = "123",
                    modifier = Modifier.weight(1f),
                    keyboardType = KeyboardType.Number,
                    visualTransformation = if (showCvv) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { onShowCvvChange(!showCvv) }) {
                            Icon(
                                imageVector = if (showCvv) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (showCvv) "Nascondi CVV" else "Mostra CVV",
                                tint = EventraColors.TextGray
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Pulsanti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIndietro,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    border = BorderStroke(
                        1.dp,
                        EventraColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Indietro",
                        fontSize = 16.sp,
                        color = EventraColors.PrimaryOrange
                    )
                }

                Button(
                    onClick = onContinua,
                    enabled = isFormValid,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EventraColors.PrimaryOrange,
                        disabledContainerColor = EventraColors.DividerGray
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Continua",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                text = label,
                color = EventraColors.TextGray
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = EventraColors.TextGray.copy(alpha = 0.6f)
            )
        },
        leadingIcon = leadingIcon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = EventraColors.TextGray
                )
            }
        },
        trailingIcon = trailingIcon,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = EventraColors.PrimaryOrange,
            unfocusedBorderColor = EventraColors.DividerGray,
            focusedLabelColor = EventraColors.PrimaryOrange,
            unfocusedLabelColor = EventraColors.TextGray,
            cursorColor = EventraColors.PrimaryOrange
        ),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        visualTransformation = visualTransformation,
        singleLine = true
    )
}

@Composable
fun ConfermaePagamentoSection(
    evento: EventoData?,
    nomeTitolare: String,
    cognomeTitolare: String,
    numeroCarta: String,
    meseScadenza: String,
    annoScadenza: String,
    prezzoFinale: Double,
    isProcessing: Boolean,
    onIndietro: () -> Unit,
    onPaga: () -> Unit
) {
    val decimalFormat = DecimalFormat("#.##")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "Conferma Pagamento",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Riepilogo dati
            PaymentSummaryCard(
                evento = evento,
                nomeTitolare = nomeTitolare,
                cognomeTitolare = cognomeTitolare,
                numeroCarta = numeroCarta,
                meseScadenza = meseScadenza,
                annoScadenza = annoScadenza,
                prezzoFinale = prezzoFinale
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Pulsanti
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onIndietro,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    border = BorderStroke(
                        1.dp,
                        EventraColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    Text(
                        text = "Indietro",
                        fontSize = 16.sp,
                        color = EventraColors.PrimaryOrange
                    )
                }

                Button(
                    onClick = onPaga,
                    enabled = !isProcessing,
                    modifier = Modifier
                        .weight(2f)
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EventraColors.PrimaryOrange,
                        disabledContainerColor = EventraColors.DividerGray
                    ),
                    shape = RoundedCornerShape(25.dp)
                ) {
                    if (isProcessing) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = "Elaborazione...",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Payment,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Paga €${decimalFormat.format(prezzoFinale)}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentSummaryCard(
    evento: EventoData?,
    nomeTitolare: String,
    cognomeTitolare: String,
    numeroCarta: String,
    meseScadenza: String,
    annoScadenza: String,
    prezzoFinale: Double
) {
    val decimalFormat = DecimalFormat("#.##")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.BackgroundGray),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
                {
                    // Evento
                    SummaryRow(
                        label = "Evento",
                        value = evento?.nome ?: "Evento"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Titolare
                    SummaryRow(
                        label = "Titolare",
                        value = "$nomeTitolare $cognomeTitolare"
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Carta
                    SummaryRow(
                        label = "Carta",
                        value = "**** **** **** ${numeroCarta.takeLast(4)} ($meseScadenza/$annoScadenza)"
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Divider(color = EventraColors.DividerGray)

                    Spacer(modifier = Modifier.height(12.dp))

                    // Totale
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Totale da pagare",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.TextDark
                        )
                        Text(
                            text = "€${decimalFormat.format(prezzoFinale)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.PrimaryOrange
                        )
                    }
                }
    }
}

@Composable
fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = EventraColors.TextGray,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = EventraColors.TextDark,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(2f),
            textAlign = TextAlign.End
        )
    }
}

@Composable
fun PaymentSuccessSection() {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "success_scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .scale(scale),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icona di successo animata
            val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "success_pulse_scale"
            )

            Box(
                modifier = Modifier
                    .size(80.dp)
                    .scale(pulseScale)
                    .background(
                        EventraColors.PrimaryOrange,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Pagamento completato",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Pagamento Completato!",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Il tuo ordine è stato elaborato con successo.\nRiceverai una email di conferma a breve.",
                fontSize = 16.sp,
                color = EventraColors.TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Controlla la tua email",
                    fontSize = 14.sp,
                    color = EventraColors.PrimaryOrange,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun PaymentLoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "payment_loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "payment_loading_rotation"
    )

    Card(
        modifier = Modifier.size(120.dp),
        shape = RoundedCornerShape(60.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Payment,
                contentDescription = "Elaborazione pagamento",
                tint = EventraColors.PrimaryOrange,
                modifier = Modifier
                    .size(40.dp)
                    .graphicsLayer { rotationZ = rotation }
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Elaborazione\npagamento...",
                fontSize = 12.sp,
                color = EventraColors.TextGray,
                textAlign = TextAlign.Center,
                lineHeight = 16.sp
            )
        }
    }
}

// Funzioni di utilità
fun formatCardNumber(number: String): String {
    val digitsOnly = number.replace(Regex("[^\\d]"), "")
    return digitsOnly.chunked(4).joinToString(" ")
}

fun isFormValid(
    nomeTitolare: String,
    cognomeTitolare: String,
    numeroCarta: String,
    meseScadenza: String,
    annoScadenza: String,
    cvv: String
): Boolean {
    return nomeTitolare.isNotBlank() &&
            cognomeTitolare.isNotBlank() &&
            numeroCarta.length >= 13 &&
            meseScadenza.length == 2 &&
            annoScadenza.length == 2 &&
            cvv.length >= 3 &&
            (meseScadenza.toIntOrNull() ?: 0) in 1..12
}

fun processPaymentWithTickets(
    pagamentoViewModel: PagamentoViewModel,
    ordineViewModel: OrdineViewModel,
    bigliettoViewModel: BigliettoViewModel,
    biglietti: List<BigliettoData>,
    nomeTitolare: String,
    cognomeTitolare: String,
    numeroCarta: String,
    meseScadenza: String,
    annoScadenza: String,
    cvv: String,
    prezzoFinale: Double,
    userData: UtenteData?,
    onProcessingStart: () -> Unit,
    onProcessingEnd: () -> Unit
) {
    onProcessingStart()

    // Reset stati precedenti
    pagamentoViewModel.resetPaymentState()
    ordineViewModel.resetOrdineState()
    bigliettoViewModel.resetState()

    // Step 1: Crea l'ordine
    val ordineData = OrdineData(
        emailProprietario = userData?.email ?: "guest@example.com",
        prezzoTotale = prezzoFinale
    )

    ordineViewModel.aggiungiOrdine(
        ordineData = ordineData,
        idProprietario = userData?.id ?: 1L
    ) { ordineCreato ->
        // Step 2: Processa il pagamento
        val pagamentoRequest = PagamentoRequest(
            nomeTitolare = nomeTitolare,
            cognomeTitolare = cognomeTitolare,
            numeroCarta = numeroCarta,
            scadenza = "$meseScadenza/$annoScadenza",
            cvv = cvv,
            importo = prezzoFinale.toString()
        )

        ordineCreato.id?.let { ordineId ->
            pagamentoViewModel.createPagamento(
                ordineId = ordineId,
                pagamentoRequest = pagamentoRequest
            ) { pagamentoCreato ->
                // Step 3: Crea i biglietti nel database dopo il pagamento
                pagamentoCreato.id?.let { pagamentoId ->
                    createTicketsInDatabase(
                        bigliettoViewModel = bigliettoViewModel,
                        biglietti = biglietti,
                        userData = userData,
                        onAllTicketsCreated = { onProcessingEnd() },
                        onError = { onProcessingEnd() },
                        pagamentoId = pagamentoId
                    )
                } ?: run {
                    onProcessingEnd()
                }
            }
        } ?: run {
            onProcessingEnd()
        }
    }
}

fun createTicketsInDatabase(
    bigliettoViewModel: BigliettoViewModel,
    biglietti: List<BigliettoData>,
    pagamentoId: Long,
    onAllTicketsCreated: () -> Unit,
    onError: () -> Unit,
    userData: UtenteData?
) {
    var createdCount = 0
    val totalTickets = biglietti.size

    biglietti.forEach { biglietto ->
        val bigliettoConPagamento = biglietto.copy(pagamentoId = pagamentoId)

        bigliettoViewModel.createBiglietto(
            bigliettoCreateData = bigliettoConPagamento,
            onSuccess = { createdBiglietto ->
                createdCount++
                println("✅ Biglietto creato: $createdCount/$totalTickets")
                if (createdCount == totalTickets) {
                    onAllTicketsCreated()
                }
            }
        )
    }
}

