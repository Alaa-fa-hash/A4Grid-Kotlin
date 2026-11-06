package com.a4grid.app.ui

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.a4grid.app.data.ProjectStore
import com.a4grid.app.model.FitMode
import com.a4grid.app.model.Slot
import com.a4grid.app.model.Transform
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class A4GridViewModel(context: Context) {
    private val store = ProjectStore(context.applicationContext)
    private val _slots = MutableStateFlow(store.load())
    val slots: StateFlow<List<Slot?>> = _slots.asStateFlow()
    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex: StateFlow<Int?> = _selectedIndex.asStateFlow()
    private val undoStack = ArrayDeque<List<Slot?>>()
    private val redoStack = ArrayDeque<List<Slot?>>()
    private val _canUndo = MutableStateFlow(false)
    val canUndo: StateFlow<Boolean> = _canUndo.asStateFlow()
    private val _canRedo = MutableStateFlow(false)
    val canRedo: StateFlow<Boolean> = _canRedo.asStateFlow()
    var saveVersion by mutableStateOf(0)
        private set

    fun select(index: Int?) { _selectedIndex.value = index }

    fun pushHistory() {
        undoStack.addLast(_slots.value.map { it?.copy(transform = it.transform.copy()) })
        while (undoStack.size > 10) undoStack.removeFirst()
        redoStack.clear()
        refreshHistory()
    }

    private fun update(block: (List<Slot?>) -> List<Slot?>) {
        _slots.value = block(_slots.value).take(8).let { it + List(8 - it.size) { null } }
        saveVersion++
    }

    fun setZoom(value: Float) = updateSelected { it.copy(zoom = value.coerceIn(1f, 3f)) }
    fun rotateRight() = updateSelected { it.copy(rotation = it.rotation + 90).normalized() }
    fun rotateLeft() = updateSelected { it.copy(rotation = it.rotation - 90).normalized() }
    fun setFineRotation(value: Float) = updateSelected { it.copy(fineRotation = value.coerceIn(-45f, 45f)) }
    fun setFit(fit: FitMode) = updateSelected { it.copy(fit = fit) }
    fun setOffset(x: Float, y: Float) = updateSelected { it.copy(offsetX = x, offsetY = y) }
    fun reset() = updateSelected { Transform() }

    private fun updateSelected(transform: (Transform) -> Transform) {
        val index = _selectedIndex.value ?: return
        update { current -> current.mapIndexed { i, slot -> if (i == index && slot != null) slot.copy(transform = transform(slot.transform).normalized()) else slot } }
    }

    fun addSlot(index: Int, uri: String) {
        pushHistory()
        update { current -> current.toMutableList().also { it[index] = Slot(uri) } }
        select(index)
    }

    fun swap(first: Int, second: Int) {
        if (first == second) return
        pushHistory()
        update { current -> current.toMutableList().also { val item = it[first]; it[first] = it[second]; it[second] = item } }
    }

    fun delete(index: Int) {
        if (_slots.value.getOrNull(index) == null) return
        pushHistory()
        update { current -> current.toMutableList().also { it[index] = null } }
        if (_selectedIndex.value == index) select(null)
    }

    fun clearAll() {
        if (_slots.value.any { it != null }) pushHistory()
        update { List(8) { null } }
        select(null)
    }

    fun undo() {
        val previous = undoStack.removeLastOrNull() ?: return
        redoStack.addLast(_slots.value)
        _slots.value = previous
        saveVersion++
        refreshHistory()
    }

    fun redo() {
        val next = redoStack.removeLastOrNull() ?: return
        undoStack.addLast(_slots.value)
        _slots.value = next
        saveVersion++
        refreshHistory()
    }

    fun save() { store.save(_slots.value) }

    private fun refreshHistory() {
        _canUndo.value = undoStack.isNotEmpty()
        _canRedo.value = redoStack.isNotEmpty()
    }
}
