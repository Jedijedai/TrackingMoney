package id.antasari.trackingmoney.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import id.antasari.trackingmoney.data.dao.CategorySum
import id.antasari.trackingmoney.data.dao.MonthlyTotal
import java.util.Locale

@Composable
fun CategoryDonutChart(
    data: List<CategorySum>,
    colors: List<Color>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.totalAmount }.toFloat()
    
    Canvas(modifier = modifier) {
        val strokeWidth = 40f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)
        
        if (total == 0f || data.isEmpty() || colors.isEmpty()) {
            drawArc(
                color = Color.LightGray.copy(alpha = 0.3f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            return@Canvas
        }

        var startAngle = -90f
        data.forEachIndexed { index, item ->
            val sweepAngle = (item.totalAmount.toFloat() / total) * 360f
            val color = colors[index % colors.size]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun TrendBarChart(
    data: List<MonthlyTotal>,
    expenseColor: Color,
    incomeColor: Color,
    modifier: Modifier = Modifier
) {
    var touchedIndex by remember { mutableStateOf<Int?>(null) }
    val textMeasurer = rememberTextMeasurer()
    
    val maxAmount = data.maxOfOrNull { maxOf(it.totalExpense, it.totalIncome) }?.toFloat() ?: 0f

    // Helper for compact numbers (e.g. 1.5M, 50K)
    val formatCompact: (Long) -> String = { amount ->
        when {
            amount >= 1_000_000 -> String.format(Locale.US, "%.1fM", amount / 1_000_000.0).replace(".0M", "M")
            amount >= 1_000 -> String.format(Locale.US, "%.1fK", amount / 1_000.0).replace(".0K", "K")
            else -> amount.toString()
        }
    }

    Box(modifier = modifier.semantics {
        // Create an accessible description for the chart
        val descriptions = data.map { "${it.month}: Pengeluaran ${it.totalExpense}, Pemasukan ${it.totalIncome}" }
        contentDescription = "Grafik Tren: ${descriptions.joinToString("; ")}"
    }) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(data) {
                    detectTapGestures { offset ->
                        if (data.isEmpty()) return@detectTapGestures
                        val barWidthWithSpacing = size.width / data.size
                        val index = (offset.x / barWidthWithSpacing).toInt().coerceIn(0, data.lastIndex)
                        touchedIndex = index
                    }
                }
        ) {
            if (data.isEmpty() || maxAmount == 0f) return@Canvas

            val barSpacing = 16f
            val groupWidth = (size.width - (barSpacing * (data.size - 1))) / data.size
            val barWidth = (groupWidth - 8f) / 2 // 8f spacing between income and expense bars
            
            data.forEachIndexed { index, item ->
                val startX = index * (groupWidth + barSpacing)
                
                // Expense Bar (Left)
                val expenseHeight = if (maxAmount > 0) (item.totalExpense.toFloat() / maxAmount) * size.height else 0f
                val expenseTop = size.height - expenseHeight
                drawRoundRect(
                    color = expenseColor,
                    topLeft = Offset(startX, expenseTop),
                    size = Size(barWidth, expenseHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                if (item.totalExpense > 0 && expenseHeight > 24f) {
                    val textStr = formatCompact(item.totalExpense)
                    val textLayoutResult = textMeasurer.measure(textStr, TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    // Draw text horizontally centered, near the top of the bar
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = Color.White,
                        topLeft = Offset(
                            x = startX + (barWidth - textLayoutResult.size.width) / 2,
                            y = expenseTop + 4f
                        )
                    )
                }
                
                // Income Bar (Right)
                val incomeHeight = if (maxAmount > 0) (item.totalIncome.toFloat() / maxAmount) * size.height else 0f
                val incomeTop = size.height - incomeHeight
                val incomeStartX = startX + barWidth + 8f
                drawRoundRect(
                    color = incomeColor,
                    topLeft = Offset(incomeStartX, incomeTop),
                    size = Size(barWidth, incomeHeight),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                if (item.totalIncome > 0 && incomeHeight > 24f) {
                    val textStr = formatCompact(item.totalIncome)
                    val textLayoutResult = textMeasurer.measure(textStr, TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
                    drawText(
                        textLayoutResult = textLayoutResult,
                        color = Color.White,
                        topLeft = Offset(
                            x = incomeStartX + (barWidth - textLayoutResult.size.width) / 2,
                            y = incomeTop + 4f
                        )
                    )
                }
            }
        }
        
        // Tooltip can be added here based on touchedIndex if needed. 
        // For simplicity and matching premium UI, we might just rely on the Canvas tap to show a small overlay or we just leave it visual.
    }
}
