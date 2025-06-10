package com.example.eventra.screens

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
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
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.StrutturaViewModel
import com.example.eventra.viewmodels.WishlistViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.StrutturaInfoUtenteData
import com.example.eventra.viewmodels.data.StrutturaMapInfoData
import com.example.eventra.Visibilita
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun EventDetailScreen(
    eventoId: Long,
    onBackPressed: () -> Unit,
    wishlistViewModel: WishlistViewModel? = null
) {
    val context = LocalContext.current

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }

    val strutturaViewModel: StrutturaViewModel = viewModel {
        StrutturaViewModel(context.applicationContext as android.app.Application)
    }

    val eventoDetail by eventiViewModel.eventoDetail.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()

    // Stati per la struttura
    val strutturaInfo by strutturaViewModel.strutturaInfo.collectAsState()
    val strutturaMapInfo by strutturaViewModel.strutturaMapInfo.collectAsState()

     var isVisible by remember { mutableStateOf(false) }

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
                    wishlistViewModel = wishlistViewModel
                )
            } else {

                EventDetailErrorState(onBackPressed = onBackPressed)
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
    wishlistViewModel: WishlistViewModel?
) {
    val scrollState = rememberScrollState()
    val baseUrl = "http://10.0.2.2:8080/images/"

    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }

    val wishlistsByVisibilita by wishlistViewModel?.wishlistsByVisibilita?.collectAsState() ?: remember { mutableStateOf(emptyList()) }

    val isInWishlist = remember(wishlistsByVisibilita, evento.id) {
        wishlistsByVisibilita?.any { wishlist ->
            wishlist.eventi.contains(evento.id)
        } ?: false
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
                onWishlistToggle = {
                    wishlistViewModel?.let { viewModel ->
                        val wishlistId = viewModel.getFirstPrivateWishlistId()
                        if (wishlistId != null) {
                            if (isInWishlist) {
                                viewModel.removeEventoFromWishlist(wishlistId, evento.id) {
                                    viewModel.getWishlistsByUtenteAndVisibilita(2L, Visibilita.PRIVATA)
                                }
                            } else {
                                viewModel.addEventoToWishlist(wishlistId, evento.id) {
                                    viewModel.getWishlistsByUtenteAndVisibilita(2L, Visibilita.PRIVATA)
                                }
                            }
                        }
                    }
                }
            )

            // Contenuto principale
            EventDetailMainContent(
                evento = evento,
                strutturaInfo = strutturaInfo,
                strutturaMapInfo = strutturaMapInfo
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
    onWishlistToggle: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ) {
//immagine evento
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
                // TODO: Implementare logica di acquisto dai mirkooooooooooooooo
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
                CircularProgressIndicator(color = EventraColors.PrimaryOrange)
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
                // Spazio dedicato solo alla mappa
                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    factory = { ctx ->
                        Configuration.getInstance().load(
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
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EventraColors.PrimaryOrange)
                }
            }
        }
    }
}

@Composable
fun OpenStreetMapView(
    latitude: Double,
    longitude: Double,
    strutturaNome: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    AndroidView(
        modifier = modifier,
        factory = { ctx ->

            Configuration.getInstance().load(
                ctx.applicationContext,
                androidx.preference.PreferenceManager.getDefaultSharedPreferences(ctx.applicationContext)
            )

            MapView(ctx).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                setMultiTouchControls(true)


                val mapController = controller
                mapController.setZoom(15.0)
                val startPoint = GeoPoint(latitude, longitude)
                mapController.setCenter(startPoint)


                val marker = Marker(this)
                marker.position = startPoint
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.title = strutturaNome

                overlays.add(marker)
            }
        },
        update = { mapView ->

            val newPoint = GeoPoint(latitude, longitude)
            mapView.controller.setCenter(newPoint)

            mapView.overlays.clear()
            val marker = Marker(mapView)
            marker.position = newPoint
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            marker.title = strutturaNome
            mapView.overlays.add(marker)
            mapView.invalidate()
        }
    )
}

@Composable
fun EventDetailErrorState(onBackPressed: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
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
                )
            ) {
                Text(
                    text = "Torna Indietro",
                    color = Color.White
                )
            }
        }
    }
}
