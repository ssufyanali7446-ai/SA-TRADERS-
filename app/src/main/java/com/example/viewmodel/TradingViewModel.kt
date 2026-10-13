package com.example.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FinancialNewsRssRepository
import com.example.data.MarketDataRepository
import com.example.model.*
import com.example.service.AiAnalysisService
import com.example.service.BacktestingEngine
import com.example.service.Mt5BridgeService
import com.example.service.RiskManagementCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class TradingViewModel(
    private val newsRepository: FinancialNewsRssRepository = FinancialNewsRssRepository()
) : ViewModel() {

    // Market Pairs & Tickers
    private val _pairs = MutableStateFlow(MarketDataRepository.getPairs())
    val pairs: StateFlow<List<MarketPair>> = _pairs.asStateFlow()

    private val _selectedPair = MutableStateFlow(MarketDataRepository.getPair("XAUUSD"))
    val selectedPair: StateFlow<MarketPair> = _selectedPair.asStateFlow()

    private val _selectedTimeframe = MutableStateFlow("H1")
    val selectedTimeframe: StateFlow<String> = _selectedTimeframe.asStateFlow()

    private val _candles = MutableStateFlow(
        MarketDataRepository.generateCandles("XAUUSD", "H1", 35)
    )
    val candles: StateFlow<List<Candle>> = _candles.asStateFlow()

    // Signals
    private val _signals = MutableStateFlow(MarketDataRepository.getDefaultSignals())
    val signals: StateFlow<List<TradeSignal>> = _signals.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    private val _analysisStatus = MutableStateFlow("")
    val analysisStatus: StateFlow<String> = _analysisStatus.asStateFlow()

    // News & Sentiment
    val newsList: StateFlow<List<FinancialNews>> = newsRepository.newsState
    val isNewsLoading: StateFlow<Boolean> = newsRepository.isLoading
    private val _newsFilter = MutableStateFlow("ALL")
    val newsFilter: StateFlow<String> = _newsFilter.asStateFlow()

    private val _sentimentSummary = MutableStateFlow(newsRepository.getSentimentSummary("XAUUSD"))
    val sentimentSummary: StateFlow<MarketSentimentSummary> = _sentimentSummary.asStateFlow()

    // MT5 Bridge
    val mt5Account = Mt5BridgeService.accountInfo
    val mt5Orders = Mt5BridgeService.openOrders

    // Risk Calculator
    private val _riskCalcResult = MutableStateFlow(
        RiskManagementCalculator.calculatePositionSize(
            balance = 10000.0,
            riskPercent = 1.5,
            stopLossPips = 25.0,
            symbol = "XAUUSD"
        )
    )
    val riskCalcResult: StateFlow<RiskCalculation> = _riskCalcResult.asStateFlow()

    // Backtest
    private val _backtestResult = MutableStateFlow<BacktestResult?>(null)
    val backtestResult: StateFlow<BacktestResult?> = _backtestResult.asStateFlow()

    private val _isBacktesting = MutableStateFlow(false)
    val isBacktesting: StateFlow<Boolean> = _isBacktesting.asStateFlow()

    init {
        // Collect live ticks
        viewModelScope.launch {
            MarketDataRepository.marketTickerFlow().collect { updatedPairs ->
                _pairs.value = updatedPairs
                val current = _selectedPair.value
                val matched = updatedPairs.find { it.symbol == current.symbol }
                if (matched != null) {
                    _selectedPair.value = matched
                }
            }
        }

        // Fetch RSS news feeds on initialization
        viewModelScope.launch {
            newsRepository.refreshRssFeeds()
            updateSentiment()
        }

        // Run default backtest for XAUUSD
        runBacktest("gold_ema_trend", "XAUUSD", "H1", 90)
    }

    fun selectPair(pair: MarketPair) {
        _selectedPair.value = pair
        updateCandles(pair.symbol, _selectedTimeframe.value)
        updateSentiment()
    }

    fun selectTimeframe(tf: String) {
        _selectedTimeframe.value = tf
        updateCandles(_selectedPair.value.symbol, tf)
    }

    private fun updateCandles(symbol: String, timeframe: String) {
        _candles.value = MarketDataRepository.generateCandles(symbol, timeframe, 35)
    }

    fun runAiAnalysis(symbol: String = _selectedPair.value.symbol, timeframe: String = _selectedTimeframe.value) {
        viewModelScope.launch {
            _isAnalyzing.value = true
            _analysisStatus.value = "Starting AI Engine..."
            try {
                val newSignal = AiAnalysisService.analyzePair(
                    symbol = symbol,
                    timeframe = timeframe,
                    onProgress = { status -> _analysisStatus.value = status }
                )
                _signals.value = listOf(newSignal) + _signals.value
            } finally {
                _isAnalyzing.value = false
                _analysisStatus.value = ""
            }
        }
    }

    fun refreshRssNews() {
        viewModelScope.launch {
            newsRepository.refreshRssFeeds()
            updateSentiment()
        }
    }

    fun setNewsFilter(symbol: String) {
        _newsFilter.value = symbol
        updateSentiment()
    }

    private fun updateSentiment() {
        val targetSymbol = if (_newsFilter.value == "ALL") _selectedPair.value.symbol else _newsFilter.value
        _sentimentSummary.value = newsRepository.getSentimentSummary(targetSymbol)
    }

    fun updateRiskCalculation(balance: Double, riskPct: Double, slPips: Double, symbol: String) {
        _riskCalcResult.value = RiskManagementCalculator.calculatePositionSize(
            balance = balance,
            riskPercent = riskPct,
            stopLossPips = slPips,
            symbol = symbol
        )
    }

    fun copySignalToClipboard(context: Context, signal: TradeSignal, lotSize: Double) {
        Mt5BridgeService.copySignalForMt5(context, signal, lotSize)
    }

    fun executeTradeToMt5(signal: TradeSignal, lotSize: Double) {
        Mt5BridgeService.executeSignalOnMt5(signal, lotSize)
    }

    fun closeMt5Position(ticket: Long) {
        Mt5BridgeService.closePosition(ticket)
    }

    fun runBacktest(strategyId: String, symbol: String, timeframe: String, days: Int) {
        viewModelScope.launch {
            _isBacktesting.value = true
            try {
                _backtestResult.value = BacktestingEngine.runBacktest(
                    strategyId = strategyId,
                    symbol = symbol,
                    timeframe = timeframe,
                    periodDays = days
                )
            } finally {
                _isBacktesting.value = false
            }
        }
    }
}
