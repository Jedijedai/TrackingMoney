package id.antasari.trackingmoney.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import id.antasari.trackingmoney.ui.theme.ChartColors

data class ChartData(
    val value: Float,
    val color: Color
)

@Composable
fun DonutChart(
    data: List<ChartData>,
    totalAmountText: String,
    modifier: Modifier = Modifier
) {
    val totalValue = data.sumOf { it.value.toDouble() }.toFloat()
    val proportions = if (totalValue > 0) data.map { it.value / totalValue } else emptyList()
    val sweepAngles = proportions.map { it * 360f }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f) // Keep it circular
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            var startAngle = -90f // Start from top
            val strokeWidth = 80f // Thickness of the donut

            if (data.isEmpty() || totalValue == 0f) {
                // Empty state donut
                drawArc(
                    color = Color.LightGray.copy(alpha = 0.3f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
            } else {
                sweepAngles.forEachIndexed { index, sweepAngle ->
                    drawArc(
                        color = data[index].color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                    )
                    startAngle += sweepAngle
                }
            }
        }

        // Text inside the donut
        Text(
            text = totalAmountText,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
