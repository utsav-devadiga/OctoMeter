/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

@file:OptIn(ExperimentalMaterial3Api::class)

package com.rwmobi.kunigami.ui.components.koalaplot

import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RichTooltip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rwmobi.kunigami.ui.theme.getDimension
import com.rwmobi.kunigami.ui.utils.getPercentageColorIndex
import io.github.koalaplot.core.ChartLayout
import io.github.koalaplot.core.bar.DefaultVerticalBar
import io.github.koalaplot.core.bar.VerticalBarPlot
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.toString
import io.github.koalaplot.core.xygraph.DoubleLinearAxisModel
import io.github.koalaplot.core.xygraph.IntLinearAxisModel
import io.github.koalaplot.core.xygraph.TickPosition
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.XYGraphScope
import io.github.koalaplot.core.xygraph.rememberAxisStyle

@OptIn(ExperimentalKoalaPlotApi::class)
@Composable
fun VerticalBarChart(
    modifier: Modifier,
    title: String? = null,
    xAxisTitle: String? = null,
    yAxisTitle: String? = null,
    yAxisTickPosition: TickPosition,
    xAxisTickPosition: TickPosition,
    barWidth: Float,
    entries: List<VerticalBarPlotEntry<Int, Double>>,
    yAxisRange: ClosedFloatingPointRange<Double>,
    labelGenerator: (index: Int) -> String?,
    tooltipGenerator: (index: Int) -> String,
    colorPalette: List<Color>,
    backgroundPlot: @Composable ((scope: XYGraphScope<Int, Double>) -> Unit)? = null,
) {
    val dimension = LocalDensity.current.getDimension()
    val barChartEntries = remember { entries }

    ChartLayout(
        modifier = modifier,
        title = {
            title?.let {
                ChartTitle(title = title)
            }
        },
    ) {
        XYGraph(
            xAxisModel = IntLinearAxisModel(
                range = 0..entries.count() + 1,
                minimumMajorTickIncrement = 1,
                minimumMajorTickSpacing = dimension.grid_1,
                minorTickCount = 0,
                allowPanning = false,
                allowZooming = false,
            ),
            xAxisStyle = rememberAxisStyle(
                tickPosition = xAxisTickPosition,
            ),
            xAxisLabels = { index ->
                labelGenerator(index)?.let { label ->
                    AxisLabel(
                        modifier = Modifier.padding(top = dimension.grid_0_25),
                        label = label,
                    )
                }
            },
            xAxisTitle = {
                xAxisTitle?.let {
                    XAxisTitle(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = dimension.grid_1),
                        title = it,
                    )
                }
            },
            yAxisModel = DoubleLinearAxisModel(
                range = yAxisRange,
                minimumMajorTickIncrement = 0.1,
                minorTickCount = 4,
                allowZooming = false,
                allowPanning = false,
            ),
            yAxisStyle = rememberAxisStyle(
                tickPosition = yAxisTickPosition,
            ),
            yAxisLabels = {
                AxisLabel(
                    modifier = Modifier.absolutePadding(right = dimension.grid_0_25),
                    label = it.toString(precision = 1),
                )
            },
            yAxisTitle = {
                yAxisTitle?.let {
                    YAxisTitle(
                        modifier = Modifier
                            .fillMaxHeight()
                            .padding(end = dimension.grid_1),
                        title = it,
                    )
                }
            },
            verticalMajorGridLineStyle = null,
            verticalMinorGridLineStyle = null,
            horizontalMajorGridLineStyle = LineStyle(
                brush = SolidColor(MaterialTheme.colorScheme.onBackground),
                strokeWidth = 1.dp,
                pathEffect = null,
                alpha = 0.5f,
                colorFilter = null,
                blendMode = DrawScope.DefaultBlendMode,
            ),
            horizontalMinorGridLineStyle = LineStyle(
                brush = SolidColor(MaterialTheme.colorScheme.onBackground),
                strokeWidth = 1.dp,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(4f, 4f), 0f), // Configure dashed pattern
                alpha = 0.25f,
                colorFilter = null,
                blendMode = DrawScope.DefaultBlendMode,
            ),
        ) {
            backgroundPlot?.let { it(this) }

            VerticalBarPlot(
                data = barChartEntries,
                bar = { index ->
                    DefaultVerticalBar(
                        modifier = Modifier.fillMaxWidth(),
                        brush = SolidColor(
                            colorPalette[
                                barChartEntries[index].y.yMax.getPercentageColorIndex(
                                    maxValue = yAxisRange.endInclusive,
                                ),
                            ],
                        ),
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomEnd = 0.dp,
                            bottomStart = 0.dp,
                        ),
                        hoverElement = {
                            RichTooltip {
                                Text(
                                    modifier = Modifier.padding(all = dimension.grid_0_25),
                                    style = MaterialTheme.typography.labelMedium,
                                    textAlign = TextAlign.Center,
                                    text = tooltipGenerator(index),
                                )
                            }
                        },
                    )
                },
                barWidth = barWidth,
            )
        }
    }
}
