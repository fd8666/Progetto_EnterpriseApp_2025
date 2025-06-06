package com.example.eventra.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.Visibilita
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.WishlistData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WishlistScreen() {
    val context = LocalContext.current

    val wishlistViewModel: WishlistViewModel = viewModel {
        WishlistViewModel(context.applicationContext as android.app.Application)
    }

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val condivisioneState by wishlistViewModel.condivisioneState.collectAsState()
    var selectedVisibilita by remember { mutableStateOf(Visibilita.PRIVATA) }

    // Stati
    val wishlists by wishlistViewModel.wishlists.collectAsState()
    val wishlistsByVisibilita by wishlistViewModel.wishlistsByVisibilita.collectAsState()
    val wishlistCondivise by wishlistViewModel.wishlistCondivise.collectAsState()
    val eventi by eventiViewModel.eventi.collectAsState()
    val isLoadingWishlists by wishlistViewModel.isLoading.collectAsState()
    val isLoadingEventi by eventiViewModel.isLoading.collectAsState()
    val errorWishlists by wishlistViewModel.error.collectAsState()

    // Gestione stato condivisione
    LaunchedEffect(condivisioneState) {
        when (condivisioneState) {
            is WishlistViewModel.CondivisioneState.Success -> {
                snackbarHostState.showSnackbar(
                    message = (condivisioneState as WishlistViewModel.CondivisioneState.Success).message,
                    duration = SnackbarDuration.Short
                )
                wishlistViewModel.resetCondivisioneState()
            }
            is WishlistViewModel.CondivisioneState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (condivisioneState as WishlistViewModel.CondivisioneState.Error).message,
                    duration = SnackbarDuration.Long
                )
                wishlistViewModel.resetCondivisioneState()
            }
            else -> { /* Nessuna azione */ }
        }
    }

    // Wishlist da visualizzare basate sulla visibilità selezionata
    val wishlistsToDisplay = remember(wishlistsByVisibilita, wishlistCondivise, selectedVisibilita) {
        when (selectedVisibilita) {
            Visibilita.CONDIVISA -> wishlistCondivise ?: emptyList()
            else -> wishlistsByVisibilita ?: emptyList()
        }
    }

    // Caricamento iniziale eventi
    LaunchedEffect(Unit) {
        eventiViewModel.getAllEventi()
    }

    // Caricamento wishlist quando cambiano eventi o visibilità
    LaunchedEffect(eventi, selectedVisibilita) {
        if (!eventi.isNullOrEmpty()) {
            when (selectedVisibilita) {
                Visibilita.PRIVATA -> {
                    wishlistViewModel.getWishlistsByUtenteAndVisibilita(1, Visibilita.PRIVATA)
                }
                Visibilita.CONDIVISA -> {
                    wishlistViewModel.getWishlistCondiviseConUtente(1)
                }
                else -> {
                    wishlistViewModel.getWishlistsByUtenteAndVisibilita(1, selectedVisibilita)
                }
            }
        }
    }

    // Funzione per ricaricare le wishlist correnti
    fun reloadCurrentWishlists() {
        if (!eventi.isNullOrEmpty()) {
            when (selectedVisibilita) {
                Visibilita.PRIVATA -> {
                    wishlistViewModel.getWishlistsByUtenteAndVisibilita(1, Visibilita.PRIVATA)
                }
                Visibilita.CONDIVISA -> {
                    wishlistViewModel.getWishlistCondiviseConUtente(1)
                }
                else -> {
                    wishlistViewModel.getWishlistsByUtenteAndVisibilita(1, selectedVisibilita)
                }
            }
        }
    }

    // ID della prima wishlist privata (per la condivisione)
    val firstPrivateWishlistId = remember(wishlistsToDisplay, selectedVisibilita) {
        if (selectedVisibilita == Visibilita.PRIVATA && wishlistsToDisplay.isNotEmpty()) {
            wishlistsToDisplay.first().id
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF1A0A38),
                        Color(0xFF24095A),
                        Color(0xFF0B1D5D),
                        Color(0xFF000B3C)
                    )
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))
            }

            // Titolo principale
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LA MIA WISHLIST",
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        letterSpacing = 2.sp,
                        style = androidx.compose.ui.text.TextStyle(
                            color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                            shadow = androidx.compose.ui.graphics.Shadow(
                                color = Color(0xFFFFFFFF).copy(alpha = 0.5f),
                                offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                                blurRadius = 8f
                            )
                        ),
                    )
                }
            }

            // Selezione visibilità con pulsante condivisione integrato
            item {
                SelezioneVisibilita(
                    selectedVisibilita = selectedVisibilita,
                    onVisibilitaSelected = { visibilita ->
                        selectedVisibilita = visibilita
                    },
                    onCondividiWishlist = if (firstPrivateWishlistId != null) {
                        { wishlistId, email ->
                            wishlistViewModel.condividiWishlistConEmail(wishlistId, email)
                        }
                    } else null,
                    wishlistId = firstPrivateWishlistId
                )
            }

            // Gestione errori
            item {
                if (errorWishlists != null) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Red.copy(alpha = 0.1f)
                        )
                    ) {
                        Text(
                            text = "Errore: ${errorWishlists?.message}",
                            fontSize = 14.sp,
                            color = Color.Red,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }

            // Sezione eventi nelle wishlist
            wishlistsToDisplay.forEach { wishlist ->
                val eventiInWishlist = eventi?.filter { evento ->
                    wishlist.eventi.contains(evento.id)
                } ?: emptyList()

                item {
                    WishlistSection(
                        wishlist = wishlist,
                        eventiInWishlist = eventiInWishlist,
                        isCondivisa = selectedVisibilita == Visibilita.CONDIVISA,
                        onRemoveEvento = { eventoId ->
                            wishlistViewModel.removeEventoFromWishlist(
                                wishlistId = wishlist.id,
                                eventoId = eventoId
                            ) {
                                reloadCurrentWishlists()
                            }
                        },
                        onCondividiWishlist = { wishlistId, email ->
                            wishlistViewModel.condividiWishlistConEmail(wishlistId, email)
                        }
                    )
                }
            }

            // Messaggio quando non ci sono wishlist
            item {
                if (wishlistsToDisplay.isEmpty() && !isLoadingWishlists) {
                    WishlistVuota(
                        message = when (selectedVisibilita) {
                            Visibilita.CONDIVISA -> "Nessuna wishlist condivisa con te"
                            else -> "Nessuna wishlist ${visibilitaToString(selectedVisibilita).lowercase()}"
                        }
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Snackbar per messaggi di feedback
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) { snackbarData ->
            Snackbar(
                snackbarData = snackbarData,
                containerColor = when {
                    snackbarData.visuals.message.contains("successo", ignoreCase = true) ->
                        Color.Green.copy(alpha = 0.9f)
                    snackbarData.visuals.message.contains("errore", ignoreCase = true) ->
                        Color.Red.copy(alpha = 0.9f)
                    else -> MaterialTheme.colorScheme.inverseSurface
                },
                contentColor = Color.White
            )
        }

        // Indicatore di caricamento
        if (isLoadingWishlists || isLoadingEventi) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
            }
        }
    }
}

@Composable
fun SelezioneVisibilita(
    selectedVisibilita: Visibilita,
    onVisibilitaSelected: (Visibilita) -> Unit,
    onCondividiWishlist: ((Long, String) -> Unit)? = null,
    wishlistId: Long? = null,
    showCondividiButton: Boolean = false
) {
    var showCondivisioneDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Bottone PRIVATA
        Button(
            onClick = { onVisibilitaSelected(Visibilita.PRIVATA) },
            modifier = Modifier
                .weight(1f)
                .height(44.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedVisibilita == Visibilita.PRIVATA)
                    Color(0xFFF44336) else Color(0xFF1A0A38).copy(alpha = 0.8f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Privata",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "PRIVATA",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Bottone CONDIVISA CON ME
        Button(
            onClick = { onVisibilitaSelected(Visibilita.CONDIVISA) },
            modifier = Modifier
                .weight(1.5f)
                .height(44.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (selectedVisibilita == Visibilita.CONDIVISA)
                    Color(0xFF2196F3) else Color(0xFF2196F3).copy(alpha = 0.8f)
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Condivisa con me",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "CONDIVISA CON ME",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Pulsante di condivisione - SEMPRE VISIBILE se onCondividiWishlist non è null
        if (onCondividiWishlist != null) { // CAMBIATO: rimuovi la condizione showCondividiButton
            IconButton(
                onClick = { showCondivisioneDialog = true },
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = Color(0xFF4CAF50).copy(alpha = 0.9f),
                        shape = RoundedCornerShape(20.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Condividi wishlist",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    // Dialog per condivisione email
    if (onCondividiWishlist != null) {
        DialogCondivisioneEmail(
            showDialog = showCondivisioneDialog,
            onDismiss = { showCondivisioneDialog = false },
            onCondividi = { email ->
                onCondividiWishlist(wishlistId ?: 1L, email)
                showCondivisioneDialog = false
            }
        )
    }
}



@Composable
fun WishlistSection(
    wishlist: WishlistData,
    eventiInWishlist: List<EventoData>,
    isCondivisa: Boolean = false,
    onRemoveEvento: (Long) -> Unit,
    onCondividiWishlist: ((Long, String) -> Unit)? = null
) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A0A38).copy(alpha = 0.3f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header della wishlist - RIMOSSO IL PULSANTE DI CONDIVISIONE
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = getVisibilitaIcona(wishlist.visibilita),
                        contentDescription = visibilitaToString(wishlist.visibilita),
                        tint = getVisibilitaColore(wishlist.visibilita),
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isCondivisa) {
                            "CONDIVISA CON TE"
                        } else {
                            visibilitaToString(wishlist.visibilita)
                        },
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

            }

            // Resto del codice rimane uguale...
            Spacer(modifier = Modifier.height(12.dp))

            if (isCondivisa) {
                Text(
                    text = "${eventiInWishlist.size} eventi condivisi",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Grid di eventi - resto uguale
            if (eventiInWishlist.isNotEmpty()) {
                eventiInWishlist.chunked(2).forEach { rowEvents ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        rowEvents.forEach { evento ->
                            WishlistEventCard(
                                evento = evento,
                                onRemoveFromWishlist = {
                                    if (!isCondivisa) {
                                        onRemoveEvento(evento.id)
                                    }
                                },
                                onBuyTickets = { /* Implementa acquisto biglietti */ },
                                isCondivisa = isCondivisa,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        if (rowEvents.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isCondivisa) {
                            "Nessun evento in questa wishlist condivisa"
                        } else {
                            "Nessun evento in questa wishlist"
                        },
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}


@Composable
fun DialogCondivisioneEmail(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onCondividi: (String) -> Unit,
    isLoading: Boolean = false
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }

    // Funzione per validare l'email
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    // Reset email quando il dialog si chiude
    LaunchedEffect(showDialog) {
        if (!showDialog) {
            email = ""
            isEmailValid = true
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isLoading) onDismiss()
            },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Email,
                        contentDescription = null,
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Condividi Wishlist",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column {
                    Text(
                        text = "Inserisci l'email dell'utente con cui vuoi condividere la tua wishlist:",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = email,
                        onValueChange = {
                            email = it
                            isEmailValid = true
                        },
                        label = { Text("Email destinatario") },
                        placeholder = { Text("esempio@email.com") },
                        singleLine = true,
                        isError = !isEmailValid,
                        supportingText = {
                            if (!isEmailValid) {
                                Text(
                                    text = "Inserisci un indirizzo email valido",
                                    color = Color.Red,
                                    fontSize = 12.sp
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = "Email",
                                tint = if (isEmailValid) Color.Gray else Color.Red
                            )
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = {
                                val trimmedEmail = email.trim()
                                if (trimmedEmail.isNotBlank() && isValidEmail(trimmedEmail)) {
                                    onCondividi(trimmedEmail)
                                } else {
                                    isEmailValid = false
                                }
                            }
                        )
                    )

                    if (isLoading) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF4CAF50)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Condivisione in corso...",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmedEmail = email.trim()
                        if (trimmedEmail.isNotBlank() && isValidEmail(trimmedEmail)) {
                            onCondividi(trimmedEmail)
                        } else {
                            isEmailValid = false
                        }
                    },
                    enabled = !isLoading && email.trim().isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Condividendo...")
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Condividi")
                        }
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    enabled = !isLoading
                ) {
                    Text(
                        text = "Annulla",
                        color = Color.Gray
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun WishlistEventCard(
    evento: EventoData,
    onRemoveFromWishlist: () -> Unit,
    onBuyTickets: () -> Unit,
    isCondivisa: Boolean = false,
    modifier: Modifier = Modifier
) {
    val baseUrl = "http://10.0.2.2:8080/images/"
    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }

    Card(
        modifier = modifier
            .height(180.dp)
            .clickable { onBuyTickets() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            // Immagine di sfondo
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = evento.nome,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF1A0A38),
                                    Color(0xFF24095A)
                                )
                            )
                        )
                )
            }

            // Overlay scuro per migliorare la leggibilità del testo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
            )

            // Icona cuore per preferiti (solo per wishlist proprie)
            if (!isCondivisa) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Rimuovi dalla wishlist",
                    tint = Color.Red,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(24.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onRemoveFromWishlist()
                        }
                )
            } else {
                // Indicatore che è condivisa
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = "Evento condiviso",
                    tint = Color(0xFF2196F3),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(20.dp)
                )
            }

            // Testo e pulsante in basso
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Nome evento
                Text(
                    text = evento.nome ?: "Evento",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Button(

                    onClick = onBuyTickets,
                    modifier = Modifier
                        .height(32.dp)
                        .widthIn(min = 100.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2196F3) // BLU
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = "Acquista",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun WishlistVuota(message: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.BookmarkBorder,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = message,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Funzioni di supporto
fun getVisibilitaColore(visibilita: Visibilita): Color {
    return when(visibilita) {
        Visibilita.PRIVATA -> Color(0xFFF44336)
        Visibilita.CONDIVISA -> Color(0xFF2196F3)
        else -> Color(0xFF9C27B0)
    }
}

fun getVisibilitaIcona(visibilita: Visibilita): androidx.compose.ui.graphics.vector.ImageVector {
    return when(visibilita) {
        Visibilita.PRIVATA -> Icons.Default.Lock
        Visibilita.CONDIVISA -> Icons.Default.Share
        else -> Icons.Default.Visibility
    }
}

fun visibilitaToString(visibilita: Visibilita): String {
    return when(visibilita) {
        Visibilita.PRIVATA -> "PRIVATA"
        Visibilita.CONDIVISA -> "CONDIVISA"
        else -> visibilita.name.lowercase().replaceFirstChar { it.uppercase() }
    }
}