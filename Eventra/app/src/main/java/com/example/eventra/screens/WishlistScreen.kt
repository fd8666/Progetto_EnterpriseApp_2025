package com.example.eventra.screens

import android.annotation.SuppressLint
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.Visibilita
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.ProfileViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import kotlinx.coroutines.delay

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

    val profileViewModel: ProfileViewModel = viewModel { ProfileViewModel(context.applicationContext as android.app.Application) }
    val userData by profileViewModel.userData.collectAsState()

    val wishlistsByVisibilita by wishlistViewModel.wishlistsByVisibilita.collectAsState()
    val wishlistCondivise by wishlistViewModel.wishlistCondivise.collectAsState()
    val eventi by eventiViewModel.eventi.collectAsState()
    val isLoading by wishlistViewModel.isLoading.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    // Carica i dati
    LaunchedEffect(Unit) {
        wishlistViewModel.getWishlistsByUtenteAndVisibilita(userData?.id, Visibilita.PRIVATA)
        wishlistViewModel.getWishlistCondiviseConUtente(userData?.id)
        eventiViewModel.getAllEventi()
    }

    // Calcola gli eventi delle wishlist private
    val eventiWishlistPrivate = remember(wishlistsByVisibilita, eventi) {
        val eventiIds = wishlistsByVisibilita?.flatMap { it.eventi }?.toSet() ?: emptySet()
        eventi?.filter { eventiIds.contains(it.id) } ?: emptyList()
    }

    // Calcola gli eventi delle wishlist condivise
    val eventiWishlistCondivise = remember(wishlistCondivise, eventi) {
        val eventiIds = wishlistCondivise?.flatMap { it.eventi }?.toSet() ?: emptySet()
        eventi?.filter { eventiIds.contains(it.id) } ?: emptyList()
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
            // Header Wishlist
            item {
                WishlistHeader()
            }

            // Tab Selector
            item {
                WishlistTabSelector(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    privateCount = eventiWishlistPrivate.size,
                    sharedCount = eventiWishlistCondivise.size
                )
            }

            // Content based on selected tab
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
                                        isInWishlistContext = true,
                                        userData = userData,
                                        onClick = { /* dettagli evento */ }
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
                    // Wishlist Condivise
                    item {
                        WishlistSectionHeader(
                            title = "Wishlist Condivise",
                            subtitle = "${eventiWishlistCondivise.size} eventi condivisi"
                        )
                    }

                    if (eventiWishlistCondivise.isNotEmpty()) {
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(((eventiWishlistCondivise.size / 2 + eventiWishlistCondivise.size % 2) * 300).dp)
                                    .padding(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                userScrollEnabled = false
                            ) {
                                items(eventiWishlistCondivise) { evento ->
                                    WishlistEventCard(
                                        evento = evento,
                                        wishlistViewModel = wishlistViewModel,
                                        isInWishlistContext = true,
                                        userData = userData,
                                        onClick = { /* dettagli evento */ }
                                    )
                                }
                            }
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

        // Loading indicator
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        }
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
    sharedCount: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
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
    wishlistViewModel: WishlistViewModel,
    isInWishlistContext: Boolean = false,
    userData: com.example.eventra.viewmodels.data.UtenteData?,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    // Se siamo nel contesto wishlist, l'evento è sempre considerato nella wishlist
    val isInWishlist = if (isInWishlistContext) {
        true
    } else {
        val wishlistsByVisibilita by wishlistViewModel.wishlistsByVisibilita.collectAsState()
        remember(wishlistsByVisibilita, evento.id) {
            wishlistsByVisibilita?.any { wishlist ->
                wishlist.eventi.contains(evento.id)
            } ?: false
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
    )

    val heartScale by animateFloatAsState(
        targetValue = if (isInWishlist) 1.2f else 1.0f,
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

                // Cuore per gestire la wishlist
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
                            val wishlistId = wishlistViewModel.getFirstPrivateWishlistId()
                            if (wishlistId != null) {
                                if (isInWishlist) {
                                    // Rimuovi dalla wishlist
                                    wishlistViewModel.removeEventoFromWishlist(wishlistId, evento.id) {
                                        // Ricarica le wishlist dopo la rimozione
                                        if (isInWishlistContext) {
                                            wishlistViewModel.getWishlistsByUtenteAndVisibilita(userData?.id, Visibilita.PRIVATA)
                                            wishlistViewModel.getWishlistCondiviseConUtente(userData?.id)
                                        } else {
                                            wishlistViewModel.getWishlistsByUtenteAndVisibilita(userData?.id, Visibilita.PRIVATA)
                                        }
                                    }
                                } else {
                                    // Aggiungi alla wishlist (solo se non siamo nel contesto wishlist)
                                    if (!isInWishlistContext) {
                                        wishlistViewModel.addEventoToWishlist(wishlistId, evento.id) {
                                            wishlistViewModel.getWishlistsByUtenteAndVisibilita(userData?.id, Visibilita.PRIVATA)
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

            // Resto del contenuto della card
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
fun WishlistEmptyState(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
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
