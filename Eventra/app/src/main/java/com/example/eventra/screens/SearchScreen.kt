package com.example.eventra.screens

import android.annotation.SuppressLint
import android.app.DatePickerDialog
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
enum class SearchType {
    ALL,
    BY_NAME,
    BY_LOCATION,
    BY_CATEGORY,
    BY_DATE_RANGE,
    BY_DATE_AFTER,
    COMBINED
}

@SuppressLint("RememberReturnType")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onNavigateToBiglietto: (Long) -> Unit = {},
    onNavigateToEventDetail: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val sessionManager = remember { SessionManager(context) }
    val isUserLoggedIn = remember { sessionManager.isLoggedIn() }
    val eventiViewModel: EventiViewModel = viewModel {
        EventiViewModel(context.applicationContext as android.app.Application)
    }
    val categoriaViewModel: TagCategoriaViewModel = viewModel {
        TagCategoriaViewModel(context.applicationContext as android.app.Application)
    }
    val profileViewModel: ProfileViewModel = viewModel {
        ProfileViewModel(context.applicationContext as android.app.Application)
    }
    val wishlistViewModel: WishlistViewModel? = if (isUserLoggedIn) {
        viewModel { WishlistViewModel(context.applicationContext as android.app.Application) }
    } else null
    val eventi by eventiViewModel.eventi.collectAsState()
    val eventiByCategoria by eventiViewModel.eventiByCategoria.collectAsState()
    val categorie by categoriaViewModel.categorie.collectAsState()
    val isLoading by eventiViewModel.isLoading.collectAsState()
    val userData by profileViewModel.userData.collectAsState()
    var searchText by remember { mutableStateOf("") }
    var searchLocation by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }
    var showFilters by remember { mutableStateOf(false) }
    var isSearchActive by remember { mutableStateOf(false) }
    var currentSearchType by remember { mutableStateOf(SearchType.ALL) }
    var showLoginAlert by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        categoriaViewModel.getAllCategorie()
        if (isUserLoggedIn) {
            profileViewModel.loadUserProfile()
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
    fun determineSearchType(): SearchType {
        val hasName = searchText.isNotBlank()
        val hasLocation = searchLocation.isNotBlank()
        val hasCategory = selectedCategoryId != null
        val hasStartDate = startDate != null
        val hasEndDate = endDate != null
        val hasBothDates = hasStartDate && hasEndDate
        val hasOnlyStartDate = hasStartDate && !hasEndDate
        val activeFiltersCount = listOf(hasName, hasLocation, hasCategory, hasBothDates || hasOnlyStartDate).count { it }

        return when {
            activeFiltersCount == 0 -> SearchType.ALL
            activeFiltersCount > 1 -> SearchType.COMBINED
            hasName -> SearchType.BY_NAME
            hasLocation -> SearchType.BY_LOCATION
            hasCategory -> SearchType.BY_CATEGORY
            hasBothDates -> SearchType.BY_DATE_RANGE
            hasOnlyStartDate -> SearchType.BY_DATE_AFTER
            else -> SearchType.ALL
        }
    }
    fun performSearch() {
        keyboardController?.hide()

        val searchType = determineSearchType()
        currentSearchType = searchType

        when (searchType) {
            SearchType.ALL -> {
                eventiViewModel.getAllEventi()
            }

            SearchType.BY_NAME -> {
                eventiViewModel.searchEventiByNome(searchText.trim())
            }

            SearchType.BY_LOCATION -> {
                eventiViewModel.searchEventiByLuogo(searchLocation.trim())
            }

            SearchType.BY_CATEGORY -> {
                selectedCategoryId?.let { categoryId ->
                    eventiViewModel.getEventiByCategoria(categoryId)
                }
            }

            SearchType.BY_DATE_RANGE -> {
                val startDateStr = startDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                val endDateStr = endDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                eventiViewModel.searchEventiByDateRange(startDateStr, endDateStr)
            }

            SearchType.BY_DATE_AFTER -> {
                val dateStr = startDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                dateStr?.let { eventiViewModel.searchEventiAfterDate(it) }
            }

            SearchType.COMBINED -> {

                eventiViewModel.searchEventiCombined(
                    nome = if (searchText.isNotBlank()) searchText.trim() else null,
                    luogo = if (searchLocation.isNotBlank()) searchLocation.trim() else null,
                    categoriaId = selectedCategoryId,
                    startDate = startDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    endDate = endDate?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
            }
        }

        isSearchActive = true
    }


    fun getCurrentEventi(): List<EventoData>? {
        return when (currentSearchType) {
            SearchType.BY_CATEGORY -> eventiByCategoria
            else -> eventi
        }
    }


    fun clearAllFilters() {
        searchText = ""
        searchLocation = ""
        selectedCategoryId = null
        startDate = null
        endDate = null
        isSearchActive = false
        currentSearchType = SearchType.ALL
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
                SearchHeader()
            }
            item {
                SearchSection(
                    searchText = searchText,
                    onSearchTextChange = { searchText = it },
                    searchLocation = searchLocation,
                    onSearchLocationChange = { searchLocation = it },
                    onSearch = { performSearch() }
                )
            }

            item {
                FiltersToggle(
                    showFilters = showFilters,
                    onToggle = { showFilters = !showFilters },

                )
            }




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
                        onClearFilters = { clearAllFilters() },
                        onApplyFilters = { performSearch() }
                    )
                }
            }




            if (isSearchActive) {
                val currentEvents = getCurrentEventi()

                item {
                    SearchResultsHeader(
                        totalEvents = currentEvents?.size ?: 0,
                        isLoading = isLoading,
                        searchType = currentSearchType
                    )
                }

                if (!currentEvents.isNullOrEmpty()) {
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(((currentEvents.size / 2 + currentEvents.size % 2) * 300).dp)
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            userScrollEnabled = false
                        ) {
                            items(currentEvents) { evento ->
                                EventraEventCard(
                                    evento = evento,
                                    wishlistViewModel = wishlistViewModel,
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
                    }
                } else if (!isLoading && isSearchActive) {
                    item {
                        NoResultsCard(
                            searchType = currentSearchType,
                            onSuggestAction = {

                                when (currentSearchType) {
                                    SearchType.BY_DATE_RANGE -> {

                                        endDate = null
                                        performSearch()
                                    }
                                    SearchType.BY_DATE_AFTER, SearchType.BY_NAME, SearchType.BY_LOCATION -> {

                                        clearAllFilters()
                                        eventiViewModel.getAllEventi()
                                        isSearchActive = true
                                    }
                                    else -> {
                                        clearAllFilters()
                                        eventiViewModel.getAllEventi()
                                        isSearchActive = true
                                    }
                                }
                            }
                        )
                    }
                }
            } else {
                item {
                    SearchPlaceholderCard(
                        onQuickSearch = { type ->
                            when (type) {
                                "today" -> {
                                    startDate = LocalDate.now()
                                    endDate = null
                                    performSearch()
                                }
                                "weekend" -> {
                                    startDate = LocalDate.now().with(java.time.DayOfWeek.SATURDAY)
                                    endDate = LocalDate.now().with(java.time.DayOfWeek.SUNDAY)
                                    performSearch()
                                }
                                "all" -> {
                                    clearAllFilters()
                                    eventiViewModel.getAllEventi()
                                    isSearchActive = true
                                }
                            }
                        }
                    )
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
fun SearchHeader() {
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
                .padding(10.dp),
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

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Cerca Eventi",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Trova l'evento perfetto per te",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SearchSection(
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    searchLocation: String,
    onSearchLocationChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Cosa stai cercando?",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = EventraColors.TextDark
            )
            SearchTextField(
                value = searchText,
                onValueChange = onSearchTextChange,
                placeholder = "Nome evento, artista...",
                leadingIcon = Icons.Default.Search,
                onSearch = onSearch
            )
            SearchTextField(
                value = searchLocation,
                onValueChange = onSearchLocationChange,
                placeholder = "Dove vuoi andare?",
                leadingIcon = Icons.Default.LocationOn,
                onSearch = onSearch
            )
            Button(
                onClick = onSearch,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Cerca Eventi",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    onSearch: () -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = EventraColors.TextGray
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = EventraColors.PrimaryOrange
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(onClick = { onValueChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "Clear",
                        tint = EventraColors.TextGray
                    )
                }
            }
        },
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = EventraColors.TextDark,
            unfocusedTextColor = EventraColors.TextDark,
            focusedBorderColor = EventraColors.PrimaryOrange,
            unfocusedBorderColor = EventraColors.DividerGray,
            cursorColor = EventraColors.PrimaryOrange
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { onSearch() }),
        singleLine = true
    )
}@Composable
fun FiltersToggle(
    showFilters: Boolean,
    onToggle: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onToggle() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = EventraColors.CardWhite
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterList,
                    contentDescription = null,
                    tint = EventraColors.TextGray,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Filtri Avanzati",
                    color = EventraColors.TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Icon(
                imageVector = if (showFilters) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (showFilters) "Nascondi filtri" else "Mostra filtri",
                tint = EventraColors.PrimaryOrange,
                modifier = Modifier.size(24.dp)
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
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (categorie.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Categoria",
                        color = EventraColors.TextDark,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    CategoryFilterSection(
                        categorie = categorie,
                        selectedCategoryId = selectedCategoryId,
                        onCategorySelected = onCategorySelected
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Periodo",
                    color = EventraColors.TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatePickerField(
                        label = "Data Inizio",
                        selectedDate = startDate,
                        onDateSelected = onStartDateSelected,
                        modifier = Modifier.weight(1f)
                    )

                    DatePickerField(
                        label = "Data Fine",
                        selectedDate = endDate,
                        onDateSelected = onEndDateSelected,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onClearFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = EventraColors.PrimaryOrange
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        EventraColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pulisci")
                }

                Button(
                    onClick = onApplyFilters,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EventraColors.PrimaryOrange
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Applica")
                }
            }
        }
    }
}

@Composable
fun CategoryFilterSection(
    categorie: List<TagCategoriaData>,
    selectedCategoryId: Long?,
    onCategorySelected: (Long?) -> Unit
) {
    val allCategories = listOf(
        TagCategoriaData(id = -1, nome = "Tutte", descrizione = null)
    ) + categorie

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(allCategories) { categoria ->
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
                }
            )
        }
    }
}

@Composable
fun FilterCategoryChip(
    categoria: TagCategoriaData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isSelected) EventraColors.PrimaryOrange else EventraColors.BackgroundGray,
        animationSpec = tween(300)
    )

    Card(
        modifier = Modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categoria.id != -1L) {
                Icon(
                    imageVector = getCategorieIcone(categoria.nome ?: ""),
                    contentDescription = null,
                    tint = if (isSelected) Color.White else EventraColors.PrimaryOrange,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }

            Text(
                text = categoria.nome ?: "",
                color = if (isSelected) Color.White else EventraColors.TextDark,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            )
        }
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
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = EventraColors.TextDark
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            EventraColors.DividerGray
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = EventraColors.TextGray
            )
            Text(
                text = selectedDate?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                    ?: "Seleziona",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SearchResultsHeader(
    totalEvents: Int,
    isLoading: Boolean,
    searchType: SearchType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = EventraColors.CardWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "$totalEvents eventi trovati",
                    color = EventraColors.TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Text(
                    text = getSearchTypeDescription(searchType),
                    color = EventraColors.TextGray,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = EventraColors.PrimaryOrange,
                    strokeWidth = 2.dp
                )
            }
        }
    }
}

@Composable
fun NoResultsCard(
    searchType: SearchType,
    onSuggestAction: () -> Unit
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
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.SearchOff,
                contentDescription = null,
                tint = EventraColors.TextGray,
                modifier = Modifier.size(64.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Nessun evento trovato",
                color = EventraColors.TextDark,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = getNoResultsMessage(searchType),
                color = EventraColors.TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = onSuggestAction,
                colors = ButtonDefaults.buttonColors(
                    containerColor = EventraColors.PrimaryOrange
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = getSuggestionButtonText(searchType),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SearchPlaceholderCard(
    onQuickSearch: (String) -> Unit
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
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
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
                tint = EventraColors.PrimaryOrange,
                modifier = Modifier
                    .size(80.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Inizia la tua ricerca",
                color = EventraColors.TextDark,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Utilizza i filtri per trovare eventi interessanti nella tua zona",
                color = EventraColors.TextGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

        }
    }
}
fun getSearchTypeDescription(searchType: SearchType): String {
    return when (searchType) {
        SearchType.ALL -> "Ricerca generale"
        SearchType.BY_NAME -> "Ricerca per nome"
        SearchType.BY_LOCATION -> "Ricerca per località"
        SearchType.BY_CATEGORY -> "Ricerca per categoria"
        SearchType.BY_DATE_RANGE -> "Ricerca per periodo"
        SearchType.BY_DATE_AFTER -> "Ricerca per data"
        SearchType.COMBINED -> "Ricerca avanzata"
    }
}

fun getNoResultsMessage(searchType: SearchType): String {
    return when (searchType) {
        SearchType.ALL -> "Non ci sono eventi disponibili al momento"
        SearchType.BY_NAME -> "Nessun evento trovato con questo nome"
        SearchType.BY_LOCATION -> "Nessun evento trovato in questa località"
        SearchType.BY_CATEGORY -> "Nessun evento trovato per questa categoria"
        SearchType.BY_DATE_RANGE -> "Nessun evento trovato in questo periodo"
        SearchType.BY_DATE_AFTER -> "Nessun evento trovato dopo questa data"
        SearchType.COMBINED -> "Prova a modificare alcuni filtri di ricerca"
    }
}

fun getSuggestionButtonText(searchType: SearchType): String {
    return when (searchType) {
        SearchType.BY_DATE_RANGE -> "Amplia periodo"
        SearchType.BY_DATE_AFTER -> "Vedi tutti gli eventi"
        SearchType.BY_NAME, SearchType.BY_LOCATION -> "Vedi tutti gli eventi"
        SearchType.BY_CATEGORY -> "Vedi tutte le categorie"
        SearchType.COMBINED -> "Pulisci filtri"
        SearchType.ALL -> "Ricarica"
    }
}