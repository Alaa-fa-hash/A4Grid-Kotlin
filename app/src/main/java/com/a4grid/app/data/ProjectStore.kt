package com.a4grid.app.data

import android.content.Context
import com.a4grid.app.model.Slot
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ProjectStore(context: Context) {
    private val preferences = context.getSharedPreferences("a4grid_project", Context.MODE_PRIVATE)
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): List<Slot?> {
        val value = preferences.getString(KEY_SLOTS, null) ?: return List(8) { null }
        return runCatching {
            json.decodeFromString<List<Slot?>>(value).let { list ->
                List(8) { index -> list.getOrNull(index) }
            }
        }.getOrElse { List(8) { null } }
    }

    fun save(slots: List<Slot?>) {
        preferences.edit().putString(KEY_SLOTS, json.encodeToString(slots)).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_SLOTS).apply()
    }

    companion object { private const val KEY_SLOTS = "slots" }
}
