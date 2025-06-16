package com.gameboost.pro.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gameboost.pro.R
import com.gameboost.pro.auth.AuthViewModel
import com.gameboost.pro.ui.components.LanguageSwitcher
import com.gameboost.pro.ui.components.LanguageToggleButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Language toggle button
                    LanguageToggleButton(
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    
                    // Sign out button
                    IconButton(
                        onClick = { authViewModel.signOut() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = stringResource(R.string.sign_out)
                        )
                    }
                }
            )
        },
        bottomBar = {
            BottomNavigationBar(
                selectedTab = uiState.selectedTab,
                onTabSelected = viewModel::selectTab
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // Connection Status Card
                ConnectionStatusCard(
                    isConnected = uiState.isConnected,
                    serverName = uiState.selectedServer?.name ?: stringResource(R.string.select_server),
                    onConnectClick = {
                        if (uiState.isConnected) {
                            viewModel.disconnect()
                        } else {
                            viewModel.connect()
                        }
                    }
                )
            }
            
            item {
                // Server Info Card
                uiState.selectedServer?.let { server ->
                    ServerInfoCard(
                        server = server,
                        onServerClick = { viewModel.selectTab(BottomNavTab.SERVERS) }
                    )
                }
            }
            
            item {
                // Performance Chart
                PerformanceChart(
                    performanceData = uiState.performanceData
                )
            }
            
            item {
                // Game Detection Widget
                GameDetectionWidget(
                    detectedGame = uiState.detectedGame,
                    isGameModeActive = uiState.isGameModeActive
                )
            }
            
            item {
                // Language Switcher
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.language),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LanguageSwitcher()
                    }
                }
            }
        }
    }
}

