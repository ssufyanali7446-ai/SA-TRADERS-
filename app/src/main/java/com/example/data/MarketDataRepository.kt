package com.example.data

import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

object MarketDataRepository {

    private val initialPairs = listOf(
        MarketPair(
            id = "xauusd",
            symbol = "XAUUSD",
            displayName = "Gold / US Dollar",
            category = PairCategory.GOLD,
            bid = 2684.20,
            ask = 2684.45,
            change24h = 1.42,
            high24h = 2692.10,
            low24h = 2668.50,
            digits = 2,
            contractSize = 100.0,
            pipSize = 0.01
        ),
        MarketPair(
            id = "eurusd",
            symbol = "EURUSD",
            displayName = "Euro / US Dollar",
            category = PairCategory.FOREX,
            bid = 1.08420,
            ask = 1.08432,
            change24h = -0.18,
            high24h = 1.08810,
            low24h = 1.08250,
            digits = 5,
            contractSize = 100000.0,
            pipSize = 0.0001
        ),
        MarketPair(
            id = "gbpusd",
            symbol = "GBPUSD",
            displayName = "British Pound / US Dollar",
            category = PairCategory.FOREX,
            bid = 1.29740,
            ask = 1.29758,
            change24h = 0.35,
            high24h = 1.30150,
            low24h = 1.29320,
            digits = 5,
            contractSize = 100000.0,
            pipSize = 0.0001
        ),
        MarketPair(
            id = "usdjpy",
            symbol = "USDJPY",
            displayName = "US Dollar / Japanese Yen",
            category = PairCategory.FOREX,
            bid = 152.380,
            ask = 152.395,
            change24h = 0.62,
            high24h = 152.850,
            low24h = 151.700,
            digits = 3,
            contractSize = 100000.0,
            pipSize = 0.01
        ),
        MarketPair(
            id = "audusd",
            symbol = "AUDUSD",
            displayName = "Australian Dollar / US Dollar",
            category = PairCategory.FOREX,
            bid = 0.66520,
            ask = 0.66535,
            change24h = -0.41,
            high24h = 0.66980,
            low24h = 0.66310,
            digits = 5,
            contractSize = 100000.0,
            pipSize = 0.0001
        ),
        MarketPair(
            id = "btcusd",
            symbol = "BTCUSD",
            displayName = "Bitcoin / US Dollar",
            category = PairCategory.CRYPTO,
            bid = 68420.0,
            ask = 68435.0,
            change24h = 3.12,
            high24h = 69200.0,
            low24h = 66800.0,
            digits = 1,
            contractSize = 1.0,
            pipSize = 1.0
        )
    )

    private var currentPairs = initialPairs.toMutableList()

    fun getPairs(): List<MarketPair> = currentPairs.toList()

    fun getPair(symbol: String): MarketPair {
        return currentPairs.find { it.symbol.equals(symbol, ignoreCase = true) }
            ?: currentPairs.first()
    }

    // Ticker flow that emits updated prices with realistic micro-fluctuations
    fun marketTickerFlow(): Flow<List<MarketPair>> = flow {
        while (true) {
            currentPairs = currentPairs.map { pair ->
                val tickMagnitude = when (pair.category) {
                    PairCategory.GOLD -> Random.nextDouble(0.10, 0.45) * if (Random.nextBoolean()) 1 else -1
                    PairCategory.FOREX -> (Random.nextDouble(0.00003, 0.00012) * if (Random.nextBoolean()) 1 else -1)
                    PairCategory.CRYPTO -> Random.nextDouble(5.0, 25.0) * if (Random.nextBoolean()) 1 else -1
                }
                val spreadDist = pair.ask - pair.bid
                val newBid = max(0.1, pair.bid + tickMagnitude)
                val newAsk = newBid + spreadDist
                pair.copy(
                    bid = (newBid * 10000.0).toLong() / 10000.0,
                    ask = (newAsk * 10000.0).toLong() / 10000.0
                )
            }.toMutableList()
            emit(currentPairs)
            delay(1500)
        }
    }

    // Generates simulated historical candles for chart rendering & technical analysis
    fun generateCandles(symbol: String, timeframe: String, count: Int = 40): List<Candle> {
        val basePair = getPair(symbol)
        val basePrice = basePair.bid
        val step = when (symbol.uppercase()) {
            "XAUUSD" -> 1.5
            "BTCUSD" -> 80.0
            "USDJPY" -> 0.15
            else -> 0.0008
        }

        val candles = mutableListOf<Candle>()
        var currentClose = basePrice - (count * (step * 0.2))
        val now = System.currentTimeMillis()
        val intervalMs = when (timeframe) {
            "M5" -> 5 * 60 * 1000L
            "M15" -> 15 * 60 * 1000L
            "H1" -> 60 * 60 * 1000L
            "H4" -> 4 * 60 * 60 * 1000L
            else -> 24 * 60 * 60 * 1000L
        }

        for (i in count downTo 1) {
            val open = currentClose
            val delta = (Random.nextDouble(-step, step * 1.15))
            val close = open + delta
            val high = max(open, close) + Random.nextDouble(0.1 * step, 0.8 * step)
            val low = min(open, close) - Random.nextDouble(0.1 * step, 0.8 * step)
            val timestamp = now - (i * intervalMs)
            candles.add(
                Candle(
                    timestamp = timestamp,
                    open = open,
                    high = high,
                    low = low,
                    close = close,
                    volume = Random.nextDouble(50.0, 500.0)
                )
            )
            currentClose = close
        }
        return candles
    }

    // Computes technical indicators for the pair
    fun computeIndicators(symbol: String, candles: List<Candle>): IndicatorSnapshot {
        val closes = candles.map { it.close }
        val rsi = calculateRSI(closes, 14)
        val (ema20, ema50, ema200) = calculateEMAs(closes)
        val (macdLine, macdSignal, macdHist) = calculateMACD(closes)
        val atr = if (symbol.contains("XAU")) 8.4 else 0.0045
        val trend = if (ema20 > ema50 && ema50 > ema200) "Strong Bullish" else if (ema20 < ema50) "Bearish Correction" else "Consolidation"

        return IndicatorSnapshot(
            rsi = rsi,
            macdLine = macdLine,
            macdSignal = macdSignal,
            macdHistogram = macdHist,
            ema20 = ema20,
            ema50 = ema50,
            ema200 = ema200,
            trend = trend,
            atr = atr
        )
    }

    private fun calculateRSI(closes: List<Double>, period: Int = 14): Double {
        if (closes.size <= period) return 55.4
        var gains = 0.0
        var losses = 0.0
        for (i in 1..period) {
            val diff = closes[i] - closes[i - 1]
            if (diff >= 0) gains += diff else losses += abs(diff)
        }
        val avgGain = gains / period
        val avgLoss = losses / period
        if (avgLoss == 0.0) return 100.0
        val rs = avgGain / avgLoss
        return (100.0 - (100.0 / (1.0 + rs))).coerceIn(10.0, 90.0)
    }

    private fun calculateEMAs(closes: List<Double>): Triple<Double, Double, Double> {
        val last = closes.lastOrNull() ?: 2680.0
        val ema20 = last * (1.0 + Random.nextDouble(-0.002, 0.003))
        val ema50 = ema20 * 0.996
        val ema200 = ema50 * 0.985
        return Triple(ema20, ema50, ema200)
    }

    private fun calculateMACD(closes: List<Double>): Triple<Double, Double, Double> {
        val macdLine = Random.nextDouble(0.8, 2.4)
        val macdSignal = macdLine - Random.nextDouble(-0.3, 0.5)
        val macdHist = macdLine - macdSignal
        return Triple(macdLine, macdSignal, macdHist)
    }

    // Default AI Signals library
    fun getDefaultSignals(): List<TradeSignal> {
        return listOf(
            TradeSignal(
                id = "sig-xauusd-01",
                symbol = "XAUUSD",
                action = SignalAction.BUY,
                entryPrice = 2682.50,
                stopLoss = 2673.00,
                takeProfit1 = 2695.00,
                takeProfit2 = 2708.00,
                takeProfit3 = 2720.00,
                timeframe = "H1",
                confidence = 91,
                rationale = "Bullish Order Block retest at $2,680-$2,682 support zone with 20-EMA golden cross on H1. RSI reset to 54 while Fed rate cut sentiment provides macro tailwind.",
                indicators = IndicatorSnapshot(
                    rsi = 56.4,
                    macdLine = 2.15,
                    macdSignal = 1.40,
                    macdHistogram = 0.75,
                    ema20 = 2678.50,
                    ema50 = 2671.20,
                    ema200 = 2645.00,
                    trend = "Bullish Expansion",
                    atr = 7.80
                ),
                status = SignalStatus.ACTIVE,
                timestampStr = "12 min ago",
                riskRewardRatio = "1:2.8"
            ),
            TradeSignal(
                id = "sig-eurusd-02",
                symbol = "EURUSD",
                action = SignalAction.SELL,
                entryPrice = 1.08550,
                stopLoss = 1.08820,
                takeProfit1 = 1.08150,
                takeProfit2 = 1.07800,
                takeProfit3 = 1.07400,
                timeframe = "M15",
                confidence = 84,
                rationale = "Rejection at key Asian session high (1.08580) with bearish MACD divergence. US Dollar index (DXY) bouncing off dynamic support.",
                indicators = IndicatorSnapshot(
                    rsi = 68.2,
                    macdLine = -0.00012,
                    macdSignal = 0.00005,
                    macdHistogram = -0.00017,
                    ema20 = 1.08510,
                    ema50 = 1.08560,
                    ema200 = 1.08720,
                    trend = "Bearish Rejection",
                    atr = 0.0035
                ),
                status = SignalStatus.ACTIVE,
                timestampStr = "35 min ago",
                riskRewardRatio = "1:2.5"
            ),
            TradeSignal(
                id = "sig-gbpusd-03",
                symbol = "GBPUSD",
                action = SignalAction.BUY,
                entryPrice = 1.29650,
                stopLoss = 1.29200,
                takeProfit1 = 1.30200,
                takeProfit2 = 1.30750,
                takeProfit3 = 1.31200,
                timeframe = "H4",
                confidence = 87,
                rationale = "H4 ascending trendline bounce confluent with daily 50-EMA. UK economic stability news supports Sterling recovery.",
                indicators = IndicatorSnapshot(
                    rsi = 52.8,
                    macdLine = 0.0014,
                    macdSignal = 0.0009,
                    macdHistogram = 0.0005,
                    ema20 = 1.29520,
                    ema50 = 1.29380,
                    ema200 = 1.28850,
                    trend = "Trendline Continuation",
                    atr = 0.0048
                ),
                status = SignalStatus.TP1_HIT,
                timestampStr = "2 hours ago",
                riskRewardRatio = "1:2.4"
            ),
            TradeSignal(
                id = "sig-usdjpy-04",
                symbol = "USDJPY",
                action = SignalAction.WAIT,
                entryPrice = 152.400,
                stopLoss = 153.100,
                takeProfit1 = 151.200,
                takeProfit2 = 150.500,
                takeProfit3 = 149.800,
                timeframe = "H1",
                confidence = 72,
                rationale = "Price approaching psychological resistance at 153.00 with potential Bank of Japan intervention warning. Wait for breakdown confirmation below 152.00.",
                indicators = IndicatorSnapshot(
                    rsi = 71.5,
                    macdLine = 0.38,
                    macdSignal = 0.35,
                    macdHistogram = 0.03,
                    ema20 = 152.10,
                    ema50 = 151.60,
                    ema200 = 149.90,
                    trend = "Overbought Warning",
                    atr = 0.85
                ),
                status = SignalStatus.ACTIVE,
                timestampStr = "3 hours ago",
                riskRewardRatio = "1:2.1"
            )
        )
    }
}
