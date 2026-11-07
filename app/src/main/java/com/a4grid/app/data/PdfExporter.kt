package com.a4grid.app.data

import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot
import java.io.File
import java.io.FileOutputStream

object PdfExporter {
    fun export(directory: File, slots: List<Slot?>): File {
        val outputDirectory = File(directory, "exports").apply { mkdirs() }
        val output = File(outputDirectory, "a4grid_${System.currentTimeMillis()}.pdf")
        val document = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val cellWidth = pageWidth / 2f
        val cellHeight = pageHeight / 4f
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)

        try {
            val page = document.startPage(PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create())
            val canvas = page.canvas
            canvas.drawColor(android.graphics.Color.WHITE)
            slots.forEachIndexed { index, slot ->
                if (slot == null) return@forEachIndexed
                val bitmap = BitmapFactory.decodeFile(slot.uri) ?: return@forEachIndexed
                val row = index / 2
                val rtlColumn = 1 - (index % 2)
                val left = rtlColumn * cellWidth
                val top = row * cellHeight
                val destination = RectF(left, top, left + cellWidth, top + cellHeight)
                canvas.save()
                canvas.clipRect(destination)
                val sourceWidth = bitmap.width.toFloat()
                val sourceHeight = bitmap.height.toFloat()
                val baseScale = if (slot.transform.fit == FitMode.COVER) {
                    maxOf(cellWidth / sourceWidth, cellHeight / sourceHeight)
                } else {
                    minOf(cellWidth / sourceWidth, cellHeight / sourceHeight)
                } * slot.transform.zoom
                val scaleX = baseScale * slot.transform.stretchX
                val scaleY = baseScale * slot.transform.stretchY
                // Apply transformations around the image center. Building the
                // transform through Canvas keeps rotated images inside their
                // intended cell; Matrix post operations can move them outside
                // the page when rotation and translation are combined.
                canvas.translate(
                    left + cellWidth / 2f + slot.transform.offsetX,
                    top + cellHeight / 2f + slot.transform.offsetY
                )
                canvas.rotate(slot.transform.rotation + slot.transform.fineRotation)
                canvas.scale(scaleX, scaleY)
                canvas.drawBitmap(bitmap, -sourceWidth / 2f, -sourceHeight / 2f, paint)
                canvas.restore()
                bitmap.recycle()
            }
            document.finishPage(page)
            FileOutputStream(output).use { document.writeTo(it) }
            return output
        } finally {
            document.close()
        }
    }
}
