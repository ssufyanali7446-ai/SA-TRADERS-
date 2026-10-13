package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.FinancialNewsRssRepository
import com.example.model.FinancialNews
import com.example.model.MarketSentimentSummary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class NewsSentimentViewModel(
    private val repository: FinancialNewsRssRepository = FinancialNewsRssRepository()
) : ViewModel() {

    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    private val _filteredNews = MutableStateFlow<List<FinancialNews>>(emptyList())
    val filteredNews: StateFlow<List<FinancialNews>> = _filteredNews.asStateFlow()

    private val _sentimentSummary = MutableStateFlow(repository.getSentimentSummary("XAUUSD"))
    val sentimentSummary: StateFlow<MarketSentimentSummary> = _sentimentSummary.asStateFlow()

    val isLoading: StateFlow<Boolean> = repository.isLoading

    init {
        updateFilteredNews()
        refreshRssFeeds()
    }

    fun refreshRssFeeds() {
        viewModelScope.launch {
            repository.refreshRssFeeds()
            updateFilteredNews()
        }
    }

    fun setFilter(symbol: String) {
        _selectedFilter.value = symbol
        updateFilteredNews()
    }

    private fun updateFilteredNews() {
        val symbol = _selectedFilter.value
        _filteredNews.value = repository.getNewsForSymbol(symbol)
        val targetSymbol = if (symbol == "ALL") "XAUUSD" else symbol
        _sentimentSummary.value = repository.getSentimentSummary(targetSymbol)
    }
}
