/*
 * Copyright (c) 2024. Ryan Wong
 * https://github.com/ryanw-mobile
 * Sponsored by RW MobiMedia UK Limited
 *
 */

package com.rwmobi.kunigami.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.rwmobi.kunigami.domain.repository.OctopusRepository
import com.rwmobi.kunigami.ui.destinations.tariffs.TariffsUIState
import com.rwmobi.kunigami.ui.model.ErrorMessage
import com.rwmobi.kunigami.ui.utils.generateRandomLong
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class TariffsViewModel(
    private val octopusRepository: OctopusRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Default,
) : ViewModel() {
    private val _uiState: MutableStateFlow<TariffsUIState> = MutableStateFlow(TariffsUIState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    fun errorShown(errorId: Long) {
        _uiState.update { currentUiState ->
            val errorMessages = currentUiState.errorMessages.filterNot { it.id == errorId }
            currentUiState.copy(errorMessages = errorMessages)
        }
    }

    fun refresh() {
        viewModelScope.launch(dispatcher) {
            val products = octopusRepository.getProducts()
            if (products.isSuccess) {
                _uiState.update { currentUiState ->
                    currentUiState.copy(
                        isLoading = false,
                        products = products.getOrNull() ?: emptyList(),
                    )
                }
            } else {
                updateUIForError(message = products.exceptionOrNull()?.message ?: "Error when retrieving tariffs")
                Logger.e("TariffsViewModel", throwable = products.exceptionOrNull(), message = { "Error when retrieving tariffs" })
            }
        }
    }

    private fun updateUIForError(message: String) {
        if (_uiState.value.errorMessages.any { it.message == message }) {
            return
        }

        val newErrorMessage = ErrorMessage(
            id = generateRandomLong(),
            message = message,
        )
        _uiState.update { currentUiState ->
            currentUiState.copy(
                isLoading = false,
                errorMessages = currentUiState.errorMessages + newErrorMessage,
            )
        }
    }
}
