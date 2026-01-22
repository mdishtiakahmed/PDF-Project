package com.itpdf.app.ui.screens.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class ToolInfo(
    val id: String,
    val titleEn: String,
    val titleBn: String,
    val descriptionEn: String,
    val descriptionBn: String,
    val icon: ImageVector,
    val iconColor: Color,
    val category: ToolCategory
)

enum class ToolCategory(val titleEn: String, val titleBn: String, val color: Color, val indicator: String) {
    POPULAR("POPULAR", "বহুল ব্যবহৃত", Color(0xFF4CAF50), "🟢"),
    CONVERT("CONVERT", "কনভার্টার", Color(0xFFFFC107), "🟡"),
    SECURITY("SECURITY", "নিরাপত্তা", Color(0xFFF44336), "🔴"),
    EDIT("EDIT", "এডিটিং", Color(0xFF2196F3), "🔵")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolsScreen(
    onToolClick: (String) -> Unit,
    isBengali: Boolean = false
) {
    var searchQuery by remember { mutableStateOf("") }

    val allTools = remember {
        listOf(
            ToolInfo("merge", "Merge PDF", "মার্জ পিডিএফ", "Join multiple files", "একাধিক ফাইল যুক্ত করুন", Icons.Default.CallMerge, Color(0xFF4CAF50), ToolCategory.POPULAR),
            ToolInfo("split", "Split PDF", "স্প্লিট পিডিএফ", "Extract pages", "পৃষ্ঠা আলাদা করুন", Icons.Default.ContentCut, Color(0xFF4CAF50), ToolCategory.POPULAR),
            ToolInfo("compress", "Compress PDF", "কমপ্রেস পিডিএফ", "Reduce file size", "ফাইলের সাইজ কমান", Icons.Default.Compress, Color(0xFF4CAF50), ToolCategory.POPULAR),
            ToolInfo("text_to_pdf", "Text to PDF", "টেক্সট টু পিডিএফ", "Convert text files", "টেক্সট থেকে পিডিএফ", Icons.Default.Description, Color(0xFFFFC107), ToolCategory.CONVERT),
            ToolInfo("web_to_pdf", "Web to PDF", "ওয়েব টু পিডিএফ", "Convert URL to PDF", "লিঙ্ক থেকে পিডিএফ", Icons.Default.Public, Color(0xFFFFC107), ToolCategory.CONVERT),
            ToolInfo("lock_pdf", "Lock PDF", "লক পিডিএফ", "Add password", "পাসওয়ার্ড দিন", Icons.Default.Lock, Color(0xFFF44336), ToolCategory.SECURITY),
            ToolInfo("unlock_pdf", "Unlock PDF", "আনলক পিডিএফ", "Remove password", "পাসওয়ার্ড সরান", Icons.Default.LockOpen, Color(0xFFF44336), ToolCategory.SECURITY),
            ToolInfo("rotate_pdf", "Rotate", "রোটেট", "Change orientation", "ঘুরিয়ে নিন", Icons.Default.RotateRight, Color(0xFF2196F3), ToolCategory.EDIT),
            ToolInfo("delete_pages", "Delete Page", "পৃষ্ঠা মুছুন", "Remove specific pages", "নির্দিষ্ট পেজ সরান", Icons.Default.DeleteSweep, Color(0xFF2196F3), ToolCategory.EDIT)
        )
    }

    val filteredTools = remember(searchQuery) {
        if (searchQuery.isEmpty()) {
            allTools
        } else {
            allTools.filter {
                it.titleEn.contains(searchQuery, ignoreCase = true) ||
                it.titleBn.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isBengali) "সরঞ্জামসমূহ" else "Tools",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            AdBannerPlaceholder()
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = {
                    Text(
                        if (isBengali) "টুলস খুঁজুন..." else "Search tools...",
                        style = MaterialTheme.typography.bodyMedium
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                trailingIcon = {
                    TextButton(onClick = { /* Filter */ }) {
                        Text(
                            if (isBengali) "ফিল্টার" else "FILTER",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f),
                    containerColor = Color.White
                )
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (searchQuery.isEmpty()) {
                    ToolCategory.entries.forEach { category ->
                        val toolsInCategory = filteredTools.filter { it.category == category }
                        if (toolsInCategory.isNotEmpty()) {
                            item(span = { GridItemSpan(2) }) {
                                SectionHeader(
                                    title = if (isBengali) category.titleBn else category.titleEn,
                                    indicator = category.indicator
                                )
                            }
                            items(toolsInCategory) { tool ->
                                ToolCard(tool, isBengali) { onToolClick(tool.id) }
                            }
                        }
                    }
                } else {
                    items(filteredTools) { tool ->
                        ToolCard(tool, isBengali) { onToolClick(tool.id) }
                    }
                }

                item(span = { GridItemSpan(2) }) {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, indicator: String) {
    Text(
        text = "$indicator $title",
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun ToolCard(
    tool: ToolInfo,
    isBengali: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .border(1.dp, Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(16.dp)),
        color = Color.White,
        shadowElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = tool.iconColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = tool.icon,
                    contentDescription = null,
                    tint = tool.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = if (isBengali) tool.titleBn else tool.titleEn,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = if (isBengali) "(${tool.descriptionBn})" else "(${tool.descriptionEn})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun AdBannerPlaceholder() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "AdMob Banner - Small & Clean",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray.copy(alpha = 0.6f)
            )
        }
    }
}