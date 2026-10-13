package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.OpenInNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarketPair
import com.example.model.PairCategory
import com.example.service.Mt5BridgeService
import com.example.ui.theme.*

@Composable
fun MarketWatchScreen(
    pairs: List<MarketPair>,
    selectedPair: MarketPair,
    onSelectPair: (MarketPair) -> Unit,
    onNavigateToChart: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<PairCategory?>(null) }
    val filteredPairs = if (selectedCategory == null) pairs else pairs.filter { it.category == selectedCategory }
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedCategory == null,
                onClick = { selectedCategory = null },
                label = { Text("All Markets", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceElevated,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedCategory == PairCategory.GOLD,
                onClick = { selectedCategory = PairCategory.GOLD },
                label = { Text("🪙 Gold (XAU)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceElevated,
                    labelColor = TextSecondary
                )
            )

            FilterChip(
                selected = selectedCategory == PairCategory.FOREX,
                onClick = { selectedCategory = PairCategory.FOREX },
                label = { Text("Forex Pairs", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = GoldPrimary,
                    selectedLabelColor = DarkBackground,
                    containerColor = DarkSurfaceElevated,
                    labelColor = TextSecondary
                )
            )
        }

        // Market Pairs List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredPairs, key = { it.symbol }) { pair ->
                val isSelected = pair.symbol == selectedPair.symbol
                val isPositive = pair.change24h >= 0

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) GoldPrimary else DarkBorder,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSelectPair(pair)
                            onNavigateToChart()
                        },
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Symbol & Name & Category Badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = pair.symbol,
                                            color = if (pair.category == PairCategory.GOLD) GoldLight else TextPrimary,
                                            fontSize = 17.sp,
                                            fontWeight = FontWeight.Bold
                                        )

                                        if (pair.category == PairCategory.GOLD) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = GoldDark.copy(alpha = 0.25f)
                                            ) {
                                                Text(
                                                    text = "MAIN FOCUS",
                                                    color = GoldLight,
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                    }
                                    Text(
                                        text = pair.displayName,
                                        color = TextMuted,
                                        fontSize = 11.sp
                                    )
                                }
                            }

                            // 24h Change Pill
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isPositive) BullGreenBg else BearRedBg,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isPositive) BullGreen.copy(alpha = 0.4f) else BearRed.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPositive) Icons.AutoMirrored.Filled.TrendingUp else Icons.AutoMirrored.Filled.TrendingDown,
                                        contentDescription = null,
                                        tint = if (isPositive) BullGreen else BearRed,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "${if (isPositive) "+" else ""}%.2f%%".format(pair.change24h),
                                        color = if (isPositive) BullGreen else BearRed,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Bid, Ask, Spread Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceElevated)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = "BID (SELL)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "%.${pair.digits}f".format(pair.bid),
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = "SPREAD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "%.1f pips".format(pair.spread),
                                    color = GoldLight,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(text = "ASK (BUY)", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "%.${pair.digits}f".format(pair.ask),
                                    color = TextPrimary,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Actions Row: Analyze & MT5 Deep-Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onSelectPair(pair)
                                    onNavigateToChart()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "AI",
                                    tint = GoldPrimary,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("AI Chart & Signals", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    Mt5BridgeService.launchMetaTrader5App(context)
                                },
                                modifier = Modifier.weight(0.7f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, DarkBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.OpenInNew,
                                    contentDescription = "MT5",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("MT5 App", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
