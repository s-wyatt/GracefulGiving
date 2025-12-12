package com.gracechurch.gracefulgiving.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val donationRepository: DonationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init { loadDashboardData() }

    private fun loadDashboardData() = viewModelScope.launch {
        val openBatches = batchRepository.getOpenBatches()
        val mtd = donationRepository.getMonthToDateTotal()
        val qtd = donationRepository.getQuarterToDateTotal()
        val ytd = donationRepository.getYearToDateTotal()

        _uiState.value = DashboardUiState(
            openBatches = openBatches,
            monthToDateTotal = mtd,
            quarterToDateTotal = qtd,
            yearToDateTotal = ytd
        )
    }
}

data class DashboardUiState(
    val openBatches: List<BatchInfo> = emptyList(),
    val monthToDateTotal: Double = 0.0,
    val quarterToDateTotal: Double = 0.0,
    val yearToDateTotal: Double = 0.0
)

data class BatchInfo(
    val batchId: Long,
    val batchName: String,
    val total: Double
)
