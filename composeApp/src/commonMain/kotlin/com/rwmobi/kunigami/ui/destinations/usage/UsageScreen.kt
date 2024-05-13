/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.destinations.usage

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.rwmobi.kunigami.ui.components.ScrollbarMultiplatform
import com.rwmobi.kunigami.ui.components.koalaplot.BarSamplePlot
import com.rwmobi.kunigami.ui.theme.getDimension
import com.rwmobi.kunigami.ui.utils.getScreenSizeInfo
import io.github.koalaplot.core.bar.DefaultVerticalBarPlotEntry
import io.github.koalaplot.core.bar.DefaultVerticalBarPosition
import io.github.koalaplot.core.bar.VerticalBarPlotEntry
import io.github.koalaplot.core.xygraph.TickPosition
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun UsageScreen(
    modifier: Modifier = Modifier,
    uiState: UsageUIState,
    uiEvent: UsageUIEvent,
) {
    if (uiState.errorMessages.isNotEmpty()) {
        val errorMessage = remember(uiState) { uiState.errorMessages[0] }
        val errorMessageText = errorMessage.message

        LaunchedEffect(errorMessage.id) {
            uiEvent.onShowSnackbar(errorMessageText)
            uiEvent.onErrorShown(errorMessage.id)
        }
    }

    val dimension = LocalDensity.current.getDimension()
    val lazyListState = rememberLazyListState()

    if (!uiState.isLoading) {
        val screenSizeInfo = getScreenSizeInfo()
        val entries: List<VerticalBarPlotEntry<Int, Double>> = remember(uiState.consumptions) {
            buildList {
                uiState.consumptions.forEachIndexed { index, consumption ->
                    add(DefaultVerticalBarPlotEntry((index + 1), DefaultVerticalBarPosition(0.0, consumption.consumption)))
                }
            }
        }
        val labels: Map<Int, String> = remember(uiState.consumptions) {
            buildMap {
                var lastRateValue: String? = null

                uiState.consumptions.forEachIndexed { index, consumption ->
                    val currentTime = consumption.intervalStart.toLocalDateTime(TimeZone.currentSystemDefault()).time.hour.toString()
                    if (currentTime != lastRateValue) {
                        put(index + 1, currentTime)
                        lastRateValue = currentTime
                    }
                }
            }
        }

        ScrollbarMultiplatform(
            modifier = modifier,
            enabled = uiState.consumptions.isNotEmpty(),
            lazyListState = lazyListState,
        ) { contentModifier ->
            LazyColumn(
                modifier = contentModifier.fillMaxSize(),
                state = lazyListState,
            ) {
                item {
                    BoxWithConstraints {
                        val constraintModifier = if (screenSizeInfo.isPortrait()) {
                            Modifier
                                .fillMaxWidth()
                                .aspectRatio(4 / 3f)
                        } else {
                            Modifier.fillMaxSize()
                                .height(screenSizeInfo.heightDp * 2 / 3)
                        }

                        BarSamplePlot(
                            modifier = constraintModifier.padding(all = dimension.grid_2),
                            entries = entries,
                            labels = labels,
                            yAxisRange = uiState.consumptionRange,
                            yAxisTickPosition = TickPosition.Outside,
                            xAxisTickPosition = TickPosition.Outside,
                            yAxisTitle = "kWh",
                            barWidth = 0.8f,
                        )
                    }
                }

                itemsIndexed(
                    items = uiState.consumptions,
                    key = { _, consumption -> consumption.intervalStart.epochSeconds },
                ) { _, consumption ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = dimension.grid_2),
                    ) {
                        val timeLabel = consumption.intervalStart.toLocalDateTime(TimeZone.currentSystemDefault())
                        Text(
                            modifier = Modifier.weight(1.0f),
                            text = "${timeLabel.date} ${timeLabel.time}",
                        )

                        Text(
                            modifier = Modifier.wrapContentWidth(),
                            fontWeight = FontWeight.Bold,
                            text = "${consumption.consumption}",
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(true) {
        uiEvent.onRefresh()
    }
}
