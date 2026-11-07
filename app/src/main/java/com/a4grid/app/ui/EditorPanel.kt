package com.a4grid.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.a4grid.app.R
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot

@Composable
fun EditorPanel(
    selectedIndex: Int?,
    slots: List<Slot?>,
    onZoom: (Float) -> Unit,
    onZoomFinished: () -> Unit,
    onStretchX: (Float) -> Unit,
    onStretchXFinished: () -> Unit,
    onStretchY: (Float) -> Unit,
    onStretchYFinished: () -> Unit,
    onCrop: (Float, Float, Float, Float) -> Unit,
    onCropFinished: () -> Unit,
    onResetCrop: () -> Unit,
    onFit: (FitMode) -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onReset: () -> Unit,
    onSwap: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val index = selectedIndex ?: return
    val slot = slots.getOrNull(index) ?: return
    var swapDialog by remember { mutableStateOf(false) }
    var cropDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(stringResource(R.string.zoom), style = MaterialTheme.typography.labelLarge)
        Slider(value = slot.transform.zoom, onValueChange = onZoom, onValueChangeFinished = onZoomFinished, valueRange = 1f..3f)
        Text(stringResource(R.string.stretch_horizontal), style = MaterialTheme.typography.labelLarge)
        Slider(value = slot.transform.stretchX, onValueChange = onStretchX, onValueChangeFinished = onStretchXFinished, valueRange = 0.5f..2f)
        Text(stringResource(R.string.stretch_vertical), style = MaterialTheme.typography.labelLarge)
        Slider(value = slot.transform.stretchY, onValueChange = onStretchY, onValueChangeFinished = onStretchYFinished, valueRange = 0.5f..2f)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = slot.transform.fit == FitMode.CONTAIN, onClick = { onFit(FitMode.CONTAIN) }, label = { Text(stringResource(R.string.fit_contain)) }, colors = FilterChipDefaults.filterChipColors())
            FilterChip(selected = slot.transform.fit == FitMode.COVER, onClick = { onFit(FitMode.COVER) }, label = { Text(stringResource(R.string.fit_cover)) }, colors = FilterChipDefaults.filterChipColors())
        }
        Button(onClick = { cropDialog = true }, modifier = Modifier.fillMaxWidth()) { Text(stringResource(R.string.crop_image)) }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onRotateLeft) { Text(stringResource(R.string.rotate_left)) }
            Button(onClick = onRotateRight) { Text(stringResource(R.string.rotate_right)) }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = onReset) { Text(stringResource(R.string.reset)) }
            TextButton(onClick = { swapDialog = true }) { Text(stringResource(R.string.swap_with)) }
            TextButton(onClick = onDelete) { Text(stringResource(R.string.delete_image)) }
        }
    }
    if (swapDialog) {
        AlertDialog(
            onDismissRequest = { swapDialog = false },
            title = { Text(stringResource(R.string.swap_with)) },
            text = { Column { slots.forEachIndexed { otherIndex, otherSlot -> if (otherIndex != index && otherSlot != null) TextButton(onClick = { swapDialog = false; onSwap(otherIndex) }) { Text("${otherIndex + 1}") } } } },
            confirmButton = { TextButton(onClick = { swapDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
    if (cropDialog) {
        var left by remember { mutableStateOf(slot.transform.cropLeft) }
        var top by remember { mutableStateOf(slot.transform.cropTop) }
        var right by remember { mutableStateOf(slot.transform.cropRight) }
        var bottom by remember { mutableStateOf(slot.transform.cropBottom) }
        Dialog(onDismissRequest = { cropDialog = false }, properties = DialogProperties(usePlatformDefaultWidth = false)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.crop_image), style = MaterialTheme.typography.titleLarge)
                Text(stringResource(R.string.crop_left)); Slider(value = left, onValueChange = { left = it.coerceAtMost(right - 0.05f) }, valueRange = 0f..0.95f)
                Text(stringResource(R.string.crop_top)); Slider(value = top, onValueChange = { top = it.coerceAtMost(bottom - 0.05f) }, valueRange = 0f..0.95f)
                Text(stringResource(R.string.crop_right)); Slider(value = right, onValueChange = { right = it.coerceAtLeast(left + 0.05f) }, valueRange = 0.05f..1f)
                Text(stringResource(R.string.crop_bottom)); Slider(value = bottom, onValueChange = { bottom = it.coerceAtLeast(top + 0.05f) }, valueRange = 0.05f..1f)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { onResetCrop(); cropDialog = false }) { Text(stringResource(R.string.reset_crop)) }
                    Button(onClick = { onCrop(left, top, right, bottom); onCropFinished(); cropDialog = false }) { Text(stringResource(R.string.apply)) }
                }
            }
        }
    }
}
