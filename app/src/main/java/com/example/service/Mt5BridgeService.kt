package com.example.service

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.example.model.Mt5AccountInfo
import com.example.model.Mt5Order
import com.example.model.SignalAction
import com.example.model.TradeSignal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object Mt5BridgeService {

    private val _accountInfo = MutableStateFlow(Mt5AccountInfo())
    val accountInfo = _accountInfo.asStateFlow()

    private val _openOrders = MutableStateFlow(
        listOf(
            Mt5Order(
                ticket = 8942101L,
                symbol = "XAUUSD",
                type = SignalAction.BUY,
                lots = 0.20,
                openPrice = 2674.20,
                currentPrice = 2684.20,
                sl = 2665.00,
                tp = 2695.00,
                pnl = 200.00,
                openTime = "Today, 08:30"
            ),
            Mt5Order(
                ticket = 8942102L,
                symbol = "EURUSD",
                type = SignalAction.SELL,
                lots = 0.50,
                openPrice = 1.08580,
                currentPrice = 1.08420,
                sl = 1.08850,
                tp = 1.08150,
                pnl = 80.00,
                openTime = "Today, 11:15"
            )
        )
    )
    val openOrders = _openOrders.asStateFlow()

    fun copySignalForMt5(context: Context, signal: TradeSignal, lotSize: Double = 0.10) {
        val formatted = buildString {
            appendLine("=== MT5 TRADE SIGNAL ===")
            appendLine("Symbol: ${signal.symbol}")
            appendLine("OrderType: ${signal.action.name}")
            appendLine("Volume: %.2f".format(lotSize))
            appendLine("Entry: ${signal.entryPrice}")
            appendLine("StopLoss: ${signal.stopLoss}")
            appendLine("TakeProfit1: ${signal.takeProfit1}")
            appendLine("TakeProfit2: ${signal.takeProfit2}")
            appendLine("Timeframe: ${signal.timeframe}")
            appendLine("MagicNumber: 778899")
            appendLine("Comment: GoldFX_AI_${signal.confidence}%")
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("MT5 Trade Signal", formatted)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "MT5 Signal copied! Ready to paste into MetaTrader 5", Toast.LENGTH_SHORT).show()
    }

    fun launchMetaTrader5App(context: Context) {
        val pm = context.packageManager
        val mt5Package = "net.metaquotes.metatrader5"
        val intent = pm.getLaunchIntentForPackage(mt5Package)
        if (intent != null) {
            context.startActivity(intent)
        } else {
            // Open Play Store page or browser to MT5
            try {
                val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$mt5Package"))
                marketIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(marketIntent)
            } catch (e: Exception) {
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$mt5Package"))
                webIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(webIntent)
            }
        }
    }

    fun executeSignalOnMt5(signal: TradeSignal, lotSize: Double): Mt5Order {
        val newTicket = System.currentTimeMillis() % 10000000L
        val order = Mt5Order(
            ticket = newTicket,
            symbol = signal.symbol,
            type = signal.action,
            lots = lotSize,
            openPrice = signal.entryPrice,
            currentPrice = signal.entryPrice,
            sl = signal.stopLoss,
            tp = signal.takeProfit1,
            pnl = 0.0,
            openTime = "Just now"
        )
        _openOrders.value = listOf(order) + _openOrders.value
        val updatedEquity = _accountInfo.value.equity
        _accountInfo.value = _accountInfo.value.copy(
            openPositionsCount = _openOrders.value.size,
            equity = updatedEquity
        )
        return order
    }

    fun closePosition(ticket: Long) {
        val closed = _openOrders.value.find { it.ticket == ticket }
        if (closed != null) {
            _openOrders.value = _openOrders.value.filter { it.ticket != ticket }
            val newBalance = _accountInfo.value.balance + closed.pnl
            _accountInfo.value = _accountInfo.value.copy(
                balance = (newBalance * 100.0).toLong() / 100.0,
                equity = (newBalance * 100.0).toLong() / 100.0,
                openPositionsCount = _openOrders.value.size
            )
        }
    }

    fun getMql5BridgeCode(): String {
        return """
//+------------------------------------------------------------------+
//|                                              GoldFX_Mobile_EA.mq5|
//|                    Copyright 2026, GoldFX Mobile Trading Bridge  |
//|                                  https://github.com/goldfx-mt5   |
//+------------------------------------------------------------------+
#property copyright "GoldFX MT5 Bridge"
#property version   "1.00"
#property strict

input string InpWebhookSecret = "GFX_SEC_9921_AUTO";
input double InpDefaultLot     = 0.10;
input ulong  InpMagicNumber    = 778899;

// Runs continuously on MT5 terminal / VPS to receive mobile signals
int OnInit()
{
   Print("GoldFX Mobile MT5 EA Bridge Initialized. Awaiting mobile webhook...");
   EventSetTimer(1);
   return(INIT_SUCCEEDED);
}

void OnTimer()
{
   // In real deployment, poll your cloud webhook endpoint:
   // Webhook: https://api.goldfx-bridge.io/v1/signals/latest
   // Execute OrderSend() automatically when new signal arrives
}
        """.trimIndent()
    }
}
