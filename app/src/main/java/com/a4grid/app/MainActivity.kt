package com.a4grid.app

import android.os.Bundle
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.a4grid.app.ui.A4GridApp
import com.a4grid.app.ui.A4GridViewModel

class MainActivity : ComponentActivity() {
    private lateinit var gridViewModel: A4GridViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.layoutDirection = View.LAYOUT_DIRECTION_RTL
        gridViewModel = A4GridViewModel(this)
        setContent {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                A4GridApp(gridViewModel)
            }
        }
    }

    override fun onPause() {
        gridViewModel.save()
        super.onPause()
    }

    override fun onStop() {
        gridViewModel.save()
        super.onStop()
    }
}
