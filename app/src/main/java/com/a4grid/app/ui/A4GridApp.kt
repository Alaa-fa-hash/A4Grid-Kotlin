package com.a4grid.app.ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.a4grid.app.R
import com.a4grid.app.data.PdfExporter
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot
import com.a4grid.app.ui.theme.A4GridTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun A4GridApp(viewModel: A4GridViewModel) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val slots by viewModel.slots.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val canUndo by viewModel.canUndo.collectAsState()
    val canRedo by viewModel.canRedo.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var newProjectDialog by remember { mutableStateOf(false) }
    var exporting by remember { mutableStateOf(false) }
    var editing by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.saveVersion) {
        delay(800)
        viewModel.save()
    }
    LaunchedEffect(selectedIndex) {
        if (selectedIndex == null) editing = false
    }
    BackHandler(enabled = editing) { editing = false }

    val picker = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(8)
    ) { uris ->
        scope.launch {
            if (uris.isEmpty()) return@launch
            val result = runCatching { copyImages(context, uris, slots, viewModel) }
            if (result.isFailure) snackbarHostState.showSnackbar(context.getString(R.string.error_pick))
        }
    }

    fun selectCell(index: Int) {
        viewModel.select(index)
        editing = true
    }

    A4GridTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        if (editing) {
                            IconButton(onClick = { editing = false }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back_to_page))
                            }
                        }
                    },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(stringResource(R.string.app_name))
                            if (editing) selectedIndex?.let { Text("  ${it + 1}", style = MaterialTheme.typography.labelLarge) }
                        }
                    },
                    actions = {
                        IconButton(onClick = { viewModel.undo() }, enabled = canUndo) {
                            Icon(Icons.Default.Undo, contentDescription = stringResource(R.string.undo))
                        }
                        IconButton(onClick = { viewModel.redo() }, enabled = canRedo) {
                            Icon(Icons.Default.Redo, contentDescription = stringResource(R.string.redo))
                        }
                        TextButton(onClick = { newProjectDialog = true }) {
                            Text(stringResource(R.string.new_project))
                        }
                    }
                )
            },
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            if (editing && selectedIndex != null) {
                SingleImageEditor(
                    slots = slots,
                    selectedIndex = selectedIndex,
                    isLandscape = isLandscape,
                    viewModel = viewModel,
                    onDone = { editing = false }
                )
            } else {
                PagePreview(
                    innerPadding = innerPadding,
                    slots = slots,
                    selectedIndex = selectedIndex,
                    onSelect = ::selectCell,
                    viewModel = viewModel,
                    picker = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    exporting = exporting,
                    onExport = {
                        exporting = true
                        scope.launch {
                            val file = runCatching { withContext(Dispatchers.IO) { PdfExporter.export(context.filesDir, slots) } }
                            exporting = false
                            file.onSuccess {
                                snackbarHostState.showSnackbar(context.getString(R.string.export_success))
                                sharePdf(context, it)
                            }.onFailure { snackbarHostState.showSnackbar(context.getString(R.string.error_export)) }
                        }
                    }
                )
            }
            if (exporting) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
    }

    if (newProjectDialog) {
        AlertDialog(
            onDismissRequest = { newProjectDialog = false },
            title = { Text(stringResource(R.string.new_project)) },
            text = { Text(stringResource(R.string.confirm_new_project)) },
            confirmButton = {
                TextButton(onClick = { newProjectDialog = false; viewModel.clearAll(); editing = false }) { Text(stringResource(R.string.ok)) }
            },
            dismissButton = { TextButton(onClick = { newProjectDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }
}

@Composable
private fun PagePreview(
    innerPadding: PaddingValues,
    slots: List<Slot?>,
    selectedIndex: Int?,
    onSelect: (Int) -> Unit,
    viewModel: A4GridViewModel,
    picker: () -> Unit,
    exporting: Boolean,
    onExport: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp, vertical = 8.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        A4Canvas(
            slots = slots,
            selectedIndex = selectedIndex,
            onSelect = onSelect,
            onDrag = { x, y ->
                val index = viewModel.selectedIndex.value
                val current = index?.let { slots.getOrNull(it)?.transform }
                if (current != null) viewModel.setOffset(current.offsetX + x, current.offsetY + y)
            },
            onDragEnd = { viewModel.pushHistory() },
            modifier = Modifier.fillMaxWidth().aspectRatio(210f / 297f)
        )
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = picker, modifier = Modifier.weight(1f), contentPadding = PaddingValues(vertical = 12.dp)) {
                Text(stringResource(R.string.add_images))
            }
            Button(onClick = onExport, modifier = Modifier.weight(1f), enabled = !exporting, contentPadding = PaddingValues(vertical = 12.dp)) {
                Text(stringResource(R.string.export_pdf))
            }
        }
    }
}

@Composable
private fun SingleImageEditor(
    slots: List<Slot?>,
    selectedIndex: Int?,
    isLandscape: Boolean,
    viewModel: A4GridViewModel,
    onDone: () -> Unit
) {
    val index = selectedIndex ?: return
    val controls = @Composable {
        Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            EditorPanel(
                selectedIndex = selectedIndex,
                slots = slots,
                onZoom = viewModel::setZoom,
                onZoomFinished = viewModel::pushHistory,
                onStretchX = viewModel::setStretchX,
                onStretchXFinished = viewModel::pushHistory,
                onStretchY = viewModel::setStretchY,
                onStretchYFinished = viewModel::pushHistory,
                onFit = { viewModel.pushHistory(); viewModel.setFit(it) },
                onRotateLeft = { viewModel.pushHistory(); viewModel.rotateLeft() },
                onRotateRight = { viewModel.pushHistory(); viewModel.rotateRight() },
                onReset = { viewModel.pushHistory(); viewModel.reset() },
                onSwap = { other -> viewModel.swap(index, other) },
                onDelete = { viewModel.delete(index) }
            )
            Button(onClick = onDone, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                Text(stringResource(R.string.back_to_page))
            }
        }
    }
    if (isLandscape) {
        Row(modifier = Modifier.fillMaxSize().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.weight(1f).fillMaxHeight(), contentAlignment = Alignment.Center) {
                CellBox(
                    index = index + 1,
                    slot = slots[index],
                    selected = true,
                    onSelect = {},
                    onDrag = { x, y ->
                        val current = slots[index]?.transform
                        if (current != null) viewModel.setOffset(current.offsetX + x, current.offsetY + y)
                    },
                    onDragEnd = viewModel::pushHistory,
                    modifier = Modifier.fillMaxSize()
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState())
            ) { controls() }
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(12.dp)) {
            Box(modifier = Modifier.fillMaxWidth().heightIn(min = 240.dp, max = 440.dp), contentAlignment = Alignment.Center) {
                CellBox(
                    index = index + 1,
                    slot = slots[index],
                    selected = true,
                    onSelect = {},
                    onDrag = { x, y ->
                        val current = slots[index]?.transform
                        if (current != null) viewModel.setOffset(current.offsetX + x, current.offsetY + y)
                    },
                    onDragEnd = viewModel::pushHistory,
                    modifier = Modifier.fillMaxSize()
                )
            }
            controls()
        }
    }
}

private suspend fun copyImages(context: Context, uris: List<Uri>, slots: List<Slot?>, viewModel: A4GridViewModel) {
    val directory = File(context.filesDir, "images").apply { mkdirs() }
    val available = slots.mapIndexedNotNull { index, slot -> if (slot == null) index else null }
    withContext(Dispatchers.IO) {
        uris.take(available.size).forEachIndexed { sourceIndex, uri ->
            val target = File(directory, "img_${System.currentTimeMillis()}_$sourceIndex.jpg")
            context.contentResolver.openInputStream(uri).use { input ->
                requireNotNull(input) { "Unable to open image" }
                target.outputStream().use { output -> input.copyTo(output) }
            }
            viewModel.addSlot(available[sourceIndex], target.absolutePath)
        }
    }
}

private fun sharePdf(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        type = "application/pdf"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }, context.getString(R.string.share_pdf)))
}
