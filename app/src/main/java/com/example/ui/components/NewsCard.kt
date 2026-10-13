package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FinancialNews
import com.example.model.NewsImpact
import com.example.model.NewsSentiment
import com.example.ui.theme.*

@Composable
fun NewsCard(
    news: FinancialNews,
    modifier: Modifier = Modifier
) {
    val sentimentColor = when (news.sentiment) {
        NewsSentiment.BULLISH -> BullGreen
        NewsSentiment.BEARISH -> BearRed
        NewsSentiment.NEUTRAL -> TextSecondary
    }

    val sentimentBg = when (news.sentiment) {
        NewsSentiment.BULLISH -> BullGreenBg
        NewsSentiment.BEARISH -> BearRedBg
        NewsSentiment.NEUTRAL -> Color(0x1994A3B8)
    }

    val impactColor = when (news.impact) {
        NewsImpact.HIGH -> WarningOrange
        NewsImpact.MEDIUM -> CyanAccent
        NewsImpact.LOW -> TextMuted
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceCard)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Symbol Tag, Impact, Sentiment Badge & Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = GoldDark.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = news.relatedSymbol,
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = impactColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "${news.impact.name} IMPACT",
                            color = impactColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                // Sentiment Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = sentimentBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, sentimentColor.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (news.sentiment) {
                                NewsSentiment.BULLISH -> Icons.AutoMirrored.Filled.TrendingUp
                                NewsSentiment.BEARISH -> Icons.AutoMirrored.Filled.TrendingDown
                                NewsSentiment.NEUTRAL -> Icons.Default.Remove
                            },
                            contentDescription = null,
                            tint = sentimentColor,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = "${news.sentiment.name} (${if (news.sentimentScore >= 0) "+" else ""}${String.format("%.2f", news.sentimentScore)})",
                            color = sentimentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Headline
            Text(
                text = news.title,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 21.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Summary
            Text(
                text = news.summary,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // AI Trading Takeaway box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceElevated)
                    .border(1.dp, Color(0x22F5B041), RoundedCornerShape(10.dp))
                    .padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Sentiment Analysis",
                    tint = GoldPrimary,
                    modifier = Modifier.size(16.dp).padding(top = 1.dp)
                )
                Column {
                    Text(
                        text = "AI Market Impact Takeaway",
                        color = GoldLight,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = news.aiTakeaway,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Footer: Source and Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = news.source,
                    color = TextMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = news.timeAgo,
                    color = TextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}
