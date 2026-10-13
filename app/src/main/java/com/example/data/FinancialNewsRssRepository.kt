package com.example.data

import com.example.model.FinancialNews
import com.example.model.MarketSentimentSummary
import com.example.model.NewsImpact
import com.example.model.NewsSentiment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class FinancialNewsRssRepository(
    private val rssService: RssFeedService = RssFeedService
) {
    private val _newsState = MutableStateFlow<List<FinancialNews>>(emptyList())
    val newsState: StateFlow<List<FinancialNews>> = _newsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        _newsState.value = NewsSentimentRepository.getNewsForSymbol("ALL")
    }

    suspend fun refreshRssFeeds(): List<FinancialNews> = withContext(Dispatchers.Default) {
        _isLoading.value = true
        try {
            val rawItems = rssService.fetchAllFeeds()
            val parsedNews = rawItems.mapIndexed { index, raw ->
                analyzeRssItem(raw, "rss-$index")
            }
            if (parsedNews.isNotEmpty()) {
                _newsState.value = parsedNews
            }
            parsedNews
        } finally {
            _isLoading.value = false
        }
    }

    fun getNewsForSymbol(symbol: String): List<FinancialNews> {
        val current = _newsState.value
        if (symbol == "ALL") return current
        return current.filter {
            it.relatedSymbol.equals(symbol, ignoreCase = true) ||
                    (symbol == "XAUUSD" && (it.relatedSymbol == "XAUUSD" || it.title.contains("Gold", ignoreCase = true))) ||
                    (symbol.contains("USD") && it.relatedSymbol == "FOREX")
        }
    }

    fun getSentimentSummary(symbol: String = "XAUUSD"): MarketSentimentSummary {
        val relevant = getNewsForSymbol(symbol)
        val bullishCount = relevant.count { it.sentiment == NewsSentiment.BULLISH }
        val bearishCount = relevant.count { it.sentiment == NewsSentiment.BEARISH }
        val neutralCount = relevant.count { it.sentiment == NewsSentiment.NEUTRAL }
        val total = maxOf(1, relevant.size)

        val bullPct = (bullishCount * 100) / total
        val bearPct = (bearishCount * 100) / total
        val neutPct = (100 - bullPct - bearPct).coerceAtLeast(0)

        val mood = when {
            bullPct >= 65 -> "Strongly Bullish (Risk-On / Gold Long Bias)"
            bullPct >= 50 -> "Moderately Bullish"
            bearPct >= 60 -> "Strongly Bearish (Defensive / Short Bias)"
            bearPct >= 45 -> "Moderately Bearish"
            else -> "Neutral / Consolidation Bias"
        }

        return MarketSentimentSummary(
            symbol = symbol,
            overallMood = mood,
            bullishPercentage = bullPct,
            bearishPercentage = bearPct,
            neutralPercentage = neutPct,
            primaryDriver = "Fed Rate Cut Expectations & Global Physical Gold Reserves",
            keyUpcomingEvent = "US Core PCE & Non-Farm Payrolls (Friday)"
        )
    }

    private fun analyzeRssItem(raw: RawRssItem, id: String): FinancialNews {
        val text = "${raw.title} ${raw.description}".lowercase()

        val symbol = when {
            text.contains("gold") || text.contains("xau") || text.contains("bullion") || text.contains("precious metal") -> "XAUUSD"
            text.contains("eur/usd") || text.contains("euro") || text.contains("ecb") || text.contains("germany") -> "EURUSD"
            text.contains("gbp/usd") || text.contains("sterling") || text.contains("pound") || text.contains("boe") -> "GBPUSD"
            text.contains("usd/jpy") || text.contains("yen") || text.contains("boj") || text.contains("japan") -> "USDJPY"
            text.contains("aud/usd") || text.contains("rba") || text.contains("aussie") -> "AUDUSD"
            text.contains("btc") || text.contains("crypto") || text.contains("bitcoin") -> "BTCUSD"
            else -> "FOREX"
        }

        val bullishWords = listOf("rally", "surge", "gain", "highs", "cut", "dovish", "support", "record", "accumulation", "easing", "beat", "climb", "rise", "soar", "jump", "strength")
        val bearishWords = listOf("drop", "plunge", "fall", "slump", "hike", "hawkish", "rejection", "pressure", "weakens", "deficit", "selloff", "dip", "tumble", "recession", "miss")

        var bullScore = 0
        var bearScore = 0

        bullishWords.forEach { word -> if (text.contains(word)) bullScore++ }
        bearishWords.forEach { word -> if (text.contains(word)) bearScore++ }

        val netScore = (bullScore - bearScore).toDouble() / maxOf(1, bullScore + bearScore).toDouble()
        val sentiment = when {
            netScore >= 0.20 -> NewsSentiment.BULLISH
            netScore <= -0.20 -> NewsSentiment.BEARISH
            else -> NewsSentiment.NEUTRAL
        }

        val impact = when {
            text.contains("fed") || text.contains("cpi") || text.contains("nfp") || text.contains("central bank") || text.contains("rate cut") || text.contains("war") || text.contains("geopolit") -> NewsImpact.HIGH
            text.contains("pmi") || text.contains("retail") || text.contains("speech") -> NewsImpact.MEDIUM
            else -> NewsImpact.LOW
        }

        val aiTakeaway = when (sentiment) {
            NewsSentiment.BULLISH -> if (symbol == "XAUUSD") "Strong tailwind for Gold upside. Dip-buying between key supports holds high statistical expectancy." else "Positive momentum catalyst. Fosters continuation toward next resistance band."
            NewsSentiment.BEARISH -> if (symbol == "XAUUSD") "Short-term profit-taking pressure on Gold. Monitor support levels for exhaustion before long entries." else "Downward pressure dominant. Favorable context for trend-following short executions."
            NewsSentiment.NEUTRAL -> "Balanced macro tone. High probability of range-bound price action and tight consolidation."
        }

        return FinancialNews(
            id = id,
            title = raw.title,
            summary = raw.description,
            source = raw.source,
            timeAgo = raw.pubDate.ifBlank { "Recent" },
            relatedSymbol = symbol,
            sentiment = sentiment,
            sentimentScore = (netScore * 100.0).toInt() / 100.0,
            impact = impact,
            aiTakeaway = aiTakeaway
        )
    }
}
