package com.gameboost.pro.localization

import android.content.Context
import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "language_settings")

@Singleton
class LanguageManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val languageKey = stringPreferencesKey("selected_language")
    
    val selectedLanguage: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[languageKey] ?: "en"
        }
    
    suspend fun setLanguage(languageCode: String) {
        context.dataStore.edit { preferences ->
            preferences[languageKey] = languageCode
        }
    }
    
    fun updateLocale(context: Context, languageCode: String): Context {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        
        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        
        return context.createConfigurationContext(configuration)
    }
}

enum class SupportedLanguage(val code: String, val displayName: String) {
    ENGLISH("en", "English"),
    ARABIC("ar", "العربية")
}

val LocalLanguageManager = staticCompositionLocalOf<LanguageManager> {
    error("LanguageManager not provided")
}

@Composable
fun ProvideLanguageManager(
    languageManager: LanguageManager,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalLanguageManager provides languageManager,
        content = content
    )
}

