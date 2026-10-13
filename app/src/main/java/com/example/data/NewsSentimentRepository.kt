package com.example.data

import com.example.model.FinancialNews
import com.example.model.MarketSentimentSummary
import com.example.model.NewsImpact
import com.example.model.NewsSentiment
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object NewsSentimentRepository {

    private val initialNews = listOf(
        FinancialNews(
            id = "news-01",
            title = "US CPI Cooldown Fuels 50bps Fed Cut Speculation; Gold Surges Toward $2,700",
            summary = "Core Consumer Price Index slowed to 3.1% YoY, weaker than consensus. Bond yields plunged and the US Dollar retreated, accelerating institutional gold bullion demand.",
            source = "Bloomberg Markets",
            timeAgo = "14m ago",
            relatedSymbol = "XAUUSD",
            sentiment = NewsSentiment.BULLISH,
            sentimentScore = 0.88,
            impact = NewsImpact.HIGH,
            aiTakeaway = "Strong Bullish catalyst for Gold. DXY weakness creates clean path for upside breakout towards $2,700-$2,720."
        ),
        FinancialNews(
            id = "news-02",
            title = "Central Banks Accumulate 290 Metric Tons of Gold in Latest Quarter",
            summary = "World Gold Council reports sovereign central banks continued de-dollarization purchases, establishing an unprecedented physical demand floor beneath current prices.",
            source = "Reuters FX",
            timeAgo = "48m ago",
            relatedSymbol = "XAUUSD",
            sentiment = NewsSentiment.BULLISH,
            sentimentScore = 0.82,
            impact = NewsImpact.HIGH,
            aiTakeaway = "Deep structural support around $2,650-$2,670 zone. Buy-the-dip setups carry high macro edge."
        ),
        FinancialNews(
            id = "news-03",
            title = "ECB Official Warns of Eurozone Growth Slump as German Industrial Output Weakens",
            summary = "German factory orders dropped 1.8%, increasing pressure on the European Central Bank to accelerate monetary easing, weighing heavily on Euro pairs.",
            source = "Financial Times",
            timeAgo = "1h ago",
            relatedSymbol = "EURUSD",
            sentiment = NewsSentiment.BEARISH,
            sentimentScore = -0.65,
            impact = NewsImpact.MEDIUM,
            aiTakeaway = "Bearish tailwind for EUR/USD. Pullbacks to 1.0860-1.0880 resistance offer favorable short opportunities."
        ),
        FinancialNews(
            id = "news-04",
            title = "Middle East Geopolitical Friction Elevates Safe-Haven Hedge Premiums",
            summary = "Energy transit lanes face heightened maritime surveillance, triggering renewed hedging flows into Swiss Franc, Gold bullion, and Crude Oil futures.",
            source = "Al Jazeera / Dow Jones",
            timeAgo = "2h ago",
            relatedSymbol = "XAUUSD",
            sentiment = NewsSentiment.BULLISH,
            sentimentScore = 0.79,
            impact = NewsImpact.HIGH,
            aiTakeaway = "Risk-off environment favors long Gold. Scalping shorts during active geopolitical news poses asymmetric downside risk."
        ),
        FinancialNews(
            id = "news-05",
            title = "Bank of Japan Discusses Additional Rate Hikes; Yen Gains Against Greenback",
            summary = "BoJ Governor Ueda reiterated that rates will rise if inflation remains on target, creating sharp two-way volatility on USD/JPY around 152.50.",
            source = "Nikkei Asia",
            timeAgo = "3h ago",
            relatedSymbol = "USDJPY",
            sentiment = NewsSentiment.BEARISH,
            sentimentScore = -0.52,
            impact = NewsImpact.HIGH,
            aiTakeaway = "Cautious for USD/JPY longs. Warning: Potential intervention volatility if price pierces 153.50."
        ),
        FinancialNews(
            id = "news-06",
            title = "UK Retail Sales Beat Estimates by 0.6%, Supporting Sterling Stability",
            summary = "Consumer spending resilient despite high mortgage rates, reducing probability of emergency BoE rate reductions in the immediate meeting.",
            source = "Sky Business",
            timeAgo = "4h ago",
            relatedSymbol = "GBPUSD",
            sentiment = NewsSentiment.BULLISH,
            sentimentScore = 0.61,
            impact = NewsImpact.MEDIUM,
            aiTakeaway = "Supportive for GBP/USD intraday upside. Favorable alignment with H4 ascending trendline."
        ),
        FinancialNews(
            id = "news-07",
            title = "US Dollar Index (DXY) Consolidates at 104.10 Ahead of Non-Farm Payrolls",
            summary = "Traders adopt cautious posture before US employment release. Treasury yields trade sideways in tight European morning range.",
            source = "FXStreet",
            timeAgo = "5h ago",
            relatedSymbol = "FOREX",
            sentiment = NewsSentiment.NEUTRAL,
            sentimentScore = 0.05,
            impact = NewsImpact.MEDIUM,
            aiTakeaway = "Neutral market tone across forex majors. Tight spreads and range-bound trading expected before US opening bell."
        )
    )

    private val _newsList = MutableStateFlow(initialNews)
    val newsList: Flow<List<FinancialNews>> = _newsList.asStateFlow()

    fun getNewsForSymbol(symbol: String): List<FinancialNews> {
        val current = _newsList.value
        if (symbol == "ALL") return current
        return current.filter {
            it.relatedSymbol.equals(symbol, ignoreCase = true) ||
                    (symbol.contains("USD") && it.relatedSymbol == "FOREX") ||
                    (symbol == "XAUUSD" && (it.relatedSymbol == "XAUUSD" || it.title.contains("Gold", ignoreCase = true)))
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
        val neutPct = 100 - bullPct - bearPct

        val mood = when {
            bullPct >= 65 -> "Strongly Bullish (Risk-On / Gold Long)"
            bullPct >= 50 -> "Moderately Bullish"
            bearPct >= 60 -> "Strongly Bearish (Defensive / Short)"
            bearPct >= 45 -> "Moderately Bearish"
            else -> "Neutral / Consolidation Bias"
        }

        return MarketSentimentSummary(
            symbol = symbol,
            overallMood = mood,
            bullishPercentage = bullPct,
            bearishPercentage = bearPct,
            neutralPercentage = neutPct,
            primaryDriver = "Fed Rate Cut Horizon & Sovereign Bullion Buying",
            keyUpcomingEvent = "US Core PCE & Non-Farm Payrolls (Friday 12:30 GMT)"
        )
    }

    fun addBreakingNews(item: FinancialNews) {
        val updated = listOf(item) + _newsList.value
        _newsList.value = updated
    }
}
