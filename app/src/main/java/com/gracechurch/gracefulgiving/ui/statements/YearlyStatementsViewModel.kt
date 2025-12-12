package com.gracechurch.gracefulgiving.ui.statements

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.DonorWithDonations
import com.gracechurch.gracefulgiving.data.repository.DonorRepository
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class YearlyStatementsUiState(
    val loading: Boolean = false,
    val donors: List<DonorWithDonations> = emptyList(),
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class YearlyStatementsViewModel @Inject constructor(
    private val donorRepository: DonorRepository,
    private val batchRepository: BatchRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YearlyStatementsUiState())
    val uiState: StateFlow<YearlyStatementsUiState> = _uiState.asStateFlow()

    fun loadDonors() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                donorRepository.getAllDonors().collect { donorsList ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        donors = donorsList
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.localizedMessage ?: "Failed to load donors"
                )
            }
        }
    }

    fun generateYearlyStatement(donorId: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(loading = true, error = null)
            try {
                // Fetch donor with donations
                donorRepository.getDonor(donorId).collect { donorWithDonations ->
                    donorWithDonations?.let {
                        batchRepository.generateBatchReport(it.donor.donorId) // Placeholder PDF generation per donor
                        _uiState.value = _uiState.value.copy(
                            loading = false,
                            successMessage = "Yearly statement generated for ${it.donor.firstName} ${it.donor.lastName}"
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    error = e.localizedMessage ?: "Failed to generate yearly statement"
                )
            }
        }
    }
}