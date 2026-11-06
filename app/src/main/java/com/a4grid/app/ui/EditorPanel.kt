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
import com.a4grid.app.R
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot

@Composable
fun EditorPanel(
    selectedIndex: Int?,
    slots: List<Slot?>,
    onZoom: (Float) -> Unit,
    onZoomFinished: () -> Unit,
    onFit: (FitMode) -> Unit,
    onRotateLeft: () -> Unit,
    onRotateRight: () -> Unit,
    onFineRotation: (Float) -> Unit,
    onFineFinished: () -> Unit,
    onReset: () -> Unit,
    onSwap: (Int) -> Unit,
    onDelete: () -> Unit
) {
    val index = selectedIndex ?: return
    val slot = slots.getOrNull(index) ?: return
    var swapDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(stringResource(R.string.zoom), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = slot.transform.zoom,
            onValueChange = onZoom,
            onValueChangeFinished = onZoomFinished,
            valueRange = 1f..3f
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = slot.transform.fit == FitMode.CONTAIN,
                onClick = { onFit(FitMode.CONTAIN) },
                label = { Text(stringResource(R.string.fit_contain)) },
                colors = FilterChipDefaults.filterChipColors()
            )
            FilterChip(
                selected = slot.transform.fit == FitMode.COVER,
                onClick = { onFit(FitMode.COVER) },
                label = { Text(stringResource(R.string.fit_cover)) },
                colors = FilterChipDefaults.filterChipColors()
            )
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Button(onClick = onRotateLeft) { Text(stringResource(R.string.rotate_left)) }
            Button(onClick = onRotateRight) { Text(stringResource(R.string.rotate_right)) }
        }
        Text(stringResource(R.string.fine_rotation), style = MaterialTheme.typography.labelLarge)
        Slider(
            value = slot.transform.fineRotation,
            onValueChange = onFineRotation,
            onValueChangeFinished = onFineFinished,
            valueRange = -45f..45f
        )
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
            text = {
                Column {
                    slots.forEachIndexed { otherIndex, otherSlot ->
                        if (otherIndex != index && otherSlot != null) {
                            TextButton(onClick = { swapDialog = false; onSwap(otherIndex) }) {
                                Text("${otherIndex + 1}")
                            }
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { swapDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}
