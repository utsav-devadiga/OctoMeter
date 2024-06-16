/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.domain.model.account

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

@Immutable
data class ElectricityMeterPoint(
    val mpan: String,
    val meterSerialNumbers: List<String>,
    val agreements: List<Agreement>,
) {
    /**
     * Returns the specific agreement in effect on a particular day.
     * Note: previous agreement end day = new agreement start day
     * Returns null if not found
     */
    fun lookupAgreement(referencePoint: Instant = Clock.System.now()): Agreement? {
        return agreements.firstOrNull { agreement ->
            val isAvailableFrom = agreement.validFrom <= referencePoint
            val isAvailableTo = agreement.validTo > referencePoint
            isAvailableFrom && isAvailableTo
        }
    }

    /**
     * Return all agreements in effect for a given date range.
     * Note: previous agreement end day = new agreement start day
     * Returns empty list if no matching result
     */
    fun lookupAgreements(
        validFrom: Instant,
        validTo: Instant,
    ): List<Agreement> {
        return agreements.filter { agreement ->
            agreement.validFrom <= validTo && agreement.validTo > validFrom
        }
    }

    /**
     * Get the agreement with the latest validTo date.
     * Returns null if the list is empty
     */
    fun getLatestAgreement(): Agreement? {
        return agreements.maxByOrNull { agreement ->
            agreement.validTo
        }
    }
}
