package com.example

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.graphicsLayer
import com.example.ui.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.MainViewModel
import com.example.ui.Screen
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MainViewModel = viewModel()) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (viewModel.currentScreen != Screen.CanvasEditor) {
                TopAppBarComponent(
                    quotaLeft = viewModel.dailyQuotaLeft,
                    onResetQuota = { viewModel.resetQuota() }
                )
            }
        },
        bottomBar = {
            if (viewModel.currentScreen != Screen.CanvasEditor) {
                BottomNavBarComponent(
                    currentScreen = viewModel.currentScreen,
                    onNavigate = { viewModel.currentScreen = it }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            AnimatedContent(
                targetState = viewModel.currentScreen,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    is Screen.Home -> HomeScreen(viewModel)
                    is Screen.Search -> SearchScreen(viewModel)
                    is Screen.Generator -> GeneratorScreen(viewModel)
                    is Screen.CanvasEditor -> CanvasEditorScreen(viewModel)
                    is Screen.Library -> LibraryScreen(viewModel)
                    is Screen.Profile -> ProfileScreen(viewModel)
                }
            }
        }
    }
}

// --- Header component ---
@Composable
fun TopAppBarComponent(quotaLeft: Int, onResetQuota: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(72.dp)
            .background(Color(0xFF0F0F12))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onResetQuota() }
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFFFFB000), Color(0xFFFF8000))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Devotional Logo",
                    tint = Color.Black,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "Shyam AI",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E2E6)
                    )
                )
                Text(
                    text = "DIVINE CREATOR PRO",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA0A0AA),
                        letterSpacing = 1.5.sp
                    )
                )
            }
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF242429))
                .border(1.dp, Color(0xFF3A3A3F), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = "Quota",
                    tint = Color(0xFFFFB000),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "$quotaLeft/3 Left",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE2E2E6)
                    )
                )
            }
        }
    }
}

// --- Bottom Navigation ---
@Composable
fun BottomNavBarComponent(currentScreen: Screen, onNavigate: (Screen) -> Unit) {
    val navItems = listOf(
        Triple(Screen.Home, Icons.Default.Create, "Create"),
        Triple(Screen.Search, Icons.Default.PhotoLibrary, "Gallery"),
        Triple(Screen.Library, Icons.Default.AutoStories, "Library"),
        Triple(Screen.Profile, Icons.Default.Person, "Profile")
    )

    Surface(
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .border(width = 1.dp, color = Color(0xFF1C1C21))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(68.dp)
                .background(Color(0xFF0F0F12))
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEach { (screen, icon, label) ->
                val isSelected = currentScreen == screen || 
                                 (screen == Screen.Home && currentScreen == Screen.Generator)
                
                val iconColor = if (isSelected) Color(0xFFFFB000) else Color(0xFFA0A0AA)
                val textColor = if (isSelected) Color(0xFFFFB000) else Color(0xFFA0A0AA)
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .testTag("nav_${label.lowercase()}")
                        .clickable { onNavigate(screen) }
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) Color(0xFF242429) else Color.Transparent)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = label,
                            tint = iconColor
                        )
                    }
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = textColor
                        )
                    )
                }
            }
        }
    }
}

// ==========================================
// SCREEN 1: HOME
// ==========================================
@Composable
fun HomeScreen(viewModel: MainViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Hero title
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Create Your Divine Vision",
                    style = MaterialTheme.typography.displayMedium.copy(
                        color = Color(0xFFFFB000),
                        textAlign = TextAlign.Center
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect with the eternal through AI-powered spiritual art.",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = Color(0xFFA0A0AA),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }

        // Quick Create Input Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF2A2A30)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = false)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Describe your vision...",
                        style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFFE2E2E6))
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = viewModel.aiPromptInput,
                        onValueChange = { viewModel.aiPromptInput = it },
                        placeholder = { Text("E.g., Khatu Shyam Ji with divine light aura, neon background...", color = Color(0xFFA0A0AA)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .testTag("ai_prompt_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFFE2E2E6),
                            unfocusedTextColor = Color(0xFFE2E2E6),
                            focusedBorderColor = Color(0xFFFFB000),
                            unfocusedBorderColor = Color(0xFF2A2A30),
                            focusedContainerColor = Color(0xFF0F0F12),
                            unfocusedContainerColor = Color(0xFF0F0F12)
                        )
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = { viewModel.enhancePromptAndGenerate() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB000)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("execute_ai_button")
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Execute AI", tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Execute AI", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        // Choose Divine Style
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHOOSE DIVINE STYLE",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA0A0AA),
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(1.dp)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFFB000).copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                val styles = listOf(
                    "Bhakti Style" to Icons.Default.TempleHindu,
                    "Pooja Style" to Icons.Default.Spa,
                    "Neon-Light" to Icons.Default.LightMode,
                    "3D Canvas" to Icons.Default.ViewInAr,
                    "Realistic" to Icons.Default.Camera,
                    "Artistic Portrait" to Icons.Default.Draw
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(styles) { (styleName, icon) ->
                        val isSelected = viewModel.selectedAiStyle == styleName
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) Color(0xFFFFB000).copy(alpha = 0.15f) else Color(0xFF16161B))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFFFFB000) else Color(0xFF2A2A30),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .clickable {
                                    viewModel.selectedAiStyle = styleName
                                    // Pre-populate input based on style choice
                                    viewModel.aiPromptInput = "Khatu Shyam Ji in beautiful $styleName"
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color(0xFFFFB000) else Color(0xFF242429)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = styleName,
                                        tint = if (isSelected) Color.Black else Color(0xFFFFB000)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = styleName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFFFB000) else Color(0xFFE2E2E6)
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Creations fallback gallery
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "RECENT CREATIONS",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA0A0AA),
                            letterSpacing = 2.sp
                        )
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "See All",
                        modifier = Modifier.clickable { viewModel.currentScreen = Screen.Search },
                        style = MaterialTheme.typography.labelLarge.copy(color = Color(0xFFFFB000))
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DEFAULTS_IMAGES.forEach { img ->
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, Color(0xFF2A2A30)),
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(3f / 4f)
                                .clickable {
                                    viewModel.selectedBackground = img.url
                                    viewModel.projectName = "Poster of ${img.title}"
                                    viewModel.currentScreen = Screen.CanvasEditor
                                }
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                AsyncImage(
                                    model = img.url,
                                    contentDescription = img.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                // Sparkle/AI badge
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopStart)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFFFB000))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "✨ AI Special",
                                        color = Color.Black,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }
                                // Bottom overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(40.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                                            )
                                        )
                                        .align(Alignment.BottomCenter)
                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = img.title.take(15) + "...",
                                        color = Color.White,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Ad banner placeholder
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
                border = BorderStroke(1.dp, Color(0xFF2A2A30)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "ADMOB BANNER PLACEHOLDER",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFA0A0AA).copy(alpha = 0.5f),
                            letterSpacing = 3.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }

        // One-tap Bhakti mode suggestions (Module 14)
        item {
            Column {
                Text(
                    text = "ONE-TAP BHAKTI CREATIONS",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFA0A0AA),
                        letterSpacing = 2.sp
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                val bhaktiTopics = listOf("Khatu Shyam", "Radha Krishna", "Mahadev", "Hanuman Ji")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bhaktiTopics.forEach { topic ->
                        InputChip(
                            selected = false,
                            onClick = { viewModel.buildOneTapBhaktiPoster(topic) },
                            label = { Text(topic, fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.Celebration, contentDescription = topic, tint = Color(0xFFFFB000)) },
                            colors = InputChipDefaults.inputChipColors(
                                containerColor = Color(0xFF242429),
                                labelColor = Color(0xFFE2E2E6)
                            ),
                            border = InputChipDefaults.inputChipBorder(
                                enabled = true,
                                selected = false,
                                borderColor = Color(0xFF3A3A3F),
                                selectedBorderColor = Color(0xFFFFB000),
                                borderWidth = 1.dp
                            )
                        )
                    }
                }
            }
        }
    }
}

// ==========================================
// SCREEN 2: GALLERY / SEARCH
// ==========================================
@Composable
fun SearchScreen(viewModel: MainViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search Bar
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.performSearch(it) },
            placeholder = { Text("Search images (Khatu Shyam, Ganesha...)", color = Color(0xFFA0A0AA)) },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFFFFB000)) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color(0xFFE2E2E6),
                unfocusedTextColor = Color(0xFFE2E2E6),
                focusedBorderColor = Color(0xFFFFB000),
                unfocusedBorderColor = Color(0xFF2A2A30),
                focusedContainerColor = Color(0xFF16161B),
                unfocusedContainerColor = Color(0xFF16161B)
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))

        // History / suggestions
        if (viewModel.searchQuery.isEmpty()) {
            Text(
                text = "SUGGESTIONS",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFA0A0AA))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.searchSuggestions.forEach { suggest ->
                    SuggestionChip(
                        onClick = { viewModel.performSearch(suggest) },
                        label = { Text(suggest) },
                        colors = SuggestionChipDefaults.suggestionChipColors(
                            containerColor = Color(0xFF16161B),
                            labelColor = Color(0xFFE2E2E6)
                        ),
                        border = SuggestionChipDefaults.suggestionChipBorder(
                            enabled = true,
                            borderColor = Color(0xFF2A2A30)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "SEARCH HISTORY",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = Color(0xFFA0A0AA))
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                viewModel.searchHistory.forEach { hist ->
                    InputChip(
                        selected = false,
                        onClick = { viewModel.performSearch(hist) },
                        label = { Text(hist) },
                        trailingIcon = { Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFFFB000)) },
                        colors = InputChipDefaults.inputChipColors(
                            containerColor = Color(0xFF242429),
                            labelColor = Color(0xFFE2E2E6)
                        ),
                        border = InputChipDefaults.inputChipBorder(
                            enabled = true,
                            selected = false,
                            borderColor = Color(0xFF3A3A3F),
                            selectedBorderColor = Color(0xFFFFB000),
                            borderWidth = 1.dp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Gridded backgrounds list
        Text(
            text = "DEVOTIONAL BACKGROUNDS (${viewModel.filteredBackgrounds.size})",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFFFB000))
        )
        Spacer(modifier = Modifier.height(12.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(viewModel.filteredBackgrounds) { img ->
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(3f / 4f)
                        .clickable {
                            viewModel.selectedBackground = img.url
                            viewModel.projectName = "Edit ${img.title}"
                            viewModel.currentScreen = Screen.CanvasEditor
                        }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = img.url,
                            contentDescription = img.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                                    )
                                )
                                .align(Alignment.BottomCenter)
                                .padding(6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Text(
                                text = img.title,
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}

// Helper to layout flow row
@Composable
fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable () -> Unit
) {
    Layout(
        content = content,
        modifier = modifier
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints) }
        val layoutWidth = constraints.maxWidth
        val lines = mutableListOf<List<androidx.compose.ui.layout.Placeable>>()
        var currentLine = mutableListOf<androidx.compose.ui.layout.Placeable>()
        var currentLineWidth = 0

        placeables.forEach { placeable ->
            if (currentLineWidth + placeable.width > layoutWidth && currentLine.isNotEmpty()) {
                lines.add(currentLine)
                currentLine = mutableListOf()
                currentLineWidth = 0
            }
            currentLine.add(placeable)
            currentLineWidth += placeable.width + 16
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine)
        }

        var totalHeight = 0
        lines.forEach { line ->
            totalHeight += (line.maxOfOrNull { it.height } ?: 0) + 16
        }

        layout(layoutWidth, totalHeight) {
            var y = 0
            lines.forEach { line ->
                var x = 0
                val lineHeight = line.maxOfOrNull { it.height } ?: 0
                line.forEach { placeable ->
                    placeable.placeRelative(x, y)
                    x += placeable.width + 16
                }
                y += lineHeight + 16
            }
        }
    }
}

// ==========================================
// SCREEN 3: GENERATOR AI SCREEN (LOADING)
// ==========================================
@Composable
fun GeneratorScreen(viewModel: MainViewModel) {
    // Shared structure for AI image generation, prompt input, and styles.
    // Standard loader shown when isProcessingAi is true
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = Color(0xFFFF9933),
                modifier = Modifier
                    .size(64.dp)
                    .animateContentSize()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Invoking the Divine AI...",
                style = MaterialTheme.typography.headlineMedium.copy(color = Color(0xFF8F4E00))
            )
            Spacer(modifier = Modifier.height(8.dp))
            CircularProgressIndicator(color = Color(0xFFFF9933))
        }
    }
}

// ==========================================
// SCREEN 4: PROFESSIONAL CANVAS EDITOR
// ==========================================
@Composable
fun CanvasEditorScreen(viewModel: MainViewModel) {
    val context = LocalContext.current
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }
    var showDialog by remember { mutableStateOf(false) }
    var dialogText by remember { mutableStateOf("") }
    
    // Bottom Sheet tab controller
    var editorTab by remember { mutableStateOf("Text") } // "Text", "Stickers", "Frames", "Effects", "Adjust"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF121212)) // Dark workspace for Canvas focus!
    ) {
        // Workspace Top Actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = { viewModel.currentScreen = Screen.Home }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            BasicTextField(
                value = viewModel.projectName,
                onValueChange = { viewModel.projectName = it },
                textStyle = TextStyle(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp)
            )

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Remix Button
                IconButton(onClick = { viewModel.remixDesign() }) {
                    Icon(Icons.Default.Cyclone, contentDescription = "Remix", tint = Color(0xFFFED65B))
                }
                // Save Button
                IconButton(onClick = {
                    viewModel.saveProject()
                    Toast.makeText(context, "Divine project saved locally!", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                }
                // HD Download Export Button
                Button(
                    onClick = {
                        Toast.makeText(context, "HD status rendered! Ready to Share.", Toast.LENGTH_LONG).show()
                        // Open share sheet simulator
                        val shareIntent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "Created this beautiful Khatu Shyam status poster using Shyam AI Status App!")
                            type = "text/plain"
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Divine Poster"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9933)),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Export HD", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }

        // Interactive Canvas Card (3:4 ratio)
        BoxWithConstraints(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            val constraintsScope = this
            val canvasWidthPx = constraintsScope.maxWidth
            val canvasHeightPx = constraintsScope.maxHeight

            Box(
                modifier = Modifier
                    .aspectRatio(3f / 4f)
                    .fillMaxHeight()
                    .shadow(16.dp, RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.Black)
                    .onGloballyPositioned { coordinates ->
                        canvasSize = coordinates.size
                    }
                    // Apply Background Filters
                    .graphicsLayer(
                        alpha = viewModel.brightnessVal,
                        scaleX = viewModel.contrastVal,
                        scaleY = viewModel.contrastVal
                    )
            ) {
                // Background image
                AsyncImage(
                    model = viewModel.selectedBackground,
                    contentDescription = "Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(if (viewModel.blurVal > 0) (viewModel.blurVal * 15).dp else 0.dp),
                    colorFilter = when (viewModel.toneTypeVal) {
                        "Warm" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                            setToScale(1.1f, 1.0f, 0.9f, 1f)
                        })
                        "Cool" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                            setToScale(0.9f, 1.0f, 1.15f, 1f)
                        })
                        else -> null
                    }
                )

                // Overlays based on Effects (Module 5)
                when (viewModel.selectedEffect) {
                    "Golden Glow" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFF9933).copy(alpha = 0.3f), Color.Transparent),
                                        center = Offset(canvasSize.width / 2f, canvasSize.height / 2f)
                                    ),
                                    alpha = 0.6f
                                )
                        )
                    }
                    "Divine Aura" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(Color(0xFFFED65B).copy(alpha = 0.35f), Color.Transparent),
                                        radius = 400f
                                    )
                                )
                        )
                    }
                    "Mandala" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .drawBehind {
                                    drawCircle(
                                        color = Color(0xFFD4AF37).copy(alpha = 0.15f),
                                        radius = size.minDimension / 3f,
                                        center = Offset(size.width / 2, size.height / 2)
                                    )
                                }
                        )
                    }
                    "Sparkles" -> {
                        // Drawing subtle light sparkles using gradient dots
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFFFED65B).copy(alpha = 0.1f), Color.Transparent)
                                    )
                                )
                        )
                    }
                }

                // Overlays based on Frames (Module 6)
                when (viewModel.selectedFrame) {
                    "Golden" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(8.dp, Color(0xFFD4AF37))
                                .padding(12.dp)
                                .border(1.5.dp, Color(0xFFFED65B))
                        )
                    }
                    "Temple" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(12.dp, Color(0xFF8F4E00))
                                .padding(12.dp)
                                .border(2.dp, Color(0xFFFED65B))
                        )
                    }
                    "Floral" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(6.dp, Color(0xFFD4AF37).copy(alpha = 0.7f))
                        )
                    }
                    "Lotus" -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(8.dp, Color(0xFFFFDAD6))
                        )
                    }
                }

                // Render Sticker layers
                viewModel.stickerLayers.forEach { sticker ->
                    val isSelected = viewModel.selectedStickerLayerId == sticker.id
                    val x = sticker.xPercent * canvasSize.width
                    val y = sticker.yPercent * canvasSize.height

                    Box(
                        modifier = Modifier
                            .offset(
                                x = (sticker.xPercent * (canvasSize.width / LocalContext.current.resources.displayMetrics.density)).dp - 24.dp,
                                y = (sticker.yPercent * (canvasSize.height / LocalContext.current.resources.displayMetrics.density)).dp - 24.dp
                            )
                            .size(54.dp)
                            .graphicsLayer(
                                scaleX = sticker.scale,
                                scaleY = sticker.scale,
                                rotationZ = sticker.rotation
                            )
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color(0xFFFF9933) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .pointerInput(sticker.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                                        viewModel.selectedStickerLayerId = sticker.id
                                        viewModel.selectedTextLayerId = null
                                        val newX = (sticker.xPercent + dragAmount.x / canvasSize.width).coerceIn(0.05f, 0.95f)
                                        val newY = (sticker.yPercent + dragAmount.y / canvasSize.height).coerceIn(0.05f, 0.95f)
                                        viewModel.updateSelectedSticker {
                                            it.xPercent = newX
                                            it.yPercent = newY
                                        }
                                    }
                                }
                            }
                            .clickable {
                                viewModel.selectedStickerLayerId = sticker.id
                                viewModel.selectedTextLayerId = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val stickerEmoji = when (sticker.stickerType) {
                            "om" -> "ॐ"
                            "swastik" -> "卐"
                            "lotus" -> "🪷"
                            "diya" -> "🪔"
                            "peacock_feather" -> "🪶"
                            "bell" -> "🔔"
                            "trishul" -> "🔱"
                            else -> "🚩"
                        }
                        Text(
                            text = stickerEmoji,
                            fontSize = 32.sp,
                            color = Color(0xFFFED65B)
                        )
                    }
                }

                // Render Text layers
                viewModel.textLayers.forEach { layer ->
                    val isSelected = viewModel.selectedTextLayerId == layer.id
                    val color = Color(android.graphics.Color.parseColor(layer.fontColorHex))

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(
                                x = (layer.xPercent * (canvasSize.width / LocalContext.current.resources.displayMetrics.density)).dp - 100.dp,
                                y = (layer.yPercent * (canvasSize.height / LocalContext.current.resources.displayMetrics.density)).dp - 30.dp
                            )
                            .width(200.dp)
                            .graphicsLayer(rotationZ = layer.rotation)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) Color(0xFFFF9933) else Color.Transparent,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .pointerInput(layer.id) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    if (canvasSize.width > 0 && canvasSize.height > 0) {
                                        viewModel.selectedTextLayerId = layer.id
                                        viewModel.selectedStickerLayerId = null
                                        val newX = (layer.xPercent + dragAmount.x / canvasSize.width).coerceIn(0.1f, 0.9f)
                                        val newY = (layer.yPercent + dragAmount.y / canvasSize.height).coerceIn(0.1f, 0.9f)
                                        viewModel.updateSelectedText {
                                            it.xPercent = newX
                                            it.yPercent = newY
                                        }
                                    }
                                }
                            }
                            .clickable {
                                viewModel.selectedTextLayerId = layer.id
                                viewModel.selectedStickerLayerId = null
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = layer.text,
                            style = TextStyle(
                                color = if (layer.isGold) Color(0xFFFED65B) else color,
                                fontSize = layer.fontSize.sp,
                                fontFamily = if (layer.fontFamily == "Libre Caslon Text") FontFamily.Serif else FontFamily.SansSerif,
                                fontWeight = if (layer.isBold) FontWeight.Bold else FontWeight.Normal,
                                shadow = if (layer.isShadow) Shadow(color = Color.Black.copy(alpha = 0.8f), offset = Offset(2f, 2f), blurRadius = 4f) else null,
                                textAlign = TextAlign.Center
                            ),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // Professional Canvas Editing Panel
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Editing panel selector tabs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    val tabs = listOf("Text", "Stickers", "Frames", "Effects", "Adjust")
                    tabs.forEach { tab ->
                        val isSelected = editorTab == tab
                        Text(
                            text = tab,
                            color = if (isSelected) Color(0xFFFF9933) else Color.Gray,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier
                                .clickable { editorTab = tab }
                                .padding(vertical = 8.dp, horizontal = 12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action panel content
                when (editorTab) {
                    "Text" -> {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { viewModel.addTextLayer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9933))
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Add Text")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Text")
                                }

                                if (viewModel.selectedTextLayerId != null) {
                                    Button(
                                        onClick = {
                                            viewModel.textLayers.find { it.id == viewModel.selectedTextLayerId }?.let {
                                                dialogText = it.text
                                                showDialog = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                                    ) {
                                        Text("Edit Text", color = Color.Black)
                                    }

                                    IconButton(onClick = { viewModel.deleteSelectedText() }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    }
                                }
                            }

                            if (viewModel.selectedTextLayerId != null) {
                                // Formatting options
                                val current = viewModel.textLayers.find { it.id == viewModel.selectedTextLayerId }
                                current?.let { layer ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        // Font family
                                        InputChip(
                                            selected = layer.fontFamily == "Libre Caslon Text",
                                            onClick = { viewModel.updateSelectedText { it.fontFamily = if (it.fontFamily == "Libre Caslon Text") "Plus Jakarta Sans" else "Libre Caslon Text" } },
                                            label = { Text("Serif") }
                                        )
                                        // Bold
                                        InputChip(
                                            selected = layer.isBold,
                                            onClick = { viewModel.updateSelectedText { it.isBold = !it.isBold } },
                                            label = { Text("Bold") }
                                        )
                                        // Gold
                                        InputChip(
                                            selected = layer.isGold,
                                            onClick = { viewModel.updateSelectedText { it.isGold = !it.isGold } },
                                            label = { Text("Gold Text") }
                                        )
                                        // Shadow
                                        InputChip(
                                            selected = layer.isShadow,
                                            onClick = { viewModel.updateSelectedText { it.isShadow = !it.isShadow } },
                                            label = { Text("Shadow") }
                                        )
                                    }

                                    // Font size & rotation sliders
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Size", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        Slider(
                                            value = layer.fontSize,
                                            onValueChange = { size -> viewModel.updateSelectedText { it.fontSize = size } },
                                            valueRange = 12f..48f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("${layer.fontSize.toInt()}", color = Color.White)
                                    }

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Rotate", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                        Slider(
                                            value = layer.rotation,
                                            onValueChange = { rot -> viewModel.updateSelectedText { it.rotation = rot } },
                                            valueRange = -45f..45f,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Text("${layer.rotation.toInt()}°", color = Color.White)
                                    }
                                }
                            }
                        }
                    }

                    "Stickers" -> {
                        val stickers = listOf(
                            "om" to "ॐ", "swastik" to "卐", "lotus" to "🪷", "diya" to "🪔",
                            "peacock_feather" to "🪶", "bell" to "🔔", "trishul" to "🔱", "nishan" to "🚩"
                        )
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("TAP STICKER TO ADD", color = Color.White, style = MaterialTheme.typography.labelSmall)
                                if (viewModel.selectedStickerLayerId != null) {
                                    IconButton(onClick = { viewModel.deleteSelectedSticker() }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Delete Sticker", tint = Color.Red)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(stickers) { (type, emoji) ->
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color(0xFF2E2E2E))
                                            .clickable { viewModel.addSticker(type) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(emoji, fontSize = 28.sp)
                                    }
                                }
                            }
                        }
                    }

                    "Frames" -> {
                        val frames = listOf("None", "Golden", "Temple", "Floral", "Lotus")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            frames.forEach { frame ->
                                val isSelected = viewModel.selectedFrame == frame
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFF9933) else Color(0xFF2E2E2E))
                                        .clickable { viewModel.selectedFrame = frame },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = frame,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    "Effects" -> {
                        val effects = listOf("None", "Golden Glow", "Divine Aura", "Mandala", "Sparkles")
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(effects) { effect ->
                                val isSelected = viewModel.selectedEffect == effect
                                Box(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .height(44.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSelected) Color(0xFFFF9933) else Color(0xFF2E2E2E))
                                        .clickable { viewModel.selectedEffect = effect },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = effect,
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }
                    }

                    "Adjust" -> {
                        Column {
                            // Blur
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Blur", color = Color.White, modifier = Modifier.width(60.dp), style = MaterialTheme.typography.labelSmall)
                                Slider(
                                    value = viewModel.blurVal,
                                    onValueChange = { viewModel.blurVal = it },
                                    valueRange = 0f..1f,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            // Tone picker
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                listOf("Normal", "Warm", "Cool").forEach { tone ->
                                    val isSelected = viewModel.toneTypeVal == tone
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) Color(0xFFFF9933) else Color(0xFF2E2E2E))
                                            .clickable { viewModel.toneTypeVal = tone },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(tone, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // AI Smart Designer Button (Module 4)
                Button(
                    onClick = {
                        viewModel.applySmartAutoLayout()
                        Toast.makeText(context, "AI Optimizer applied best contrast, sizes, and layout!", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFED65B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = Color(0xFF745C00))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Optimize Layout with AI", color = Color(0xFF745C00), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Edit text Dialog
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Edit Devotional Text") },
            text = {
                OutlinedTextField(
                    value = dialogText,
                    onValueChange = { dialogText = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateSelectedText { it.text = dialogText }
                    showDialog = false
                }) {
                    Text("Done")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// SCREEN 5: PROJECT LIBRARY (Module 9)
// ==========================================
@Composable
fun LibraryScreen(viewModel: MainViewModel) {
    val projects by viewModel.savedProjects.collectAsState()
    val context = LocalContext.current
    var showRenameDialog by remember { mutableStateOf<Project?>(null) }
    var renameInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "MY DEVOTIONAL CREATIONS",
            style = MaterialTheme.typography.displayMedium.copy(color = Color(0xFFFFB000))
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Your custom spiritual status creations are saved offline locally.",
            style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFFA0A0AA))
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (projects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.CloudQueue,
                        contentDescription = "Empty",
                        tint = Color(0xFFA0A0AA),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No saved projects found. Create your first status!", color = Color(0xFFA0A0AA))
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { viewModel.currentScreen = Screen.Home },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFB000), contentColor = Color.Black)
                    ) {
                        Text("Create New Poster", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(projects) { project ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail background image preview
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .size(72.dp)
                                    .aspectRatio(1f)
                            ) {
                                AsyncImage(
                                    model = project.backgroundImage,
                                    contentDescription = "Thumbnail",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = project.name,
                                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFE2E2E6))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Layers: ${project.getTextLayers().size + project.getStickerLayers().size}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA0A0AA))
                                )
                            }

                            // Actions
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                IconButton(onClick = { viewModel.openProject(project) }) {
                                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color(0xFFFFB000))
                                }
                                IconButton(onClick = { viewModel.duplicateProject(project) }) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color(0xFFA0A0AA))
                                }
                                IconButton(onClick = { 
                                    showRenameDialog = project
                                    renameInput = project.name
                                }) {
                                    Icon(Icons.Default.DriveFileRenameOutline, contentDescription = "Rename", tint = Color(0xFFA0A0AA))
                                }
                                IconButton(onClick = { viewModel.deleteProject(project) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Rename project dialog
    showRenameDialog?.let { project ->
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Project") },
            text = {
                OutlinedTextField(
                    value = renameInput,
                    onValueChange = { renameInput = it },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (renameInput.isNotBlank()) {
                        viewModel.renameProject(project, renameInput)
                    }
                    showRenameDialog = null
                }) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ==========================================
// SCREEN 6: DEVOTIONAL PROFILE / STATS
// ==========================================
@Composable
fun ProfileScreen(viewModel: MainViewModel) {
    var generatedCount by remember { mutableStateOf(5) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Devotional avatar representation
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF242429))
                .border(2.dp, Color(0xFFFFB000), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.AutoAwesome,
                contentDescription = "Avatar",
                tint = Color(0xFFFFB000),
                modifier = Modifier.size(48.dp)
            )
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Divine Creator Account",
                style = MaterialTheme.typography.displayMedium.copy(fontSize = 28.sp, color = Color(0xFFE2E2E6))
            )
            Text(
                text = "Premium Life Member",
                style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFFFB000), fontWeight = FontWeight.Bold)
            )
        }

        // Stats Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(20.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("AI Creas", fontWeight = FontWeight.Bold, color = Color(0xFFA0A0AA), style = MaterialTheme.typography.labelSmall)
                    Text("$generatedCount", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB000))
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(Color(0xFF2A2A30))
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Limit Quota", fontWeight = FontWeight.Bold, color = Color(0xFFA0A0AA), style = MaterialTheme.typography.labelSmall)
                    Text("${viewModel.dailyQuotaLeft}/3", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFB000))
                }
            }
        }

        // Feature checklist
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF16161B)),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, Color(0xFF2A2A30), RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "APPLICATION FEATURES",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, color = Color(0xFFA0A0AA))
                )
                
                val features = listOf(
                    "AI Image Search with suggestions" to true,
                    "Prompt-based Bhakti, Neon, 3D style creation" to true,
                    "Professional Canvas Editor layer adjustments" to true,
                    "Mandala overlay, Divine effects, Gold aura" to true,
                    "Premium Borders and Gold frames overlays" to true,
                    "Offline project saving and management" to true
                )

                features.forEach { (text, status) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Ok",
                            tint = Color(0xFFFFB000),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE2E2E6))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Brand signature
        Text(
            text = "Divine Presence AI © 2026",
            style = MaterialTheme.typography.labelSmall.copy(color = Color(0xFFA0A0AA))
        )
    }
}
