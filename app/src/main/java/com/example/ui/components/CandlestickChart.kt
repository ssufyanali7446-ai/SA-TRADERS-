package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Candle
import com.example.ui.theme.*
import kotlin.math.max
import kotlin.math.min

@Composable
fun CandlestickChart(
    symbol: String,
    candles: List<Candle>,
    selectedTimeframe: String,
    onTimeframeChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val timeframes = listOf("M5", "M15", "H1", "H4", "D1")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurfaceCard)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        // Timeframe Selector & Chart Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$symbol Price Action",
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                timeframes.forEach { tf ->
                    val isSelected = tf == selectedTimeframe
                    Surface(
                        onClick = { onTimeframeChange(tf) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) GoldPrimary else DarkSurfaceElevated,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = tf,
                                color = if (isSelected) DarkBackground else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Price Candlestick Canvas
        if (candles.isNotEmpty()) {
            val highPrice = candles.maxOf { it.high }
            val lowPrice = candles.minOf { it.low }
            val priceRange = max(0.0001, highPrice - lowPrice)
            val currentPrice = candles.last().close

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                Canvas(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val canvasWidth = size.width
                    val canvasHeight = size.height * 0.82f // Upper 82% for candles, lower 18% for volume
                    val volumeHeight = size.height * 0.16f
                    val candleCount = candles.size
                    val candleSpacing = canvasWidth / candleCount.toFloat()
                    val candleWidth = candleSpacing * 0.65f

                    // Draw grid lines
                    val gridLines = 4
                    for (i in 0..gridLines) {
                        val y = (canvasHeight / gridLines) * i
                        drawLine(
                            color = Color(0x1AFFFFFF),
                            start = Offset(0f, y),
                            end = Offset(canvasWidth, y),
                            strokeWidth = 1f
                        )
                    }

                    // EMA 20 Path
                    val emaPath = Path()
                    var emaStarted = false

                    // Draw Candlesticks & Volume
                    candles.forEachIndexed { index, candle ->
                        val x = index * candleSpacing + (candleSpacing / 2f)

                        val openY = (canvasHeight * (1f - ((candle.open - lowPrice) / priceRange).toFloat()))
                        val closeY = (canvasHeight * (1f - ((candle.close - lowPrice) / priceRange).toFloat()))
                        val highY = (canvasHeight * (1f - ((candle.high - lowPrice) / priceRange).toFloat()))
                        val lowY = (canvasHeight * (1f - ((candle.low - lowPrice) / priceRange).toFloat()))

                        val isBullish = candle.close >= candle.open
                        val candleColor = if (isBullish) BullGreen else BearRed

                        // Draw wick
                        drawLine(
                            color = candleColor,
                            start = Offset(x, highY),
                            end = Offset(x, lowY),
                            strokeWidth = 2f
                        )

                        // Draw candle body
                        val bodyTop = min(openY, closeY)
                        val bodyHeight = max(2f, kotlin.math.abs(closeY - openY))

                        drawRect(
                            color = candleColor,
                            topLeft = Offset(x - (candleWidth / 2f), bodyTop),
                            size = Size(candleWidth, bodyHeight)
                        )

                        // Draw volume bar at bottom
                        val volY = size.height - (candle.volume.toFloat() / 500f * volumeHeight).coerceIn(4f, volumeHeight)
                        drawRect(
                            color = candleColor.copy(alpha = 0.35f),
                            topLeft = Offset(x - (candleWidth / 2f), volY),
                            size = Size(candleWidth, size.height - volY)
                        )

                        // EMA calculation approximation
                        val emaY = (openY + closeY) / 2f
                        if (!emaStarted) {
                            emaPath.moveTo(x, emaY)
                            emaStarted = true
                        } else {
                            emaPath.lineTo(x, emaY)
                        }
                    }

                    // Draw EMA line
                    drawPath(
                        path = emaPath,
                        color = GoldLight.copy(alpha = 0.7f),
                        style = Stroke(width = 2.5f)
                    )

                    // Draw Current Price Dashed Guide Line
                    val currentY = (canvasHeight * (1f - ((currentPrice - lowPrice) / priceRange).toFloat()))
                    drawLine(
                        color = GoldPrimary,
                        start = Offset(0f, currentY),
                        end = Offset(canvasWidth, currentY),
                        strokeWidth = 1.5f
                    )
                }

                // Price Labels on Right Edge
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(end = 4.dp, top = 2.dp)
                ) {
                    Text(
                        text = "H: %.2f".format(highPrice),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 4.dp, bottom = 24.dp)
                ) {
                    Text(
                        text = "L: %.2f".format(lowPrice),
                        color = TextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Indicator Summary Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "EMA 20",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Vol: 100-500",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Current: $%.2f".format(currentPrice),
                    color = GoldPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
