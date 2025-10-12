package com.example.g_kash.investment.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.g_kash.accounts.domain.AccountsRepository
import com.example.g_kash.accounts.data.CreateAccountRequest
import com.example.g_kash.investment.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import android.util.Log

/**
 * ViewModel for managing investment account creation flow
 */
class InvestmentAccountCreationViewModel(
    private val accountsRepository: AccountsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InvestmentAccountCreationUiState())
    val uiState: StateFlow<InvestmentAccountCreationUiState> = _uiState.asStateFlow()

    fun selectAccountType(accountType: InvestmentAccountType) {
        _uiState.value = _uiState.value.copy(
            selectedAccountType = accountType,
            error = null
        )
    }

    fun selectInvestmentGoal(goal: InvestmentGoal) {
        _uiState.value = _uiState.value.copy(
            selectedGoal = goal,
            error = null
        )
    }

    fun selectInvestmentHorizon(horizon: InvestmentHorizon) {
        _uiState.value = _uiState.value.copy(
            selectedHorizon = horizon,
            error = null
        )
    }

    fun selectRiskTolerance(riskTolerance: RiskTolerance) {
        _uiState.value = _uiState.value.copy(
            selectedRiskTolerance = riskTolerance,
            error = null
        )
    }

    fun setInitialDeposit(amount: Double) {
        _uiState.value = _uiState.value.copy(
            initialDeposit = amount,
            error = null
        )
    }

    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        val nextStep = when (currentStep) {
            InvestmentCreationStep.SELECT_TYPE -> {
                if (_uiState.value.selectedAccountType == null) {
                    showError("Please select an investment type")
                    return
                }
                InvestmentCreationStep.SET_GOALS
            }
            InvestmentCreationStep.SET_GOALS -> {
                if (_uiState.value.selectedGoal == null) {
                    showError("Please select your investment goal")
                    return
                }
                InvestmentCreationStep.RISK_ASSESSMENT
            }
            InvestmentCreationStep.RISK_ASSESSMENT -> {
                if (_uiState.value.selectedRiskTolerance == null) {
                    showError("Please select your risk tolerance")
                    return
                }
                InvestmentCreationStep.INITIAL_DEPOSIT
            }
            InvestmentCreationStep.INITIAL_DEPOSIT -> {
                if (!validateInitialDeposit()) {
                    return
                }
                InvestmentCreationStep.REVIEW_AND_CONFIRM
            }
            InvestmentCreationStep.REVIEW_AND_CONFIRM -> {
                createInvestmentAccount()
                return
            }
        }

        _uiState.value = _uiState.value.copy(
            currentStep = nextStep,
            error = null
        )
    }

    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        val previousStep = when (currentStep) {
            InvestmentCreationStep.SELECT_TYPE -> return // Already at first step
            InvestmentCreationStep.SET_GOALS -> InvestmentCreationStep.SELECT_TYPE
            InvestmentCreationStep.RISK_ASSESSMENT -> InvestmentCreationStep.SET_GOALS
            InvestmentCreationStep.INITIAL_DEPOSIT -> InvestmentCreationStep.RISK_ASSESSMENT
            InvestmentCreationStep.REVIEW_AND_CONFIRM -> InvestmentCreationStep.INITIAL_DEPOSIT
        }

        _uiState.value = _uiState.value.copy(
            currentStep = previousStep,
            error = null
        )
    }

    private fun validateInitialDeposit(): Boolean {
        val selectedType = _uiState.value.selectedAccountType
        val deposit = _uiState.value.initialDeposit

        if (selectedType == null) {
            showError("Please select an investment type first")
            return false
        }

        if (deposit < selectedType.minimumAmount) {
            showError("Minimum deposit for ${selectedType.displayName} is KES ${selectedType.minimumAmount.toInt()}")
            return false
        }

        return true
    }

    private fun createInvestmentAccount() {
        val state = _uiState.value
        
        // Validate all required fields
        if (state.selectedAccountType == null || 
            state.selectedGoal == null || 
            state.selectedRiskTolerance == null) {
            showError("Please complete all required fields")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val request = CreateAccountRequest(
                    accountType = state.selectedAccountType.name.lowercase()
                )

                accountsRepository.createAccount(request).collect { result ->
                    result.fold(
                        onSuccess = { account ->
                            Log.d("InvestmentCreation", "Investment account created successfully: ${account.id}")
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                isAccountCreated = true,
                                error = null
                            )
                        },
                        onFailure = { error ->
                            Log.e("InvestmentCreation", "Failed to create investment account", error)
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = error.message ?: "Failed to create investment account"
                            )
                        }
                    )
                }
            } catch (e: Exception) {
                Log.e("InvestmentCreation", "Exception during account creation", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "An unexpected error occurred"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    private fun showError(message: String) {
        _uiState.value = _uiState.value.copy(error = message)
    }

    fun resetFlow() {
        _uiState.value = InvestmentAccountCreationUiState()
    }

    // Helper functions for UI
    fun getProgressPercentage(): Float {
        return when (_uiState.value.currentStep) {
            InvestmentCreationStep.SELECT_TYPE -> 0.2f
            InvestmentCreationStep.SET_GOALS -> 0.4f
            InvestmentCreationStep.RISK_ASSESSMENT -> 0.6f
            InvestmentCreationStep.INITIAL_DEPOSIT -> 0.8f
            InvestmentCreationStep.REVIEW_AND_CONFIRM -> 1.0f
        }
    }

    fun getStepTitle(): String {
        return when (_uiState.value.currentStep) {
            InvestmentCreationStep.SELECT_TYPE -> "Choose Investment Type"
            InvestmentCreationStep.SET_GOALS -> "Set Your Goals"
            InvestmentCreationStep.RISK_ASSESSMENT -> "Risk Assessment"
            InvestmentCreationStep.INITIAL_DEPOSIT -> "Initial Deposit"
            InvestmentCreationStep.REVIEW_AND_CONFIRM -> "Review & Confirm"
        }
    }

    fun getStepDescription(): String {
        return when (_uiState.value.currentStep) {
            InvestmentCreationStep.SELECT_TYPE -> "Select the type of investment account that suits your needs"
            InvestmentCreationStep.SET_GOALS -> "Tell us about your investment goals and timeline"
            InvestmentCreationStep.RISK_ASSESSMENT -> "Help us understand your risk tolerance"
            InvestmentCreationStep.INITIAL_DEPOSIT -> "Set your initial investment amount"
            InvestmentCreationStep.REVIEW_AND_CONFIRM -> "Review your selections and create your account"
        }
    }
}