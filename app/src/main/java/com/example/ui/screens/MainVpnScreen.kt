package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.AddSubscriptionSheet
import com.example.ui.components.ConnectButton
import com.example.ui.components.HeaderBar
import com.example.ui.components.ServerCard
import com.example.ui.components.ServerSelectorSheet
import com.example.ui.components.StatusBadge
import com.example.ui.components.TrafficMetricsCard
import com.example.ui.theme.ParsElectricBlue
import com.example.ui.theme.ParsNavyDark
import com.example.ui.theme.ParsNavySurface
import com.example.ui.viewmodel.VpnViewModel
import com.example.vpn.VpnStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainVpnScreen(
    viewModel: VpnViewModel,
    onPrepareVpn: (android.content.Intent) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectorSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Handle toast messages
    LaunchedEffect(uiState.toastMessage) {
        uiState.toastMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            viewModel.clearToast()
        }
    }

    // BackHandler for bottom sheets
    BackHandler(enabled = uiState.isAddSubscriptionOpen || uiState.isServerSelectorOpen) {
        if (uiState.isAddSubscriptionOpen) {
            viewModel.closeAddSubscription()
        } else if (uiState.isServerSelectorOpen) {
            viewModel.closeServerSelector()
        }
    }

    // Force RTL layout for Persian UI
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            modifier = modifier
                .fillMaxSize()
                .background(ParsNavyDark),
            containerColor = ParsNavyDark,
            snackbarHost = { SnackbarHost(snackbarHostState) }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                ParsNavyDark,
                                ParsNavySurface.copy(alpha = 0.85f),
                                ParsNavyDark
                            )
                        )
                    )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Header Bar (Pars Ping, Protection state, + button, Clipboard button)
                    HeaderBar(
                        vpnStatus = uiState.connectionInfo.status,
                        onAddClick = { viewModel.openAddSubscription() },
                        onClipboardClick = {
                            val clipText = clipboardManager.getText()?.text
                            viewModel.handleClipboardUrl(clipText)
                        }
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Section A: AUTO Badge with Blue Shield
                    StatusBadge()

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section B, C, D: Connection Status, Main Power Button, Timer
                    ConnectButton(
                        status = uiState.connectionInfo.status,
                        formattedDuration = uiState.connectionInfo.formattedDuration,
                        onClick = {
                            viewModel.toggleVpn(context) { intent ->
                                onPrepareVpn(intent)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Real-time Traffic metrics (Download, Upload, Protocol)
                    TrafficMetricsCard(
                        connectionInfo = uiState.connectionInfo,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section E: Current Server Card
                    ServerCard(
                        selectedConfig = uiState.selectedConfig,
                        isTestingPing = (uiState.isTestingPingForId == uiState.selectedConfig?.id),
                        onCardClick = { viewModel.openServerSelector() },
                        onTestPingClick = {
                            uiState.selectedConfig?.let { cfg ->
                                viewModel.testPingForConfig(cfg)
                            }
                        },
                        modifier = Modifier
                            .padding(horizontal = 20.dp)
                            .padding(bottom = 24.dp)
                    )
                }

                // Add Subscription Bottom Sheet
                if (uiState.isAddSubscriptionOpen) {
                    AddSubscriptionSheet(
                        urlInput = uiState.subscriptionUrlInput,
                        isLoading = uiState.isAddingSubscription,
                        notice = uiState.subscriptionNotice,
                        configs = uiState.configs,
                        onUrlChange = { viewModel.onUrlInputChange(it) },
                        onSubmit = { viewModel.submitSubscription() },
                        onPasteClipboard = {
                            val text = clipboardManager.getText()?.text
                            if (!text.isNullOrBlank()) {
                                viewModel.onUrlInputChange(text)
                            } else {
                                Toast.makeText(context, "کلیپبورد خالی است", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onSelectConfig = { viewModel.selectConfig(it) },
                        onDeleteConfig = { viewModel.deleteConfig(it) },
                        onDismiss = { viewModel.closeAddSubscription() },
                        sheetState = addSheetState
                    )
                }

                // Server Selector Bottom Sheet
                if (uiState.isServerSelectorOpen) {
                    ServerSelectorSheet(
                        configs = uiState.configs,
                        selectedConfig = uiState.selectedConfig,
                        isTestingPingForId = uiState.isTestingPingForId,
                        onSelectConfig = { viewModel.selectConfig(it) },
                        onTestPing = { viewModel.testPingForConfig(it) },
                        onOpenAddSubscription = {
                            viewModel.closeServerSelector()
                            viewModel.openAddSubscription()
                        },
                        onDismiss = { viewModel.closeServerSelector() },
                        sheetState = selectorSheetState
                    )
                }
            }
        }
    }
}
