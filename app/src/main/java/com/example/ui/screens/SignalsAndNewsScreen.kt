package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FinancialNews
import com.example.model.MarketSentimentSummary
import com.example.model.TradeSignal
import com.example.ui.components.NewsCard
import com.example.ui.components.SentimentOverviewBanner
import com.example.ui.components.SignalCard
import com.example.ui.theme.*

@Composable
fun SignalsAndNewsScreen(
    signals: List<TradeSignal>,
    newsList: List<FinancialNews>,
    sentimentSummary: MarketSentimentSummary,
    selectedFilter: String,
    onFilterChange: (String) -> Unit,
    isNewsLoading: Boolean,
    onRefreshNews: () -> Unit,
    onExecuteSignal: (TradeSignal) -> Unit,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableIntStateOf(0) } // 0: AI Signals, 1: Financial News & Sentiment
    val filterOptions = listOf("ALL", "XAUUSD", "EURUSD", "GBPUSD", "USDJPY")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Tab Switcher: AI Signals vs News & Sentiment
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(DarkSurfaceElevated)
                .padding(4.dp)
        ) {
            Surface(
                onClick = { activeTab = 0 },
                shape = RoundedCornerShape(10.dp),
                color = if (activeTab == 0) GoldPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = if (activeTab == 0) DarkBackground else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AI Signals (${signals.size})",
                        color = if (activeTab == 0) DarkBackground else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Surface(
                onClick = { activeTab = 1 },
                shape = RoundedCornerShape(10.dp),
                color = if (activeTab == 1) GoldPrimary else Color.Transparent,
                modifier = Modifier
                    .weight(1.2f)
                    .height(36.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Newspaper,
                        contentDescription = null,
                        tint = if (activeTab == 1) DarkBackground else TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "News & Sentiment (${newsList.size})",
                        color = if (activeTab == 1) DarkBackground else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Symbol Filter Bar with RSS Refresh button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                filterOptions.forEach { filter ->
                    val isSelected = filter == selectedFilter
                    FilterChip(
                        selected = isSelected,
                        onClick = { onFilterChange(filter) },
                        label = {
                            Text(
                                text = if (filter == "ALL") "All Pairs" else filter,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = GoldDark.copy(alpha = 0.35f),
                            selectedLabelColor = GoldLight,
                            containerColor = DarkSurfaceElevated,
                            labelColor = TextMuted
                        )
                    )
                }
            }

            IconButton(
                onClick = onRefreshNews,
                enabled = !isNewsLoading,
                modifier = Modifier.size(36.dp)
            ) {
                if (isNewsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = GoldPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh RSS Feeds",
                        tint = GoldPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        // Main Content List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Sentiment Overview Gauge Banner
            item {
                SentimentOverviewBanner(summary = sentimentSummary)
            }

            if (activeTab == 0) {
                // Signals Tab
                val filteredSignals = if (selectedFilter == "ALL") signals else signals.filter { it.symbol == selectedFilter }
                if (filteredSignals.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No trade signals found for $selectedFilter. Switch to 'All Pairs' or scan in Chart view.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredSignals, key = { it.id }) { signal ->
                        SignalCard(
                            signal = signal,
                            onExecuteClick = onExecuteSignal
                        )
                    }
                }
            } else {
                // Financial News & Sentiment Tab
                val filteredNews = if (selectedFilter == "ALL") {
                    newsList
                } else {
                    newsList.filter {
                        it.relatedSymbol.equals(selectedFilter, ignoreCase = true) ||
                                (selectedFilter == "XAUUSD" && it.title.contains("Gold", ignoreCase = true)) ||
                                (selectedFilter.contains("USD") && it.relatedSymbol == "FOREX")
                    }
                }

                if (filteredNews.isEmpty()) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceCard,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                Text(
                                    text = "No RSS news articles available for $selectedFilter. Tap refresh icon above to fetch.",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    items(filteredNews, key = { it.id }) { news ->
                        NewsCard(news = news)
                    }
                }
            }
        }
    }
}
