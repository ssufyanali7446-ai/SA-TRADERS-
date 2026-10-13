package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BacktestResult
import com.example.model.SignalAction
import com.example.service.BacktestingEngine
import com.example.ui.theme.*
import kotlin.math.max

@Composable
fun BacktestScreen(
    result: BacktestResult?,
    isBacktesting: Boolean,
    onRunBacktest: (strategyId: String, symbol: String, timeframe: String, days: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strategies = BacktestingEngine.strategies
    var selectedStrategyId by remember { mutableStateOf(strategies.first().id) }
    var selectedSymbol by remember { mutableStateOf("XAUUSD") }
    var selectedTimeframe by remember { mutableStateOf("H1") }
    var selectedDays by remember { mutableIntStateOf(90) }

    val symbols = listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY")
    val periods = listOf(30, 60, 90, 180)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(GoldPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QueryStats,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Historical Strategy Backtester",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Refine edge, test win rates, and verify drawdown before executing live",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Strategy Selector Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select Quantitative Strategy",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    strategies.forEach { strat ->
                        val isSel = strat.id == selectedStrategyId
                        Surface(
                            onClick = { selectedStrategyId = strat.id },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) DarkSurfaceElevated else Color.Transparent,
                            border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = strat.name,
                                    color = if (isSel) GoldLight else TextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strat.description,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset, Timeframe, Period Rows
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Asset", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                symbols.forEach { sym ->
                                    val isS = sym == selectedSymbol
                                    Surface(
                                        onClick = { selectedSymbol = sym },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isS) GoldPrimary else DarkSurfaceElevated
                                    ) {
                                        Text(
                                            text = sym,
                                            color = if (isS) DarkBackground else TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }

                        Column {
                            Text(text = "Period", color = TextMuted, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                periods.forEach { d ->
                                    val isP = d == selectedDays
                                    Surface(
                                        onClick = { selectedDays = d },
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isP) GoldPrimary else DarkSurfaceElevated
                                    ) {
                                        Text(
                                            text = "${d}d",
                                            color = if (isP) DarkBackground else TextSecondary,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Run Button
                    Button(
                        onClick = {
                            onRunBacktest(selectedStrategyId, selectedSymbol, selectedTimeframe, selectedDays)
                        },
                        enabled = !isBacktesting,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                    ) {
                        if (isBacktesting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = DarkBackground,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = DarkBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Run Backtest Simulation ($selectedSymbol)",
                                color = DarkBackground,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Backtest Results Dashboard
        if (result != null) {
            item {
                Text(
                    text = "Performance Metrics (${result.symbol} ${result.timeframe} - ${result.periodDays} Days)",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // 4-Card Stat Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Win Rate Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "WIN RATE", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "%.1f%%".format(result.winRate),
                                color = BullGreen,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${result.winCount}W / ${result.lossCount}L (${result.totalTrades} Trades)",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Profit Factor Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "PROFIT FACTOR", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "%.2f".format(result.profitFactor),
                                color = GoldLight,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Sharpe: %.2f".format(result.sharpeRatio),
                                color = CyanAccent,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Net Profit Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "NET PROFIT", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            val absProfit = kotlin.math.abs(result.netProfitDollars)
                            Text(
                                text = "${if (result.netProfitDollars >= 0) "+$" else "-$"}%.2f".format(absProfit),
                                color = BullGreen,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "End: $%.0f".format(result.finalBalance),
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    // Max Drawdown Card
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = "MAX DRAWDOWN", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "%.1f%%".format(result.maxDrawdownPct),
                                color = if (result.maxDrawdownPct > 8.0) BearRed else GoldLight,
                                fontSize = 19.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Safe Prop Threshold",
                                color = TextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // Equity Curve Line Chart Canvas
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Equity Growth Curve",
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Start: $%.0f ➔ Final: $%.0f".format(result.initialBalance, result.finalBalance),
                                color = GoldLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        val points = result.equityCurve
                        if (points.isNotEmpty()) {
                            val minVal = points.minOrNull() ?: 10000.0
                            val maxVal = points.maxOrNull() ?: 12000.0
                            val valRange = max(1.0, maxVal - minVal)

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(150.dp)
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    val w = size.width
                                    val h = size.height
                                    val stepX = w / (points.size - 1).toFloat()

                                    val linePath = Path()
                                    val fillPath = Path()

                                    points.forEachIndexed { i, pt ->
                                        val x = i * stepX
                                        val y = h * (1f - ((pt - minVal) / valRange).toFloat())
                                        if (i == 0) {
                                            linePath.moveTo(x, y)
                                            fillPath.moveTo(x, h)
                                            fillPath.lineTo(x, y)
                                        } else {
                                            linePath.lineTo(x, y)
                                            fillPath.lineTo(x, y)
                                        }
                                    }
                                    fillPath.lineTo(w, h)
                                    fillPath.close()

                                    // Gradient fill
                                    drawPath(
                                        path = fillPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(BullGreen.copy(alpha = 0.35f), Color.Transparent),
                                            startY = 0f,
                                            endY = h
                                        )
                                    )

                                    // Curve line
                                    drawPath(
                                        path = linePath,
                                        color = BullGreen,
                                        style = Stroke(width = 3.dp.toPx())
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Trade-by-trade log list
            item {
                Text(
                    text = "Recent Simulated Trades (${result.trades.size})",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            items(result.trades.take(15), key = { it.id }) { trade ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = DarkSurfaceElevated,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (trade.isWin) BullGreenBg else BearRedBg
                            ) {
                                Text(
                                    text = if (trade.isWin) "WIN" else "LOSS",
                                    color = if (trade.isWin) BullGreen else BearRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = "${trade.type.name} at ${trade.entryPrice} ➔ ${trade.exitPrice}",
                                    color = TextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${trade.exitReason} • ${trade.dateStr}",
                                    color = TextMuted,
                                    fontSize = 10.sp
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            val absPnl = kotlin.math.abs(trade.pnl)
                            Text(
                                text = "${if (trade.pnl >= 0) "+$" else "-$"}%.2f".format(absPnl),
                                color = if (trade.isWin) BullGreen else BearRed,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${if (trade.pnlPercent >= 0) "+" else ""}%.1f%%".format(trade.pnlPercent),
                                color = if (trade.isWin) BullGreenLight else BearRedLight,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
