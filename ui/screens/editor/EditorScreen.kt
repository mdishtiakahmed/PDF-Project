package com.itpdf.app.ui.screens.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.itpdf.app.domain.model.EditorElement
import com.itpdf.app.domain.model.ElementType
import java.util.UUID
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    onBackClick: () -> Unit,
    onSaveClick: (List<EditorElement>) -> Unit,
    onAiAssistClick: () -> Unit
) {
    var elements by remember { mutableStateOf(listOf<EditorElement>()) }
    var selectedElementId by remember { mutableStateOf<String?>(null) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    
    val selectedElement = elements.find { it.id == selectedElementId }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editor", style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = { onSaveClick(elements) }) {
                        Text("SAVE", fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAiAssistClick,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = { Icon(Icons.Default.AutoAwesome, null) },
                text = { Text("Ask AI") }
            )
        },
        bottomBar = {
            EditorBottomToolbar(
                selectedElement = selectedElement,
                onUpdateElement = { updated ->
                    elements = elements.map { if (it.id == updated.id) updated else it }
                },
                onDelete = {
                    elements = elements.filter { it.id != selectedElementId }
                    selectedElementId = null
                },
                onAddText = {
                    val newElement = EditorElement(
                        id = UUID.randomUUID().toString(),
                        type = ElementType.TEXT,
                        text = "New Text Block",
                        x = 0.1f,
                        y = 0.1f,
                        fontSize = 18f
                    )
                    elements = elements + newElement
                    selectedElementId = newElement.id
                },
                onAddImage = {
                    val newElement = EditorElement(
                        id = UUID.randomUUID().toString(),
                        type = ElementType.IMAGE,
                        imageUrl = "https://via.placeholder.com/300",
                        x = 0.2f,
                        y = 0.2f,
                        width = 150f,
                        height = 150f
                    )
                    elements = elements + newElement
                    selectedElementId = newElement.id
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.TopStart)
                    .zIndex(2f)
            ) {
                FloatingActionButton(
                    onClick = { /* Undo */ },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) { Icon(Icons.Default.Undo, "Undo", modifier = Modifier.size(20.dp)) }
                Spacer(Modifier.height(12.dp))
                FloatingActionButton(
                    onClick = { /* Redo */ },
                    modifier = Modifier.size(40.dp),
                    containerColor = MaterialTheme.colorScheme.surface,
                    elevation = FloatingActionButtonDefaults.elevation(2.dp)
                ) { Icon(Icons.Default.Redo, "Redo", modifier = Modifier.size(20.dp)) }
            }

            val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
                scale = (scale * zoomChange).coerceIn(0.5f, 4f)
                offset += offsetChange
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(state = transformState)
                    .pointerInput(Unit) {
                        detectTapGestures { selectedElementId = null }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures { _, dragAmount -> offset += dragAmount }
                    }
            ) {
                A4Paper(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        ),
                    elements = elements,
                    selectedElementId = selectedElementId,
                    onElementClick = { selectedElementId = it },
                    onElementMove = { id, dx, dy ->
                        elements = elements.map {
                            if (it.id == id) it.copy(x = it.x + dx, y = it.y + dy) else it
                        }
                    },
                    onElementResize = { id, dw, dh ->
                        elements = elements.map {
                            if (it.id == id) {
                                it.copy(
                                    width = (it.width + dw).coerceAtLeast(40f),
                                    height = (it.height + dh).coerceAtLeast(20f)
                                )
                            } else it
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun A4Paper(
    modifier: Modifier = Modifier,
    elements: List<EditorElement>,
    selectedElementId: String?,
    onElementClick: (String) -> Unit,
    onElementMove: (String, Float, Float) -> Unit,
    onElementResize: (String, Float, Float) -> Unit
) {
    val density = LocalDensity.current
    BoxWithConstraints(modifier = modifier) {
        val a4WidthPx = constraints.maxWidth * 0.85f
        val a4HeightPx = a4WidthPx * 1.414f
        val a4WidthDp = with(density) { a4WidthPx.toDp() }
        val a4HeightDp = with(density) { a4HeightPx.toDp() }

        Box(
            modifier = Modifier
                .size(a4WidthDp, a4HeightDp)
                .shadow(12.dp, shape = RoundedCornerShape(2.dp))
                .background(Color.White)
                .pointerInput(Unit) { detectTapGestures { onElementClick("") } }
        ) {
            elements.forEach { element ->
                val isSelected = element.id == selectedElementId
                val xPos = element.x * a4WidthPx
                val yPos = element.y * a4HeightPx

                Box(
                    modifier = Modifier
                        .offset { IntOffset(xPos.roundToInt(), yPos.roundToInt()) }
                        .sizeIn(minWidth = 40.dp, minHeight = 20.dp)
                        .then(
                            if (element.type == ElementType.IMAGE) 
                                Modifier.size(element.width.dp, element.height.dp)
                            else Modifier.widthIn(max = a4WidthDp - with(density) { xPos.toDp() })
                        )
                        .border(
                            width = if (isSelected) 1.5.dp else 0.dp,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                            shape = RoundedCornerShape(2.dp)
                        )
                        .pointerInput(element.id) {
                            detectDragGestures(
                                onDragStart = { onElementClick(element.id) },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    onElementMove(element.id, dragAmount.x / a4WidthPx, dragAmount.y / a4HeightPx)
                                }
                            )
                        }
                        .padding(if (isSelected) 4.dp else 0.dp)
                ) {
                    when (element.type) {
                        ElementType.TEXT -> {
                            Text(
                                text = element.text,
                                fontSize = element.fontSize.sp,
                                color = Color(element.color),
                                fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
                                textAlign = when (element.alignment) {
                                    "center" -> TextAlign.Center
                                    "right" -> TextAlign.Right
                                    else -> TextAlign.Left
                                }
                            )
                        }
                        ElementType.IMAGE -> {
                            AsyncImage(
                                model = element.imageUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    if (isSelected) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .align(Alignment.BottomEnd)
                                .offset(7.dp, 7.dp)
                                .background(MaterialTheme.colorScheme.primary, CircleShape)
                                .border(2.dp, Color.White, CircleShape)
                                .pointerInput(element.id) {
                                    detectDragGestures { change, dragAmount ->
                                        change.consume()
                                        onElementResize(element.id, dragAmount.x / density.density, dragAmount.y / density.density)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditorBottomToolbar(
    selectedElement: EditorElement?,
    onUpdateElement: (EditorElement) -> Unit,
    onDelete: () -> Unit,
    onAddText: () -> Unit,
    onAddImage: () -> Unit
) {
    var activeTab by remember { mutableStateOf("tools") }

    Surface(
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (selectedElement != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedElement.type == ElementType.TEXT) "EDITING TEXT" else "EDITING IMAGE",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            }

            Box(modifier = Modifier.height(110.dp)) {
                if (selectedElement == null || activeTab == "tools") {
                    Row(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ToolItem(Icons.Default.TextFields, "Text", onAddText)
                        ToolItem(Icons.Default.Image, "Image", onAddImage)
                        ToolItem(Icons.Default.ColorLens, "Page Bg") { }
                        ToolItem(Icons.Default.SquareFoot, "Margins") { }
                    }
                } else if (selectedElement.type == ElementType.TEXT) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = selectedElement.isBold,
                                onClick = { onUpdateElement(selectedElement.copy(isBold = !selectedElement.isBold)) },
                                label = { Text("Bold") },
                                leadingIcon = { Icon(Icons.Default.FormatBold, null) }
                            )
                        }
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))) {
                                IconButton(onClick = { onUpdateElement(selectedElement.copy(fontSize = (selectedElement.fontSize - 1).coerceAtLeast(8f))) }) {
                                    Icon(Icons.Default.Remove, null)
                                }
                                Text("${selectedElement.fontSize.toInt()}", style = MaterialTheme.typography.bodyMedium)
                                IconButton(onClick = { onUpdateElement(selectedElement.copy(fontSize = (selectedElement.fontSize + 1).coerceAtMost(72f))) }) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                        }
                        item {
                            ColorCircle(Color.Black) { onUpdateElement(selectedElement.copy(color = 0xFF000000.toInt())) }
                        }
                        item {
                            ColorCircle(Color.Red) { onUpdateElement(selectedElement.copy(color = 0xFFFF0000.toInt())) }
                        }
                        item {
                            ColorCircle(Color.Blue) { onUpdateElement(selectedElement.copy(color = 0xFF0000FF.toInt())) }
                        }
                    }
                }
            }

            NavigationBar(containerColor = Color.Transparent, modifier = Modifier.height(64.dp)) {
                val tabs = listOf(
                    Triple("tools", Icons.Default.Build, "Tools"),
                    Triple("style", Icons.Default.TextFormat, "Style"),
                    Triple("color", Icons.Default.Palette, "Color"),
                    Triple("layers", Icons.Default.Layers, "Layers")
                )
                tabs.forEach { (id, icon, label) ->
                    NavigationBarItem(
                        selected = activeTab == id,
                        onClick = { activeTab = id },
                        icon = { Icon(icon, null) },
                        label = { Text(label) }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        FilledTonalIconButton(onClick = onClick, modifier = Modifier.size(48.dp)) {
            Icon(icon, contentDescription = label)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun ColorCircle(color: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(color)
            .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
            .pointerInput(Unit) { detectTapGestures { onClick() } }
    )
}