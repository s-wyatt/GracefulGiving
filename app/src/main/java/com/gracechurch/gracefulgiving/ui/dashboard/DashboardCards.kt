package com.gracechurch.gracefulgiving.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


@Composable
fun TotalsRow(mtd: Double, qtd: Double, ytd: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // The weight modifier is applied to the children of this Row
        StatCard(title = "MTD", amount = mtd)
        StatCard(title = "QTD", amount = qtd)
        StatCard(title = "YTD", amount = ytd)
    }
}

@Composable
fun RowScope.StatCard(title: String, amount: Double) { // GENTLE FIX: Explicitly use RowScope
    Card(modifier = Modifier.weight(1f)) { // This now correctly resolves within the RowScope
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally // Center content for better look
        ) {
            Text(title, style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(8.dp))
            Text(
                String.format("$%,.2f", amount),
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}

@Composable
fun OpenBatchesList(batches: List<BatchInfo>) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(batches) { batch ->
            Card(modifier = Modifier.fillMaxWidth()) { // Make cards fill the width
                Row(
                    modifier = Modifier.padding(12.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(batch.batchName)
                        Text("ID: ${batch.batchId}", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(String.format("$%,.2f", batch.total))
                }
            }
        }
    }
}

@Composable
fun MiniBarChart(values: List<Double>, modifier: Modifier = Modifier.height(80.dp).fillMaxWidth()) {
    if (values.isEmpty()) {
        Box(modifier = modifier)
        return
    }

    val maxValue = values.maxOrNull() ?: 1.0
    val barColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        val barWidth = size.width / values.size

        values.forEachIndexed { index, value ->
            val barHeight = (value / maxValue) * size.height

            drawRect(
                color = barColor,
                topLeft = Offset(
                    x = index * barWidth + (barWidth * 0.1f),
                    y = (size.height - barHeight).toFloat()
                ),
                size = Size(
                    width = barWidth * 0.8f,
                    height = barHeight.toFloat()
                )
            )
        }
    }
}
