package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.RiskCalculation
import com.example.ui.theme.*

@Composable
fun RiskManagementScreen(
    riskResult: RiskCalculation,
    onCalculate: (balance: Double, riskPct: Double, slPips: Double, symbol: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var balanceText by remember { mutableStateOf(riskResult.balance.toInt().toString()) }
    var selectedRiskPct by remember { mutableDoubleStateOf(riskResult.riskPercent) }
    var slPipsText by remember { mutableStateOf(riskResult.stopLossPips.toInt().toString()) }
    var selectedSymbol by remember { mutableStateOf(riskResult.symbol) }

    val presetBalances = listOf(1000.0, 5000.0, 10000.0, 50000.0)
    val presetRisks = listOf(0.5, 1.0, 1.5, 2.0, 3.0)
    val symbols = listOf("XAUUSD", "EURUSD", "GBPUSD", "USDJPY")

    fun triggerCalc() {
        val b = balanceText.toDoubleOrNull() ?: 10000.0
        val sl = slPipsText.toDoubleOrNull() ?: 25.0
        onCalculate(b, selectedRiskPct, sl, selectedSymbol)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Screen Header
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = DarkBackground,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Risk & Position Calculator",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Protect capital, calculate exact lot size & stay prop-firm compliant",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Live Calculated Lot Size Output Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, GoldPrimary.copy(alpha = 0.5f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "RECOMMENDED POSITION SIZE",
                            color = GoldLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.5.sp
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkBackground
                        ) {
                            Text(
                                text = selectedSymbol,
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "%.2f".format(riskResult.calculatedLotSize),
                            color = GoldPrimary,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Standard Lots",
                            color = TextSecondary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // 3-Metric Summary Grid
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkBackground)
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "DOLLAR RISK", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "-$%.2f".format(riskResult.riskAmountDollars),
                                color = BearRedLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "${riskResult.riskPercent}% of Balance", color = TextMuted, fontSize = 10.sp)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = "RISK : REWARD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "1 : %.1f".format(riskResult.riskRewardRatio),
                                color = CyanAccent,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "Optimal Ratio", color = TextMuted, fontSize = 10.sp)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(text = "TARGET REWARD", color = TextMuted, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = "+$%.2f".format(riskResult.rewardAmountDollars),
                                color = BullGreenLight,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(text = "At TP2 Exit", color = TextMuted, fontSize = 10.sp)
                        }
                    }

                    // Warning notice if parameters are too risky
                    if (riskResult.warningMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x22EF4444))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = BearRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = riskResult.warningMessage,
                                color = BearRedLight,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }

        // Configuration Inputs
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
                        text = "Trade Parameters",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Asset selector
                    Text(text = "Select Asset", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symbols.forEach { sym ->
                            val isSel = sym == selectedSymbol
                            Surface(
                                onClick = {
                                    selectedSymbol = sym
                                    triggerCalc()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) GoldPrimary else DarkSurfaceElevated,
                                modifier = Modifier.weight(1f).height(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = sym,
                                        color = if (isSel) DarkBackground else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Account Balance Input
                    Text(text = "Account Balance ($ USD)", color = TextSecondary, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = balanceText,
                        onValueChange = {
                            balanceText = it
                            triggerCalc()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        )
                    )

                    // Preset Balances
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetBalances.forEach { b ->
                            Surface(
                                onClick = {
                                    balanceText = b.toInt().toString()
                                    triggerCalc()
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = DarkSurfaceElevated
                            ) {
                                Text(
                                    text = "$${b.toInt()}",
                                    color = TextMuted,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Risk Percentage
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Risk Per Trade", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            text = "$selectedRiskPct%",
                            color = if (selectedRiskPct > 2.0) BearRed else GoldLight,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Slider(
                        value = selectedRiskPct.toFloat(),
                        onValueChange = {
                            selectedRiskPct = (it * 10).toInt() / 10.0
                            triggerCalc()
                        },
                        valueRange = 0.5f..5.0f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = GoldPrimary,
                            activeTrackColor = GoldPrimary,
                            inactiveTrackColor = DarkBorder
                        )
                    )

                    // Preset Risk Buttons
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        presetRisks.forEach { r ->
                            val isSel = r == selectedRiskPct
                            Surface(
                                onClick = {
                                    selectedRiskPct = r
                                    triggerCalc()
                                },
                                shape = RoundedCornerShape(6.dp),
                                color = if (isSel) GoldPrimary.copy(alpha = 0.2f) else DarkSurfaceElevated,
                                border = if (isSel) androidx.compose.foundation.BorderStroke(1.dp, GoldPrimary) else null
                            ) {
                                Text(
                                    text = "$r%",
                                    color = if (isSel) GoldLight else TextMuted,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Stop Loss Pips Input
                    Text(
                        text = if (selectedSymbol == "XAUUSD") "Stop Loss Distance (e.g. 25 pips = $2.50)" else "Stop Loss Distance (Pips)",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = slPipsText,
                        onValueChange = {
                            slPipsText = it
                            triggerCalc()
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldPrimary,
                            unfocusedBorderColor = DarkBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurfaceElevated,
                            unfocusedContainerColor = DarkSurfaceElevated
                        )
                    )
                }
            }
        }

        // Prop Firm & Risk Guardrails Card
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
                        text = "Capital Preservation Rules",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    val rules = listOf(
                        "Max Daily Drawdown: Never exceed 4% daily loss. Stop trading after 2 consecutive full stop-outs.",
                        "Gold Spread Padding: Maintain minimum 20 pips ($2.00) SL on Gold to avoid market maker stop-hunts.",
                        "Breakeven Automation: Move Stop Loss to Entry once Take Profit 1 (+1.5 RR) is reached.",
                        "Partial Profit Taking: Close 50% lot volume at TP1 and let remaining 50% run with trailing stop."
                    )

                    rules.forEach { rule ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = BullGreen,
                                modifier = Modifier.size(16.dp).padding(top = 2.dp)
                            )
                            Text(
                                text = rule,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
