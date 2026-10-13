package com.example

import com.example.data.FinancialNewsRssRepository
import com.example.data.MarketDataRepository
import com.example.model.NewsSentiment
import com.example.service.BacktestingEngine
import com.example.service.RiskManagementCalculator
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testRiskManagementCalculator_GoldPosition() {
        val result = RiskManagementCalculator.calculatePositionSize(
            balance = 10000.0,
            riskPercent = 1.0, // $100 risk
            stopLossPips = 20.0,
            symbol = "XAUUSD"
        )
        // 1.0% of $10,000 is $100. At $10/pip per lot and 20 pips SL = $200/lot. Lot = 0.50
        assertEquals(0.50, result.calculatedLotSize, 0.05)
        assertTrue(result.riskAmountDollars > 0.0)
        assertTrue(result.riskRewardRatio >= 1.5)
        assertNull(result.warningMessage)
    }

    @Test
    fun testRiskManagementCalculator_ExcessiveRiskWarning() {
        val result = RiskManagementCalculator.calculatePositionSize(
            balance = 5000.0,
            riskPercent = 4.0, // violates prop firm rule
            stopLossPips = 10.0,
            symbol = "XAUUSD"
        )
        assertNotNull(result.warningMessage)
        assertTrue(result.warningMessage!!.contains("prop firm", ignoreCase = true))
    }

    @Test
    fun testMarketDataRepository_PairsAndCandles() {
        val pairs = MarketDataRepository.getPairs()
        assertTrue(pairs.isNotEmpty())
        val gold = MarketDataRepository.getPair("XAUUSD")
        assertEquals("XAUUSD", gold.symbol)
        assertTrue(gold.spread > 0)

        val candles = MarketDataRepository.generateCandles("XAUUSD", "H1", 20)
        assertEquals(20, candles.size)
        val indicators = MarketDataRepository.computeIndicators("XAUUSD", candles)
        assertTrue(indicators.rsi in 0.0..100.0)
    }

    @Test
    fun testFinancialNewsRssRepository_Sentiment() {
        val repo = FinancialNewsRssRepository()
        val summary = repo.getSentimentSummary("XAUUSD")
        assertTrue(summary.bullishPercentage >= 0)
        assertTrue(summary.bearishPercentage >= 0)
        assertTrue(summary.neutralPercentage >= 0)
        assertTrue(summary.overallMood.isNotBlank())
    }

    @Test
    fun testBacktestingEngine_Simulation() = runBlocking {
        val result = BacktestingEngine.runBacktest(
            strategyId = "gold_ema_trend",
            symbol = "XAUUSD",
            timeframe = "H1",
            periodDays = 60
        )
        assertTrue(result.totalTrades > 0)
        assertTrue(result.winRate in 20.0..95.0)
        assertTrue(result.profitFactor > 0.0)
        assertTrue(result.equityCurve.isNotEmpty())
        assertEquals(result.totalTrades, result.trades.size)
    }
}
