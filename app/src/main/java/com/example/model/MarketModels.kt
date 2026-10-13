package com.example.model

enum class PairCategory {
    GOLD, FOREX, CRYPTO
}

data class MarketPair(
    val id: String,
    val symbol: String,
    val displayName: String,
    val category: PairCategory,
    val bid: Double,
    val ask: Double,
    val change24h: Double,
    val high24h: Double,
    val low24h: Double,
    val digits: Int = 2,
    val contractSize: Double = 100.0, // 100 oz for Gold
    val pipSize: Double = 0.01
) {
    val spread: Double
        get() = ((ask - bid) / pipSize)
}

data class Candle(
    val timestamp: Long,
    val open: Double,
    val high: Double,
    val low: Double,
    val close: Double,
    val volume: Double = 100.0
)

enum class SignalAction {
    BUY, SELL, WAIT
}

enum class SignalStatus {
    ACTIVE, TP1_HIT, TP2_HIT, STOPPED_OUT, EXPIRED
}

data class IndicatorSnapshot(
    val rsi: Double,
    val macdLine: Double,
    val macdSignal: Double,
    val macdHistogram: Double,
    val ema20: Double,
    val ema50: Double,
    val ema200: Double,
    val trend: String,
    val atr: Double
)

data class TradeSignal(
    val id: String,
    val symbol: String,
    val action: SignalAction,
    val entryPrice: Double,
    val stopLoss: Double,
    val takeProfit1: Double,
    val takeProfit2: Double,
    val takeProfit3: Double,
    val timeframe: String,
    val confidence: Int, // e.g. 88%
    val rationale: String,
    val indicators: IndicatorSnapshot,
    val status: SignalStatus,
    val timestampStr: String,
    val riskRewardRatio: String = "1:2.4"
)

enum class NewsSentiment {
    BULLISH, BEARISH, NEUTRAL
}

enum class NewsImpact {
    HIGH, MEDIUM, LOW
}

data class FinancialNews(
    val id: String,
    val title: String,
    val summary: String,
    val source: String,
    val timeAgo: String,
    val relatedSymbol: String,
    val sentiment: NewsSentiment,
    val sentimentScore: Double, // -1.0 to 1.0
    val impact: NewsImpact,
    val aiTakeaway: String
)

data class MarketSentimentSummary(
    val symbol: String,
    val overallMood: String,
    val bullishPercentage: Int,
    val bearishPercentage: Int,
    val neutralPercentage: Int,
    val primaryDriver: String,
    val keyUpcomingEvent: String
)

data class Mt5AccountInfo(
    val isConnected: Boolean = true,
    val brokerName: String = "MetaQuotes-Demo / Raw ECN",
    val loginNumber: String = "8942103",
    val server: String = "MetaQuotes-Access",
    val balance: Double = 10000.0,
    val equity: Double = 10342.50,
    val margin: Double = 274.20,
    val freeMargin: Double = 10068.30,
    val marginLevel: Double = 3771.88,
    val openPositionsCount: Int = 2
)

data class Mt5Order(
    val ticket: Long,
    val symbol: String,
    val type: SignalAction,
    val lots: Double,
    val openPrice: Double,
    val currentPrice: Double,
    val sl: Double,
    val tp: Double,
    val pnl: Double,
    val openTime: String
)

data class RiskCalculation(
    val balance: Double,
    val riskPercent: Double,
    val stopLossPips: Double,
    val symbol: String,
    val calculatedLotSize: Double,
    val riskAmountDollars: Double,
    val rewardAmountDollars: Double,
    val riskRewardRatio: Double,
    val warningMessage: String? = null
)

data class BacktestStrategy(
    val id: String,
    val name: String,
    val description: String,
    val idealFor: String
)

data class BacktestTrade(
    val id: String,
    val dateStr: String,
    val type: SignalAction,
    val entryPrice: Double,
    val exitPrice: Double,
    val lotSize: Double,
    val pnl: Double,
    val pnlPercent: Double,
    val isWin: Boolean,
    val exitReason: String
)

data class BacktestResult(
    val strategyName: String,
    val symbol: String,
    val timeframe: String,
    val periodDays: Int,
    val totalTrades: Int,
    val winRate: Double,
    val profitFactor: Double,
    val maxDrawdownPct: Double,
    val netProfitDollars: Double,
    val initialBalance: Double,
    val finalBalance: Double,
    val sharpeRatio: Double,
    val winCount: Int,
    val lossCount: Int,
    val equityCurve: List<Double>,
    val trades: List<BacktestTrade>
)
