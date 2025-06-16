package com.gameboost.pro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameboost.pro.R
import com.gameboost.pro.localization.SupportedLanguage
import com.gameboost.pro.ui.viewmodels.LanguageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSwitcher(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = when (currentLanguage) {
                "ar" -> stringResource(R.string.arabic)
                else -> stringResource(R.string.english)
            },
            onValueChange = {},
            readOnly = true,
            label = { Text(stringResource(R.string.language)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = "Language"
                )
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            SupportedLanguage.values().forEach { language ->
                DropdownMenuItem(
                    text = { Text(language.displayName) },
                    onClick = {
                        languageViewModel.setLanguage(language.code)
                        expanded = false
                    },
                    leadingIcon = {
                        RadioButton(
                            selected = currentLanguage == language.code,
                            onClick = null
                        )
                    }
                )
            }
        }
    }
}

@Composable
fun LanguageToggleButton(
    modifier: Modifier = Modifier,
    languageViewModel: LanguageViewModel = hiltViewModel()
) {
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    
    FloatingActionButton(
        onClick = {
            val newLanguage = if (currentLanguage == "ar") "en" else "ar"
            languageViewModel.setLanguage(newLanguage)
        },
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondary
    ) {
        Icon(
            imageVector = Icons.Default.Language,
            contentDescription = stringResource(R.string.language),
            tint = MaterialTheme.colorScheme.onSecondary
        )
    }
}
