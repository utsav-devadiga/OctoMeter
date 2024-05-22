/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.destinations.usage

import androidx.compose.runtime.Immutable

@Immutable
data class UsageUIEvent(
    val onInitialLoad: () -> Unit,
    val onPreviousTimeFrame: () -> Unit,
    val onNextTimeFrame: () -> Unit,
    val onErrorShown: (errorId: Long) -> Unit,
    val onScrolledToTop: () -> Unit,
    val onShowSnackbar: suspend (message: String) -> Unit,
)
