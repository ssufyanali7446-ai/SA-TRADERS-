package com.example.service

import com.example.model.RiskCalculation
import kotlin.math.max
import kotlin.math.roundToInt

object RiskManagementCalculator {

    fun calculatePositionSize(
        balance: Double,
        riskPercent: Double,
        stopLossPips: Double,
        symbol: String,
        rewardPips: Double = stopLossPips * 2.5
    ): RiskCalculation {
        val safeBalance = max(10.0, balance)
        val safeRiskPct = riskPercent.coerceIn(0.1, 50.0)
        val safeSlPips = max(1.0, stopLossPips)

        val riskDollars = (safeBalance * (safeRiskPct / 100.0))

        // Pip value calculation per standard lot:
        // For Forex (EURUSD, GBPUSD): 1 pip (0.0001) = $10 per lot
        // For Gold (XAUUSD): 1 pip (0.01 = 10 cents) = $1.00 per lot; 1 full point ($1.00 move = 100 pips) = $100 per lot ($10 per pip in traditional 1-point terminology).
        // Let's standardize: If stop loss is given in price distance, for Gold $1.00 move = $100 per 1.0 lot ($1 per 0.01 lot).
        val pipValuePerLot = when {
            symbol.contains("XAU", ignoreCase = true) -> 10.0 // $10 per pip/dollar fraction per lot
            symbol.contains("JPY", ignoreCase = true) -> 9.0  // approx JPY pip value
            else -> 10.0 // standard forex $10/pip
        }

        val totalRiskPerLot = safeSlPips * pipValuePerLot
        val rawLot = if (totalRiskPerLot > 0) riskDollars / totalRiskPerLot else 0.01

        // Round to 2 decimals, minimum 0.01 lot
        val lotSize = max(0.01, (rawLot * 100.0).roundToInt() / 100.0)
        val actualRiskAmount = (lotSize * safeSlPips * pipValuePerLot * 100.0).roundToInt() / 100.0
        val actualRewardAmount = (lotSize * rewardPips * pipValuePerLot * 100.0).roundToInt() / 100.0
        val rrRatio = if (safeSlPips > 0) (rewardPips / safeSlPips * 10.0).roundToInt() / 10.0 else 2.5

        val warning = when {
            safeRiskPct > 3.0 -> "⚠️ Danger: Risking > 3% on a single trade violates prop firm & capital preservation rules!"
            safeRiskPct > 2.0 -> "Caution: Risking > 2% per trade is aggressive for Gold volatility."
            symbol.contains("XAU") && safeSlPips < 15.0 -> "Notice: Stop Loss < $1.50 (15 pips) on Gold risks premature stop-out by spread spikes."
            rrRatio < 1.5 -> "Notice: Risk/Reward ratio is below 1:1.5. Minimum 1:2 is recommended."
            else -> null
        }

        return RiskCalculation(
            balance = safeBalance,
            riskPercent = safeRiskPct,
            stopLossPips = safeSlPips,
            symbol = symbol,
            calculatedLotSize = lotSize,
            riskAmountDollars = actualRiskAmount,
            rewardAmountDollars = actualRewardAmount,
            riskRewardRatio = rrRatio,
            warningMessage = warning
        )
    }
}
