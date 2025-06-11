package com.example.eventra.screens

import android.content.res.Configuration
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.Visibilita
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.StrutturaViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.StrutturaInfoUtenteData
import com.example.eventra.viewmodels.data.StrutturaMapInfoData
import com.example.eventra.viewmodels.data.UtenteData
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun EventDetailScreen(
    eventoId: Long,
    onBackPressed: () -> Unit,
    onNavigateToBiglietto: (Long) -> Unit = {},
    wishlistViewModel: WishlistViewModel? = null
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val isUserLoggedIn = remember { sessionManager.isLoggedIn() }

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }

    val strutturaViewModel: StrutturaViewModel = viewModel {
        StrutturaViewModel(context.applicationContext as android.app.Application)
    }

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as android.app.Application)
    }

    val eventoDetail by eventiViewModel.eventoDetail.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()
    val userData by profileViewModel.userData.collectAsState()


    val strutturaInfo by strutturaViewModel.strutturaInfo.collectAsState()
    val strutturaMapInfo by strutturaViewModel.strutturaMapInfo.collectAsState()

    var isVisible by remember { mutableStateOf(false) }
    var showLoginAlert by remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        if (isUserLoggedIn) {
            profileViewModel.loadUserProfile()
        }
    }


    LaunchedEffect(eventoId) {
        eventiViewModel.getEventoById(eventoId)
        strutturaViewModel.getStrutturaByEventoId(eventoId)
        delay(50)
        isVisible = true
    }


    LaunchedEffect(strutturaInfo) {
        strutturaInfo?.let { info ->
            strutturaViewModel.getStrutturaMapInfo(info.id)
        }
    }
    LaunchedEffect(userData?.id) {
        if (isUserLoggedIn && userData?.id != null && wishlistViewModel != null) {
            wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                userData!!.id,
                Visibilita.PRIVATA
            )
        }
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(durationMillis = 300, easing = FastOutLinearInEasing)
        ) + fadeOut(animationSpec = tween(durationMillis = 200))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(EventraColors.BackgroundGray)
                .zIndex(999f)
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            } else if (eventoDetail != null) {
                EventDetailContent(
                    evento = eventoDetail!!,
                    strutturaInfo = strutturaInfo,
                    strutturaMapInfo = strutturaMapInfo,
                    onBackPressed = {
                        isVisible = false
                        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                            delay(300)
                            onBackPressed()
                        }
                    },
                    onNavigateToBiglietto = { eventoId ->
                        if (isUserLoggedIn) {
                            onNavigateToBiglietto(eventoId)
                        } else {
                            showLoginAlert = true
                        }
                    },
                    wishlistViewModel = if (isUserLoggedIn) wishlistViewModel else null,
                    userData = userData,
                    isUserLoggedIn = isUserLoggedIn
                )
            } else {
                EventDetailErrorState(onBackPressed = onBackPressed)
            }


            if (showLoginAlert) {
                LoginRequiredAlert(
                    onDismiss = { showLoginAlert = false }
                )
            }
        }
    }
}

@Composable
fun EventDetailContent(
    evento: EventoData,
    strutturaInfo: StrutturaInfoUtenteData?,
    strutturaMapInfo: StrutturaMapInfoData?,
    onBackPressed: () -> Unit,
    onNavigateToBiglietto: (Long) -> Unit = {},
    wishlistViewModel: WishlistViewModel?,
    userData: UtenteData?,
    isUserLoggedIn: Boolean
) {
    val scrollState = rememberScrollState()
    val baseUrl = "http://10.0.2.2:8080/images/"

    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }
    val wishlistsByVisibilita by if (wishlistViewModel != null) {
        wishlistViewModel.wishlistsByVisibilita.collectAsState()
    } else {
        remember { mutableStateOf(emptyList()) }
    }

    val isInWishlist = remember(wishlistsByVisibilita, evento.id) {
        if (isUserLoggedIn) {
            wishlistsByVisibilita?.any { wishlist ->
                wishlist.eventi.contains(evento.id)
            } ?: false
        } else {
            false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            EventDetailHeader(
                imageUrl = imageUrl,
                eventoNome = evento.nome ?: "Evento",
                onBackPressed = onBackPressed,
                isInWishlist = isInWishlist,
                showWishlistButton = isUserLoggedIn,
                onWishlistToggle = {
                    if (isUserLoggedIn && wishlistViewModel != null && userData != null) {
                        wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                            if (wishlistId != null) {
                                if (isInWishlist) {
                                    wishlistViewModel.removeEventoFromWishlist(wishlistId, evento.id) {
                                        wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                            userData.id,
                                            Visibilita.PRIVATA
                                        )
                                    }
                                } else {
                                    wishlistViewModel.addEventoToWishlist(wishlistId, evento.id) {
                                        wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                            userData.id,
                                            Visibilita.PRIVATA
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            )
            EventDetailMainContent(
                evento = evento,
                strutturaInfo = strutturaInfo,
                strutturaMapInfo = strutturaMapInfo,
                onNavigateToBiglietto = onNavigateToBiglietto,
                isUserLoggedIn = isUserLoggedIn
            )
        }
    }
}

@Composable
fun EventDetailHeader(
    imageUrl: String?,
    eventoNome: String,
    onBackPressed: () -> Unit,
    isInWishlist: Boolean,
    showWishlistButton: Boolean,
    onWishlistToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = eventoNome,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EventraColors.DividerGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro",
                tint = EventraColors.TextDark
            )
        }
        if (showWishlistButton) {
            IconButton(
                onClick = onWishlistToggle,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        Color.White.copy(alpha = 0.9f),
                        shape = CircleShape
                    )
                    .size(48.dp)
            ) {
                Icon(
                    imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isInWishlist) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                    tint = if (isInWishlist) EventraColors.PrimaryOrange else EventraColors.TextGray
                )
            }
        }
        Text(
            text = eventoNome,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            lineHeight = 32.sp
        )
    }
}

@Composable
fun EventDetailMainContent(
    evento: EventoData,
    onNavigateToBiglietto: (Long) -> Unit = {},
    strutturaInfo: StrutturaInfoUtenteData?,
    strutturaMapInfo: StrutturaMapInfoData?,
    isUserLoggedIn: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        if (isUserLoggedIn) {
            EventPurchaseButton(
                onPurchaseClick = {
                    onNavigateToBiglietto(evento.id)
                }
            )
        } else {
            EventPurchaseButtonNotLoggedIn()
        }
        EventDetailInfoCard(evento = evento)
        if (!evento.descrizione.isNullOrBlank()) {
            EventDetailDescriptionCard(descrizione = evento.descrizione)
        }
        StrutturaInfoCard(strutturaInfo = strutturaInfo)
        StrutturaMapOnlyCard(
            strutturaInfo = strutturaInfo,
            mapInfo = strutturaMapInfo
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun EventPurchaseButtonNotLoggedIn(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Acquista Biglietto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Login,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = "Accesso Richiesto",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Per acquistare i biglietti è necessario effettuare l'accesso.\nVai alla sezione Profilo per accedere o creare un account.",
                    fontSize = 14.sp,
                    color = EventraColors.TextGray,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )
            }
        }
    }
}

@Composable
fun EventDetailErrorState(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(64.dp)
                )

                Text(
                    text = "Errore nel caricamento",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = EventraColors.TextDark,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Non è stato possibile caricare i dettagli dell'evento",
                    fontSize = 14.sp,
                    color = EventraColors.TextGray,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onBackPressed,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EventraColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Torna Indietro",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}


@Composable
fun EventDetailHeader(
    imageUrl: String?,
    eventoNome: String,
    onBackPressed: () -> Unit,
    isInWishlist: Boolean,
    onWishlistToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
        if (!imageUrl.isNullOrBlank()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = eventoNome,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(EventraColors.DividerGray),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(80.dp)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.7f)
                        )
                    )
                )
        )
        IconButton(
            onClick = onBackPressed,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Indietro",
                tint = EventraColors.TextDark
            )
        }
        IconButton(
            onClick = onWishlistToggle,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .background(
                    Color.White.copy(alpha = 0.9f),
                    shape = CircleShape
                )
                .size(48.dp)
        ) {
            Icon(
                imageVector = if (isInWishlist) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isInWishlist) "Rimuovi dai preferiti" else "Aggiungi ai preferiti",
                tint = if (isInWishlist) EventraColors.PrimaryOrange else EventraColors.TextGray
            )
        }
        Text(
            text = eventoNome,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            lineHeight = 32.sp
        )
    }
}

@Composable
fun EventDetailMainContent(
    evento: EventoData,
    onNavigateToBiglietto: (Long) -> Unit = {},
    strutturaInfo: StrutturaInfoUtenteData?,
    strutturaMapInfo: StrutturaMapInfoData?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        EventPurchaseButton(
            onPurchaseClick = {
                onNavigateToBiglietto(evento.id)  // ECCO COSA METTERE QUI
            }
        )
        EventDetailInfoCard(evento = evento)
        if (!evento.descrizione.isNullOrBlank()) {
            EventDetailDescriptionCard(descrizione = evento.descrizione)
        }
        StrutturaInfoCard(strutturaInfo = strutturaInfo)
        StrutturaMapOnlyCard(
            strutturaInfo = strutturaInfo,
            mapInfo = strutturaMapInfo
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun EventPurchaseButton(
    onPurchaseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Acquista Biglietto",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }

            Button(
                onClick = onPurchaseClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Acquista Ora",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Text(
                text = "Clicca per procedere all'acquisto del biglietto",
                fontSize = 14.sp,
                color = EventraColors.TextGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EventDetailInfoCard(evento: EventoData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EventDetailInfoRow(
                icon = Icons.Default.CalendarToday,
                title = "Data e Ora",
                content = evento.dataOraEvento
            )

            EventDetailInfoRow(
                icon = Icons.Default.Schedule,
                title = "Apertura Cancelli",
                content = evento.dataOraAperturaCancelli
            )

            EventDetailInfoRow(
                icon = Icons.Default.LocationOn,
                title = "Luogo",
                content = evento.luogo
            )

            EventDetailInfoRow(
                icon = Icons.Default.People,
                title = "Posti Disponibili",
                content = "${evento.postiDisponibili} posti"
            )
        }
    }
}

@Composable
fun EventDetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    content: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = EventraColors.PrimaryOrange,
            modifier = Modifier
                .size(24.dp)
                .padding(top = 2.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = EventraColors.TextGray
            )

            Text(
                text = content,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = EventraColors.TextDark,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun EventDetailDescriptionCard(descrizione: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Descrizione",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = descrizione,
                fontSize = 16.sp,
                color = EventraColors.TextDark,
                lineHeight = 24.sp
            )
        }
    }
}

@Composable
fun StrutturaInfoCard(
    strutturaInfo: StrutturaInfoUtenteData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Info Struttura",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }

            if (strutturaInfo != null) {
                Text(
                    text = strutturaInfo.nome ?: "Nome non disponibile",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EventraColors.TextDark
                )
                Text(
                    text = strutturaInfo.indirizzo ?: "Indirizzo non disponibile",
                    fontSize = 14.sp,
                    color = EventraColors.TextGray
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = EventraColors.PrimaryOrange,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StrutturaMapOnlyCard(
    strutturaInfo: StrutturaInfoUtenteData?,
    mapInfo: StrutturaMapInfoData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = "Posizione struttura",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }

            if (strutturaInfo != null && mapInfo != null) {
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            EventraColors.DividerGray,
                            RoundedCornerShape(12.dp)
                        ),
                    factory = { ctx ->
                         org.osmdroid.config.Configuration.getInstance().load(
                            ctx.applicationContext,
                            androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)
                        )

                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)

                            controller.setZoom(15.0)
                            val point = GeoPoint(
                                mapInfo.coordinateLatitude.toDoubleOrNull() ?: 0.0,
                                mapInfo.coordinateLongitude.toDoubleOrNull() ?: 0.0
                            )
                            controller.setCenter(point)

                            val marker = Marker(this)
                            marker.position = point
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = strutturaInfo.nome ?: "Struttura"
                            overlays.add(marker)
                        }
                    },
                    update = { mapView ->
                        val point = GeoPoint(
                            mapInfo.coordinateLatitude.toDoubleOrNull() ?: 0.0,
                            mapInfo.coordinateLongitude.toDoubleOrNull() ?: 0.0
                        )
                        mapView.controller.setCenter(point)
                        mapView.overlays.clear()
                        val marker = Marker(mapView)
                        marker.position = point
                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        marker.title = strutturaInfo.nome ?: "Struttura"
                        mapView.overlays.add(marker)
                        mapView.invalidate()
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .background(
                            EventraColors.DividerGray,
                            RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = EventraColors.PrimaryOrange,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }

}

