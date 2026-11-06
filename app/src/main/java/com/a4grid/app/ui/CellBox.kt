package com.a4grid.app.ui

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a4grid.app.R
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot

@Composable
fun CellBox(
    index: Int,
    slot: Slot?,
    selected: Boolean,
    onSelect: () -> Unit,
    onDrag: (Float, Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(slot?.uri) { slot?.uri?.let { BitmapFactory.decodeFile(it) } }
    Box(
        modifier = modifier
            .fillMaxSize()
            .border(if (selected) 2.dp else 1.dp, if (selected) Color(0xFF1565C0) else Color.LightGray)
            .clipToBounds()
            .clickable(onClick = onSelect)
            .pointerInput(index, selected) {
                detectDragGestures(
                    onDragStart = { onSelect() },
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragEnd,
                    onDrag = { change, amount ->
                        change.consume()
                        if (selected) onDrag(amount.x, amount.y)
                    }
                )
            },
        contentAlignment = Alignment.TopEnd
    ) {
        if (bitmap != null && slot != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = stringResource(R.string.selected_cell),
                contentScale = if (slot.transform.fit == FitMode.COVER) ContentScale.Crop else ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        scaleX = slot.transform.zoom * slot.transform.stretchX
                        scaleY = slot.transform.zoom * slot.transform.stretchY
                        rotationZ = slot.transform.rotation + slot.transform.fineRotation
                        translationX = slot.transform.offsetX
                        translationY = slot.transform.offsetY
                    }
            )
        }
        Text(
            text = index.toString(),
            modifier = Modifier.padding(5.dp),
            color = if (selected) Color(0xFF1565C0) else Color.DarkGray,
            fontSize = 14.sp
        )
    }
}
