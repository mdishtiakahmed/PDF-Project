package com.itpdf.app.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import java.io.File
import java.io.FileOutputStream

/**
 * PdfGenerator Engine
 * High-performance PDF generation engine optimized for Android.
 * Features: Bangla Unicode support, Auto-pagination, Image processing, and CV Templates.
 */
class PdfGenerator(private val context: Context) {

    companion object {
        const val A4_WIDTH = 595 // 72 DPI
        const val A4_HEIGHT = 842
        const val DEFAULT_MARGIN = 50f
        const val PAGE_BOTTOM_THRESHOLD = 800f
    }

    sealed class PdfElement {
        data class Text(
            val content: String,
            val x: Float,
            val y: Float,
            val fontSize: Float = 12f,
            val color: Int = Color.BLACK,
            val isBold: Boolean = false,
            val typeface: Typeface? = null,
            val alignment: Layout.Alignment = Layout.Alignment.ALIGN_NORMAL,
            val maxWidth: Int = (A4_WIDTH - (DEFAULT_MARGIN * 2)).toInt()
        ) : PdfElement()

        data class Image(
            val bitmap: Bitmap,
            val x: Float,
            val y: Float,
            val width: Float,
            val height: Float,
            val rotation: Float = 0f
        ) : PdfElement()

        data class Line(
            val startX: Float,
            val startY: Float,
            val endX: Float,
            val endY: Float,
            val thickness: Float = 1f,
            val color: Int = Color.LTGRAY
        ) : PdfElement()
    }

    data class PageData(
        val elements: List<PdfElement>,
        val backgroundColor: Int = Color.WHITE
    )

    /**
     * Creates a PDF file from structured page data.
     */
    fun createPdf(
        fileName: String,
        pages: List<PageData>,
        onSuccess: (File) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val pdfDocument = PdfDocument()
        
        try {
            pages.forEachIndexed { index, pageData ->
                val pageInfo = PdfDocument.PageInfo.Builder(A4_WIDTH, A4_HEIGHT, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas

                canvas.drawColor(pageData.backgroundColor)

                pageData.elements.forEach { element ->
                    when (element) {
                        is PdfElement.Text -> drawText(canvas, element)
                        is PdfElement.Image -> drawImage(canvas, element)
                        is PdfElement.Line -> drawLine(canvas, element)
                    }
                }
                pdfDocument.finishPage(page)
            }

            val directory = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "IT_PDF")
            if (!directory.exists()) directory.mkdirs()

            val sanitizedFileName = if (fileName.endsWith(".pdf")) fileName else "$fileName.pdf"
            val file = File(directory, sanitizedFileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }

            onSuccess(file)
        } catch (e: Exception) {
            onError(e)
        } finally {
            pdfDocument.close()
        }
    }

    private fun drawText(canvas: Canvas, element: PdfElement.Text) {
        val textPaint = TextPaint().apply {
            color = element.color
            textSize = element.fontSize
            isAntiAlias = true
            typeface = element.typeface ?: if (element.isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }

        val staticLayout = StaticLayout.Builder.obtain(
            element.content, 0, element.content.length, textPaint, element.maxWidth
        )
            .setAlignment(element.alignment)
            .setLineSpacing(0f, 1.15f)
            .setIncludePad(true)
            .build()

        canvas.save()
        canvas.translate(element.x, element.y)
        staticLayout.draw(canvas)
        canvas.restore()
    }

    private fun drawImage(canvas: Canvas, element: PdfElement.Image) {
        canvas.save()
        if (element.rotation != 0f) {
            canvas.rotate(element.rotation, element.x + element.width / 2, element.y + element.height / 2)
        }

        val destRect = Rect(
            element.x.toInt(),
            element.y.toInt(),
            (element.x + element.width).toInt(),
            (element.y + element.height).toInt()
        )
        
        val paint = Paint().apply {
            isFilterBitmap = true
            isAntiAlias = true
        }

        canvas.drawBitmap(element.bitmap, null, destRect, paint)
        canvas.restore()
    }

    private fun drawLine(canvas: Canvas, element: PdfElement.Line) {
        val paint = Paint().apply {
            color = element.color
            strokeWidth = element.thickness
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawLine(element.startX, element.startY, element.endX, element.endY, paint)
    }

    fun calculateTextHeight(text: String, fontSize: Float, maxWidth: Int, typeface: Typeface? = null, isBold: Boolean = false): Float {
        val textPaint = TextPaint().apply {
            this.textSize = fontSize
            this.typeface = typeface ?: if (isBold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        }
        val staticLayout = StaticLayout.Builder.obtain(text, 0, text.length, textPaint, maxWidth)
            .setLineSpacing(0f, 1.15f)
            .build()
        return staticLayout.height.toFloat()
    }

    /**
     * Generates a Professional CV with automatic pagination support.
     */
    fun generateCv(
        fileName: String,
        userName: String,
        jobTitle: String,
        details: List<Pair<String, String>>,
        profileImage: Bitmap?,
        onComplete: (File?) -> Unit
    ) {
        val pages = mutableListOf<PageData>()
        var currentElements = mutableListOf<PdfElement>()
        var currentY = DEFAULT_MARGIN

        // Header Section
        currentElements.add(PdfElement.Text(
            content = userName,
            x = DEFAULT_MARGIN,
            y = currentY,
            fontSize = 26f,
            isBold = true,
            color = Color.parseColor("#0D47A1")
        ))
        currentY += 32f

        currentElements.add(PdfElement.Text(
            content = jobTitle,
            x = DEFAULT_MARGIN,
            y = currentY,
            fontSize = 14f,
            color = Color.GRAY
        ))
        currentY += 40f

        profileImage?.let {
            currentElements.add(PdfElement.Image(
                bitmap = it,
                x = A4_WIDTH - 130f,
                y = DEFAULT_MARGIN,
                width = 80f,
                height = 80f
            ))
        }

        // Content Sections
        details.forEach { (title, content) ->
            val titleHeight = 30f
            val contentHeight = calculateTextHeight(content, 11f, (A4_WIDTH - (DEFAULT_MARGIN * 2)).toInt())
            
            // Check for page overflow
            if (currentY + titleHeight + contentHeight > PAGE_BOTTOM_THRESHOLD) {
                pages.add(PageData(currentElements))
                currentElements = mutableListOf()
                currentY = DEFAULT_MARGIN
            }

            // Section Title
            currentElements.add(PdfElement.Text(
                content = title.uppercase(),
                x = DEFAULT_MARGIN,
                y = currentY,
                fontSize = 13f,
                isBold = true,
                color = Color.parseColor("#1565C0")
            ))
            currentY += 18f

            currentElements.add(PdfElement.Line(
                startX = DEFAULT_MARGIN,
                startY = currentY,
                endX = A4_WIDTH - DEFAULT_MARGIN,
                endY = currentY,
                thickness = 1f,
                color = Color.parseColor("#E3F2FD")
            ))
            currentY += 12f

            // Section Content
            currentElements.add(PdfElement.Text(
                content = content,
                x = DEFAULT_MARGIN,
                y = currentY,
                fontSize = 11f,
                color = Color.BLACK
            ))
            currentY += contentHeight + 25f
        }

        pages.add(PageData(currentElements))

        createPdf(
            fileName = fileName,
            pages = pages,
            onSuccess = { onComplete(it) },
            onError = { onComplete(null) }
        )
    }
}