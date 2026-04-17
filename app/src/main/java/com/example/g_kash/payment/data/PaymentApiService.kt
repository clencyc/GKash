package com.example.g_kash.payment.data

import android.util.Log
import com.example.g_kash.data.SessionStorage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.first

private const val TAG = "GkashPaymentApiService"

class GkashPaymentApiService(
    private val client: HttpClient,
    private val sessionStorage: SessionStorage,
    private val baseUrl: String = "https://gkash.onrender.com/api"
) {

    /**
     * POST /payments/deposit
     * Initiates an M-Pesa STK push and returns a transaction_id for polling.
     */
    suspend fun initiateDeposit(request: DepositRequest): Result<DepositResponse> {
        return try {
            val token = sessionStorage.authTokenStream.first()
            Log.d(TAG, "initiateDeposit → phone=${request.phone}, amount=${request.amount}, accountId=${request.accountId}")

            val response = client.post("$baseUrl/payments/deposit") {
                contentType(ContentType.Application.Json)
                token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
                setBody(request)
            }

            if (response.status.isSuccess()) {
                val body = response.body<DepositResponse>()
                Log.d(TAG, "✓ STK push initiated – transactionId=${body.transactionId}")
                Result.success(body)
            } else {
                val errorBody = runCatching { response.body<DepositResponse>() }.getOrNull()
                val msg = errorBody?.message ?: "Deposit failed (${response.status.value})"
                Log.e(TAG, "✗ initiateDeposit failed: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ initiateDeposit exception", e)
            Result.failure(e)
        }
    }

    /**
     * GET /payments/status/{transaction_id}
     * Polls the payment gateway until the STK push is confirmed or fails.
     */
    suspend fun getTransactionStatus(transactionId: String): Result<PaymentStatusResponse> {
        return try {
            val token = sessionStorage.authTokenStream.first()
            val response = client.get("$baseUrl/payments/status/$transactionId") {
                token?.let { header(HttpHeaders.Authorization, "Bearer $it") }
            }

            if (response.status.isSuccess()) {
                val body = response.body<PaymentStatusResponse>()
                Log.d(TAG, "Status poll: status=${body.status}, confirmed=${body.confirmed}")
                Result.success(body)
            } else {
                val errorBody = runCatching { response.body<PaymentStatusResponse>() }.getOrNull()
                val msg = errorBody?.message ?: "Status check failed (${response.status.value})"
                Log.e(TAG, "✗ getTransactionStatus: $msg")
                Result.failure(Exception(msg))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ getTransactionStatus exception", e)
            Result.failure(e)
        }
    }
}
