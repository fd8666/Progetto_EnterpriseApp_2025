package com.example.eventra.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventra.R
import com.example.eventra.viewmodels.EventiViewModel
import com.example.eventra.viewmodels.TagCategoriaViewModel
import com.example.eventra.viewmodels.data.EventoData
import com.example.eventra.viewmodels.data.TagCategoriaData
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // ViewModels
    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }
    val categoriaViewModel: TagCategoriaViewModel = viewModel {
        TagCategoriaViewModel(context.applicationContext as android.app.Application)
    }

    // Stati
    val eventi by eventiViewModel.eventi.collectAsState()
    val categorie by categoriaViewModel.categorie.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()

    // Stati di ricerca
    var searchText by remember { mutableStateOf("") }
    var searchLocation by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }

    // Carica categorie all'avvio
    LaunchedEffect(Unit) {
        categoriaViewModel.getAllCategorie()
    }

    // Funzione di ricerca modificata per combinare i filtri
    fun performSearch() {
        keyboardController?.hide()

        // Controlla se almeno un filtro è attivo
        val hasActiveFilters = searchText.isNotBlank() ||
                searchLocation.isNotBlank() ||
                selectedCategoryId != null ||
                startDate != null ||
                endDate != null

        if (!hasActiveFilters) {
            // Se non ci sono filtri attivi, carica tutti gli eventi
            eventiViewModel.getAllEventi()
        } else {
            // Usa la ricerca combinata con tutti i parametri
            eventiViewModel.searchEventiCombined(
                nome = if (searchText.isNotBlank()) searchText else null,
                luogo = if (searchLocation.isNotBlank()) searchLocation else null,
                categoriaId = selectedCategoryId,
                startDate = startDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                endDate = endDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            )
        }

        isSearchActive = true
    }

    // Background gradient
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

            // Titolo
            item {
                SearchHeader()
            }

            // Barra di ricerca principale
            item {
                SearchTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = stringResource(R.string.search_hint),
                    onSearch = { performSearch() }
                )
            }

            // Barra di ricerca per luogo
            item {
                SearchTextField(
                    value = searchLocation,
                    onValueChange = { searchLocation = it },
                    placeholder = stringResource(R.string.search_location_hint),
                    leadingIcon = Icons.Default.LocationOn,
                    onSearch = { performSearch() }
                )
            }

            // Toggle filtri
            item {
                FiltersToggle(
                    showFilters = showFilters,
                    onToggle = { showFilters = !showFilters }
                )
            }

            // Sezione filtri (animata)
            item {
                AnimatedVisibility(
                    visible = showFilters,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    FiltersSection(
                        categorie = categorie ?: emptyList(),
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = { selectedCategoryId = it },
                        startDate = startDate,
                        endDate = endDate,
                        onStartDateSelected = { startDate = it },
                        onEndDateSelected = { endDate = it },
                        onClearFilters = {
                            selectedCategoryId = null
                            startDate = null
                            endDate = null
                            searchText = ""
                            searchLocation = ""
                            isSearchActive = false
                        },
                        onApplyFilters = { performSearch() }
                    )
                }
            }

            // Risultati di ricerca
            if (isSearchActive) {
                item {
                    SearchResultsHeader(
                        totalEvents = eventi?.size ?: 0,
                        isLoading = isLoading
                    )
                }

                if (!eventi.isNullOrEmpty()) {
                    items(eventi!!.chunked(2)) { rowEvents ->
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
                } else if (!isLoading && isSearchActive) {
                    item {
                        NoResultsCard()
                    }
                }
            } else {
                // Mostra il placeholder quando non c'è ricerca attiva
                item {
                    SearchPlaceholderCard()
                }
            }

            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }

        // Loading overlay
        if (isLoading) {
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
fun SearchHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.search_title),
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp,
            style = androidx.compose.ui.text.TextStyle(
                color = Color(0xFF6A00FF).copy(alpha = 0.8f),
                shadow = androidx.compose.ui.graphics.Shadow(
                    color = Color(0xFF6A00FF).copy(alpha = 0.5f),
                    offset = androidx.compose.ui.geometry.Offset(0f, 4f),
                    blurRadius = 8f
                )
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Default.Search,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = Color.White.copy(alpha = 0.6f)
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f)
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White.copy(alpha = 0.9f),
            focusedBorderColor = Color(0xFF6A00FF),
            unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
            cursorColor = Color(0xFF6A00FF)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true
    )
}

@Composable
fun FiltersToggle(
    showFilters: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.search_filters),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FiltersSection(
    categorie: List<TagCategoriaData>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onStartDateSelected: (LocalDate?) -> Unit,
    onEndDateSelected: (LocalDate?) -> Unit,
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Sezione categorie
            if (categorie.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.search_category),
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                CategoryFilterGrid(
                    categorie = categorie,
                    selectedCategoryId = selectedCategoryId,
                    onCategorySelected = onCategorySelected
                )
            }

            // Sezione date
            Text(
                text = stringResource(R.string.search_date_range),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                DatePickerField(
                    label = stringResource(R.string.search_start_date),
                    selectedDate = startDate,
                    onDateSelected = onStartDateSelected,
                    modifier = Modifier.weight(1f)
                )

                DatePickerField(
                    label = stringResource(R.string.search_end_date),
                    selectedDate = endDate,
                    onDateSelected = onEndDateSelected,
                    modifier = Modifier.weight(1f)
                )
            }

            // Bottoni azione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White.copy(alpha = 0.8f)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        Color.White.copy(alpha = 0.3f)
                    )
                ) {
                    Text(stringResource(R.string.search_clear_filters))
                }

                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF6A00FF)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.search_apply_filters),
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryFilterGrid(
    categorie: List<TagCategoriaData>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    // Aggiungi opzione "Tutte le categorie"
    val allCategories = listOf(
        TagCategoriaData(id = -1, nome = stringResource(R.string.search_all_categories), descrizione = null)
    ) + categorie

    val categorieChunked = allCategories.chunked(3)

    categorieChunked.forEach { rowCategorie ->
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            rowCategorie.forEach { categoria ->
                val isSelected = if (categoria.id == -1L) {
                    selectedCategoryId == null
                } else {
                    selectedCategoryId == categoria.id
                }

                FilterCategoryChip(
                    categoria = categoria,
                    isSelected = isSelected,
                    onClick = {
                        onCategorySelected(if (categoria.id == -1L) null else categoria.id)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            // Aggiungi spazi vuoti se necessario
            repeat(3 - rowCategorie.size) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun FilterCategoryChip(
    categoria: TagCategoriaData,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF6A00FF) else Color.White.copy(alpha = 0.1f),
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier
            .clickable { onClick() }
            .padding(2.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Text(
            text = categoria.nome ?: "",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DatePickerField(
    label: String,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate?) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    OutlinedButton(
        onClick = {
            val calendar = Calendar.getInstance()
            val year = selectedDate?.year ?: calendar.get(Calendar.YEAR)
            val month = selectedDate?.monthValue?.minus(1) ?: calendar.get(Calendar.MONTH)
            val day = selectedDate?.dayOfMonth ?: calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                context,
                { _, selectedYear, selectedMonth, selectedDay ->
                    onDateSelected(LocalDate.of(selectedYear, selectedMonth + 1, selectedDay))
                },
                year, month, day
            ).show()
        },
        modifier = modifier,
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Color.White.copy(alpha = 0.8f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color.White.copy(alpha = 0.3f)
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color.White.copy(alpha = 0.6f)
            )
            Text(
                text = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ?: stringResource(R.string.search_select_date),
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun SearchResultsHeader(
    totalEvents: Int,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.total_events, totalEvents),
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color(0xFF6A00FF),
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun NoResultsCard() {
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
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(48.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.search_no_results),
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun SearchPlaceholderCard() {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
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
                // Icona lente di ricerca animata
                val infiniteTransition = rememberInfiniteTransition()
                val scale by infiniteTransition.animateFloat(
                    initialValue = 1f,
                    targetValue = 1.1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(2000, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )

                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = Color(0xFF6A00FF).copy(alpha = 0.6f),
                    modifier = Modifier
                        .size(64.dp)
                        .scale(scale)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = stringResource(R.string.search_placeholder_title),
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.search_placeholder_subtitle),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        }
}

