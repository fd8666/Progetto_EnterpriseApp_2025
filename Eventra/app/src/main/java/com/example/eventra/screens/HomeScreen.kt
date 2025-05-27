package com.example.eventra.screens

import android.annotation.SuppressLint
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.TagCategoriaViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.TagCategoriaData
import kotlinx.coroutines.delay

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current

    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }

    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }

    val categoriaViewModel: TagCategoriaViewModel = viewModel {
        TagCategoriaViewModel(context.applicationContext as android.app.Application)
    }

    val eventi by eventiViewModel.eventi.collectAsState()
    val categorie by categoriaViewModel.categorie.collectAsState()
    val isLoadingEventi by eventiViewModel.isLoading.collectAsState()
    val isLoadingCategorie by categoriaViewModel.isLoading.collectAsState()

    // Carica i dati all'avvio
    LaunchedEffect(Unit) {
        eventiViewModel.getAllEventi()
        categoriaViewModel.getAllCategorie()
    }

    val eventiFiltrati = remember(eventi, selectedCategoryId) {
        if (selectedCategoryId != null) {
            eventi?.filter { it.categoriaId == selectedCategoryId } ?: emptyList()
        } else {
            eventi ?: emptyList()
        }
    }


    //Background
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

            // Titolo dell'app EVENTRA animato
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp, horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Contenitore del titolo
                    Box(
                        modifier = Modifier
                            .wrapContentSize()
                            .padding(horizontal = 16.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "EVENTRA",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            letterSpacing = 4.sp,
                            style = androidx.compose.ui.text.TextStyle(
                                color = Color(0xFF6A00FF).copy(alpha = 0.5f),
                                shadow = androidx.compose.ui.graphics.Shadow(
                                    color = Color(0xFF6A00FF).copy(alpha = 0.5f),
                                    offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                                    blurRadius = 8f
                                )
                            ),

                        )
                    }
                }
            }

            // Sezione categorie
            item {
                if (!categorie.isNullOrEmpty()) {
                    SelezioneCategoria(
                        categorie = categorie!!,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { categoryId ->
                            selectedCategoryId = if (selectedCategoryId == categoryId) null else categoryId
                            categoryId?.let { eventiViewModel.getEventiByCategoria(it) }
                                ?: eventiViewModel.getAllEventi()
                        }
                    )
                }
            }

            //
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Eventi totali: ${eventi?.size}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            // Eventi Card
            items(eventiFiltrati.chunked(2)) { rowEvents ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowEvents.forEach { evento ->
                        EventiCard(
                            evento = evento,
                            modifier = Modifier.weight(1f),
                            onClick = { /* dettagli evento */ }
                        )
                    }
                    if (rowEvents.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                if (eventiFiltrati.isEmpty() && !isLoadingEventi) {
                    EventiCardVuoti(
                        message = if (selectedCategoryId != null)
                            "Nessun evento per questa categoria"
                        else
                            "Nessun evento disponibile"
                    )
                }
                Spacer(modifier = Modifier.height(100.dp))
            }
        }


        if (isLoadingEventi || isLoadingCategorie) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                AnimazioneCaricamento()
            }
        }
    }
}



@Composable
fun SelezioneCategoria(
    categorie: List<TagCategoriaData>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dividi le categorie in righe
        val categorieChunked = categorie.chunked(3)

        categorieChunked.forEach { rowCategorie ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                rowCategorie.forEach { categoria ->
                    CategoriaAnimata(
                        categoria = categoria,
                        isSelected = selectedCategoryId == categoria.id,
                        onClick = { onCategorySelected(categoria.id) },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Aggiungi spazi vuoti se la riga non è completa
                repeat(3 - rowCategorie.size) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun CategoriaAnimata(
    categoria: TagCategoriaData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "ScaleAnim"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isSelected) 5f else 0f,
        animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing),
        label = "RotationAnim"
    )

    val glowColor = getCategoriaColori(categoria.id)

    // Colore base  quando  selezionato e non
    val animatedGlowColor by animateColorAsState(
        targetValue = if (isSelected)
            glowColor.copy(alpha = 0.9f)
        else
            glowColor.copy(alpha = 0.85f),
        animationSpec = tween(500)
    )

    // Background quando selezionato e non
    val backgroundBrush = Brush.radialGradient(
        colors = if (isSelected)
            listOf(
                animatedGlowColor.copy(alpha = 0.8f),
                animatedGlowColor.copy(alpha = 0.5f),
                animatedGlowColor.copy(alpha = 0.2f)
            )
        else
            listOf(
                animatedGlowColor.copy(alpha = 0.7f),
                animatedGlowColor.copy(alpha = 0.4f),
                animatedGlowColor.copy(alpha = 0.1f)
            ),
        center = androidx.compose.ui.geometry.Offset.Zero,
        radius = 250f
    )

    // Icona quando selezionata e non
    val iconColor by animateColorAsState(
        targetValue = if (isSelected)
            Color.White
        else
            Color.White.copy(alpha = 0.95f),
        animationSpec = tween(300)
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .padding(vertical = 16.dp, horizontal = 8.dp)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    rotationZ = rotation
                }
                .background(brush = backgroundBrush, shape = CircleShape)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = getCategorieIcone(categoria.nome ?: ""),
                contentDescription = categoria.nome,
                tint = iconColor,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        val textAlpha by animateFloatAsState(
            targetValue = if (isSelected) 1f else 0.9f,
            animationSpec = tween(300)
        )

        Text(
            text = categoria.nome ?: "Categoria",
            color = Color.White.copy(alpha = textAlpha),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .animateContentSize()
                .padding(horizontal = 4.dp)
        )
    }
}


@Composable
fun EventiCard(
    evento: EventoData,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isFavorite by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    // Animazione per il cuore
    val heartScale by animateFloatAsState(
        targetValue = if (isFavorite) 1.3f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heartScale"
    )

    val heartColor by animateColorAsState(
        targetValue = if (isFavorite) Color(0xFF6404FF) else Color.Gray,
        animationSpec = tween(300),
        label = "heartColor"
    )

    //prendere le immagini dal database
    val baseUrl = "http://10.0.2.2:8080/images/"

    val imageUrl = remember(evento.immagine) {
        if (!evento.immagine.isNullOrBlank()) {
            if (evento.immagine.startsWith("http")) evento.immagine
            else "$baseUrl${evento.immagine}"
        } else null
    }

    Card(
        modifier = modifier
            .height(260.dp)
            .scale(scale)
            .clickable {
                isPressed = true
                onClick()
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.05f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.5f)
                            )
                        )
                    )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        color = Color.White.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = evento.nome ?: "Evento",
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${evento.postiDisponibili} posti",
                                color = Color.DarkGray,
                                fontSize = 12.sp,
                            )
                        }
                        // Cuore animato
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = heartColor,
                            modifier = Modifier
                                .size(24.dp)
                                .scale(heartScale)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    isFavorite = !isFavorite
                                }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(isPressed) {
        if (isPressed) {
            delay(150)
            isPressed = false
        }
    }
}


@Composable
fun EventiCardVuoti(message: String) {
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
                imageVector = Icons.Default.EventBusy,
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

@Composable
fun AnimazioneCaricamento() {
    val infiniteTransition = rememberInfiniteTransition()
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    Box(
        modifier = Modifier
            .size(60.dp)
            .background(
                Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(30.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Loading",
            tint = Color.White,
            modifier = Modifier
                .size(32.dp)
                .graphicsLayer { rotationZ = rotation }
        )
    }
}

//colori per le categorie
fun getCategoriaColori(categoriaId: Long): Color {
    val colors = listOf(
        Color(0xFFFF0055),
        Color(0xFF00FFAA),
        Color(0xFFFFC800),
    )
    return colors[(categoriaId % colors.size).toInt()]
}

//mappa le categorie del database
fun getCategorieIcone(categoryName: String): ImageVector {
    return when (categoryName.lowercase()) {
        "concerti" -> Icons.Default.MusicNote
        "sport" -> Icons.Default.SportsSoccer
        "intrattenimento" -> Icons.Default.Movie
        else -> Icons.Default.Category
    }
}
