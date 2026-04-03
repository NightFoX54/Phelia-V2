package com.example.myapplication.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import kotlin.math.ceil

@Composable
fun SimpleBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4338CA),
    height: Dp = 200.dp,
) {
    Box(modifier = modifier.fillMaxWidth().height(height).padding(top = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.isEmpty()) return@Canvas

            val topPadding = 12.dp.toPx()
            val bottomPadding = 12.dp.toPx()
            val sidePadding = 10.dp.toPx()
            val chartHeight = size.height - topPadding - bottomPadding
            val chartWidth = size.width - sidePadding * 2
            val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val min = 0f
            val range = (max - min).coerceAtLeast(1f)

            // subtle horizontal guide lines for better readability
            repeat(4) { i ->
                val ratio = i / 3f
                val y = topPadding + chartHeight * ratio
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(sidePadding, y),
                    end = Offset(size.width - sidePadding, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val barWidth = chartWidth / (values.size * 1.7f)
            val gap = barWidth * 0.7f
            values.forEachIndexed { i, v ->
                val x = sidePadding + i * (barWidth + gap) + gap * 0.5f
                val normalized = ((v - min) / range).coerceIn(0f, 1f)
                val h = normalized * chartHeight
                drawRoundRect(
                    color = barColor,
                    topLeft = Offset(x, topPadding + (chartHeight - h)),
                    size = Size(barWidth, h),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                )
            }
        }
    }
}

data class LineChartEntry(val label: String, val value: Float)
data class HorizontalBarEntry(val label: String, val value: Float)
data class VerticalBarEntry(val label: String, val value: Float)

@Composable
fun ReadableLineChart(
    points: List<LineChartEntry>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF7C3AED),
    height: Dp = 240.dp,
) {
    if (points.size < 2) return
    val maxValue = points.maxOf { it.value }.coerceAtLeast(1f)
    val minValue = 0f
    val yTicks = 4
    val yStep = ceil(maxValue / yTicks)
    val yLabels = List(yTicks + 1) { i -> ((yTicks - i) * yStep).toInt().toString() }

    Column(modifier = modifier.fillMaxWidth().height(height)) {
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.width(36.dp).fillMaxSize().padding(top = 6.dp, bottom = 14.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.End,
            ) {
                yLabels.forEach { label ->
                    Text(text = label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxSize().padding(start = 8.dp, end = 4.dp, bottom = 14.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    val chartHeight = size.height
                    val chartWidth = size.width
                    val range = (maxValue - minValue).coerceAtLeast(1f)
                    val stepX = chartWidth / (points.size - 1)

                    repeat(yTicks + 1) { i ->
                        val y = (chartHeight / yTicks) * i
                        drawLine(Color(0xFFE5E7EB), Offset(0f, y), Offset(chartWidth, y), pathEffect = dash)
                    }
                    points.indices.forEach { i ->
                        val x = i * stepX
                        drawLine(Color(0xFFE5E7EB), Offset(x, 0f), Offset(x, chartHeight), pathEffect = dash)
                    }

                    fun pointY(v: Float): Float {
                        val n = ((v - minValue) / range).coerceIn(0f, 1f)
                        return chartHeight - (n * chartHeight)
                    }

                    val path = Path().apply {
                        moveTo(0f, pointY(points.first().value))
                        for (i in 1 until points.size) {
                            val prevX = (i - 1) * stepX
                            val prevY = pointY(points[i - 1].value)
                            val x = i * stepX
                            val y = pointY(points[i].value)
                            val c1x = prevX + (stepX / 2f)
                            val c2x = x - (stepX / 2f)
                            cubicTo(c1x, prevY, c2x, y, x, y)
                        }
                    }
                    drawPath(path, color = lineColor, style = Stroke(width = 4f))

                    points.indices.forEach { i ->
                        val x = i * stepX
                        val y = pointY(points[i].value)
                        drawCircle(Color.White, radius = 7f, center = Offset(x, y))
                        drawCircle(lineColor, radius = 5f, center = Offset(x, y))
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 44.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            points.forEach { entry ->
                Text(entry.label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun ReadableHorizontalBarChart(
    values: List<HorizontalBarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4338CA),
    height: Dp = 260.dp,
) {
    if (values.isEmpty()) return
    val maxValue = values.maxOf { it.value }.coerceAtLeast(1f)
    val roundedMax = ceil(maxValue / 150f) * 150f
    val xTicks = 4
    val tickValues = List(xTicks + 1) { i -> ((roundedMax / xTicks) * i).toInt().toString() }

    Column(modifier = modifier.fillMaxWidth().height(height)) {
        Row(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.widthIn(min = 94.dp).fillMaxSize().padding(top = 12.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.SpaceEvenly,
            ) {
                values.forEach { item ->
                    Text(item.label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodyMedium)
                }
            }
            Box(modifier = Modifier.weight(1f).fillMaxSize().padding(start = 8.dp, end = 4.dp, bottom = 16.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                    val rowHeight = size.height / values.size
                    repeat(xTicks + 1) { i ->
                        val x = (size.width / xTicks) * i
                        drawLine(Color(0xFFE5E7EB), Offset(x, 0f), Offset(x, size.height), pathEffect = dash)
                    }
                    values.forEachIndexed { index, item ->
                        val barTop = index * rowHeight + (rowHeight * 0.2f)
                        val barHeight = rowHeight * 0.62f
                        val width = (item.value / roundedMax).coerceIn(0f, 1f) * size.width
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(0f, barTop),
                            size = Size(width, barHeight),
                            cornerRadius = CornerRadius(0f, 0f),
                        )
                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset((width - 14f).coerceAtLeast(0f), barTop),
                            size = Size(14f, barHeight),
                            cornerRadius = CornerRadius(10f, 10f),
                        )
                    }
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(start = 110.dp, end = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            tickValues.forEach { tick ->
                Text(tick, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
fun ReadableVerticalBarChart(
    values: List<VerticalBarEntry>,
    modifier: Modifier = Modifier,
    barColor: Color = Color(0xFF4338CA),
    height: Dp = 230.dp,
) {
    if (values.isEmpty()) return
    val justValues = values.map { it.value }
    SimpleBarChart(values = justValues, modifier = modifier, barColor = barColor, height = height - 24.dp)
    Spacer(modifier = Modifier.height(6.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
        values.forEach {
            Text(it.label, color = Color(0xFF94A3B8), style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
fun SimpleLineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = Color(0xFF7C3AED),
    height: Dp = 200.dp,
) {
    Box(modifier = modifier.fillMaxWidth().height(height).padding(top = 8.dp)) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            if (values.size < 2) return@Canvas
            val topPadding = 10.dp.toPx()
            val bottomPadding = 12.dp.toPx()
            val sidePadding = 10.dp.toPx()
            val chartHeight = size.height - topPadding - bottomPadding
            val chartWidth = size.width - sidePadding * 2

            val max = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
            val min = values.minOrNull() ?: 0f
            val range = (max - min).coerceAtLeast(1f)

            repeat(4) { i ->
                val ratio = i / 3f
                val y = topPadding + chartHeight * ratio
                drawLine(
                    color = Color(0xFFE5E7EB),
                    start = Offset(sidePadding, y),
                    end = Offset(size.width - sidePadding, y),
                    strokeWidth = 1.dp.toPx(),
                )
            }

            val stepX = chartWidth / (values.size - 1)
            fun x(index: Int) = sidePadding + (index * stepX)
            fun y(v: Float): Float {
                val normalized = ((v - min) / range).coerceIn(0f, 1f)
                return topPadding + (chartHeight - (normalized * chartHeight))
            }

            val linePath = Path().apply {
                moveTo(x(0), y(values[0]))
                for (i in 1 until values.size) {
                    lineTo(x(i), y(values[i]))
                }
            }
            val areaPath = Path().apply {
                addPath(linePath)
                lineTo(x(values.lastIndex), topPadding + chartHeight)
                lineTo(x(0), topPadding + chartHeight)
                close()
            }

            drawPath(
                path = areaPath,
                color = lineColor.copy(alpha = 0.08f),
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 3.dp.toPx()),
            )

            values.forEachIndexed { i, v ->
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = Offset(x(i), y(v)),
                )
                drawCircle(
                    color = lineColor,
                    radius = 2.5.dp.toPx(),
                    center = Offset(x(i), y(v)),
                )
            }
        }
    }
}

