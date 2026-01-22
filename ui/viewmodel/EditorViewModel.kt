package com.itpdf.app.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.UUID

enum class TextAlignment {
    LEFT, CENTER, RIGHT
}

class EditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(EditorUIState())
    val uiState: StateFlow<EditorUIState> = _uiState.asStateFlow()

    private val undoStack = ArrayDeque<List<EditorElement>>()
    private val redoStack = ArrayDeque<List<EditorElement>>()

    @Immutable
    data class PageSettings(
        val width: Float = 595f,
        val height: Float = 842f,
        val backgroundColor: Color = Color.White,
        val margin: Float = 20f
    )

    sealed class EditorElement {
        abstract val id: String
        abstract val x: Float
        abstract val y: Float
        abstract val rotation: Float
        abstract val scale: Float
        abstract val zIndex: Int

        data class TextElement(
            override val id: String = UUID.randomUUID().toString(),
            override val x: Float = 50f,
            override val y: Float = 50f,
            override val rotation: Float = 0f,
            override val scale: Float = 1f,
            override val zIndex: Int = 0,
            val text: String = "New Text",
            val fontSize: Float = 16f,
            val color: Color = Color.Black,
            val fontFamily: String = "Roboto",
            val isBold: Boolean = false,
            val isItalic: Boolean = false,
            val alignment: TextAlignment = TextAlignment.LEFT
        ) : EditorElement()

        data class ImageElement(
            override val id: String = UUID.randomUUID().toString(),
            override val x: Float = 100f,
            override val y: Float = 100f,
            override val rotation: Float = 0f,
            override val scale: Float = 1f,
            override val zIndex: Int = 0,
            val imageUri: String,
            val width: Float = 200f,
            val height: Float = 200f
        ) : EditorElement()
    }

    data class EditorUIState(
        val elements: List<EditorElement> = emptyList(),
        val selectedElementId: String? = null,
        val pageSettings: PageSettings = PageSettings(),
        val canUndo: Boolean = false,
        val canRedo: Boolean = false,
        val isLoading: Boolean = false,
        val errorMessage: String? = null
    )

    fun addTextElement(content: String = "New Text") {
        saveToHistory()
        val nextZ = (_uiState.value.elements.maxByOrNull { it.zIndex }?.zIndex ?: -1) + 1
        val newText = EditorElement.TextElement(text = content, zIndex = nextZ)
        _uiState.update { it.copy(
            elements = (it.elements + newText).sortedBy { el -> el.zIndex },
            selectedElementId = newText.id
        ) }
    }

    fun addImageElement(uri: String) {
        saveToHistory()
        val nextZ = (_uiState.value.elements.maxByOrNull { it.zIndex }?.zIndex ?: -1) + 1
        val newImage = EditorElement.ImageElement(imageUri = uri, zIndex = nextZ)
        _uiState.update { it.copy(
            elements = (it.elements + newImage).sortedBy { el -> el.zIndex },
            selectedElementId = newImage.id
        ) }
    }

    fun selectElement(id: String?) {
        _uiState.update { it.copy(selectedElementId = id) }
    }

    fun deleteSelectedElement() {
        val selectedId = _uiState.value.selectedElementId ?: return
        saveToHistory()
        _uiState.update { state ->
            state.copy(
                elements = state.elements.filter { it.id != selectedId },
                selectedElementId = null
            )
        }
    }

    fun updateElementPosition(id: String, newX: Float, newY: Float) {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map {
                    if (it.id == id) {
                        when (it) {
                            is EditorElement.TextElement -> it.copy(x = newX, y = newY)
                            is EditorElement.ImageElement -> it.copy(x = newX, y = newY)
                        }
                    } else it
                }
            )
        }
    }

    fun updateTextProperties(
        id: String,
        text: String? = null,
        fontSize: Float? = null,
        color: Color? = null,
        fontFamily: String? = null,
        isBold: Boolean? = null,
        isItalic: Boolean? = null,
        alignment: TextAlignment? = null
    ) {
        saveToHistory()
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map {
                    if (it.id == id && it is EditorElement.TextElement) {
                        it.copy(
                            text = text ?: it.text,
                            fontSize = fontSize ?: it.fontSize,
                            color = color ?: it.color,
                            fontFamily = fontFamily ?: it.fontFamily,
                            isBold = isBold ?: it.isBold,
                            isItalic = isItalic ?: it.isItalic,
                            alignment = alignment ?: it.alignment
                        )
                    } else it
                }
            )
        }
    }

    fun updateElementTransform(id: String, rotation: Float? = null, scale: Float? = null) {
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map {
                    if (it.id == id) {
                        when (it) {
                            is EditorElement.TextElement -> it.copy(
                                rotation = rotation ?: it.rotation,
                                scale = scale ?: it.scale
                            )
                            is EditorElement.ImageElement -> it.copy(
                                rotation = rotation ?: it.rotation,
                                scale = scale ?: it.scale
                            )
                        }
                    } else it
                }
            )
        }
    }

    fun bringToFront(id: String) {
        saveToHistory()
        val maxZ = _uiState.value.elements.maxOfOrNull { it.zIndex } ?: 0
        _uiState.update { state ->
            state.copy(
                elements = state.elements.map {
                    if (it.id == id) {
                        when (it) {
                            is EditorElement.TextElement -> it.copy(zIndex = maxZ + 1)
                            is EditorElement.ImageElement -> it.copy(zIndex = maxZ + 1)
                        }
                    } else it
                }.sortedBy { it.zIndex }
            )
        }
    }

    fun updatePageBackground(color: Color) {
        saveToHistory()
        _uiState.update { it.copy(pageSettings = it.pageSettings.copy(backgroundColor = color)) }
    }

    fun saveToHistory() {
        undoStack.addFirst(_uiState.value.elements.toList())
        if (undoStack.size > 50) undoStack.removeLast()
        redoStack.clear()
        updateHistoryFlags()
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val currentState = _uiState.value.elements.toList()
            redoStack.addFirst(currentState)
            val previousState = undoStack.removeFirst()
            _uiState.update { it.copy(elements = previousState) }
            updateHistoryFlags()
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val currentState = _uiState.value.elements.toList()
            undoStack.addFirst(currentState)
            val nextState = redoStack.removeFirst()
            _uiState.update { it.copy(elements = nextState) }
            updateHistoryFlags()
        }
    }

    private fun updateHistoryFlags() {
        _uiState.update { it.copy(
            canUndo = undoStack.isNotEmpty(),
            canRedo = redoStack.isNotEmpty()
        ) }
    }

    fun getSelectedElement(): EditorElement? {
        return _uiState.value.elements.find { it.id == _uiState.value.selectedElementId }
    }

    fun clearCanvas() {
        saveToHistory()
        _uiState.update { it.copy(elements = emptyList(), selectedElementId = null) }
    }

    fun restoreState(elements: List<EditorElement>) {
        _uiState.update { it.copy(elements = elements.sortedBy { el -> el.zIndex }) }
        undoStack.clear()
        redoStack.clear()
        updateHistoryFlags()
    }
}