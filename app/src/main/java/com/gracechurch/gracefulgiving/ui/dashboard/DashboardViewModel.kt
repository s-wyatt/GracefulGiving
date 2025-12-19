package com.gracechurch.gracefulgiving.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gracechurch.gracefulgiving.domain.model.BatchInfo
import com.gracechurch.gracefulgiving.domain.model.UserRole
import com.gracechurch.gracefulgiving.domain.repository.BatchRepository
import com.gracechurch.gracefulgiving.domain.repository.DonationRepository
import com.gracechurch.gracefulgiving.domain.repository.UserRepository
import com.gracechurch.gracefulgiving.domain.repository.UserSessionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val batchRepository: BatchRepository,
    private val donationRepository: DonationRepository,
    private val userRepository: UserRepository,
    private val userSessionRepository: UserSessionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() = viewModelScope.launch {
        val currentUser = userSessionRepository.currentUser
        val user = currentUser?.let { userRepository.getUserById(it.id) }
        val openBatches = batchRepository.getOpenBatches()
        val mtd = donationRepository.getMonthToDateTotal()
        val qtd = donationRepository.getQuarterToDateTotal()
        val ytd = donationRepository.getYearToDateTotal()

        // Calculate last 12 months totals
        val last12Months = getLast12MonthsTotals()
        val monthLabels = getMonthLabels()

        _uiState.value = DashboardUiState(
            username = user?.username ?: "",
            userRole = user?.role,
            openBatches = openBatches,
            monthToDateTotal = mtd,
            quarterToDateTotal = qtd,
            yearToDateTotal = ytd,
            last12MonthsTotals = last12Months,
            oldestMonthLabel = monthLabels.first,
            currentMonthLabel = monthLabels.second
        )
    }

    private suspend fun getLast12MonthsTotals(): List<Double> {
        val calendar = Calendar.getInstance()

        return (11 downTo 0).map { monthsAgo ->
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.MONTH, -monthsAgo)

            // First day of month
            val startOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis

            // Last day of month
            val endOfMonth = calendar.apply {
                set(Calendar.DAY_OF_MONTH, getActualMaximum(Calendar.DAY_OF_MONTH))
                set(Calendar.HOUR_OF_DAY, 23)
                set(Calendar.MINUTE, 59)
                set(Calendar.SECOND, 59)
                set(Calendar.MILLISECOND, 999)
            }.timeInMillis

            donationRepository.getTotalBetweenDates(startOfMonth, endOfMonth)
        }
    }

    private fun getMonthLabels(): Pair<String, String> {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("MMM ''yy", Locale.getDefault())

        val currentMonth = format.format(calendar.time)

        calendar.add(Calendar.MONTH, -11)
        val oldestMonth = format.format(calendar.time)

        return Pair(oldestMonth, currentMonth)
    }
}

data class DashboardUiState(
    val username: String = "",
    val userRole: UserRole? = null,
    val openBatches: List<BatchInfo> = emptyList(),
    val monthToDateTotal: Double = 0.0,
    val quarterToDateTotal: Double = 0.0,
    val yearToDateTotal: Double = 0.0,
    val last12MonthsTotals: List<Double> = emptyList(),
    val oldestMonthLabel: String = "",
    val currentMonthLabel: String = ""
)