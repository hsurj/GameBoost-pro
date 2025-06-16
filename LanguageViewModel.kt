package com.gameboost.pro.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gameboost.pro.localization.LanguageManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LanguageViewModel @Inject constructor(
    private val languageManager: LanguageManager
) : ViewModel() {
    
    val currentLanguage: StateFlow<String> = languageManager.selectedLanguage
        .stateIn(
            scope = viewModelScope,
            started = kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000),
            initialValue = "en"
        )
    
    fun setLanguage(languageCode: String) {
        viewModelScope.launch {
            languageManager.setLanguage(languageCode)
        }
    }
}

