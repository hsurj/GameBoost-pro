package com.gameboost.pro.ui.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameboost.pro.auth.AuthViewModel
import com.gameboost.pro.auth.LoginScreen
import com.gameboost.pro.localization.LanguageManager
import com.gameboost.pro.localization.ProvideLanguageManager
import com.gameboost.pro.ui.splash.SplashScreen
import com.gameboost.pro.ui.theme.GameBoostProTheme
import com.gameboost.pro.ui.viewmodels.LanguageViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    @Inject
    lateinit var languageManager: LanguageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val languageViewModel: LanguageViewModel = hiltViewModel()
            val currentLanguage by languageViewModel.currentLanguage.collectAsState()
            val context = LocalContext.current
            
            // Apply language configuration
            val updatedContext = remember(currentLanguage) {
                languageManager.updateLocale(context, currentLanguage)
            }
            
            var showSplash by remember { mutableStateOf(true) }
            
            GameBoostProTheme {
                ProvideLanguageManager(languageManager = languageManager) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (showSplash) {
                            SplashScreen(
                                onSplashFinished = { showSplash = false }
                            )
                        } else {
                            MainAppContent()
                        }
                    }
                }
            }
        }
    }
    
    override fun attachBaseContext(newBase: android.content.Context) {
        // Get stored language code
        val languageCode = runBlocking {
            languageManager.selectedLanguage.first()
        }
        
        // Apply the stored language to the context
        val context = languageManager.updateLocale(newBase, languageCode)
        super.attachBaseContext(context)
    }
}

@Composable
private fun MainAppContent() {
    val authViewModel: AuthViewModel = hiltViewModel()
    val authState by authViewModel.authState.collectAsState()
    
    when {
        authState.isLoading -> {
            // Show loading screen
            SplashScreen(onSplashFinished = {})
        }
        authState.isSignedIn -> {
            // Show main app
            MainScreen(viewModel = hiltViewModel())
        }
        else -> {
            // Show login screen
            LoginScreen(
                onLoginSuccess = {
                    // Navigation will be handled by state change
                },
                onContinueAsGuest = {
                    // Handle guest mode
                    authViewModel.continueAsGuest()
                }
            )
        }
    }
}

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
