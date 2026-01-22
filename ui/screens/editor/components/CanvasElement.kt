package com.itpdf.app.ui.screens.editor.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.itpdf.app.domain.model.EditorElement
import com.itpdf.app.domain.model.ElementType
import kotlin.math.roundToInt

@Composable
fun CanvasElement(
    element: EditorElement,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onMove: (Float, Float) -> Unit,
    onResize: (Float, Float) -> Unit,
    onRotate: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .offset { IntOffset(element.x.roundToInt(), element.y.roundToInt()) }
            .size(element.width.dp, element.height.dp)
            .graphicsLayer {
                rotationZ = element.rotation
            }
            .pointerInput(element.id) {
                detectTapGestures(onTap = { onSelect() })
            }
            .pointerInput(element.id) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onMove(dragAmount.x, dragAmount.y)
                    }
                )
            }
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = RectangleShape
                    )
                } else {
                    Modifier
                }
            )
    ) {
        when (element.type) {
            ElementType.TEXT -> TextElementContent(element)
            ElementType.IMAGE -> ImageElementContent(element)
        }

        if (isSelected) {
            // Resize Handle (Bottom Right)
            EditorHandle(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 8.dp, y = 8.dp),
                onDrag = onResize
            )

            // Rotation Handle (Top Center)
            RotationHandle(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-24).dp),
                onRotate = { dragAmountX ->
                    onRotate(dragAmountX)
                }
            )
        }
    }
}

@Composable
private fun TextElementContent(element: EditorElement) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(2.dp),
        contentAlignment = when (element.textAlign.lowercase()) {
            "center" -> Alignment.Center
            "right" -> Alignment.CenterEnd
            else -> Alignment.CenterStart
        }
    ) {
        Text(
            text = element.content,
            color = Color(element.textColor),
            fontSize = element.fontSize.sp,
            fontWeight = if (element.isBold) FontWeight.Bold else FontWeight.Normal,
            fontStyle = if (element.isItalic) FontStyle.Italic else FontStyle.Normal,
            textAlign = when (element.textAlign.lowercase()) {
                "center" -> TextAlign.Center
                "right" -> TextAlign.Right
                else -> TextAlign.Left
            },
            lineHeight = (element.fontSize * 1.3).sp
        )
    }
}

@Composable
private fun ImageElementContent(element: EditorElement) {
    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(element.content)
            .crossfade(true)
            .build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.FillBounds
    )
}

@Composable
private fun EditorHandle(
    modifier: Modifier = Modifier,
    onDrag: (Float, Float) -> Unit
) {
    Surface(
        modifier = modifier
            .size(16.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onDrag(dragAmount.x, dragAmount.y)
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primary,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
    ) {}
}

@Composable
private fun RotationHandle(
    modifier: Modifier = Modifier,
    onRotate: (Float) -> Unit
) {
    Surface(
        modifier = modifier
            .size(24.dp)
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    onRotate(dragAmount.x)
                }
            },
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondary,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(2.dp, Color.White)
    ) {
        Icon(
            imageVector = Icons.Default.Refresh,
            contentDescription = "Rotate",
            modifier = Modifier.padding(4.dp),
            tint = Color.White
        )
    }
}