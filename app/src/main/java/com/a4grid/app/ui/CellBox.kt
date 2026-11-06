package com.a4grid.app.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun CellBox(
    index: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .border(1.dp, Color.LightGray),
        contentAlignment = Alignment.TopEnd
    ) {
        Text(
            text = index.toString(),
            modifier = Modifier.padding(6.dp),
            color = Color.DarkGray,
            fontSize = 16.sp
        )
    }
}
