package com.example.g_kash.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.g_kash.transactions.data.Transaction

@Composable
fun SpendingGraphCard(
    transactions: List<Transaction>,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Spending Analysis",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "AI Predicted Trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = primaryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = secondaryColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        "Last 7 Days",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // The Graph
            SpendingLineChart(
                data = listOf(120f, 450f, 300f, 600f, 200f, 850f, 500f), // Mock historical
                prediction = listOf(500f, 650f, 800f), // Mock prediction
                lineColor = primaryColor,
                predictionColor = primaryColor.copy(alpha = 0.4f)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem("Actual", primaryColor, isDashed = false)
                Spacer(modifier = Modifier.width(20.dp))
                LegendItem("Predicted", primaryColor.copy(alpha = 0.4f), isDashed = true)
            }
        }
    }
}

@Composable
fun SpendingLineChart(
    data: List<Float>,
    prediction: List<Float>,
    lineColor: Color,
    predictionColor: Color
) {
    val allPoints = data + prediction
    val maxVal = allPoints.maxOrNull() ?: 1f
    
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
    ) {
        val width = size.width
        val height = size.height
        val stepX = width / (allPoints.size - 1)
        
        // Draw Grid Lines (Horizontal)
        val gridLines = 4
        for (i in 0..gridLines) {
            val y = height - (i * height / gridLines)
            drawLine(
                color = Color.LightGray.copy(alpha = 0.3f),
                start = Offset(0f, y),
                end = Offset(width, y),
                strokeWidth = 1.dp.toPx()
            )
        }

        // 1. Draw Historical Line
        val path = Path()
        data.forEachIndexed { index, value ->
            val x = index * stepX
            val y = height - (value / maxVal * height)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        
        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
        
        // Draw Gradient Fill for Historical
        val fillPath = Path().apply {
            addPath(path)
            lineTo((data.size - 1) * stepX, height)
            lineTo(0f, height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.2f), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        // 2. Draw Prediction Line (Dashed)
        if (prediction.isNotEmpty()) {
            val predPath = Path()
            val lastHistoricalX = (data.size - 1) * stepX
            val lastHistoricalY = height - (data.last() / maxVal * height)
            predPath.moveTo(lastHistoricalX, lastHistoricalY)
            
            prediction.forEachIndexed { index, value ->
                val x = (data.size + index) * stepX
                val y = height - (value / maxVal * height)
                predPath.lineTo(x, y)
            }
            
            drawPath(
                path = predPath,
                color = predictionColor,
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // 3. Draw Points (Last actual and future points)
        data.lastOrNull()?.let { value ->
            val x = (data.size - 1) * stepX
            val y = height - (value / maxVal * height)
            // Use onSurface for the outer circle to match dark/light mode
            drawCircle(color = Color.White, radius = 6.dp.toPx(), center = Offset(x, y))
            drawCircle(color = lineColor, radius = 4.dp.toPx(), center = Offset(x, y))
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color, isDashed: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(20.dp)
                .height(3.dp)
                .background(
                    color = if (isDashed) Color.Transparent else color,
                    shape = RoundedCornerShape(2.dp)
                )
        ) {
            if (isDashed) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = color,
                        start = Offset(0f, size.height/2),
                        end = Offset(size.width, size.height/2),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
    }
}
