package com.ipsakti.sahayak.ui.screens.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ipsakti.sahayak.data.model.InvestigationDetail
import com.ipsakti.sahayak.data.repository.IpSaktiRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ResultUiState(
    val isLoading: Boolean = true,
    val investigation: InvestigationDetail? = null,
    val errorMessage: String? = null
)

class ResultViewModel(
    private val repository: IpSaktiRepository = IpSaktiRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    fun loadInvestigation(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getInvestigationDetail(id)
            result.onSuccess { detail ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    investigation = detail
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.localizedMessage ?: "Failed to load investigation"
                )
            }
        }
    }
}
