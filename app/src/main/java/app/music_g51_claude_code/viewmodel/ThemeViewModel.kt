package app.music_g51_claude_code.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.music_g51_claude_code.ui.theme.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeViewModel(application: Application) : AndroidViewModel(application) {
    private val _themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    companion object {
        private val THEME_KEY = stringPreferencesKey("theme_mode")
    }

    init {
        viewModelScope.launch {
            getApplication<Application>().dataStore.data.map { prefs ->
                val name = prefs[THEME_KEY] ?: ThemeMode.SYSTEM.name
                try { ThemeMode.valueOf(name) } catch (_: Exception) { ThemeMode.SYSTEM }
            }.collect { mode ->
                _themeMode.value = mode
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        _themeMode.value = mode
        viewModelScope.launch {
            getApplication<Application>().dataStore.edit { prefs ->
                prefs[THEME_KEY] = mode.name
            }
        }
    }
}
