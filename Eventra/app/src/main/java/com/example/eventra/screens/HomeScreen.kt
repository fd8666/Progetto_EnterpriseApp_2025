package com.example.eventra.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.R
import com.example.eventra.Visibilita
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.TagCategoriaViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.TagCategoriaData
import kotlinx.coroutines.delay

// Colori
object EventraColors {
    val PrimaryOrange = Color(0xFFFF5722) // Arancione principale
    val DarkOrange = Color(0xFFE64A19) // Arancione scuro
    val LightOrange = Color(0xFFFFAB91) // Arancione molto chiaro
    val BackgroundGray = Color(0xFFF5F5F5) // Grigio chiaro di sfondo
    val CardWhite = Color(0xFFFFFFFF) // Bianco delle card
    val TextDark = Color(0xFF212121) // Testo scuro
    val TextGray = Color(0xFF757575) // Testo grigio
    val DividerGray = Color(0xFFE0E0E0) // Divisori
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen(
    onNavigateToEventDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isUserLoggedIn = remember { sessionManager.isLoggedIn() }

    var showLoginAlert by remember { mutableStateOf(false) }

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as android.app.Application)
    }
    val userData by profileViewModel.userData.collectAsState()

    val wishlistViewModel: WishlistViewModel = viewModel {
        WishlistViewModel(context.applicationContext as android.app.Application)
    }

    val eventi by eventiViewModel.eventi.collectAsState()
    val isLoadingEventi by eventiViewModel.isLoading.collectAsState()

    // Carica i dati iniziali
    LaunchedEffect(Unit) {
        eventiViewModel.getAllEventi()

        // Carica la wishlist solo se l'utente è loggato
        if (isUserLoggedIn) {
            profileViewModel.loadUserProfile()
        }
    }

    // Aggiorna la wishlist quando userData è disponibile
    LaunchedEffect(userData?.id) {
        if (isUserLoggedIn && userData?.id != null) {
            wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                userData!!.id,
                Visibilita.PRIVATA
            )
        }
    }

    val eventiFiltrati = remember(eventi) {
        eventi ?: emptyList()
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
            item {
                EventraHeader()
            }

            item {
                if (eventiFiltrati.isNotEmpty()) {
                    EventiInEvidenzaCarousel(
                        eventi = eventiFiltrati,
                        onEventClick = { eventoId ->
                            if (isUserLoggedIn) {
                                onNavigateToEventDetail(eventoId)
                            } else {
                                showLoginAlert = true
                            }
                        }
                    )
                }
            }

            item {
                SectionHeader(title = "Tutti gli Eventi", eventiCount = eventiFiltrati.size)
            }

            item {
                if (eventiFiltrati.isNotEmpty()) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(((eventiFiltrati.size / 2 + eventiFiltrati.size % 2) * 300).dp)
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        userScrollEnabled = false
                    ) {
                        items(eventiFiltrati) { evento ->
                            EventraEventCard(
                                evento = evento,
                                wishlistViewModel = if (isUserLoggedIn) wishlistViewModel else null,
                                userData = userData,
                                onClick = {
                                    if (isUserLoggedIn) {
                                        onNavigateToEventDetail(evento.id)
                                    } else {
                                        showLoginAlert = true
                                    }
                                }
                            )
                        }
                    }
                } else {
                    // Card quando non ci sono eventi
                    EmptyEventsCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Loading indicator
        if (isLoadingEventi) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }

        // Alert per login richiesto
        if (showLoginAlert) {
            LoginRequiredAlert(
                onDismiss = { showLoginAlert = false }
            )
        }
    }
}

@Composable
fun EventraHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
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
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.logoeventra),
                contentDescription = "Logo Eventra",
                modifier = Modifier
                    .height(120.dp)
                    .wrapContentWidth(),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "I tuoi eventi preferiti",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Scopri concerti, sport e spettacoli",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun EventiInEvidenzaCarousel(
    eventi: List<EventoData>,
    onEventClick: (Long) -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }

    val eventiInEvidenza = remember(eventi) {
        if (eventi.size >= 3) {
            eventi.shuffled().take(3)
        } else {
            eventi
        }
    }

    val scrollState = rememberLazyListState()

    LaunchedEffect(eventiInEvidenza) {
        if (eventiInEvidenza.isNotEmpty()) {
            while (true) {
                delay(5000)
                currentIndex = (currentIndex + 1) % eventiInEvidenza.size

                scrollState.animateScrollToItem(
                    index = currentIndex,
                    scrollOffset = -50
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "In Evidenza",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )


            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                repeat(eventiInEvidenza.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                color = if (index == currentIndex)
                                    EventraColors.PrimaryOrange
                                else
                                    EventraColors.DividerGray,
                                shape = CircleShape
                            )
                    )
                }
            }
        }

        if (eventiInEvidenza.isNotEmpty()) {
            LazyRow(
                state = scrollState,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                itemsIndexed(eventiInEvidenza) { index, evento ->
                    val isCurrentEvent = index == currentIndex

                    EventoInEvidenzaCard(
                        evento = evento,
                        isHighlighted = isCurrentEvent,
                        onClick = { onEventClick(evento.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun EventoInEvidenzaCard(
    evento: EventoData,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isHighlighted) 1.05f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val baseUrl = "http://10.0.2.2:8080/images/"
    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }

    Card(
        modifier = Modifier
            .width(280.dp)
            .height(200.dp)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isHighlighted) 12.dp else 6.dp
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = evento.nome,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            EventraColors.DividerGray,
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = EventraColors.TextGray,
                        modifier = Modifier.size(60.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.7f)
                            )
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = evento.nome ?: "Evento",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "da €25 • ${evento.postiDisponibili} posti",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            // Badge "IN EVIDENZA" per l'evento corrente
            if (isHighlighted) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .background(
                            EventraColors.PrimaryOrange,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "IN EVIDENZA",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}




@Composable
fun SectionHeader(title: String, eventiCount: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )
            Text(
                text = "$eventiCount eventi disponibili",
                fontSize = 14.sp,
                color = EventraColors.TextGray
            )
        }
    }
}
@Composable
fun EventraEventCard(
    evento: EventoData,
    modifier: Modifier = Modifier,
    wishlistViewModel: WishlistViewModel? = null,
    userData: com.example.eventra.viewmodels.data.UtenteData?,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Ottieni lo stato della wishlist dal ViewModel solo se è presente
    val wishlistsByVisibilita by wishlistViewModel?.wishlistsByVisibilita?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }

    // Controlla se l'evento è nella wishlist
    val isInWishlist = remember(wishlistsByVisibilita, evento.id) {
        wishlistsByVisibilita?.any { wishlist ->
            wishlist.eventi.contains(evento.id)
        } ?: false
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
    )

    val heartScale by animateFloatAsState(
        targetValue = if (isInWishlist) 1.2f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "heart_scale"
    )

    val baseUrl = "http://10.0.2.2:8080/images/"
    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                if (!imageUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = evento.nome,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                EventraColors.DividerGray,
                                RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = EventraColors.TextGray,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }

                // Cuore per gestire la wishlist - solo se l'utente è loggato
                if (wishlistViewModel != null && userData != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp)
                            .size(36.dp)
                            .background(
                                Color.White.copy(alpha = 0.9f),
                                shape = CircleShape
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                // Gestione del click sul cuore
                                wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                                    if (wishlistId != null) {
                                        if (isInWishlist) {
                                            // Rimuovi dalla wishlist
                                            wishlistViewModel.removeEventoFromWishlist(wishlistId, evento.id) {
                                                wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                                    userData.id, Visibilita.PRIVATA
                                                )
                                            }
                                        } else {
                                            // Aggiungi alla wishlist
                                            wishlistViewModel.addEventoToWishlist(wishlistId, evento.id) {
                                                wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                                    userData.id, Visibilita.PRIVATA
                                                )
                                            }
                                        }
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isInWishlist) "Rimuovi dalla wishlist" else "Aggiungi alla wishlist",
                            tint = if (isInWishlist) EventraColors.PrimaryOrange else EventraColors.TextGray,
                            modifier = Modifier
                                .size(20.dp)
                                .scale(heartScale)
                        )
                    }
                }
            }

            // Contenuto della card
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = evento.nome ?: "Evento",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = EventraColors.TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = evento.luogo,
                            fontSize = 11.sp,
                            color = EventraColors.TextGray,
                            modifier = Modifier.padding(start = 3.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            tint = EventraColors.TextGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = evento.dataOraEvento,
                            fontSize = 11.sp,
                            color = EventraColors.TextGray,
                            modifier = Modifier.padding(start = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Column {
                    Text(
                        text = "${evento.postiDisponibili} posti",
                        fontSize = 13.sp,
                        color = EventraColors.PrimaryOrange,
                        fontWeight = FontWeight.Medium
                    )

                    Text(
                        text = "da €25",
                        fontSize = 14.sp,
                        color = EventraColors.TextDark,
                        fontWeight = FontWeight.Bold
                    )
                }
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
fun EmptyEventsCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EventNote,
                contentDescription = null,
                tint = EventraColors.TextGray,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nessun evento disponibile",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Al momento non ci sono eventi in programma. Torna presto per nuove opportunità!",
                fontSize = 14.sp,
                color = EventraColors.TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun LoadingIndicator() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Card(
        modifier = Modifier.size(80.dp),
        shape = RoundedCornerShape(40.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Loading",
                tint = EventraColors.PrimaryOrange,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer { rotationZ = rotation }
            )
        }
    }
}

fun getCategorieIcone(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "concerti" -> Icons.Default.MusicNote
        "sport" -> Icons.Default.SportsSoccer
        "intrattenimento" -> Icons.Default.Movie
        else -> Icons.Default.Category
    }
}
@Composable
fun LoginRequiredAlert(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange
                )
                Text(
                    text = "Accesso Richiesto",
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }
        },
        text = {
            Text(
                text = "Per visualizzare i dettagli degli eventi è necessario effettuare l'accesso o registrarsi.\n\nVai alla sezione Profilo per accedere o creare un account.",
                color = EventraColors.TextDark,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = EventraColors.PrimaryOrange
                )
            ) {
                Text(
                    text = "Ho Capito",
                    fontWeight = FontWeight.Medium
                )
            }
        },
        containerColor = EventraColors.CardWhite,
        shape = RoundedCornerShape(16.dp)
    )
}