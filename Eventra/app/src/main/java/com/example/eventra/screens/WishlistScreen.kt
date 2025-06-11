package com.example.eventra.screens

import android.annotation.SuppressLint
import android.app.Application
import android.util.Patterns
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.Visibilita
import com.example.eventra.untils.SessionManager
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.UtenteData
import com.example.eventra.viewmodels.data.WishlistData
import kotlinx.coroutines.delay

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WishlistScreen(
    onNavigateToEventDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val wishlistViewModel: WishlistViewModel = viewModel {
        WishlistViewModel(context.applicationContext as Application)
    }

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as Application)
    }

    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as Application)
    }
    val wishlistsByVisibilita by wishlistViewModel.wishlistsByVisibilita.collectAsState()
    val wishlistCondivise by wishlistViewModel.wishlistCondivise.collectAsState()
    val eventi by eventiViewModel.eventi.collectAsState()
    val isLoading by wishlistViewModel.isLoading.collectAsState()
    val userData by profileViewModel.userData.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    var showLoginAlert by remember { mutableStateOf(false) }

    val isUserLoggedIn = remember { sessionManager.isLoggedIn() }
    LaunchedEffect(Unit) {
        if (isUserLoggedIn) {
            profileViewModel.loadUserProfile()
            eventiViewModel.getAllEventi()
        }
    }
    LaunchedEffect(userData?.id) {
        if (isUserLoggedIn && userData?.id != null) {
            wishlistViewModel.getWishlistsByUtenteAndVisibilita(userData!!.id, Visibilita.PRIVATA)
            wishlistViewModel.getWishlistCondiviseConUtente(userData!!.id)
        }
    }
    val eventiWishlistPrivate = remember(wishlistsByVisibilita, eventi) {
        val eventiIds = wishlistsByVisibilita?.flatMap { it.eventi }?.toSet() ?: emptySet()
        eventi?.filter { eventiIds.contains(it.id) } ?: emptyList()
    }

    if (!isUserLoggedIn) {
        WishlistNotLoggedInScreen()
        return
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
                WishlistHeader()
            }
            item {
                WishlistTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    privateCount = eventiWishlistPrivate.size,
                    sharedCount = wishlistCondivise?.size ?: 0,
                    showShareButton = eventiWishlistPrivate.isNotEmpty(),
                    wishlistViewModel = wishlistViewModel,
                    userData = userData
                )
            }
            when (selectedTab) {
                0 -> {
                    if (eventiWishlistPrivate.isNotEmpty()) {
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(((eventiWishlistPrivate.size / 2 + eventiWishlistPrivate.size % 2) * 300).dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                userScrollEnabled = false
                            ) {
                                items(eventiWishlistPrivate) { evento ->
                                    WishlistEventCard(
                                        evento = evento,
                                        wishlistViewModel = wishlistViewModel,
                                        userData = userData,
                                        isInWishlistContext = true,
                                        onClick = { onNavigateToEventDetail(evento.id) }
                                    )
                                }
                            }
                        }
                    } else {
                        item {
                            WishlistEmptyState(
                                title = "Nessun evento salvato",
                                subtitle = "Inizia ad aggiungere eventi alle tue wishlist!",
                                icon = Icons.Default.FavoriteBorder
                            )
                        }
                    }
                }
                1 -> {
                    item {
                        WishlistSectionHeader(
                            title = "Wishlist Condivise",
                            subtitle = "${wishlistCondivise?.size ?: 0} wishlist condivise con te"
                        )
                    }

                    if (!wishlistCondivise.isNullOrEmpty()) {
                        items(wishlistCondivise!!) { wishlist ->
                            WishlistCondivisaCard(
                                wishlist = wishlist,
                                eventi = eventi ?: emptyList(),
                                wishlistViewModel = wishlistViewModel,
                                userData = userData,
                                onNavigateToEventDetail = { eventoId ->
                                    onNavigateToEventDetail(eventoId)
                                }
                            )
                        }
                    } else {
                        item {
                            WishlistEmptyState(
                                title = "Nessuna wishlist condivisa",
                                subtitle = "Non hai ancora ricevuto wishlist condivise da altri utenti.",
                                icon = Icons.Default.Share
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
        if (showLoginAlert) {
            LoginRequiredAlert(
                onDismiss = { showLoginAlert = false }
            )
        }
    }
}

@Composable
fun WishlistNotLoggedInScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(EventraColors.BackgroundGray)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            WishlistHeader()
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
                        imageVector = Icons.Default.Login,
                        contentDescription = null,
                        tint = EventraColors.PrimaryOrange,
                        modifier = Modifier.size(64.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Accesso Richiesto",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = EventraColors.TextDark,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Per gestire le tue wishlist è necessario effettuare l'accesso.\n\nVai alla sezione Profilo per accedere o creare un account.",
                        fontSize = 14.sp,
                        color = EventraColors.TextGray,
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}


@Composable
fun WishlistCondivisaCard(
    wishlist: WishlistData,
    eventi: List<EventoData>,
    wishlistViewModel: WishlistViewModel,
    userData: UtenteData?,
    onNavigateToEventDetail: (Long) -> Unit
) {
    val eventiWishlist = remember(wishlist.eventi, eventi) {
        eventi.filter { evento -> wishlist.eventi.contains(evento.id) }
    }

    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { isExpanded = !isExpanded },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Wishlist #${wishlist.id}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = EventraColors.TextDark
                        )

                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Wishlist condivisa",
                            tint = EventraColors.PrimaryOrange,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Text(
                        text = "${eventiWishlist.size} eventi",
                        fontSize = 14.sp,
                        color = EventraColors.TextGray
                    )

                    Text(
                        text = "Creata il ${formatDate(wishlist.dataCreazione)}",
                        fontSize = 12.sp,
                        color = EventraColors.TextGray,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (isExpanded) "Comprimi" else "Espandi",
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(24.dp)
                )
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Divider(
                        color = EventraColors.DividerGray,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    if (eventiWishlist.isNotEmpty()) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((eventiWishlist.size / 2 + eventiWishlist.size % 2) * 300).dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = false
                        ) {
                            items(eventiWishlist) { evento ->
                                WishlistEventCardCondivisa(
                                    evento = evento,
                                    wishlist = wishlist,
                                    wishlistViewModel = wishlistViewModel,
                                    userData = userData,
                                    onNavigateToEventDetail = onNavigateToEventDetail
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EventBusy,
                                    contentDescription = null,
                                    tint = EventraColors.TextGray,
                                    modifier = Modifier.size(48.dp)
                                )

                                Text(
                                    text = "Nessun evento in questa wishlist",
                                    fontSize = 14.sp,
                                    color = EventraColors.TextGray,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WishlistEventCardCondivisa(
    evento: EventoData,
    wishlist: WishlistData,
    modifier: Modifier = Modifier,
    wishlistViewModel: WishlistViewModel,
    userData: UtenteData?,
    onNavigateToEventDetail: (Long) -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "card_scale"
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
                onNavigateToEventDetail(evento.id)
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

                // Badge per indicare che è una wishlist condivisa (solo visivo)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(10.dp)
                        .size(36.dp)
                        .background(
                            EventraColors.PrimaryOrange.copy(alpha = 0.9f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Evento condiviso",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

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

                    Spacer(modifier = Modifier.height(8.dp))

                    // Footer con informazioni e call-to-action
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
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

                            // Indicatore che puoi acquistare
                            Card(
                                colors = CardDefaults.cardColors(
                                    EventraColors.PrimaryOrange.copy(0.1f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ShoppingCart,
                                        contentDescription = null,
                                        tint = EventraColors.PrimaryOrange,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = "Acquista",
                                        fontSize = 10.sp,
                                        color = EventraColors.PrimaryOrange,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
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
}

private fun formatDate(dateString: String): String {
    return try {
        val parts = dateString.split("T")[0].split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) {
        dateString
    }
}
@Composable
fun WishlistHeader() {
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
                .padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = "Wishlist",
                modifier = Modifier.size(64.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Le Tue Wishlist",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "I tuoi eventi preferiti in un posto",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun WishlistTabSelector(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    privateCount: Int,
    sharedCount: Int,
    showShareButton: Boolean,
    wishlistViewModel: WishlistViewModel,
    userData: UtenteData?
) {
    var showShareDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isWishlistCondivisa by wishlistViewModel.isWishlistCondivisa.collectAsState()
    val condivisioneState by wishlistViewModel.condivisioneState.collectAsState()

    LaunchedEffect(showShareButton, userData?.id) {
        if (showShareButton && userData?.id != null) {
            wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                if (wishlistId != null) {
                    wishlistViewModel.verificaSeWishlistECondivisa(wishlistId)
                }
            }
        }
    }

    LaunchedEffect(condivisioneState) {
        when (condivisioneState) {
            is WishlistViewModel.CondivisioneState.Success -> {
                wishlistViewModel.resetCondivisioneState()
            }
            is WishlistViewModel.CondivisioneState.Error -> {
                wishlistViewModel.resetCondivisioneState()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier.padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            WishlistTabItem(
                title = "Eventi",
                count = privateCount,
                isSelected = selectedTab == 0,
                onClick = { onTabSelected(0) },
                modifier = Modifier.weight(1f)
            )
            WishlistTabItem(
                title = "Condivise",
                count = sharedCount,
                isSelected = selectedTab == 1,
                onClick = { onTabSelected(1) },
                modifier = Modifier.weight(1f)
            )
            if (showShareButton && userData != null) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(88.dp),
                    verticalArrangement = if (isWishlistCondivisa) {
                        Arrangement.spacedBy(6.dp)
                    } else {
                        Arrangement.Center
                    },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (isWishlistCondivisa) 38.dp else 56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showShareDialog = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Condividi Wishlist",
                                tint = EventraColors.PrimaryOrange,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    if (isWishlistCondivisa) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showDeleteConfirmDialog = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PersonRemove,
                                    contentDescription = "Elimina Condivisioni",
                                    tint = Color.Red,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    if (showShareDialog) {
        ShareDialog(
            onDismiss = { showShareDialog = false },
            onShare = { email ->
                if (userData != null) {
                    wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                        if (wishlistId != null) {
                            wishlistViewModel.condividiWishlistConEmail(wishlistId, email)
                        }
                    }
                }
                showShareDialog = false
            }
        )
    }
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = {
                Text(
                    text = "Elimina Condivisioni",
                    fontWeight = FontWeight.Bold,
                    color = Color.Red
                )
            },
            text = {
                Text("Sei sicuro di voler rimuovere tutte le condivisioni di questa wishlist? Questa azione non può essere annullata.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (userData != null) {
                            wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                                if (wishlistId != null) {
                                    wishlistViewModel.rimuoviTutteCondivisioni(wishlistId) {
                                        wishlistViewModel.getWishlistCondiviseConUtente(userData.id)
                                    }
                                }
                            }
                        }
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Elimina", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
fun WishlistTabItem(
    title: String,
    count: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    Card(
        modifier = modifier
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                EventraColors.PrimaryOrange
            else
                EventraColors.CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 8.dp else 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else EventraColors.PrimaryOrange
            )

            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else EventraColors.TextDark
            )
        }
    }
}

@Composable
fun WishlistSectionHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = EventraColors.TextDark
        )
        Text(
            text = subtitle,
            fontSize = 14.sp,
            color = EventraColors.TextGray
        )
    }
}
@Composable
fun WishlistEventCard(
    evento: EventoData,
    modifier: Modifier = Modifier,
    wishlistViewModel: WishlistViewModel? = null,
    userData: UtenteData?,
    isInWishlistContext: Boolean = false,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val wishlistsByVisibilita by wishlistViewModel?.wishlistsByVisibilita?.collectAsState()
        ?: remember { mutableStateOf(emptyList()) }
    val isInWishlist = if (isInWishlistContext) {
        true // Se siamo nel contesto wishlist, l'evento è già in wishlist
    } else {
        remember(wishlistsByVisibilita, evento.id) {
            wishlistsByVisibilita?.any { wishlist ->
                wishlist.eventi.contains(evento.id)
            } ?: false
        }
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
                                wishlistViewModel.getFirstPrivateWishlistId(userData.id) { wishlistId ->
                                    if (wishlistId != null) {
                                        if (isInWishlist) {
                                            wishlistViewModel.removeEventoFromWishlist(wishlistId, evento.id) {
                                                wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                                    userData.id, Visibilita.PRIVATA
                                                )
                                            }
                                        } else {
                                            if (!isInWishlistContext) {
                                                wishlistViewModel.addEventoToWishlist(wishlistId, evento.id) {
                                                    wishlistViewModel.getWishlistsByUtenteAndVisibilita(
                                                        userData.id, Visibilita.PRIVATA
                                                    )
                                                }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareDialog(
    onDismiss: () -> Unit,
    onShare: (String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var isEmailValid by remember { mutableStateOf(true) }
    val emailPattern = remember {
        Patterns.EMAIL_ADDRESS
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = EventraColors.PrimaryOrange
                )
                Text(
                    text = "Condividi Wishlist",
                    fontWeight = FontWeight.Bold,
                    color = EventraColors.TextDark
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Inserisci l'email dell'utente con cui vuoi condividere la tua wishlist:",
                    fontSize = 14.sp,
                    color = EventraColors.TextGray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = {
                        email = it
                        isEmailValid = emailPattern.matcher(it).matches() || it.isEmpty()
                    },
                    label = { Text("Email destinatario") },
                    placeholder = { Text("esempio@email.com") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = EventraColors.PrimaryOrange
                        )
                    },
                    isError = !isEmailValid,
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = EventraColors.PrimaryOrange,
                        focusedLabelColor = EventraColors.PrimaryOrange,
                        cursorColor = EventraColors.PrimaryOrange
                    )
                )

                if (!isEmailValid) {
                    Text(
                        text = "Inserisci un indirizzo email valido",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (email.isNotBlank() && isEmailValid) {
                        onShare(email)
                    }
                },
                enabled = email.isNotBlank() && isEmailValid,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                )
            ) {
                Text("Condividi", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = EventraColors.TextGray
                )
            ) {
                Text("Annulla")
            }
        },
        containerColor = EventraColors.CardWhite,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun WishlistEmptyState(
    title: String,
    subtitle: String,
    icon: ImageVector
) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = EventraColors.TextGray,
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = subtitle,
                fontSize = 14.sp,
                color = EventraColors.TextGray,
                textAlign = TextAlign.Center
            )
        }
    }
}