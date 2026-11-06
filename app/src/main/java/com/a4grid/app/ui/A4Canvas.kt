package com.a4grid.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun A4Canvas(
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(8.dp)

    Surface(
        modifier = modifier
            .shadow(elevation = 4.dp, shape = shape)
            .clip(shape)
            .background(Color.White, shape),
        color = Color.White,
        shape = shape
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            repeat(4) { rowIndex ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    repeat(2) { columnIndex ->
                        val cellNumber = rowIndex * 2 + columnIndex + 1
                        CellBox(
                            index = cellNumber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
