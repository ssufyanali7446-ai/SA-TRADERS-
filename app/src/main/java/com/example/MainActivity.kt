package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.TradingTopBar
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.TradingViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val selectedIcon: ImageVector) {
    MARKETS("Markets", Icons.Outlined.CandlestickChart, Icons.Filled.CandlestickChart),
    AI_CHART("AI Chart", Icons.Outlined.Timeline, Icons.Filled.Timeline),
    SIGNALS_NEWS("Signals & News", Icons.Outlined.AutoAwesome, Icons.Filled.AutoAwesome),
    RISK_CALC("Risk Calc", Icons.Outlined.Security, Icons.Filled.Security),
    MT5_BRIDGE("MT5 & Backtest", Icons.Outlined.Hub, Icons.Filled.Hub)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                GoldFxTradingApp()
            }
        }
    }
}

@Composable
fun GoldFxTradingApp(
    viewModel: TradingViewModel = viewModel()
) {
    var currentTab by remember { mutableStateOf(NavigationTab.MARKETS) }
    var mt5SubTab by remember { mutableIntStateOf(0) } // 0: MT5 Bridge, 1: Backtesting

    val pairs by viewModel.pairs.collectAsStateWithLifecycle()
    val selectedPair by viewModel.selectedPair.collectAsStateWithLifecycle()
    val selectedTimeframe by viewModel.selectedTimeframe.collectAsStateWithLifecycle()
    val candles by viewModel.candles.collectAsStateWithLifecycle()
    val signals by viewModel.signals.collectAsStateWithLifecycle()
    val isAnalyzing by viewModel.isAnalyzing.collectAsStateWithLifecycle()
    val analysisStatus by viewModel.analysisStatus.collectAsStateWithLifecycle()

    val newsList by viewModel.newsList.collectAsStateWithLifecycle()
    val isNewsLoading by viewModel.isNewsLoading.collectAsStateWithLifecycle()
    val newsFilter by viewModel.newsFilter.collectAsStateWithLifecycle()
    val sentimentSummary by viewModel.sentimentSummary.collectAsStateWithLifecycle()

    val mt5Account by viewModel.mt5Account.collectAsStateWithLifecycle()
    val mt5Orders by viewModel.mt5Orders.collectAsStateWithLifecycle()

    val riskCalcResult by viewModel.riskCalcResult.collectAsStateWithLifecycle()
    val backtestResult by viewModel.backtestResult.collectAsStateWithLifecycle()
    val isBacktesting by viewModel.isBacktesting.collectAsStateWithLifecycle()

    // Handle back button to return to Markets screen
    BackHandler(enabled = currentTab != NavigationTab.MARKETS) {
        currentTab = NavigationTab.MARKETS
    }

    val goldPair = pairs.find { it.symbol == "XAUUSD" } ?: selectedPair

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TradingTopBar(
                goldPrice = goldPair.bid,
                goldChange = goldPair.change24h,
                isMt5Connected = mt5Account.isConnected,
                onNotificationClick = {
                    currentTab = NavigationTab.SIGNALS_NEWS
                }
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationTab.entries.forEach { tab ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTab = tab },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.icon,
                                contentDescription = tab.label,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.label,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = DarkBackground,
                            selectedTextColor = GoldLight,
                            indicatorColor = GoldPrimary,
                            unselectedIconColor = TextSecondary,
                            unselectedTextColor = TextMuted
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            when (currentTab) {
                NavigationTab.MARKETS -> {
                    MarketWatchScreen(
                        pairs = pairs,
                        selectedPair = selectedPair,
                        onSelectPair = { viewModel.selectPair(it) },
                        onNavigateToChart = { currentTab = NavigationTab.AI_CHART }
                    )
                }

                NavigationTab.AI_CHART -> {
                    ChartAndAiScreen(
                        pair = selectedPair,
                        candles = candles,
                        selectedTimeframe = selectedTimeframe,
                        onTimeframeChange = { viewModel.selectTimeframe(it) },
                        signals = signals,
                        isAnalyzing = isAnalyzing,
                        analysisStatus = analysisStatus,
                        onRunAnalysis = { viewModel.runAiAnalysis() },
                        onExecuteSignal = { signal ->
                            viewModel.executeTradeToMt5(signal, riskCalcResult.calculatedLotSize)
                            currentTab = NavigationTab.MT5_BRIDGE
                            mt5SubTab = 0
                        }
                    )
                }

                NavigationTab.SIGNALS_NEWS -> {
                    SignalsAndNewsScreen(
                        signals = signals,
                        newsList = newsList,
                        sentimentSummary = sentimentSummary,
                        selectedFilter = newsFilter,
                        onFilterChange = { viewModel.setNewsFilter(it) },
                        isNewsLoading = isNewsLoading,
                        onRefreshNews = { viewModel.refreshRssNews() },
                        onExecuteSignal = { signal ->
                            viewModel.executeTradeToMt5(signal, riskCalcResult.calculatedLotSize)
                            currentTab = NavigationTab.MT5_BRIDGE
                            mt5SubTab = 0
                        }
                    )
                }

                NavigationTab.RISK_CALC -> {
                    RiskManagementScreen(
                        riskResult = riskCalcResult,
                        onCalculate = { b, r, sl, s ->
                            viewModel.updateRiskCalculation(b, r, sl, s)
                        }
                    )
                }

                NavigationTab.MT5_BRIDGE -> {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Sub-Tab Switcher between MT5 Live Bridge & Historical Backtester
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .background(DarkSurfaceElevated, RoundedCornerShape(10.dp))
                                .padding(4.dp)
                        ) {
                            Surface(
                                onClick = { mt5SubTab = 0 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (mt5SubTab == 0) GoldPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "MT5 Live Terminal",
                                        color = if (mt5SubTab == 0) DarkBackground else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Surface(
                                onClick = { mt5SubTab = 1 },
                                shape = RoundedCornerShape(8.dp),
                                color = if (mt5SubTab == 1) GoldPrimary else Color.Transparent,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "Strategy Backtester",
                                        color = if (mt5SubTab == 1) DarkBackground else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        if (mt5SubTab == 0) {
                            Mt5BridgeScreen(
                                accountInfo = mt5Account,
                                openOrders = mt5Orders,
                                onCloseOrder = { ticket -> viewModel.closeMt5Position(ticket) }
                            )
                        } else {
                            BacktestScreen(
                                result = backtestResult,
                                isBacktesting = isBacktesting,
                                onRunBacktest = { stratId, sym, tf, days ->
                                    viewModel.runBacktest(stratId, sym, tf, days)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
