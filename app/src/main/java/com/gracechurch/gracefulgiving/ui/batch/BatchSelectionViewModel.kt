package com.gracechurch.gracefulgiving.ui.batch

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.data.local.relations.BatchWithDonations
import com.gracechurch.gracefulgiving.domain.model.Fund
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.FundRepository
import com.gracechurch.gracefulgiving.util.printDepositReport
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class BatchSelectionViewModel @Inject constructor(
    private val batchRepo: BatchRepository,
    private val fundRepo: FundRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BatchSelectionUiState())
    val uiState = _uiState.asStateFlow()

    fun loadBatches() {
        viewModelScope.launch {
            try {
                batchRepo.getAllBatches().collect { batches ->
                    _uiState.update { it.copy(batches = batches) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun loadFunds() {
        viewModelScope.launch {
            try {
                fundRepo.getFunds().collect { funds ->
                    _uiState.update { it.copy(funds = funds) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun createNewBatch(userId: Long, createdOn: Long, fundId: Long, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                val batchId = batchRepo.createBatch(userId, createdOn, fundId)
                onCreated(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun printBatchReport(batchId: Long) {
        // TODO: Implement batch report printing
        viewModelScope.launch {
            _uiState.update { it.copy(error = "Print report not yet implemented") }
        }
    }

    fun deleteBatch(batchId: Long) {
        viewModelScope.launch {
            try {
                batchRepo.deleteBatch(batchId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun printDepositSlip(batchId: Long) {
        viewModelScope.launch {
            try {
                // Get the batch with donations
                val batchWithDonations = batchRepo.getBatch(batchId).firstOrNull()

                if (batchWithDonations == null) {
                    _uiState.update { it.copy(error = "Batch not found") }
                    return@launch
                }

                // Get the fund information
                val fund = fundRepo.getFund(batchWithDonations.batch.fundId)

                if (fund == null) {
                    _uiState.update { it.copy(error = "Fund information not found") }
                    return@launch
                }

                // Generate the PDF
                val pdfFile = printDepositReport(
                    context = context,
                    fund = fund,
                    donations = batchWithDonations.donations,
                    batchDate = batchWithDonations.batch.createdOn
                )

                // Open the PDF with an intent
                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    pdfFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_GRANT_READ_URI_PERMISSION
                }

                context.startActivity(intent)

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Failed to generate deposit report: ${e.message}") }
            }
        }
    }
}

data class BatchSelectionUiState(
    val batches: List<BatchWithDonations> = emptyList(),
    val funds: List<Fund> = emptyList(),
    val error: String? = null
)