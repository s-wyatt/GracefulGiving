package com.gracechurch.gracefulgiving.ui.fund

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FundManagementViewModel @Inject constructor(
    private val fundRepository: FundRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FundManagementUiState())
    val uiState = _uiState.asStateFlow()

    fun loadFunds() {
        viewModelScope.launch {
            fundRepository.getFunds().collect { funds ->
                _uiState.update { it.copy(funds = funds) }
            }
        }
    }

    fun addFund(fund: Fund) {
        viewModelScope.launch {
            fundRepository.insertFund(fund)
        }
    }

    fun updateFund(fund: Fund) {
        viewModelScope.launch {
            fundRepository.insertFund(fund)
        }
    }
}

data class FundManagementUiState(
    val funds: List<Fund> = emptyList(),
    val error: String? = null
)
