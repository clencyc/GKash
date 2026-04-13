package com.example.g_kash.payment.data

interface PaymentRepository {
    suspend fun initiateDeposit(request: DepositRequest): Result<DepositResponse>
    suspend fun getTransactionStatus(transactionId: String): Result<PaymentStatusResponse>
}

class PaymentRepositoryImpl(
    private val apiService: PaymentApiService
) : PaymentRepository {

    override suspend fun initiateDeposit(request: DepositRequest): Result<DepositResponse> =
        apiService.initiateDeposit(request)

    override suspend fun getTransactionStatus(transactionId: String): Result<PaymentStatusResponse> =
        apiService.getTransactionStatus(transactionId)
}
